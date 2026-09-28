package com.abpvt.newsapp.ui.news.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.ui.theme.Amber
import com.abpvt.newsapp.ui.theme.DeepBlue
import com.abpvt.newsapp.ui.theme.GradientEnd
import com.abpvt.newsapp.ui.theme.GradientStart
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareStoryDialog(
    article: Article,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share Article",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose how you'd like to share this story",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Option 1: Share Link
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        shareLink(context, article)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DeepBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Share Article Link", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Send link to WhatsApp, Twitter, Messages", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 2: Share Story Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        shareStoryCard(context, article)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Amber, Color(0xFFFF6F00)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Share as Story Card 📸", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Generate branded image card for Instagram & Stories", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun shareLink(context: Context, article: Article) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "📰 ${article.title}\n\nRead on Newsynk: ${article.url}"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
}

private fun shareStoryCard(context: Context, article: Article) {
    try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Background
        val bgPaint = Paint().apply {
            shader = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                android.graphics.Color.parseColor("#1565C0"),
                android.graphics.Color.parseColor("#1A237E"),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card container
        val cardMargin = 80f
        val cardRect = RectF(cardMargin, 360f, width - cardMargin, height - 360f)
        val cardPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)

        // Newsynk Logo Badge
        val logoPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            textSize = 64f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("📰 Newsynk", 140f, 500f, logoPaint)

        // Source badge
        val sourcePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FFB300")
            textSize = 42f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(article.source.name.uppercase(), 140f, 580f, sourcePaint)

        // Title Text
        val titlePaint = android.text.TextPaint().apply {
            color = android.graphics.Color.parseColor("#0D0D0D")
            textSize = 56f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val layoutWidth = (width - (cardMargin * 2) - 120).toInt()
        val staticLayout = android.text.StaticLayout.Builder.obtain(
            article.title, 0, article.title.length, titlePaint, layoutWidth
        ).build()

        canvas.save()
        canvas.translate(140f, 660f)
        staticLayout.draw(canvas)
        canvas.restore()

        // Footer CTA
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#888888")
            textSize = 38f
            isAntiAlias = true
        }
        canvas.drawText("Stay synced. Download Newsynk on Play Store", 140f, height - 460f, footerPaint)

        // Save image to cache directory
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "newsynk_story_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, "Read on Newsynk: ${article.url}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Story Card"))
    } catch (e: Exception) {
        shareLink(context, article)
    }
}
