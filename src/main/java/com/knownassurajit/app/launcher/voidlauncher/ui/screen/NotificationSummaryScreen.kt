package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.app.Application
import android.app.Notification
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.AppNotificationSummary
import com.knownassurajit.app.launcher.voidlauncher.helper.AiSummarizer
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability
import com.knownassurajit.app.launcher.voidlauncher.helper.NotificationService
import com.knownassurajit.app.launcher.voidlauncher.ui.components.DismissibleCard
import com.knownassurajit.app.launcher.voidlauncher.ui.components.NotificationAccessPrompt
import com.knownassurajit.app.launcher.voidlauncher.ui.components.NotificationCard
import com.knownassurajit.app.launcher.voidlauncher.ui.components.PanelMessage
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidPanelTitle
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.components.panelTitlePadding
import com.knownassurajit.app.launcher.voidlauncher.ui.components.rememberNotificationAccess
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ──

class NotificationSummaryViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "NotificationSummaryVM"
        private const val SUMMARY_DEBOUNCE_MS = 250L
    }

    private val summarizer = AiSummarizer(application.applicationContext)
    private val summaryJobs = mutableMapOf<String, Job>()
    private var generation = 0L

    private val _summaries = MutableStateFlow<List<AppNotificationSummary>>(emptyList())
    val summaries: StateFlow<List<AppNotificationSummary>> = _summaries.asStateFlow()

    private val _isAiAvailable = MutableStateFlow<Boolean?>(null)
    val isAiAvailable: StateFlow<Boolean?> = _isAiAvailable.asStateFlow()

    init {
        viewModelScope.launch {
            _isAiAvailable.value = summarizer.isAvailable()
        }

        viewModelScope.launch {
            NotificationService.notificationsState.collect { groups ->
                generation += 1
                val currentGeneration = generation
                val activePackages = groups.map { it.packageName }.toSet()
                summaryJobs.keys.filter { it !in activePackages }.forEach { pkg ->
                    summaryJobs.remove(pkg)?.cancel()
                }

                val summaryList = groups
                    .sortedByDescending { it.latestTimestamp }
                    .map { group ->
                        AppNotificationSummary(
                            packageName = group.packageName,
                            appLabel = group.appLabel,
                            notifications = group.notifications,
                            latestTimestamp = group.latestTimestamp,
                            isLoading = true
                        )
                    }

                _summaries.value = summaryList

                // Generate AI summaries in background
                summaryList.forEach { summary ->
                    summaryJobs[summary.packageName]?.cancel()
                    summaryJobs[summary.packageName] = launch {
                        val texts = extractNotificationTexts(summary.notifications)

                        val aiResult = summarizer.summarize(summary.appLabel, texts)
                        if (currentGeneration != generation) return@launch

                        val fallbackSummary = texts.joinToString(". ")
                        _summaries.value = _summaries.value.map { current ->
                            if (current.packageName == summary.packageName) {
                                current.copy(
                                    aiSummary = aiResult ?: fallbackSummary,
                                    isLoading = false
                                )
                            } else {
                                current
                            }
                        }
                    }
                }
            }
        }
    }

    fun dismissApp(packageName: String) {
        summaryJobs.remove(packageName)?.cancel()
        NotificationService.dismissNotificationsForPackage(packageName)
        _summaries.value = _summaries.value.filter { it.packageName != packageName }
    }

    private fun extractNotificationTexts(notifications: List<android.service.notification.StatusBarNotification>): List<String> {
        return notifications.mapNotNull { sbn ->
            val notification = sbn.notification
            val extras = notification.extras

            // ── Tier 1: Try MessagingStyle extraction for conversation reconstruction ──
            val messagingText = try {
                val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
                if (style != null) {
                    val messages = style.messages
                    if (messages.isNotEmpty()) {
                        messages.joinToString(" | ") { msg: NotificationCompat.MessagingStyle.Message ->
                            val sender = msg.person?.name ?: style.conversationTitle ?: ""
                            val text = msg.text?.toString()?.trim().orEmpty()
                            val mimeType = msg.dataMimeType
                            val mediaTag = when {
                                mimeType == null -> ""
                                mimeType.startsWith("image/") -> " [Image attached]"
                                mimeType.startsWith("video/") -> " [Video attached]"
                                mimeType.startsWith("audio/") -> " [Audio attached]"
                                else -> " [Media attached]"
                            }
                            if (sender.isNotBlank()) "$sender: $text$mediaTag" else "$text$mediaTag"
                        }
                    } else null
                } else null
            } catch (_: Exception) { null }

            if (!messagingText.isNullOrBlank()) return@mapNotNull messagingText

            // ── Tier 2: Standard notification fields ──
            val title = extras.getCharSequence("android.title")?.toString()?.trim().orEmpty()
            val text = extras.getCharSequence("android.text")?.toString()?.trim().orEmpty()
            val bigText = extras.getCharSequence("android.bigText")?.toString()?.trim().orEmpty()
            val lines = extras.getCharSequenceArray("android.textLines")
                ?.map { it.toString().trim() }
                ?.filter { it.isNotBlank() }
                .orEmpty()

            // Detect media metadata from extras — no binary processing
            val hasImage = extras.containsKey(Notification.EXTRA_PICTURE) ||
                           extras.containsKey(Notification.EXTRA_LARGE_ICON)
            val mediaTag = if (hasImage) " [Image attached]" else ""

            when {
                title.isNotBlank() && bigText.isNotBlank() -> "$title: $bigText$mediaTag"
                title.isNotBlank() && text.isNotBlank() -> "$title: $text$mediaTag"
                bigText.isNotBlank() -> "$bigText$mediaTag"
                text.isNotBlank() -> "$text$mediaTag"
                lines.isNotEmpty() -> lines.joinToString(" • ") + mediaTag
                title.isNotBlank() -> "$title$mediaTag"
                else -> null
            }
        }
    }
}


