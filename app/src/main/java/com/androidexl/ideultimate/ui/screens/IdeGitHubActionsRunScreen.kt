package com.androidexl.ideultimate.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.GitHubActionsBuildEngine
import com.androidexl.ideultimate.data.GitHubToolsRepository
import com.androidexl.ideultimate.data.WorkflowJobItem
import com.androidexl.ideultimate.data.WorkflowRunDetail
import com.androidexl.ideultimate.data.WorkflowStepItem
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun IdeGitHubActionsRunScreen(
    project: WorkspaceItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ghTools = remember { GitHubToolsRepository(context) }
    val account = remember { ghTools.getSavedAccount() }

    val owner = remember(account) { account?.login ?: "AndroidEXL" }
    val repoName = remember(project) {
        val candidate = project.name.replace(" ", "-")
        if (candidate.isNotBlank() && candidate != "IDE-Ultimate") candidate else "IDE-Ultimate"
    }

    val engine = remember(account, owner, repoName) {
        GitHubActionsBuildEngine(
            token = account?.token.orEmpty(),
            owner = owner,
            repo = repoName,
        )
    }

    var runDetail by remember { mutableStateOf<WorkflowRunDetail?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("TREE") } // "TREE" or "LOGS" or "ERROR"
    var selectedErrorDetail by remember { mutableStateOf<String?>(null) }

    val expandedJobs = remember { mutableStateMapOf<Long, Boolean>() }

    fun refreshRun() {
        isLoading = true
        coroutineScope.launch(Dispatchers.IO) {
            val res = engine.fetchLatestWorkflowRun()
            withContext(Dispatchers.Main) {
                isLoading = false
                res.onSuccess {
                    runDetail = it
                    it.jobs.forEach { job ->
                        expandedJobs[job.id] = true
                    }
                    if (it.conclusion == "failure") {
                        selectedErrorDetail = it.errorMessage ?: "حدث خطأ أثناء تنفيذ عملية البناء السحابي."
                    }
                }
                res.onFailure {
                    Toast.makeText(context, it.message ?: "تعذر جلب تفاصيل البناء", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshRun()
    }

    BackHandler {
        if (selectedErrorDetail != null) {
            selectedErrorDetail = null
        } else {
            onBack()
        }
    }

    // If viewing error page
    if (selectedErrorDetail != null) {
        IdeBuildErrorPage(
            errorText = selectedErrorDetail!!,
            taskName = runDetail?.errorTaskName ?: "Gradle Build Task",
            onBack = { selectedErrorDetail = null },
            modifier = modifier,
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B1E)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1330))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                    )
                }

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "GitHub Actions Build",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                            color = Ink,
                        )
                        val statusConclusion = runDetail?.conclusion ?: runDetail?.status ?: "loading"
                        val (badgeColor, badgeText) = when (statusConclusion) {
                            "success" -> Color(0xFF22C55E) to "نجاح ✓"
                            "failure" -> Color(0xFFF87171) to "فشل ✕"
                            "in_progress", "queued" -> Color(0xFFFBBF24) to "جاري البناء..."
                            else -> Cyan to statusConclusion
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = badgeColor,
                            )
                        }
                    }

                    Text(
                        text = "$owner/$repoName • ${project.name}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Cyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(onClick = { refreshRun() }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = Cyan,
                    )
                }
            }

            // Tabs Selector: Tree View vs Full Logs vs Error
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0E1638))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "TREE" to "شجرة المهام (Workflow Tree)",
                    "LOGS" to "سجل البناء الكامل (Full Logs)",
                ).forEach { (key, label) ->
                    val active = activeTab == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) Cyan.copy(alpha = 0.2f) else Color(0x14FFFFFF))
                            .border(1.dp, if (active) Cyan else Color(0x24FFFFFF), RoundedCornerShape(8.dp))
                            .clickable { activeTab = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                            ),
                            color = if (active) Cyan else Muted,
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Cyan)
                }
            } else if (runDetail == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "لا توجد سجلات متاحة حالياً. تأكد من رفع المشروع وبدء البناء.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            } else {
                val detail = runDetail!!

                // Error Warning Banner if build failed
                if (detail.conclusion == "failure" && !detail.errorMessage.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF38101E))
                            .border(1.dp, Color(0xFFF87171), RoundedCornerShape(12.dp))
                            .clickable {
                                selectedErrorDetail = detail.errorMessage
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "فشل في عملية البناء! (اضغط لعرض تفاصيل الخطأ)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color(0xFFF87171),
                            )
                            Text(
                                text = detail.errorTaskName ?: "Gradle Task Failure",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                ),
                                color = Color(0xFFFDA4AF),
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Main Tab Content
                if (activeTab == "TREE") {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(detail.jobs, key = { it.id }) { job ->
                            val isExpanded = expandedJobs[job.id] ?: true
                            val jobFailed = job.conclusion == "failure"

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0F1735))
                                    .border(
                                        width = if (jobFailed) 1.5.dp else 1.dp,
                                        color = if (jobFailed) Color(0xFFF87171) else Color(0x28FFFFFF),
                                        shape = RoundedCornerShape(14.dp),
                                    ),
                            ) {
                                // Job Root Item
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedJobs[job.id] = !isExpanded }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Cyan,
                                        modifier = Modifier.size(20.dp),
                                    )

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = job.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = JetBrainsMono,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                            ),
                                            color = Ink,
                                        )
                                        Text(
                                            text = "${job.steps.size} خطوات • ${job.status}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Muted,
                                        )
                                    }

                                    val jConclusion = job.conclusion ?: job.status
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (jConclusion) {
                                                    "success" -> Color(0xFF22C55E).copy(alpha = 0.2f)
                                                    "failure" -> Color(0xFFF87171).copy(alpha = 0.2f)
                                                    else -> Cyan.copy(alpha = 0.2f)
                                                },
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = jConclusion,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = when (jConclusion) {
                                                "success" -> Color(0xFF22C55E)
                                                "failure" -> Color(0xFFF87171)
                                                else -> Cyan
                                            },
                                        )
                                    }
                                }

                                // Children Steps Tree
                                if (isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF090E26))
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        job.steps.forEach { step ->
                                            val stepFailed = step.conclusion == "failure"
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (stepFailed) Color(0xFF2B0B17) else Color(0x14FFFFFF))
                                                    .border(1.dp, if (stepFailed) Color(0xFFF87171) else Color(0x18FFFFFF), RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        if (stepFailed) {
                                                            selectedErrorDetail = detail.errorMessage ?: "فشل في خطوة: ${step.name}"
                                                        } else {
                                                            Toast.makeText(context, "${step.name}: ${step.conclusion ?: step.status}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                // Status Icon
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when (step.conclusion) {
                                                                "success" -> Color(0xFF22C55E).copy(alpha = 0.2f)
                                                                "failure" -> Color(0xFFF87171).copy(alpha = 0.2f)
                                                                else -> Color(0x22FFFFFF)
                                                            },
                                                        ),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        imageVector = when (step.conclusion) {
                                                            "success" -> Icons.Filled.Check
                                                            "failure" -> Icons.Filled.Close
                                                            else -> Icons.Filled.Refresh
                                                        },
                                                        contentDescription = null,
                                                        tint = when (step.conclusion) {
                                                            "success" -> Color(0xFF22C55E)
                                                            "failure" -> Color(0xFFF87171)
                                                            else -> Cyan
                                                        },
                                                        modifier = Modifier.size(12.dp),
                                                    )
                                                }

                                                Text(
                                                    text = "${step.number}. ${step.name}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = JetBrainsMono,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = if (stepFailed) FontWeight.Bold else FontWeight.Normal,
                                                    ),
                                                    color = if (stepFailed) Color(0xFFFDA4AF) else Ink,
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )

                                                if (stepFailed) {
                                                    Text(
                                                        text = "عرض الخطأ ➔",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.5.sp,
                                                        ),
                                                        color = Color(0xFFF87171),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Full Logs Tab (LTR monospace text)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFF050816))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState()),
                        ) {
                            Text(
                                text = detail.fullLogs.ifBlank { "لا توجد سجلات مفصلة بعد" },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    lineHeight = 17.sp,
                                ),
                                color = Color(0xFFE2E8F0),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IdeBuildErrorPage(
    errorText: String,
    taskName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF14050D)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF260A19))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Build Error Details • تفاصيل الخطأ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFFF87171),
                    )
                    Text(
                        text = "Task: $taskName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Color(0xFFFDA4AF),
                    )
                }
            }

            // Error Display Box (LTR text)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F040A))
                        .border(1.dp, Color(0xFFF87171).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        ),
                        color = Color(0xFFFECDD3),
                    )
                }
            }
        }
    }
}
