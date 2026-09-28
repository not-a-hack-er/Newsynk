package com.abpvt.newsapp.ui.news

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.theme.*
import com.abpvt.newsapp.data.repository.ReaderPalette
import android.speech.tts.TextToSpeech
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    navController: NavHostController,
    selectedArticleViewModel: SelectedArticleViewModel = hiltViewModel(),
    fallbackArticle: Article? = null,
    onCommentClick: () -> Unit,
    interactionViewModel: ArticleInteractionViewModel = hiltViewModel(),
    readerPreferencesViewModel: ReaderPreferencesViewModel = hiltViewModel()
) {
    val selectedArticle by selectedArticleViewModel.selectedArticle.collectAsState()
    val article = selectedArticle ?: fallbackArticle
    val readerPalette by readerPreferencesViewModel.palette.collectAsState()
    val readerFontScale by readerPreferencesViewModel.fontScale.collectAsState()
    val readerLineHeight by readerPreferencesViewModel.lineHeight.collectAsState()
    val baseScheme = MaterialTheme.colorScheme
    val readerScheme = when (readerPalette) {
        ReaderPalette.LIGHT -> baseScheme.copy(background = Color(0xFFF9FAFC), onBackground = Color(0xFF111318))
        ReaderPalette.DARK -> baseScheme.copy(background = Color(0xFF111318), onBackground = Color(0xFFF1F3F7))
        ReaderPalette.SEPIA -> baseScheme.copy(background = Color(0xFFF4ECD8), onBackground = Color(0xFF3B3024))
        ReaderPalette.SYSTEM -> baseScheme
    }
    val isDark = readerPalette == ReaderPalette.DARK ||
        (readerPalette == ReaderPalette.SYSTEM && MaterialTheme.colorScheme.background == DarkBackground)

    if (article == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val a = article

    val interactionStates by interactionViewModel.interactionStates.collectAsState()
    val interactionState = interactionStates[a.url]
    val isBookmarked = interactionState?.isBookmarked == true

    LaunchedEffect(a.url) {
        interactionViewModel.loadInteractions(a.url)
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val gNewsTruncationPattern = Regex("""\[\d+ chars\]\s*$""")
    val hasFullBody = !a.content.isNullOrBlank() &&
            !gNewsTruncationPattern.containsMatchIn(a.content!!)

    var showShareDialog by remember { mutableStateOf(false) }
    var showReaderControls by remember { mutableStateOf(false) }
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) textToSpeech?.language = Locale.getDefault()
        }
        textToSpeech = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    if (showShareDialog) {
        com.abpvt.newsapp.ui.news.components.ShareStoryDialog(
            article = a,
            onDismiss = { showShareDialog = false }
        )
    }

    if (showReaderControls) {
        com.abpvt.newsapp.ui.news.components.ReaderControlsSheet(
            palette = readerPalette,
            fontScale = readerFontScale,
            lineHeight = readerLineHeight,
            onPaletteChange = readerPreferencesViewModel::setPalette,
            onFontScaleChange = readerPreferencesViewModel::setFontScale,
            onLineHeightChange = readerPreferencesViewModel::setLineHeight,
            onDismiss = { showReaderControls = false }
        )
    }

    MaterialTheme(colorScheme = readerScheme) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Column {
                            Text(
                                text = a.source.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val readTimeMin = com.abpvt.newsapp.utils.ReadingTimeCalculator.calculateMinutes(
                                a.title, a.description, a.content
                            )
                            Text(
                                text = "⏱️ ${readTimeMin} min read",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showReaderControls = true }) {
                            Icon(Icons.Default.TextFormat, contentDescription = "Reading preferences", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val spokenText = listOfNotNull(a.title, a.description, a.content)
                                .joinToString(". ")
                                .let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_COMPACT).toString() }
                            if (isSpeaking) {
                                textToSpeech?.stop()
                                isSpeaking = false
                            } else {
                                textToSpeech?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "newsynk-${a.id}")
                                isSpeaking = true
                            }
                        }) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = if (isSpeaking) "Stop listening" else "Listen", tint = if (isSpeaking) Amber else Color.White)
                        }
                        IconButton(onClick = {
                            val translated = "https://translate.google.com/translate?sl=auto&u=${android.net.Uri.encode(a.url)}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(translated)))
                        }) {
                            Icon(Icons.Default.Translate, contentDescription = "Translate original article", tint = Color.White)
                        }
                        // Bookmark
                        IconButton(onClick = { interactionViewModel.toggleBookmark(a) }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                                tint = if (isBookmarked) BookmarkActive else Color.White
                            )
                        }
                        // Share
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                        // Comment
                        IconButton(onClick = onCommentClick) {
                            Text("💬", style = MaterialTheme.typography.bodyLarge)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Top Accent Reading Progress Line
                if (scrollState.maxValue > 0) {
                    val progress = (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = Amber,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (hasFullBody) {
                NativeReader(article = a, isDark = isDark, scrollState = scrollState, fontScale = readerFontScale, lineHeight = readerLineHeight)
            } else {
                ArticleSummaryCard(article = a, isDark = isDark, scrollState = scrollState, context = context, fontScale = readerFontScale)
            }
        }
    }
    }
}

