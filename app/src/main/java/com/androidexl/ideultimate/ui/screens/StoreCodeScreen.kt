package com.androidexl.ideultimate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

private val StoreCurve = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

data class StoreCodeTemplate(
    val id: String,
    val title: String,
    val subtitleAr: String,
    val language: ProjectLanguage,
    val iconPreset: Int,
    val stars: String,
    val codeSnippet: String,
)

private val StoreTemplates = listOf(
    StoreCodeTemplate(
        id = "compose_neon_auth",
        title = "NeonAuthUI",
        subtitleAr = "شاشة تسجيل دخول زجاجية متحركة بـ Jetpack Compose",
        language = ProjectLanguage.KOTLIN,
        iconPreset = 3,
        stars = "4.9 ★",
        codeSnippet = """
            @Composable
            fun NeonAuthScreen(onLogin: (String) -> Unit) {
                var email by remember { mutableStateOf("") }
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it })
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onLogin(email) }) { Text("Sign In") }
                }
            }
        """.trimIndent(),
    ),
    StoreCodeTemplate(
        id = "flutter_shop_cart",
        title = "FlutterShopUI",
        subtitleAr = "واجهة متجر إلكتروني وسلة مشتريات متكاملة بـ Flutter",
        language = ProjectLanguage.FLUTTER,
        iconPreset = 1,
        stars = "4.8 ★",
        codeSnippet = """
            class ShopCartPage extends StatefulWidget {
              const ShopCartPage({super.key});
              @override
              State<ShopCartPage> createState() => _ShopCartPageState();
            }

            class _ShopCartPageState extends State<ShopCartPage> {
              int count = 1;
              @override
              Widget build(BuildContext context) {
                return Scaffold(
                  appBar: AppBar(title: const Text('Flutter Shop')),
                  floatingActionButton: FloatingActionButton(
                    onPressed: () => setState(() => count++),
                    child: Text('${'$'}count'),
                  ),
                );
              }
            }
        """.trimIndent(),
    ),
    StoreCodeTemplate(
        id = "java_notes_db",
        title = "JavaNotesManager",
        subtitleAr = "إدارة الملاحظات وحفظ البيانات محليًا بلغة Java",
        language = ProjectLanguage.JAVA,
        iconPreset = 2,
        stars = "4.7 ★",
        codeSnippet = """
            public class NotesRepository {
                private final List<String> notes = new ArrayList<>();

                public void addNote(String text) {
                    if (text != null && !text.trim().isEmpty()) {
                        notes.add(text.trim());
                    }
                }

                public List<String> getAllNotes() {
                    return Collections.unmodifiableList(notes);
                }
            }
        """.trimIndent(),
    ),
    StoreCodeTemplate(
        id = "kotlin_rest_client",
        title = "KotlinApiClient",
        subtitleAr = "عميل شبكة سريع باستخدام Kotlin Coroutines",
        language = ProjectLanguage.KOTLIN,
        iconPreset = 0,
        stars = "4.9 ★",
        codeSnippet = """
            suspend fun fetchJson(url: String): Result<String> =
                withContext(Dispatchers.IO) {
                    runCatching {
                        URL(url).openStream().bufferedReader().use { it.readText() }
                    }
                }
        """.trimIndent(),
    ),
    StoreCodeTemplate(
        id = "flutter_glass_nav",
        title = "FlutterGlassNav",
        subtitleAr = "شريط تنقل سفلي زجاجي مع أنميشن سلس في Flutter",
        language = ProjectLanguage.FLUTTER,
        iconPreset = 4,
        stars = "5.0 ★",
        codeSnippet = """
            Widget buildGlassBar(int selectedIndex, ValueChanged<int> onTap) {
              return Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: const Color(0xCC0D1330),
                  borderRadius: BorderRadius.circular(24),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    IconButton(icon: const Icon(Icons.folder), onPressed: () => onTap(0)),
                    IconButton(icon: const Icon(Icons.code), onPressed: () => onTap(1)),
                  ],
                ),
              );
            }
        """.trimIndent(),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreCodeScreen(
    onUseTemplate: (StoreCodeTemplate) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    var selectedFilter by rememberSaveable { mutableStateOf("ALL") }
    var previewTemplate by remember { mutableStateOf<StoreCodeTemplate?>(null) }

    val filtered = remember(selectedFilter) {
        if (selectedFilter == "ALL") {
            StoreTemplates
        } else {
            StoreTemplates.filter { it.language.name == selectedFilter }
        }
    }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 650, easing = StoreCurve))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 32.dp.toPx()
            },
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogoMark(modifier = Modifier.size(42.dp), animated = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.store_code_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.store_code_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Muted,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Cyan.copy(alpha = 0.14f))
                    .border(1.dp, Cyan.copy(alpha = 0.40f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${StoreTemplates.size} PACKS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Cyan,
                )
            }
        }

        // Language Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChipPill(
                label = stringResource(R.string.filter_all),
                selected = selectedFilter == "ALL",
                accent = Cyan,
                onClick = { selectedFilter = "ALL" },
            )
            ProjectLanguage.entries.forEach { lang ->
                FilterChipPill(
                    label = lang.displayName,
                    selected = selectedFilter == lang.name,
                    accent = languageColor(lang),
                    onClick = { selectedFilter = lang.name },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 6.dp,
                bottom = 110.dp + bottomBarPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            itemsIndexed(filtered, key = { _, item -> item.id }) { _, template ->
                StoreTemplateCard(
                    template = template,
                    onPreview = { previewTemplate = template },
                    onCopy = { copySnippet(context, template) },
                    onUse = { onUseTemplate(template) },
                )
            }
        }
    }

    val currentPreview = previewTemplate
    if (currentPreview != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { previewTemplate = null },
            sheetState = sheetState,
            containerColor = Color(0xFF0B1028),
            contentColor = Ink,
            scrimColor = Color(0xD903060F),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProjectIconView(
                        iconPreset = currentPreview.iconPreset,
                        customIconPath = null,
                        modifier = Modifier.size(46.dp),
                        cornerRadius = 14.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = currentPreview.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Text(
                            text = currentPreview.subtitleAr,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Muted,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF060919))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(14.dp),
                    ) {
                        Text(
                            text = currentPreview.codeSnippet,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                            ),
                            color = Ink,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GradientButton(
                        text = stringResource(R.string.store_use_as_project),
                        icon = Icons.Filled.Add,
                        onClick = {
                            val chosen = currentPreview
                            previewTemplate = null
                            onUseTemplate(chosen)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x19FFFFFF))
                            .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .clickable { copySnippet(context, currentPreview) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.store_copy_code),
                            style = MaterialTheme.typography.labelLarge,
                            color = Cyan,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.22f) else Color(0x12FFFFFF),
        animationSpec = tween(200),
        label = "chip-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.65f) else Color(0x22FFFFFF),
        animationSpec = tween(200),
        label = "chip-border",
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            color = if (selected) accent else Muted,
        )
    }
}

