package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.ProjectZipExporter
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.VioletSoft

private data class ProjectFile(
    val path: String,
    val labelRes: Int,
    val badge: String,
    val tint: Color,
)

private fun projectFiles(packageName: String): List<ProjectFile> {
    val packagePath = packageName.replace('.', '/')
    return listOf(
        ProjectFile("settings.gradle.kts", R.string.project_file_settings, "KTS", Cyan),
        ProjectFile("build.gradle.kts", R.string.project_file_root_build, "KTS", VioletSoft),
        ProjectFile("app/build.gradle.kts", R.string.project_file_app_build, "KTS", Fuchsia),
        ProjectFile("app/src/main/AndroidManifest.xml", R.string.project_file_manifest, "XML", VioletSoft),
        ProjectFile(
            "app/src/main/java/$packagePath/MainActivity.kt",
            R.string.project_file_code,
            "KT",
            Teal,
        ),
        ProjectFile("app/src/main/res/values/strings.xml", R.string.project_file_resources, "XML", Cyan),
        ProjectFile("README.md", R.string.project_file_readme, "MD", Muted),
    )
}

/**
 * Detail view opened by tapping a project tile: shows the project identity, the
 * files that will be packaged, and the full action set (export / rename /
 * delete / create another project).
 */
@Composable
fun ProjectDetailScreen(
    project: WorkspaceItem,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val packageName = remember(project.name) { ProjectZipExporter.packageName(project.name) }
    val files = remember(packageName) { projectFiles(packageName) }

    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.workspace_back),
                onClick = onBack,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.project_label),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Muted,
                )
            }
            LogoMark(modifier = Modifier.size(40.dp), animated = false)
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2A1B5E), Color(0xFF101637)),
                            ),
                        )
                        .border(1.dp, Cyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "</>",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Cyan,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.project_detail_ready),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = Teal,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.project_detail_package),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                color = Muted,
            )
            Spacer(Modifier.height(5.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x66070B1C))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = Cyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientButton(
                    text = stringResource(R.string.project_detail_export),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Share,
                    onClick = onExport,
                )
                Spacer(Modifier.width(8.dp))
                GlassIconButton(
                    icon = Icons.Filled.Create,
                    contentDescription = stringResource(R.string.action_rename),
                    onClick = onRename,
                    modifier = Modifier.size(46.dp),
                )
                Spacer(Modifier.width(8.dp))
                GlassIconButton(
                    icon = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDelete,
                    modifier = Modifier.size(46.dp),
                )
                Spacer(Modifier.width(8.dp))
                GlassIconButton(
                    icon = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.action_new_project_here),
                    onClick = onNewProject,
                    modifier = Modifier.size(46.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.project_detail_hint),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 15.sp),
                color = Muted.copy(alpha = 0.85f),
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.project_detail_files),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Cyan.copy(alpha = 0.12f))
                    .border(1.dp, Cyan.copy(alpha = 0.3f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    text = files.size.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Cyan,
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.project_detail_files_desc),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = Muted,
        )

        Spacer(Modifier.height(12.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 14.dp,
                vertical = 8.dp,
            ),
        ) {
            files.forEachIndexed { index, file ->
                FileRow(file)
                if (index != files.lastIndex) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x12FFFFFF)),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.home_footer),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = Muted.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun FileRow(file: ProjectFile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(file.tint.copy(alpha = 0.13f))
                .border(1.dp, file.tint.copy(alpha = 0.26f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = file.badge,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = file.tint,
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(file.labelRes),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                color = Ink,
            )
            Spacer(Modifier.height(2.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = file.path,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
