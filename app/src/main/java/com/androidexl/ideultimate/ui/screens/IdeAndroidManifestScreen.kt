package com.androidexl.ideultimate.ui.screens

import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.GlassStrokeTop
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.Muted
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.CodeEditorFlavor
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted

@Composable
fun IdeAndroidManifestScreen(
    project: WorkspaceItem,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    val initialContent = remember(project.id) {
        repository.readProjectManifest(project)
    }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(initialContent, selection = TextRange(initialContent.length)))
    }

    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    var showAutocomplete by rememberSaveable { mutableStateOf(false) }

    fun updateContent(newValue: TextFieldValue, recordHistory: Boolean = true) {
        if (recordHistory && newValue.text != textFieldValue.text) {
            undoStack.add(textFieldValue)
            redoStack.clear()
        }
        textFieldValue = newValue
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(textFieldValue)
            textFieldValue = prev
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(textFieldValue)
            textFieldValue = next
        }
    }

    fun applyCompletion(suggestion: IdeCodeCompletionSuggestion) {
        val curText = textFieldValue.text
        val cursor = textFieldValue.selection.start
        val before = curText.substring(0, cursor)
        val after = curText.substring(cursor)

        // Replace partial token if matched
        val tokenMatch = Regex("([<a-zA-Z0-9_.:\"-]+)$").find(before)
        val (prefix, insert) = if (tokenMatch != null) {
            val tok = tokenMatch.value
            val isTag = tok.startsWith("<")
            val p = before.dropLast(tok.length)
            val ins = if (isTag && suggestion.insertText.startsWith("<")) {
                suggestion.insertText
            } else if (isTag) {
                "<" + suggestion.insertText
            } else {
                suggestion.insertText
            }
            p to ins
        } else {
            before to suggestion.insertText
        }

        val newText = prefix + insert + after
        val newCursor = (prefix.length + insert.length - suggestion.cursorBackOffset).coerceIn(0, newText.length)
        updateContent(TextFieldValue(newText, TextRange(newCursor)))
        showAutocomplete = false
    }

    val beforeCursor = remember(textFieldValue.text, textFieldValue.selection.start) {
        val cursor = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
        textFieldValue.text.substring(0, cursor)
    }

    val xmlSuggestions = remember(beforeCursor) {
        if (beforeCursor.isBlank()) {
            emptyList()
        } else {
            AndroidXmlCompletions.computeXmlCompletions(beforeCursor, isManifest = true)
        }
    }

    val linesCount = remember(textFieldValue.text) {
        textFieldValue.text.count { it == '\n' } + 1
    }

    val verticalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .imePadding(),
    ) {
        // 1. Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Midnight)
                .padding(horizontal = 8.dp, vertical = 6.dp),
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
                    text = "AndroidManifest.xml",
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
                    text = "${project.name} • ${project.packageName}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFFFB923C),
                )
            }

            // Undo Button
            IconButton(
                onClick = { handleUndo() },
                enabled = undoStack.isNotEmpty(),
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = IdeIcons.Undo,
                    contentDescription = "Undo",
                    tint = if (undoStack.isNotEmpty()) Cyan else Muted,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Redo Button
            IconButton(
                onClick = { handleRedo() },
                enabled = redoStack.isNotEmpty(),
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = IdeIcons.Redo,
                    contentDescription = "Redo",
                    tint = if (redoStack.isNotEmpty()) Cyan else Muted,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Reset Default Button
            IconButton(
                onClick = {
                    val defaultXml = repository.readProjectManifest(project)
                    updateContent(TextFieldValue(defaultXml, TextRange(defaultXml.length)))
                },
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(18.dp),
                )
            }

            // Save Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable {
                        repository.writeProjectManifest(project, textFieldValue.text)
                        Toast.makeText(context, "تم حفظ AndroidManifest.xml بنجاح", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
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
                        text = "حفظ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                        color = Color(0xFF22C55E),
                    )
                }
            }
        }

        // 2. Editor Body & Autocomplete Floating Box (LTR for code editing)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DeepNavy),
            ) {
                val minEditorHeight = (linesCount * 20).coerceAtLeast(300).dp
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                        .padding(vertical = 8.dp),
                ) {
                    // Line Numbers
                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 6.dp)
                            .defaultMinSize(minHeight = minEditorHeight),
                        horizontalAlignment = Alignment.End,
                    ) {
                        for (line in 1..linesCount) {
                            Text(
                                text = line.toString(),
                                style = TextStyle(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.5.sp,
                                    lineHeight = 20.sp,
                                    color = Muted,
                                ),
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(minEditorHeight)
                            .background(GlassStrokeTop),
                    )

                    // Editable XML BasicTextField
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newVal ->
                            updateContent(newVal)
                            showAutocomplete = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = minEditorHeight)
                            .padding(start = 10.dp, end = 12.dp),
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = Ink,
                        ),
                        cursorBrush = SolidColor(Cyan),
                        visualTransformation = AndroidCodeSyntaxTransformation(CodeEditorFlavor.XML_ANDROID),
                    )
                }

                // Autocomplete Dropdown overlay
                if (showAutocomplete && xmlSuggestions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Midnight)
                            .border(1.dp, Color(0xFFFB923C).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x26FB923C))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "مكمل نصوص XML و Permissions (${xmlSuggestions.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    ),
                                    color = Color(0xFFFB923C),
                                )
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Muted,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { showAutocomplete = false },
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp),
                            ) {
                                items(xmlSuggestions) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { applyCompletion(item) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(item.category.color.copy(alpha = 0.18f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = item.category.badge.substringBefore(" •"),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = item.category.color,
                                            )
                                        }

                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = JetBrainsMono,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                ),
                                                color = Ink,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (item.detail.isNotEmpty()) {
                                                Text(
                                                    text = item.detail,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                                    color = Muted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Symbol and XML Snippets bar
        val symbols = listOf("<", ">", "\"", "=", "/", ":", "android:", "uses-permission", "activity", "application")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepNavy)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            symbols.forEach { sym ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IndigoDeep)
                        .border(1.dp, GlassStrokeTop, RoundedCornerShape(6.dp))
                        .clickable {
                            val cur = textFieldValue.text
                            val c = textFieldValue.selection.start
                            val updated = cur.substring(0, c) + sym + cur.substring(c)
                            updateContent(TextFieldValue(updated, TextRange(c + sym.length)))
                            showAutocomplete = true
                        }
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sym,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Cyan,
                    )
                }
            }
        }
    }
}