@Composable
private fun StoreTemplateCard(
    template: StoreCodeTemplate,
    onPreview: () -> Unit,
    onCopy: () -> Unit,
    onUse: () -> Unit,
) {
    val accent = languageColor(template.language)
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPreview),
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProjectIconView(
                iconPreset = template.iconPreset,
                customIconPath = null,
                modifier = Modifier.size(50.dp),
                cornerRadius = 16.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(accent.copy(alpha = 0.16f))
                            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = template.language.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = accent,
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = template.subtitleAr,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = template.stars,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                ),
                color = Color(0xFFFBBF24),
            )
        }

        Spacer(Modifier.height(12.dp))

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0x99060919))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            ) {
                Text(
                    text = template.codeSnippet.lines().take(4).joinToString("\n"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                    ),
                    color = Ink.copy(alpha = 0.9f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Violet.copy(alpha = 0.70f), Cyan.copy(alpha = 0.55f)),
                        ),
                    )
                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(onClick = onUse)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.store_use_as_project),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                    color = Color.White,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(onClick = onCopy)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.store_copy_code),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                    color = Cyan,
                )
            }
        }
    }
}

private fun copySnippet(context: Context, template: StoreCodeTemplate) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(template.title, template.codeSnippet))
    Toast.makeText(
        context,
        context.getString(R.string.store_copied_msg, template.title),
        Toast.LENGTH_SHORT,
    ).show()
}
