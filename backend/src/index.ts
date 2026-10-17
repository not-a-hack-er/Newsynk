import {initializeApp} from "firebase-admin/app";
import {getAppCheck} from "firebase-admin/app-check";
import {defineSecret} from "firebase-functions/params";
import {onRequest} from "firebase-functions/v2/https";
import {providerCategory} from "./newsCategories";

initializeApp();

const gnewsKey = defineSecret("GNEWS_API_KEY");
const currentsKey = defineSecret("CURRENTS_API_KEY");
const newsDataKey = defineSecret("NEWSDATA_API_KEY");

type NormalizedArticle = {
  id: string;
  webTitle: string;
  webUrl: string;
  webPublicationDate: string;
  sectionName: string;
  fields: {
    thumbnail?: string;
    trailText?: string;
    byline?: string;
    bodyText?: string;
  };
};

async function jsonOrNull(url: URL, headers?: HeadersInit): Promise<any | null> {
  try {
    const response = await fetch(url, {headers, signal: AbortSignal.timeout(8_000)});
    return response.ok ? await response.json() : null;
  } catch {
    return null;
  }
}

function gnewsUrl(page: number, query?: string, category?: string): URL {
  const url = new URL(`https://gnews.io/api/v4/${query ? "search" : "top-headlines"}`);
  url.searchParams.set("page", String(page));
  url.searchParams.set("max", "10");
  url.searchParams.set("lang", "en");
  if (query) url.searchParams.set("q", query);
  const providerFilter = providerCategory("gnews", category);
  if (providerFilter) url.searchParams.set("category", providerFilter);
  return url;
}

function currentsUrl(page: number, query?: string, category?: string): URL {
  const url = new URL(`https://api.currentsapi.services/v1/${query || category ? "search" : "latest-news"}`);
  url.searchParams.set("page_number", String(page));
  url.searchParams.set("language", "en");
  if (query) url.searchParams.set("keywords", query);
  const providerFilter = providerCategory("currents", category);
  if (providerFilter) url.searchParams.set("category", providerFilter);
  return url;
}

function newsDataUrl(query?: string, category?: string): URL {
  const url = new URL("https://newsdata.io/api/1/latest");
  url.searchParams.set("apikey", newsDataKey.value());
  url.searchParams.set("language", "en");
  if (query) url.searchParams.set("q", query);
  const providerFilter = providerCategory("newsdata", category);
  if (providerFilter) url.searchParams.set("category", providerFilter);
  return url;
}

function normalizeGnews(data: any): NormalizedArticle[] {
  return (data?.articles ?? []).map((item: any) => ({
    id: item.url,
    webTitle: item.title ?? "Untitled story",
    webUrl: item.url,
    webPublicationDate: item.publishedAt ?? "",
    sectionName: item.source?.name ?? "GNews",
    fields: {
      thumbnail: item.image,
      trailText: item.description,
      byline: item.source?.name,
      bodyText: item.content,
    },
  }));
}

function normalizeCurrents(data: any): NormalizedArticle[] {
  return (data?.news ?? []).map((item: any) => ({
    id: item.id ?? item.url,
    webTitle: item.title ?? "Untitled story",
    webUrl: item.url,
    webPublicationDate: item.published ?? "",
    sectionName: publisherFromUrl(item.url, "Currents"),
    fields: {
      thumbnail: item.image,
      trailText: item.description,
      byline: item.author,
    },
  }));
}

function publisherFromUrl(raw: string | undefined, fallback: string): string {
  try {
    return new URL(raw ?? "").hostname.replace(/^www\./, "") || fallback;
  } catch {
    return fallback;
  }
}

function normalizeNewsData(data: any): NormalizedArticle[] {
  return (data?.results ?? []).map((item: any) => ({
    id: item.article_id ?? item.link,
    webTitle: item.title ?? "Untitled story",
    webUrl: item.link,
    webPublicationDate: item.pubDate ?? "",
    sectionName: item.source_name ?? "NewsData.io",
    fields: {
      thumbnail: item.image_url,
      trailText: item.description,
      byline: item.creator?.[0],
    },
  }));
}

export const api = onRequest(
  {cors: true, secrets: [gnewsKey, currentsKey, newsDataKey], timeoutSeconds: 30},
  async (request, response) => {
    if (request.path !== "/feed" || request.method !== "GET") {
      response.status(404).json({error: "Not found"});
      return;
    }
    if (!process.env.FUNCTIONS_EMULATOR) {
      const appCheckToken = request.header("X-Firebase-AppCheck");
      if (!appCheckToken) {
        response.status(401).json({error: "App Check token required"});
        return;
      }
      try {
        await getAppCheck().verifyToken(appCheckToken);
      } catch {
        response.status(401).json({error: "Invalid App Check token"});
        return;
      }
    }
    const page = Math.max(1, Math.min(10, Number(request.query.page) || 1));
    const query = String(request.query.query ?? "").trim().slice(0, 120) || undefined;
    const category = String(request.query.category ?? "").trim().toLowerCase().slice(0, 40) || undefined;

    const [gnews, currents, newsData] = await Promise.all([
      jsonOrNull(gnewsUrl(page, query, category), {"X-Api-Key": gnewsKey.value()}),
      jsonOrNull(currentsUrl(page, query, category), {Authorization: `Bearer ${currentsKey.value()}`}),
      page === 1 ? jsonOrNull(newsDataUrl(query, category)) : Promise.resolve(null),
    ]);

    const merged = [normalizeGnews(gnews), normalizeCurrents(currents), normalizeNewsData(newsData)]
      .flatMap((items, sourceIndex) => items.map((item, index) => ({item, rank: index * 3 + sourceIndex})))
      .sort((a, b) => a.rank - b.rank)
      .map(({item}) => item)
      .filter((item, index, all) => item.webUrl && all.findIndex((other) => other.webUrl === item.webUrl) === index);

    // Never let a shared intermediary replay a response around App Check verification.
    response.set("Cache-Control", "private, max-age=120");
    response.json({
      response: {status: "ok", total: merged.length, results: merged},
    });
  }
);
