export type NewsProvider = "gnews" | "currents" | "newsdata";

/** Translate Newsynk's UI labels to each provider's documented category codes. */
export function providerCategory(provider: NewsProvider, category?: string): string | undefined {
  if (!category) return undefined;
  const normalized = category.trim().toLowerCase();
  if (!normalized || normalized === "all" || normalized === "for you") return undefined;
  if (normalized === "tech") return "technology";
  if (provider === "currents" && normalized === "world") return "regional";
  return normalized;
}
