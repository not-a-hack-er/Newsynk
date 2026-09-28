package com.abpvt.newsapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.ui.components.PremiumBottomBar
import com.abpvt.newsapp.ui.news.components.SourceTransparencyCard
import org.junit.Rule
import org.junit.Test

class PremiumComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBarShowsFourPrimaryDestinations() {
        composeRule.setContent {
            MaterialTheme {
                PremiumBottomBar(rememberNavController(), currentRoute = "news")
            }
        }

        listOf("Home", "Explore", "Saved", "Profile").forEach { label ->
            composeRule.onNodeWithText(label).assertIsDisplayed()
        }
    }

    @Test
    fun transparencyCardLabelsOpinionContent() {
        val article = Article(
            title = "Opinion: a better way forward",
            url = "https://example.com/story",
            sectionName = "Opinion"
        )
        composeRule.setContent {
            MaterialTheme { SourceTransparencyCard(article, onOpenOriginal = {}) }
        }

        composeRule.onNodeWithText("Source transparency").assertIsDisplayed()
        composeRule.onNodeWithText("Content type: Opinion or analysis").assertIsDisplayed()
    }
}
