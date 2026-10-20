package com.androidexl.ideultimate.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.androidexl.ideultimate.data.GitHubAccount
import com.androidexl.ideultimate.data.GitHubContentEntry
import com.androidexl.ideultimate.data.GitHubFileDetail
import com.androidexl.ideultimate.data.GitHubRepoItem
import com.androidexl.ideultimate.data.GitHubToolsRepository
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IdeGitHubToolsScreen(
    project: WorkspaceItem,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gitHubRepo = remember { GitHubToolsRepository(context) }

    var account by remember { mutableStateOf(gitHubRepo.getSavedAccount()) }
    var openedRepo by remember { mutableStateOf<GitHubRepoItem?>(null) }

    // If a repo is opened, show repo folder browser
    val currentOpenedRepo = openedRepo
    if (account != null && currentOpenedRepo != null) {
        GitHubRepoBrowserScreen(
            account = account!!,
            repo = currentOpenedRepo,
            currentProject = project,
            repository = repository,
            gitHubRepo = gitHubRepo,
            onBack = { openedRepo = null },
            modifier = modifier,
        )
        return
    }

    BackHandler(onBack = onBack)

    var reposList by remember { mutableStateOf<List<GitHubRepoItem>>(emptyList()) }
    var isLoadingRepos by remember { mutableStateOf(false) }
    var reposSearchQuery by rememberSaveable { mutableStateOf("") }

    fun refreshRepos() {
        if (account == null) return
        isLoadingRepos = true
        coroutineScope.launch(Dispatchers.IO) {
            val res = gitHubRepo.listUserRepositories()
            withContext(Dispatchers.Main) {
                isLoadingRepos = false
                res.onSuccess { reposList = it }
                res.onFailure {
                    Toast.makeText(context, it.message ?: "تعذر جلب المستودعات", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(account) {
        if (account != null) {
            refreshRepos()
        }
    }

    var showMainFabSheet by rememberSaveable { mutableStateOf(false) }
    var showCreateRepoDialog by rememberSaveable { mutableStateOf(false) }
    var showUploadProjectDialog by rememberSaveable { mutableStateOf(false) }

    var longPressedRepo by remember { mutableStateOf<GitHubRepoItem?>(null) }
    var showRenameRepoDialog by remember { mutableStateOf<GitHubRepoItem?>(null) }
    var showDeleteRepoDialog by remember { mutableStateOf<GitHubRepoItem?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Midnight),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Midnight)
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
                        text = "GitHub Tools",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = Ink,
                    )
                    Text(
                        text = if (account != null) "@${account?.login} • ${reposList.size} مستودع" else "تسجيل الدخول وإدارة المستودعات",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Cyan,
                    )
                }

                if (account != null) {
                    IconButton(onClick = { refreshRepos() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = Cyan,
                        )
                    }
                    IconButton(
                        onClick = {
                            gitHubRepo.logout()
                            account = null
                            reposList = emptyList()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Logout",
                            tint = Color(0xFFF87171),
                        )
                    }
                }
            }

            // Case A: Not Logged In -> Show Login Form
            if (account == null) {
                GitHubLoginCard(
                    onLoginSuccess = { loggedInAccount ->
                        account = loggedInAccount
                    },
                    gitHubRepo = gitHubRepo,
                )
            } else {
                // Case B: Logged In -> Show Search bar + Repositories List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    OutlinedTextField(
                        value = reposSearchQuery,
                        onValueChange = { reposSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "بحث في المستودعات (Search repositories)...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Muted,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Cyan,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = {
                            if (reposSearchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    tint = Muted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { reposSearchQuery = "" },
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedContainerColor = Midnight,
                            unfocusedContainerColor = Midnight,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                }

                val filteredRepos = remember(reposList, reposSearchQuery) {
                    val q = reposSearchQuery.trim().lowercase()
                    if (q.isEmpty()) {
                        reposList
                    } else {
                        reposList.filter {
                            it.name.lowercase().contains(q) || it.description.lowercase().contains(q)
                        }
                    }
                }

                if (isLoadingRepos) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Cyan)
                    }
                } else if (filteredRepos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (reposList.isEmpty()) "لا توجد مستودعات حتى الآن، اضغط + لإنشاء أو رفع مشروع" else "لا توجد نتائج مطابقة للبحث",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(filteredRepos, key = { it.id }) { repoItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Midnight)
                                    .border(1.dp, GlassStrokeTop, RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { openedRepo = repoItem },
                                        onLongClick = { longPressedRepo = repoItem },
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Cyan.copy(alpha = 0.16f))
                                        .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = IdeIcons.Folder,
                                        contentDescription = null,
                                        tint = Cyan,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = repoItem.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = JetBrainsMono,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                            ),
                                            color = Ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (repoItem.isPrivate) {
                                            Icon(
                                                imageVector = Icons.Filled.Lock,
                                                contentDescription = "Private",
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }

                                    if (repoItem.description.isNotBlank()) {
                                        Text(
                                            text = repoItem.description,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Muted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    Text(
                                        text = "${repoItem.fullName} • ${repoItem.defaultBranch}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 9.5.sp,
                                        ),
                                        color = Muted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB when Logged in
        if (account != null) {
            FloatingActionButton(
                onClick = { showMainFabSheet = true },
                containerColor = Cyan,
                contentColor = Color(0xFF050816),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Actions",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }

    // Main FAB Bottom Sheet (Upload current project OR Create new repository)
    if (showMainFabSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMainFabSheet = false },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xCC03060F),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "إجراءات GitHub",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )

                // 1. Upload Current Project
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showMainFabSheet = false
                            showUploadProjectDialog = true
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = IdeIcons.Upload,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(22.dp),
                    )
                    Column {
                        Text(
                            text = "رفع المشروع الحالي",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(
                            text = "رفع كافة ملفات (${project.name}) إلى مستودع جديد أو موجود",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                // 2. Create New Repository
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showMainFabSheet = false
                            showCreateRepoDialog = true
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(22.dp),
                    )
                    Column {
                        Text(
                            text = "إنشاء repository جديدة",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(
                            text = "إنشاء مستودع فارغ جديد على حسابك في GitHub",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }

    // Long Press on Repo Item Bottom Sheet: Rename or Delete
    val repoToManage = longPressedRepo
    if (repoToManage != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { longPressedRepo = null },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xCC03060F),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = repoToManage.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )
                Text(
                    text = repoToManage.fullName,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = Cyan,
                )

                Spacer(Modifier.height(4.dp))

                // 1. Download Entire Project
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            longPressedRepo = null
                            Toast.makeText(context, "جاري تحميل مستودع ${repoToManage.name} بالكامل...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch(Dispatchers.IO) {
                                val rootEntry = GitHubContentEntry(
                                    name = repoToManage.name,
                                    path = "",
                                    sha = "",
                                    size = 0L,
                                    type = "dir",
                                    downloadUrl = "",
                                )
                                val dlRes = gitHubRepo.downloadFileOrFolderRecursive(repoToManage.ownerLogin, repoToManage.name, rootEntry)
                                withContext(Dispatchers.Main) {
                                    dlRes.onSuccess {
                                        Toast.makeText(context, "تم تحميل المشروع كاملاً في: ${it.path}", Toast.LENGTH_LONG).show()
                                    }
                                    dlRes.onFailure {
                                        Toast.makeText(context, it.message ?: "فشل التحميل", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = IdeIcons.Download,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "تحميل المشروع كامل (Download Repo)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(
                            text = "تنزيل كافة ملفات ومجلدات المستودع إلى مجلد التنزيلات بالجهاز",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                // 2. Rename
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            longPressedRepo = null
                            showRenameRepoDialog = repoToManage
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "تعديل الاسم",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(
                            text = "تغيير اسم المستودع في GitHub",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                // 3. Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF38101E))
                        .clickable {
                            longPressedRepo = null
                            showDeleteRepoDialog = repoToManage
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "حذف العنصر",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF87171),
                        )
                        Text(
                            text = "حذف المستودع بالكامل من GitHub",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }

    // Rename Repo Dialog
    val itemToRename = showRenameRepoDialog
    if (itemToRename != null) {
        var inputName by rememberSaveable { mutableStateOf(itemToRename.name) }
        var isSaving by remember { mutableStateOf(false) }
        var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!isSaving) showRenameRepoDialog = null },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "تعديل اسم المستودع",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                            errorMsg = null
                        },
                        label = { Text("الاسم الجديد") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg.orEmpty(),
                            color = Color(0xFFF87171),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSaving,
                    onClick = {
                        isSaving = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val res = gitHubRepo.renameRepository(itemToRename.ownerLogin, itemToRename.name, inputName)
                            withContext(Dispatchers.Main) {
                                isSaving = false
                                res.onSuccess {
                                    refreshRepos()
                                    showRenameRepoDialog = null
                                }
                                res.onFailure {
                                    errorMsg = it.message ?: "فشل تعديل الاسم"
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isSaving) "جاري الحفظ..." else "حفظ", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameRepoDialog = null }, enabled = !isSaving) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Delete Repo Dialog
    val itemToDelete = showDeleteRepoDialog
    if (itemToDelete != null) {
        var isDeleting by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteRepoDialog = null },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "حذف المستودع",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF87171),
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف المستودع ${itemToDelete.fullName}؟ سيتم حذفه من حساب GitHub نهائياً.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val res = gitHubRepo.deleteRepository(itemToDelete.ownerLogin, itemToDelete.name)
                            withContext(Dispatchers.Main) {
                                isDeleting = false
                                res.onSuccess {
                                    refreshRepos()
                                    showDeleteRepoDialog = null
                                }
                                res.onFailure {
                                    Toast.makeText(context, it.message ?: "فشل الحذف", Toast.LENGTH_LONG).show()
                                    showDeleteRepoDialog = null
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isDeleting) "جاري الحذف..." else "حذف", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteRepoDialog = null }, enabled = !isDeleting) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Create New Repo Dialog
    if (showCreateRepoDialog) {
        var repoName by rememberSaveable { mutableStateOf("") }
        var repoDesc by rememberSaveable { mutableStateOf("") }
        var isPrivate by rememberSaveable { mutableStateOf(false) }
        var isCreating by remember { mutableStateOf(false) }
        var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isCreating) showCreateRepoDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "إنشاء repository جديدة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = repoName,
                        onValueChange = {
                            repoName = it
                            errorMsg = null
                        },
                        label = { Text("اسم المستودع (e.g. MyAwesomeApp)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    OutlinedTextField(
                        value = repoDesc,
                        onValueChange = { repoDesc = it },
                        label = { Text("الوصف (اختياري)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (isPrivate) "مستودع خاص (Private)" else "مستودع عام (Public)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink,
                        )
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Cyan,
                                checkedTrackColor = Cyan.copy(alpha = 0.3f),
                            ),
                        )
                    }
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg.orEmpty(),
                            color = Color(0xFFF87171),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isCreating && repoName.isNotBlank(),
                    onClick = {
                        isCreating = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val res = gitHubRepo.createRepository(repoName, repoDesc, isPrivate)
                            withContext(Dispatchers.Main) {
                                isCreating = false
                                res.onSuccess {
                                    refreshRepos()
                                    showCreateRepoDialog = false
                                }
                                res.onFailure {
                                    errorMsg = it.message ?: "فشل إنشاء المستودع"
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isCreating) "جاري الإنشاء..." else "إنشاء", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRepoDialog = false }, enabled = !isCreating) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Upload Current Project Dialog
    if (showUploadProjectDialog) {
        var repoName by rememberSaveable {
            mutableStateOf(project.name.replace(" ", "-"))
        }
        var repoDesc by rememberSaveable {
            mutableStateOf("Uploaded from IDE Ultimate - ${project.packageName}")
        }
        var isPrivate by rememberSaveable { mutableStateOf(false) }
        var isUploading by remember { mutableStateOf(false) }
        var progressText by remember { mutableStateOf("") }
        var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isUploading) showUploadProjectDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "رفع المشروع الحالي إلى GitHub",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "سيتم تجميع كافة ملفات المشروع (${project.name}) ورفعها مباشرة إلى مستودع جديد على حسابك في GitHub.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    OutlinedTextField(
                        value = repoName,
                        onValueChange = {
                            repoName = it
                            errorMsg = null
                        },
                        label = { Text("اسم المستودع") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    OutlinedTextField(
                        value = repoDesc,
                        onValueChange = { repoDesc = it },
                        label = { Text("الوصف") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (isPrivate) "مستودع خاص (Private)" else "مستودع عام (Public)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink,
                        )
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Cyan,
                                checkedTrackColor = Cyan.copy(alpha = 0.3f),
                            ),
                        )
                    }
                    if (progressText.isNotEmpty()) {
                        Text(
                            text = progressText,
                            color = Cyan,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        )
                    }
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg.orEmpty(),
                            color = Color(0xFFF87171),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isUploading && repoName.isNotBlank(),
                    onClick = {
                        isUploading = true
                        progressText = "جاري تحضير ملفات المشروع..."
                        coroutineScope.launch(Dispatchers.IO) {
                            val files = repository.collectAllProjectFilesForUpload(project)
                            val res = gitHubRepo.uploadCurrentProjectToGitHub(
                                rawRepoName = repoName,
                                description = repoDesc,
                                isPrivate = isPrivate,
                                projectFiles = files,
                                onProgress = { cur, total, name ->
                                    progressText = "جاري رفع ($cur / $total):\n$name"
                                },
                            )
                            withContext(Dispatchers.Main) {
                                isUploading = false
                                res.onSuccess {
                                    Toast.makeText(context, "تم رفع المشروع بنجاح إلى GitHub!", Toast.LENGTH_LONG).show()
                                    refreshRepos()
                                    showUploadProjectDialog = false
                                }
                                res.onFailure {
                                    errorMsg = it.message ?: "فشل الرفع"
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isUploading) "جاري الرفع..." else "بدء الرفع", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadProjectDialog = false }, enabled = !isUploading) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }
}

@Composable
private fun GitHubLoginCard(
    onLoginSuccess: (GitHubAccount) -> Unit,
    gitHubRepo: GitHubToolsRepository,
) {
    var tokenInput by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(IndigoDeep)
                .border(1.5.dp, Cyan, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "GH",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = Cyan,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "تسجيل الدخول باستخدام GitHub",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = Ink,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "أدخل رمز الدخول (GitHub Personal Access Token) بصلاحيات (repo, delete_repo) للتحكم الكامل في مستودعاتك.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            lineHeight = 18.sp,
        )

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = tokenInput,
            onValueChange = {
                tokenInput = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Personal Access Token (ghp_...)") },
            placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cyan,
                unfocusedBorderColor = GlassStrokeTop,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
            ),
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = Color(0xFFF87171),
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (tokenInput.isNotBlank() && !isLoading) Cyan else Color(0x3338BDF8))
                .clickable(enabled = tokenInput.isNotBlank() && !isLoading) {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch(Dispatchers.IO) {
                        val res = gitHubRepo.verifyAndSaveToken(tokenInput)
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            res.onSuccess {
                                Toast.makeText(context, "مرحباً ${it.name}!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(it)
                            }
                            res.onFailure {
                                errorMessage = it.message ?: "تعذر التحقق من الرمز"
                            }
                        }
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF050816), strokeWidth = 2.dp)
            } else {
                Text(
                    text = "تسجيل الدخول",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF050816),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun GitHubRepoBrowserScreen(
    account: GitHubAccount,
    repo: GitHubRepoItem,
    currentProject: WorkspaceItem,
    repository: WorkspaceRepository,
    gitHubRepo: GitHubToolsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentPath by rememberSaveable { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<GitHubContentEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Multi-selection for folders/files
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedEntries = remember { mutableStateListOf<GitHubContentEntry>() }

    var openedFileDetail by remember { mutableStateOf<GitHubFileDetail?>(null) }

    fun refreshContents() {
        isLoading = true
        coroutineScope.launch(Dispatchers.IO) {
            val res = gitHubRepo.listRepoContents(repo.ownerLogin, repo.name, currentPath)
            withContext(Dispatchers.Main) {
                isLoading = false
                res.onSuccess {
                    entries = it
                    selectedEntries.clear()
                }
                res.onFailure {
                    Toast.makeText(context, it.message ?: "تعذر فتح المجلد", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(currentPath) {
        refreshContents()
    }

    BackHandler {
        if (openedFileDetail != null) {
            openedFileDetail = null
        } else if (isSelectionMode) {
            isSelectionMode = false
            selectedEntries.clear()
        } else if (currentPath.isNotEmpty()) {
            val parent = currentPath.substringBeforeLast('/', "")
            currentPath = parent
        } else {
            onBack()
        }
    }

    // Modal dialogs and sheets
    var showRepoFabSheet by rememberSaveable { mutableStateOf(false) }
    var showCreateFileDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateFolderDialog by rememberSaveable { mutableStateOf(false) }

    var longPressedFolder by remember { mutableStateOf<GitHubContentEntry?>(null) }
    var longPressedFile by remember { mutableStateOf<GitHubContentEntry?>(null) }
    var showRenameFileDialog by remember { mutableStateOf<GitHubContentEntry?>(null) }
    var showDeleteFileConfirmDialog by remember { mutableStateOf<GitHubContentEntry?>(null) }
    var showSelectionActionsSheet by rememberSaveable { mutableStateOf(false) }

    // Launchers for importing from device
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                val res = gitHubRepo.importPhoneFilesToRepo(
                    owner = repo.ownerLogin,
                    repo = repo.name,
                    currentFolderPath = currentPath,
                    uris = uris,
                )
                withContext(Dispatchers.Main) {
                    res.onSuccess {
                        Toast.makeText(context, "تم رفع $it ملف بنجاح إلى المستودع!", Toast.LENGTH_SHORT).show()
                        refreshContents()
                    }
                    res.onFailure {
                        Toast.makeText(context, it.message ?: "فشل الرفع", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
        if (treeUri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val res = gitHubRepo.importDeviceFolderTreeToRepo(
                    owner = repo.ownerLogin,
                    repo = repo.name,
                    currentFolderPath = currentPath,
                    treeUri = treeUri,
                )
                withContext(Dispatchers.Main) {
                    res.onSuccess {
                        Toast.makeText(context, "تم رفع مجلد الجهاز ($it ملف) إلى المستودع بنجاح!", Toast.LENGTH_LONG).show()
                        refreshContents()
                    }
                    res.onFailure {
                        Toast.makeText(context, it.message ?: "فشل الرفع", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Midnight),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Midnight)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        if (currentPath.isNotEmpty()) {
                            currentPath = currentPath.substringBeforeLast('/', "")
                        } else {
                            onBack()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = Ink,
                    )
                    Text(
                        text = if (currentPath.isEmpty()) "root" else currentPath,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Cyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Cyan.copy(alpha = 0.2f))
                            .border(1.dp, Cyan, RoundedCornerShape(8.dp))
                            .clickable { showSelectionActionsSheet = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${selectedEntries.size} محددة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Cyan,
                            ),
                        )
                    }
                }

                IconButton(onClick = { refreshContents() }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = Cyan,
                    )
                }
            }

            // Path Breadcrumbs Bar
            if (currentPath.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Midnight)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "root",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = Cyan,
                        modifier = Modifier.clickable { currentPath = "" },
                    )
                    val segments = currentPath.split('/')
                    var accumulated = ""
                    segments.forEach { segment ->
                        accumulated = if (accumulated.isEmpty()) segment else "$accumulated/$segment"
                        val thisPath = accumulated
                        Text(text = "/", color = Muted, fontSize = 10.sp)
                        Text(
                            text = segment,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = if (thisPath == currentPath) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (thisPath == currentPath) Ink else Cyan,
                            modifier = Modifier.clickable { currentPath = thisPath },
                        )
                    }
                }
            }

            // Files & Folders List
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Cyan)
                }
            } else if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "المجلد فارغ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(entries, key = { it.path }) { item ->
                        val isSelected = selectedEntries.any { it.path == item.path }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) IndigoDeep else Midnight)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Cyan else GlassStrokeTop,
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (isSelected) {
                                                selectedEntries.removeAll { it.path == item.path }
                                                if (selectedEntries.isEmpty()) isSelectionMode = false
                                            } else {
                                                selectedEntries.add(item)
                                            }
                                        } else if (item.isDirectory) {
                                            currentPath = item.path
                                        } else {
                                            // Open file detail
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val detailRes = gitHubRepo.readFileContent(repo.ownerLogin, repo.name, item.path)
                                                withContext(Dispatchers.Main) {
                                                    detailRes.onSuccess { openedFileDetail = it }
                                                    detailRes.onFailure {
                                                        Toast.makeText(context, it.message ?: "تعذر قراءة الملف", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        if (isSelectionMode) {
                                            showSelectionActionsSheet = true
                                        } else if (item.isDirectory) {
                                            longPressedFolder = item
                                        } else {
                                            longPressedFile = item
                                        }
                                    },
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (item.isDirectory) Cyan.copy(alpha = 0.16f) else GlassStrokeTop)
                                    .border(1.dp, if (item.isDirectory) Cyan.copy(alpha = 0.5f) else GlassStrokeTop, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (item.isDirectory) IdeIcons.Folder else Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = if (item.isDirectory) Cyan else Muted,
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                    ),
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = if (item.isDirectory) "مجلد" else "${item.size} بايت",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Muted,
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Cyan,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB inside project/folder
        FloatingActionButton(
            onClick = { showRepoFabSheet = true },
            containerColor = Cyan,
            contentColor = Color(0xFF050816),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New",
                modifier = Modifier.size(24.dp),
            )
        }
    }

    // Repo FAB Bottom Sheet: (Import file, Import folder, Create file, Create folder)
    if (showRepoFabSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRepoFabSheet = false },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xCC03060F),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "إضافة إلى المستودع (${if (currentPath.isEmpty()) "root" else currentPath})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )

                // 1. استيراد ملف من الهاتف
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showRepoFabSheet = false
                            filePickerLauncher.launch(arrayOf("*/*"))
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = IdeIcons.Upload, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
                    Text(text = "استيراد ملف من الهاتف", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }

                // 2. استيراد فولدر من الجهاز
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showRepoFabSheet = false
                            folderPickerLauncher.launch(null)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = IdeIcons.Folder, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Text(text = "استيراد فولدر من الجهاز", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }

                // 3. إنشاء فولدر جديد
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showRepoFabSheet = false
                            showCreateFolderDialog = true
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                    Text(text = "إنشاء فولدر جديد", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }

                // 4. إنشاء ملف جديد
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showRepoFabSheet = false
                            showCreateFileDialog = true
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                    Text(text = "إنشاء ملف جديد", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }
            }
        }
    }

    // Long Press Folder Bottom Sheet: (حذف الفولدر / تحميل الفولدر / تحديد الفولدرات)
    val folderToManage = longPressedFolder
    if (folderToManage != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { longPressedFolder = null },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xCC03060F),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = folderToManage.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )

                // 1. تحميل الفولدر
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            longPressedFolder = null
                            Toast.makeText(context, "جاري تحميل المجلد ${folderToManage.name} إلى التنزيلات...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch(Dispatchers.IO) {
                                val dlRes = gitHubRepo.downloadFileOrFolderRecursive(repo.ownerLogin, repo.name, folderToManage)
                                withContext(Dispatchers.Main) {
                                    dlRes.onSuccess {
                                        Toast.makeText(context, "تم حفظ المجلد في: ${it.path}", Toast.LENGTH_LONG).show()
                                    }
                                    dlRes.onFailure {
                                        Toast.makeText(context, it.message ?: "فشل التحميل", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = IdeIcons.Download, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
                    Text(text = "تحميل الفولدر (Download)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }

                // 2. تحديد الفولدرات
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            longPressedFolder = null
                            isSelectionMode = true
                            if (selectedEntries.none { it.path == folderToManage.path }) {
                                selectedEntries.add(folderToManage)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                    Text(text = "تحديد الفولدرات (Multi-Select)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }

                // 3. حذف الفولدر
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF38101E))
                        .clickable {
                            longPressedFolder = null
                            Toast.makeText(context, "جاري حذف ${folderToManage.name}...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch(Dispatchers.IO) {
                                val delRes = gitHubRepo.deleteFileOrFolderRecursive(repo.ownerLogin, repo.name, folderToManage)
                                withContext(Dispatchers.Main) {
                                    delRes.onSuccess {
                                        Toast.makeText(context, "تم حذف المجلد بنجاح!", Toast.LENGTH_SHORT).show()
                                        refreshContents()
                                    }
                                    delRes.onFailure {
                                        Toast.makeText(context, it.message ?: "فشل الحذف", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                    Text(text = "حذف الفولدر", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF87171))
                }
            }
        }
    }

    // Selection Actions Bottom Sheet: (حذف الملفات/الفولدرات المختارة أو تحميل الملفات/الفولدرات المحددة)
    if (showSelectionActionsSheet && selectedEntries.isNotEmpty()) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSelectionActionsSheet = false },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xCC03060F),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "العناصر المحددة (${selectedEntries.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )

                // تحميل الملفات المحددة
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            showSelectionActionsSheet = false
                            val toDownload = selectedEntries.toList()
                            Toast.makeText(context, "جاري تحميل ${toDownload.size} عناصر...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch(Dispatchers.IO) {
                                toDownload.forEach { entry ->
                                    gitHubRepo.downloadFileOrFolderRecursive(repo.ownerLogin, repo.name, entry)
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "تم حفظ العناصر المحددة في مجلد Downloads!", Toast.LENGTH_LONG).show()
                                    isSelectionMode = false
                                    selectedEntries.clear()
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = IdeIcons.Download, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
                    Text(text = "تحميل الملفات المحددة", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }

                // حذف الملفات المختارة
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF38101E))
                        .clickable {
                            showSelectionActionsSheet = false
                            val toDelete = selectedEntries.toList()
                            Toast.makeText(context, "جاري حذف ${toDelete.size} عناصر من GitHub...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch(Dispatchers.IO) {
                                toDelete.forEach { entry ->
                                    gitHubRepo.deleteFileOrFolderRecursive(repo.ownerLogin, repo.name, entry)
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "تم حذف العناصر المختارة بنجاح!", Toast.LENGTH_SHORT).show()
                                    isSelectionMode = false
                                    selectedEntries.clear()
                                    refreshContents()
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                    Text(text = "حذف الملفات المختارة", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF87171))
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        var folderNameInput by rememberSaveable { mutableStateOf("") }
        var isCreating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isCreating) showCreateFolderDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(text = "إنشاء فولدر جديد", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    label = { Text("اسم الفولدر (e.g. assets)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = GlassStrokeTop,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isCreating && folderNameInput.isNotBlank(),
                    onClick = {
                        isCreating = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val res = gitHubRepo.createFolderInRepo(
                                owner = repo.ownerLogin,
                                repo = repo.name,
                                parentPath = currentPath,
                                rawFolderName = folderNameInput,
                            )
                            withContext(Dispatchers.Main) {
                                isCreating = false
                                res.onSuccess {
                                    refreshContents()
                                    showCreateFolderDialog = false
                                }
                                res.onFailure {
                                    Toast.makeText(context, it.message ?: "فشل إنشاء الفولدر", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isCreating) "جاري الإنشاء..." else "إنشاء", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }, enabled = !isCreating) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Create File Dialog
    if (showCreateFileDialog) {
        var fileNameInput by rememberSaveable { mutableStateOf("") }
        var fileContentInput by rememberSaveable { mutableStateOf("") }
        var isCreating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isCreating) showCreateFileDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(text = "إنشاء ملف جديد في المستودع", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("اسم الملف (e.g. README.md / Code.kt)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    OutlinedTextField(
                        value = fileContentInput,
                        onValueChange = { fileContentInput = it },
                        label = { Text("المحتوى الأولي للملف") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isCreating && fileNameInput.isNotBlank(),
                    onClick = {
                        isCreating = true
                        val targetPath = if (currentPath.isEmpty()) fileNameInput.trim() else "${currentPath.trim('/')}/${fileNameInput.trim()}"
                        coroutineScope.launch(Dispatchers.IO) {
                            val res = gitHubRepo.uploadOrUpdateFile(
                                owner = repo.ownerLogin,
                                repo = repo.name,
                                path = targetPath,
                                bytes = fileContentInput.toByteArray(Charsets.UTF_8),
                                commitMessage = "Create $fileNameInput via IDE Ultimate",
                            )
                            withContext(Dispatchers.Main) {
                                isCreating = false
                                res.onSuccess {
                                    refreshContents()
                                    showCreateFileDialog = false
                                }
                                res.onFailure {
                                    Toast.makeText(context, it.message ?: "فشل إنشاء الملف", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isCreating) "جاري الإنشاء..." else "إنشاء", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }, enabled = !isCreating) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // View/Edit file detail editor page
    val fileDetail = openedFileDetail
    if (fileDetail != null) {
        var editedContent by remember(fileDetail.path) { mutableStateOf(fileDetail.textContent) }
        var isUpdatingFile by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Midnight),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Midnight)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { openedFileDetail = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink,
                        )
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = fileDetail.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                            ),
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = fileDetail.path,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                            ),
                            color = Cyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Save Button -> Pushes commit update to GitHub
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable(enabled = !isUpdatingFile) {
                                isUpdatingFile = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val res = gitHubRepo.uploadOrUpdateFile(
                                        owner = repo.ownerLogin,
                                        repo = repo.name,
                                        path = fileDetail.path,
                                        bytes = editedContent.toByteArray(Charsets.UTF_8),
                                        commitMessage = "Update ${fileDetail.name} via IDE Ultimate",
                                        existingSha = fileDetail.sha,
                                    )
                                    withContext(Dispatchers.Main) {
                                        isUpdatingFile = false
                                        res.onSuccess {
                                            Toast.makeText(context, "تم رفع التحديث بنجاح إلى GitHub!", Toast.LENGTH_SHORT).show()
                                            refreshContents()
                                            openedFileDetail = null
                                        }
                                        res.onFailure {
                                            Toast.makeText(context, it.message ?: "فشل رفع التحديث", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isUpdatingFile) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF22C55E), strokeWidth = 2.dp)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Save",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "حفظ ورفع",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    ),
                                    color = Color(0xFF22C55E),
                                )
                            }
                        }
                    }
                }

                // Editor Body (LTR monospace)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Midnight)
                            .padding(10.dp),
                    ) {
                        OutlinedTextField(
                            value = editedContent,
                            onValueChange = { editedContent = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Ink,
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan,
                                unfocusedBorderColor = GlassStrokeTop,
                                focusedTextColor = Ink,
                                unfocusedTextColor = Ink,
                            ),
                        )
                    }
                }
            }
        }
    }

    // Long Press File Bottom Sheet: (حذف ملف / تغيير اسم الملف / تحميل الملف)
    val fileToManage = longPressedFile
    if (fileToManage != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { longPressedFile = null },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xCC03060F),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = fileToManage.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )
                Text(
                    text = fileToManage.path,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = Cyan,
                )

                Spacer(Modifier.height(4.dp))

                // 1. تحميل الملف
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            longPressedFile = null
                            Toast.makeText(context, "جاري تحميل ${fileToManage.name} إلى التنزيلات...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch(Dispatchers.IO) {
                                val dlRes = gitHubRepo.downloadFileOrFolderRecursive(repo.ownerLogin, repo.name, fileToManage)
                                withContext(Dispatchers.Main) {
                                    dlRes.onSuccess {
                                        Toast.makeText(context, "تم حفظ الملف في: ${it.path}", Toast.LENGTH_LONG).show()
                                    }
                                    dlRes.onFailure {
                                        Toast.makeText(context, it.message ?: "فشل التحميل", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = IdeIcons.Download, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
                    Column {
                        Text(text = "تحميل الملف (Download)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                        Text(text = "حفظ الملف على هاتفك داخل مجلد Downloads", style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }

                // 2. تغيير اسم الملف
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            longPressedFile = null
                            showRenameFileDialog = fileToManage
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
                    Column {
                        Text(text = "تغيير اسم الملف (Rename)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                        Text(text = "تعديل اسم الملف في المستودع", style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }

                // 3. حذف ملف
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF38101E))
                        .clickable {
                            longPressedFile = null
                            showDeleteFileConfirmDialog = fileToManage
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                    Column {
                        Text(text = "حذف الملف (Delete)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF87171))
                        Text(text = "حذف الملف نهائياً من GitHub", style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }
            }
        }
    }

    // Rename File Dialog
    val fileToRename = showRenameFileDialog
    if (fileToRename != null) {
        var inputName by rememberSaveable { mutableStateOf(fileToRename.name) }
        var isRenaming by remember { mutableStateOf(false) }
        var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isRenaming) showRenameFileDialog = null },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(text = "تغيير اسم الملف", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                            errorMsg = null
                        },
                        label = { Text("الاسم الجديد") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    if (errorMsg != null) {
                        Text(text = errorMsg.orEmpty(), color = Color(0xFFF87171), style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isRenaming && inputName.isNotBlank() && inputName != fileToRename.name,
                    onClick = {
                        isRenaming = true
                        coroutineScope.launch(Dispatchers.IO) {
                            // Read content -> create new path -> delete old
                            val detailRes = gitHubRepo.readFileContent(repo.ownerLogin, repo.name, fileToRename.path)
                            val detail = detailRes.getOrNull()
                            if (detail == null) {
                                withContext(Dispatchers.Main) {
                                    isRenaming = false
                                    errorMsg = "تعذر قراءة محتوى الملف القديم"
                                }
                                return@launch
                            }
                            val parent = fileToRename.path.substringBeforeLast('/', "")
                            val newPath = if (parent.isEmpty()) inputName.trim() else "$parent/${inputName.trim()}"
                            val upRes = gitHubRepo.uploadOrUpdateFile(
                                owner = repo.ownerLogin,
                                repo = repo.name,
                                path = newPath,
                                bytes = detail.rawBytes,
                                commitMessage = "Rename ${fileToRename.name} to $inputName",
                            )
                            if (upRes.isSuccess) {
                                gitHubRepo.deleteFileOrFolderRecursive(repo.ownerLogin, repo.name, fileToRename)
                            }
                            withContext(Dispatchers.Main) {
                                isRenaming = false
                                if (upRes.isSuccess) {
                                    refreshContents()
                                    showRenameFileDialog = null
                                } else {
                                    errorMsg = upRes.exceptionOrNull()?.message ?: "فشل تعديل الاسم"
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isRenaming) "جاري الحفظ..." else "حفظ", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameFileDialog = null }, enabled = !isRenaming) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Delete File Confirm Dialog
    val fileToDelete = showDeleteFileConfirmDialog
    if (fileToDelete != null) {
        var isDeleting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteFileConfirmDialog = null },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(text = "حذف الملف", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF87171))
            },
            text = {
                Text(text = "هل أنت متأكد من حذف ${fileToDelete.name} من المستودع نهائياً؟", style = MaterialTheme.typography.bodyMedium, color = Ink)
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val res = gitHubRepo.deleteFileOrFolderRecursive(repo.ownerLogin, repo.name, fileToDelete)
                            withContext(Dispatchers.Main) {
                                isDeleting = false
                                res.onSuccess {
                                    refreshContents()
                                    showDeleteFileConfirmDialog = null
                                }
                                res.onFailure {
                                    Toast.makeText(context, it.message ?: "فشل الحذف", Toast.LENGTH_LONG).show()
                                    showDeleteFileConfirmDialog = null
                                }
                            }
                        }
                    },
                ) {
                    Text(if (isDeleting) "جاري الحذف..." else "حذف", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFileConfirmDialog = null }, enabled = !isDeleting) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }
}