@Composable
private fun ArticleSummaryCard(
    article: Article,
    isDark: Boolean,
    scrollState: ScrollState,
    context: android.content.Context,
    fontScale: Float
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        HeroImage(urlToImage = article.urlToImage)
        ArticleHeader(article = article, isDark = isDark)

        com.abpvt.newsapp.ui.news.components.ExecutiveTakeawaysCard(article = article)
        com.abpvt.newsapp.ui.news.components.SourceTransparencyCard(article) {
            openOriginalArticle(context, article.url)
        }

        val desc = article.description
        if (!desc.isNullOrBlank()) {
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (18f * fontScale).sp,
                    lineHeight = (28f * fontScale).sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
            Spacer(Modifier.width(12.dp))
            Text("• • •", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), fontSize = 12.sp)
            Spacer(Modifier.width(12.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        }

        Spacer(Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Color(0xFF1E2A3A) else Color(0xFFF0F4FF),
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "This article has more to offer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Continue reading the full story on ${article.source.name}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        try {
                            val toolbarColor = android.graphics.Color.parseColor("#1565C0")
                            val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                                .setDefaultColorSchemeParams(
                                    androidx.browser.customtabs.CustomTabColorSchemeParams.Builder()
                                        .setToolbarColor(toolbarColor).build()
                                )
                                .setShowTitle(true)
                                .setUrlBarHidingEnabled(true)
                                .setShareState(androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON)
                                .build()
                            customTabsIntent.launchUrl(context, android.net.Uri.parse(article.url))
                        } catch (e: Exception) {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(article.url)
                            )
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Read on ${article.source.name}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun NativeReader(
    article: Article,
    isDark: Boolean,
    scrollState: ScrollState,
    fontScale: Float,
    lineHeight: Float
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        HeroImage(urlToImage = article.urlToImage)
        ArticleHeader(article = article, isDark = isDark)
        com.abpvt.newsapp.ui.news.components.ExecutiveTakeawaysCard(article = article)
        val context = LocalContext.current
        com.abpvt.newsapp.ui.news.components.SourceTransparencyCard(article) {
            openOriginalArticle(context, article.url)
        }
        Spacer(Modifier.height(4.dp))
        BodyText(html = article.content ?: "", isDark = isDark, fontScale = fontScale, lineHeight = lineHeight)
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun HeroImage(urlToImage: String?) {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
        SubcomposeAsyncImage(
            model = urlToImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { HeroShimmer() },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                    contentAlignment = Alignment.Center
                ) { Text("📰", fontSize = 52.sp) }
            }
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
        )
    }
}

@Composable
private fun ArticleHeader(article: Article, isDark: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Surface(shape = RoundedCornerShape(20.dp), color = DeepBlue.copy(alpha = 0.12f)) {
            Text(
                text = article.source.name.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = DeepBlue,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = article.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 30.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (article.author?.firstOrNull() ?: article.source.name.firstOrNull() ?: 'N').toString(),
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(
                    text = article.author ?: article.source.name,
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatReaderDate(article.publishedAt),
                    fontStyle = FontStyle.Italic, fontSize = 11.sp, color = MetadataText
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = DividerColor)
    }
}

@Composable
private fun BodyText(html: String, isDark: Boolean, fontScale: Float, lineHeight: Float) {
    if (html.isBlank()) return
    val plain = remember(html) {
        android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT).toString().trim()
    }
    val paragraphs = remember(plain) {
        plain.split(Regex("\n{1,}")).map { it.trim() }.filter { it.isNotBlank() }
    }
    val bodyColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
    val mutedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        paragraphs.forEachIndexed { index, para ->
            val isHeading = para.length < 80 && para == para.uppercase() && para.any { it.isLetter() }
            if (isHeading) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = para,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = DeepBlue
                )
                Spacer(Modifier.height(6.dp))
            } else {
                if (index == 0 && para.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = para.first().toString(),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = DeepBlue,
                            modifier = Modifier.padding(end = 6.dp).alignByBaseline()
                        )
                        Text(
                            text = para.drop(1),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (18f * fontScale).sp,
                                lineHeight = (18f * fontScale * lineHeight).sp
                            ),
                            color = bodyColor,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                } else {
                    Text(
                        text = para,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (18f * fontScale).sp,
                            lineHeight = (18f * fontScale * lineHeight).sp
                        ),
                        color = bodyColor
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
            Spacer(Modifier.width(12.dp))
            Text("✦", color = mutedColor, fontSize = 12.sp)
            Spacer(Modifier.width(12.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        }
    }
}

private fun openOriginalArticle(context: android.content.Context, url: String) {
    try {
        androidx.browser.customtabs.CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()
            .launchUrl(context, android.net.Uri.parse(url))
    } catch (_: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
    }
}

@Composable
private fun HeroShimmer() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_shimmer")
    val x by infiniteTransition.animateFloat(
        initialValue = -800f, targetValue = 1600f,
        animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "hero_shimmer_x"
    )
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(
                colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
                start = Offset(x, 0f), end = Offset(x + 600f, 600f)
            )
        )
    )
}

private fun formatReaderDate(raw: String): String {
    return try {
        val parsers = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()),
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        ).onEach { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }
        val date = parsers.firstNotNullOfOrNull { runCatching { it.parse(raw) }.getOrNull() } ?: return raw
        java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault()).format(date)
    } catch (e: Exception) {
        raw.takeIf { it.isNotBlank() } ?: ""
    }
}