// ── Screen ──

@Composable
fun NotificationSummaryScreen(
    viewModel: NotificationSummaryViewModel = viewModel(),
    onBack: () -> Unit
) {
    if (!FeatureAvailability.isNotificationSummaryAvailable(LocalContext.current)) {
        FeatureUnavailableScreen(
            stringResource(R.string.notification_summary),
            stringResource(R.string.summary_unavailable_body),
            onBack
        )
        return
    }

    val hasAccess = rememberNotificationAccess()
    val summaries by viewModel.summaries.collectAsState()
    val aiAvailable by viewModel.isAiAvailable.collectAsState()
    val totalCount = remember(summaries) { summaries.sumOf { it.notifications.size } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = LocalFixedStatusBarHeight.current)
            .navigationBarsPadding()
            .screenBackSwipe(onBack)
    ) {
        VoidPanelTitle(
            text = stringResource(R.string.notification_summary),
            modifier = Modifier.panelTitlePadding()
        ) {
            if (hasAccess && totalCount > 0) TotalCountPill(totalCount)
        }
        if (hasAccess && aiAvailable == false) {
            Text(
                text = stringResource(R.string.ai_summary_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = VoidDimens.screenPadding,
                    end = VoidDimens.screenPadding,
                    bottom = VoidDimens.rowSpacing
                )
            )
        }
        VoidSectionDivider(modifier = Modifier.padding(horizontal = VoidDimens.screenPadding))

        val contentModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        when {
            !hasAccess -> NotificationAccessPrompt(modifier = contentModifier)
            summaries.isEmpty() -> PanelMessage(
                title = stringResource(R.string.summary_all_clear),
                body = stringResource(R.string.no_summary),
                modifier = contentModifier
            )
            else -> SummaryList(summaries = summaries, onDismiss = viewModel::dismissApp, modifier = contentModifier)
        }
    }
}

/** How many notifications the summary covers, said in words to a screen reader. */
@Composable
private fun TotalCountPill(count: Int) {
    val description = pluralStringResource(R.plurals.notification_count, count, count)
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .padding(start = VoidDimens.rowSpacing)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = VoidDimens.rowSpacing, vertical = VoidDimens.compactSpacing)
        )
    }
}

@Composable
private fun SummaryList(
    summaries: List<AppNotificationSummary>,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissLabel = stringResource(R.string.dismiss)
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing),
        verticalArrangement = Arrangement.spacedBy(VoidDimens.cardGap)
    ) {
        items(items = summaries, key = { it.packageName }) { summary ->
            DismissibleCard(
                dismissLabel = dismissLabel,
                onDismiss = { onDismiss(summary.packageName) },
                modifier = Modifier.animateItem()
            ) {
                SummaryCard(summary)
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: AppNotificationSummary) {
    val relativeTime = remember(summary.latestTimestamp) { relativeTimeOf(summary.latestTimestamp) }
    NotificationCard(
        appLabel = summary.appLabel,
        count = summary.notifications.size,
        time = relativeTime
    ) {
        if (summary.isLoading) {
            Text(
                text = stringResource(R.string.summarizing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val bullets = remember(summary.aiSummary) { SummaryBullets.parse(summary.aiSummary) }
            when (bullets.size) {
                0 -> SummaryText(stringResource(R.string.summary_empty), muted = true)
                1 -> SummaryText(bullets.first())
                else -> Column(verticalArrangement = Arrangement.spacedBy(VoidDimens.compactSpacing)) {
                    bullets.forEach { bullet -> SummaryText(stringResource(R.string.summary_bullet, bullet)) }
                }
            }
        }
    }
}

@Composable
private fun SummaryText(text: String, muted: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    )
}

/** Splits a summary into its points: one per line, without the bullet a model or the fallback put in front. */
internal object SummaryBullets {
    /** A bullet, star or hyphen that starts a line and stands alone: followed by space or nothing ("Re-sent" keeps its hyphen). */
    private val marker = Regex("""^[•*-](\s+|$)""")

    fun parse(text: String?): List<String> =
        text.orEmpty()
            .lines()
            .map { line -> line.trim().replaceFirst(marker, "").trim() }
            .filter { it.isNotBlank() }
}
