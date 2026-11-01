package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The notification and summary cards as a screen reader and a finger use them: a card can be
 * dismissed by swiping or by the Dismiss action, an expandable header says whether it is expanded,
 * and the count badge is read as words.
 *
 * Run it on an emulator (`./gradlew connectedDebugAndroidTest`), not on the physical device.
 */
@RunWith(AndroidJUnit4::class)
class NotificationCardsInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun dismissActionReachesTheSameCallbackAsTheSwipe() {
        var dismissed = 0
        rule.setContent {
            DismissibleCard(dismissLabel = DISMISS, onDismiss = { dismissed++ }) {
                Text(BODY)
            }
        }

        val card = rule.onNode(hasCustomAction(DISMISS))
        card.assertIsDisplayed()
        val action = card.fetchSemanticsNode().config[SemanticsActions.CustomActions].single { it.label == DISMISS }
        rule.runOnIdle { action.action() }
        assertEquals(1, dismissed)
    }

    @Test
    fun swipingTheCardAwayDismissesIt() {
        var dismissed = false
        rule.setContent {
            DismissibleCard(dismissLabel = DISMISS, onDismiss = { dismissed = true }) {
                Text(BODY)
            }
        }

        rule.onNodeWithText(BODY).performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertTrue(dismissed)
    }

    @Test
    fun expandableHeaderTogglesAndSaysItsState() {
        rule.setContent {
            var expanded by remember { mutableStateOf(false) }
            NotificationCard(
                appLabel = APP,
                count = 3,
                time = TIME,
                expanded = expanded,
                onToggle = { expanded = !expanded }
            ) {
                Text(BODY)
            }
        }

        val header = rule.onNode(hasStateDescription(COLLAPSED))
        header.assertIsDisplayed()
        header.performClick()
        rule.onNode(hasStateDescription(EXPANDED)).assertIsDisplayed()
        rule.onNode(hasStateDescription(EXPANDED)).performSemanticsAction(SemanticsActions.OnClick)
        rule.onNode(hasStateDescription(COLLAPSED)).assertIsDisplayed()
    }

    @Test
    fun countBadgeIsReadAsWords() {
        rule.setContent {
            NotificationCard(appLabel = APP, count = 3, time = TIME) { Text(BODY) }
        }

        rule.onNodeWithContentDescription("3 notifications").assertIsDisplayed()
    }

    @Test
    fun cardWithoutToggleHasNoClickAction() {
        rule.setContent {
            NotificationCard(appLabel = APP, count = 1, time = TIME) { Text(BODY) }
        }

        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)).assertCountEquals(0)
    }

    private fun hasCustomAction(label: String) = SemanticsMatcher("has custom action $label") { node ->
        node.config.getOrNull(SemanticsActions.CustomActions)?.any { it.label == label } == true
    }

    private fun hasStateDescription(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private companion object {
        const val DISMISS = "Dismiss"
        const val APP = "Messages"
        const val TIME = "5 min. ago"
        const val BODY = "See you at 6"
        const val EXPANDED = "Expanded"
        const val COLLAPSED = "Collapsed"
    }
}
