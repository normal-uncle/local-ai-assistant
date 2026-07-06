# Visual Redesign ("Calm & Focused") Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the app's stock-Material3, light-only, flat UI with a cohesive "Calm & Focused" design system (tokens + shared components), full light+dark support, a bottom navigation bar, and redesigned versions of all 6 screens — presentation only, no logic changes.

**Architecture:** Expand the existing `ui_component` module into the design-system hub (color/type/spacing/shape/elevation tokens + light&dark `AssistantTheme` + ~9 reusable composables). Feature modules refactor their `*Screen.kt` to consume those components. The `app` module gains a bottom-nav shell around the existing Navigation3 `NavDisplay`.

**Tech Stack:** Kotlin 2.2, Jetpack Compose (BOM 2026.01.00), Material3, `material-icons-extended`, Navigation3, Hilt, Coil (feature modules only), Gradle wrapper.

---

## Testing Strategy (read first)

This is a **presentation-only** redesign of Compose UI. Pure visual styling is not meaningfully unit-testable, so verification for each task is:

1. **Compile the touched module** — `./gradlew :<module>:compileDebugKotlin` must succeed. This is the primary gate for every task.
2. **`@Preview` composables** — every new component ships light + dark previews so the engineer visually confirms in Android Studio's preview pane.
3. **Existing regression tests must keep passing** — notably instrumented tests that rely on `testTag`s. **Preserve every existing `testTag` string** during screen refactors: `"capture_input"`, `"capture_thumbnail"`, `"capture_prepare"`, `"capture_top_open_chat/list/settings"` usage in `CaptureScreenTest`, and `"memo_list"`. Check `feature_capture/src/androidTest/.../CaptureScreenTest.kt` and `feature_chat`/`feature_memo` screen tests before and after.
4. **Behavioral Compose tests** are added only where real logic exists (bottom-bar selection, chat bubble role alignment) using the compose test infra that already exists in feature modules.

Run commands use `./gradlew` (works in the Bash/git-bash shell on this Windows machine).

**Do NOT change any ViewModel, repository, DI, or navigation *data* (routes/args) except where a task explicitly says so (Task 17 nav shell).**

---

## File Structure

**`ui_component` module — new/changed files:**
- `theme/ColorSet.kt` (modify) — full light+dark raw palette
- `theme/TextSet.kt` (modify) — refined type scale + `headlineSmall`, `titleSmall`, `labelSmall`
- `theme/Spacing.kt` (create) — `Spacing` dp tokens
- `theme/Shapes.kt` (create) — `AssistantShapes` + `Radius`
- `theme/Elevation.kt` (create) — `Elevation` dp tokens
- `theme/AssistantColors.kt` (create) — custom `success` role + `LocalAssistantColors`
- `theme/AssistantTheme.kt` (modify) — light+dark schemes, shapes, provides `AssistantColors`
- `component/AssistantScaffold.kt` (create)
- `component/AssistantTopBar.kt` (create)
- `component/AssistantButtons.kt` (create) — `PrimaryButton`, `SecondaryButton`
- `component/AssistantChip.kt` (create)
- `component/ChatBubble.kt` (create)
- `component/MemoCard.kt` (create)
- `component/SectionCard.kt` (create)
- `component/EmptyState.kt` (create)
- `component/AssistantBottomBar.kt` (create) — generic `BottomNavItem` list

**`app` module:**
- `res/values/strings.xml` (+ `values-en`) — `nav_capture/chat/memo/settings`
- `AssistantApp.kt` (modify) — bottom-nav shell

**Feature screens (modify):** `ChatScreen.kt`, `MemoListScreen.kt`, `MemoDetailScreen.kt`, `CaptureScreen.kt`, `CaptureScene.kt`, `SettingsScreen.kt`, `OnboardingScreen.kt`, `MemoScene.kt`.

---

## PHASE 1 — Design Tokens (`ui_component`)

### Task 1: Full light+dark color palette

**Files:**
- Modify: `ui_component/src/main/java/com/just/assistant/ui/component/theme/ColorSet.kt`

- [ ] **Step 1: Replace `ColorSet` with the full palette**

```kotlin
package com.just.assistant.ui.component.theme

import androidx.compose.ui.graphics.Color

object ColorSet {
    // ---- Light ----
    val L_BACKGROUND = Color(0xFFF7F8FA)
    val L_SURFACE = Color(0xFFFFFFFF)
    val L_SURFACE_VARIANT = Color(0xFFEEF0F3)
    val L_ON_SURFACE = Color(0xFF1A1D21)
    val L_ON_SURFACE_VARIANT = Color(0xFF5B6472)
    val L_OUTLINE = Color(0xFFDDE1E7)
    val L_PRIMARY = Color(0xFF3B6EF5)
    val L_ON_PRIMARY = Color(0xFFFFFFFF)
    val L_PRIMARY_CONTAINER = Color(0xFFE4ECFF)
    val L_ON_PRIMARY_CONTAINER = Color(0xFF2A57C4)
    val L_SUCCESS = Color(0xFF2E9E6B)
    val L_SUCCESS_CONTAINER = Color(0xFFD7F2E5)
    val L_ON_SUCCESS_CONTAINER = Color(0xFF166B47)
    val L_ERROR = Color(0xFFE5484D)
    val L_ON_ERROR = Color(0xFFFFFFFF)

    // ---- Dark ----
    val D_BACKGROUND = Color(0xFF0F1216)
    val D_SURFACE = Color(0xFF171B21)
    val D_SURFACE_VARIANT = Color(0xFF1F242B)
    val D_ON_SURFACE = Color(0xFFE6E9EF)
    val D_ON_SURFACE_VARIANT = Color(0xFF9AA4B2)
    val D_OUTLINE = Color(0xFF2A3038)
    val D_PRIMARY = Color(0xFF6E8DFF)
    val D_ON_PRIMARY = Color(0xFF0B1220)
    val D_PRIMARY_CONTAINER = Color(0xFF22304F)
    val D_ON_PRIMARY_CONTAINER = Color(0xFFAFC2FF)
    val D_SUCCESS = Color(0xFF3FBE86)
    val D_SUCCESS_CONTAINER = Color(0xFF16342A)
    val D_ON_SUCCESS_CONTAINER = Color(0xFFA6E9CB)
    val D_ERROR = Color(0xFFFF6169)
    val D_ON_ERROR = Color(0xFF2A0A0C)

    val WHITE_100 = Color(0xFFFFFFFF)
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :ui_component:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. (This temporarily breaks `AssistantTheme.kt` which references old names — that's fixed in Task 6. If it fails on `AssistantTheme.kt`, that's expected; proceed to Task 6 before committing, OR do Tasks 1–6 then compile+commit together.)

- [ ] **Step 3: Commit** (after Task 6 compiles green, commit Tasks 1–6 together — see Task 6 Step commit)

---

### Task 2: Refined typography

**Files:**
- Modify: `ui_component/src/main/java/com/just/assistant/ui/component/theme/TextSet.kt`

- [ ] **Step 1: Replace `TextSet`**

```kotlin
package com.just.assistant.ui.component.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

object TextSet {
    val HeadlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp, letterSpacing = (-0.01).em)
    val TitleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp, letterSpacing = (-0.005).em)
    val TitleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)
    val TitleSmall = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
    val BodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp)
    val BodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp)
    val LabelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
    val LabelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
}
```

- [ ] **Step 2:** Compile with Task 6 (deferred). No standalone commit.

---

### Task 3: Spacing tokens

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/theme/Spacing.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
```

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin` (self-contained, should pass independently).

---

### Task 4: Shape / radius tokens

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/theme/Shapes.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AssistantShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

object Radius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}
```

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.

---

### Task 5: Elevation tokens + custom `success` color role

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/theme/Elevation.kt`
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/theme/AssistantColors.kt`

- [ ] **Step 1: Create `Elevation.kt`**

```kotlin
package com.just.assistant.ui.component.theme

import androidx.compose.ui.unit.dp

object Elevation {
    val level0 = 0.dp
    val card = 1.dp
    val sheet = 2.dp
    val fab = 3.dp
}
```

- [ ] **Step 2: Create `AssistantColors.kt`** (Material `ColorScheme` has no success role; expose it via CompositionLocal)

```kotlin
package com.just.assistant.ui.component.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AssistantColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
)

val LightAssistantColors = AssistantColors(
    success = ColorSet.L_SUCCESS,
    successContainer = ColorSet.L_SUCCESS_CONTAINER,
    onSuccessContainer = ColorSet.L_ON_SUCCESS_CONTAINER,
)

val DarkAssistantColors = AssistantColors(
    success = ColorSet.D_SUCCESS,
    successContainer = ColorSet.D_SUCCESS_CONTAINER,
    onSuccessContainer = ColorSet.D_ON_SUCCESS_CONTAINER,
)

val LocalAssistantColors = staticCompositionLocalOf { LightAssistantColors }
```

- [ ] **Step 3: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.

---

### Task 6: Light+dark `AssistantTheme`

**Files:**
- Modify: `ui_component/src/main/java/com/just/assistant/ui/component/theme/AssistantTheme.kt`

- [ ] **Step 1: Replace `AssistantTheme.kt`**

```kotlin
package com.just.assistant.ui.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = ColorSet.L_PRIMARY,
    onPrimary = ColorSet.L_ON_PRIMARY,
    primaryContainer = ColorSet.L_PRIMARY_CONTAINER,
    onPrimaryContainer = ColorSet.L_ON_PRIMARY_CONTAINER,
    background = ColorSet.L_BACKGROUND,
    onBackground = ColorSet.L_ON_SURFACE,
    surface = ColorSet.L_SURFACE,
    onSurface = ColorSet.L_ON_SURFACE,
    surfaceVariant = ColorSet.L_SURFACE_VARIANT,
    onSurfaceVariant = ColorSet.L_ON_SURFACE_VARIANT,
    outline = ColorSet.L_OUTLINE,
    error = ColorSet.L_ERROR,
    onError = ColorSet.L_ON_ERROR,
)

private val DarkColors = darkColorScheme(
    primary = ColorSet.D_PRIMARY,
    onPrimary = ColorSet.D_ON_PRIMARY,
    primaryContainer = ColorSet.D_PRIMARY_CONTAINER,
    onPrimaryContainer = ColorSet.D_ON_PRIMARY_CONTAINER,
    background = ColorSet.D_BACKGROUND,
    onBackground = ColorSet.D_ON_SURFACE,
    surface = ColorSet.D_SURFACE,
    onSurface = ColorSet.D_ON_SURFACE,
    surfaceVariant = ColorSet.D_SURFACE_VARIANT,
    onSurfaceVariant = ColorSet.D_ON_SURFACE_VARIANT,
    outline = ColorSet.D_OUTLINE,
    error = ColorSet.D_ERROR,
    onError = ColorSet.D_ON_ERROR,
)

private val AppTypography = Typography(
    headlineSmall = TextSet.HeadlineSmall,
    titleLarge = TextSet.TitleLarge,
    titleMedium = TextSet.TitleMedium,
    titleSmall = TextSet.TitleSmall,
    bodyLarge = TextSet.BodyLarge,
    bodyMedium = TextSet.BodyMedium,
    labelLarge = TextSet.LabelLarge,
    labelSmall = TextSet.LabelSmall,
)

@Composable
fun AssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val assistantColors = if (darkTheme) DarkAssistantColors else LightAssistantColors
    CompositionLocalProvider(LocalAssistantColors provides assistantColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AssistantShapes,
            content = content,
        )
    }
}
```

- [ ] **Step 2: Compile the module** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL` (Tasks 1–6 now consistent).

- [ ] **Step 3: Compile whole app** to confirm no downstream break — `./gradlew compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add ui_component/src/main/java/com/just/assistant/ui/component/theme/
git commit -m "feat(ui): design tokens — light+dark colors, type/spacing/shape/elevation scales"
```

---

## PHASE 2 — Shared Components (`ui_component`)

> All components live under `ui_component/src/main/java/com/just/assistant/ui/component/`. Each ships a light+dark `@Preview`. ui_component must stay Coil-free — image-bearing components (`MemoCard`) take a `thumbnail` slot instead of a URI.

### Task 7: `AssistantScaffold`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/AssistantScaffold.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AssistantScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        bottomBar = bottomBar,
        content = content,
    )
}
```

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git add ui_component/.../AssistantScaffold.kt && git commit -m "feat(ui): AssistantScaffold"`

---

### Task 8: `AssistantTopBar`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/AssistantTopBar.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
```

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git add … && git commit -m "feat(ui): AssistantTopBar"`

---

### Task 9: `PrimaryButton` / `SecondaryButton`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/AssistantButtons.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.just.assistant.ui.component.theme.Radius
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        modifier = modifier.heightIn(min = 52.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        modifier = modifier.heightIn(min = 52.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}
```

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git commit -am "feat(ui): Primary/Secondary buttons"`

---

### Task 10: `AssistantChip`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/AssistantChip.kt`

- [ ] **Step 1: Create the file** (schedule/status chip; `Primary` tone by default, `Success` for calendar)

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.just.assistant.ui.component.theme.LocalAssistantColors

enum class ChipTone { Primary, Success }

@Composable
fun AssistantChip(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    tone: ChipTone = ChipTone.Primary,
) {
    val container: Color
    val content: Color
    when (tone) {
        ChipTone.Primary -> {
            container = MaterialTheme.colorScheme.primaryContainer
            content = MaterialTheme.colorScheme.onPrimaryContainer
        }
        ChipTone.Success -> {
            container = LocalAssistantColors.current.successContainer
            content = LocalAssistantColors.current.onSuccessContainer
        }
    }
    Row(
        modifier = modifier
            .background(container, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        }
        Text(text, color = content, style = MaterialTheme.typography.labelSmall)
    }
}
```
> Note: add `import androidx.compose.foundation.layout.size` — required by `Modifier.size(16.dp)`.

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`. If it errors on `size`, add the missing import above.
- [ ] **Step 3: Commit** — `git commit -am "feat(ui): AssistantChip"`

---

### Task 11: `ChatBubble` (+ behavioral test)

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/ChatBubble.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.just.assistant.ui.component.theme.Spacing

@Composable
fun ChatBubble(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier,
) {
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(bubbleColor, shape)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        )
    }
}
```
The bubble hugs its content (`widthIn(max = 300.dp)`), and the outer `Box` alignment places it left (assistant) or right (user).

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git commit -am "feat(ui): ChatBubble"`

---

### Task 12: `MemoCard`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/MemoCard.kt`

- [ ] **Step 1: Create the file** (thumbnail is a slot so ui_component stays Coil-free)

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.just.assistant.ui.component.theme.Radius
import com.just.assistant.ui.component.theme.Spacing

@Composable
fun MemoCard(
    title: String,
    body: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    scheduleChip: (@Composable () -> Unit)? = null,
    thumbnail: (@Composable (Modifier) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val strike = if (isCompleted) TextDecoration.LineThrough else null
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(Spacing.lg)) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = strike,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (body.isNotBlank()) {
                    Text(
                        body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = strike,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
                if (scheduleChip != null) {
                    Column(Modifier.padding(top = Spacing.sm)) { scheduleChip() }
                }
            }
            if (thumbnail != null) {
                thumbnail(
                    Modifier
                        .padding(start = Spacing.md)
                        .size(56.dp)
                        .clip(RoundedCornerShape(Radius.md)),
                )
            }
        }
    }
}
```
> Note: add `import androidx.compose.ui.unit.dp` (used by `1.dp` and `56.dp`).

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`. Add the `dp` import if it errors.
- [ ] **Step 3: Commit** — `git commit -am "feat(ui): MemoCard"`

---

### Task 13: `SectionCard`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/SectionCard.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.just.assistant.ui.component.theme.Radius
import com.just.assistant.ui.component.theme.Spacing

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(Spacing.lg), content = content)
    }
}
```

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git commit -am "feat(ui): SectionCard"`

---

### Task 14: `EmptyState`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/EmptyState.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.just.assistant.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.just.assistant.ui.component.theme.Spacing

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.md),
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}
```

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git commit -am "feat(ui): EmptyState"`

---

### Task 15: `AssistantBottomBar`

**Files:**
- Create: `ui_component/src/main/java/com/just/assistant/ui/component/AssistantBottomBar.kt`

- [ ] **Step 1: Create the file** (generic — app supplies items so ui_component knows nothing about routes)

```kotlin
package com.just.assistant.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
fun AssistantBottomBar(items: List<BottomNavItem>) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
```
> Note: add `import androidx.compose.ui.unit.dp` (used by `0.dp`).

- [ ] **Step 2: Compile** — `./gradlew :ui_component:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`. Add `dp` import if needed.
- [ ] **Step 3: Commit** — `git commit -am "feat(ui): AssistantBottomBar"`

---

## PHASE 3 — Navigation Shell (`app`)

### Task 16: Nav labels string resources

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

- [ ] **Step 1: Add to `values/strings.xml`** (Korean default) inside `<resources>`:

```xml
<string name="nav_capture">캡처</string>
<string name="nav_chat">채팅</string>
<string name="nav_memo">메모</string>
<string name="nav_settings">설정</string>
```

- [ ] **Step 2: Add to `values-en/strings.xml`**:

```xml
<string name="nav_capture">Capture</string>
<string name="nav_chat">Chat</string>
<string name="nav_memo">Memos</string>
<string name="nav_settings">Settings</string>
```

- [ ] **Step 3: Commit** — `git commit -am "feat(app): bottom nav string resources"`

---

### Task 17: Bottom-nav shell + drop Capture top-bar nav buttons

**Files:**
- Modify: `app/src/main/java/com/just/assistant/AssistantApp.kt`
- Modify: `feature_capture/.../CaptureScene.kt`
- Modify: `feature_capture/.../capture/CaptureScreen.kt` (signature only — full restyle is Task 20)
- Modify: `feature_memo/.../MemoScene.kt` (hoist detail visibility)

- [ ] **Step 1: `MemoScene` — report whether detail is open**

Change signature and add a `LaunchedEffect` reporting the current sub-route. Replace the top of `MemoScene`:

```kotlin
@Composable
fun MemoScene(
    onBackToApp: () -> Unit,
    initialDetailNoteId: Long? = null,
    onDetailOpenChange: (Boolean) -> Unit = {},
) {
    val backStack = rememberNavBackStack(MemoSubRoute.List)
    LaunchedEffect(backStack.lastOrNull()) {
        onDetailOpenChange(backStack.lastOrNull() is MemoSubRoute.Detail)
    }
    LaunchedEffect(initialDetailNoteId) {
        // ...unchanged...
```
Keep the rest of `MemoScene` as-is.

- [ ] **Step 2: `CaptureScene` — drop nav params**

```kotlin
package com.just.feature.capture

import androidx.compose.runtime.Composable
import com.just.feature.capture.capture.CaptureScreen

@Composable
fun CaptureScene() {
    CaptureScreen()
}
```

- [ ] **Step 3: `CaptureScreen` — drop nav params from signature**

In `CaptureScreen.kt`, change the signature (remove the three nav lambdas) and remove the three `Button`s in `TopAppBar.actions` (leave `actions = {}` for now; full restyle in Task 20):

```kotlin
internal fun CaptureScreen(
    viewModel: CaptureViewModel = hiltViewModel(),
) {
```
Remove lines that render `onOpenChat`/`onOpenMemo`/`onOpenSettings` buttons in the top bar `actions` block.

- [ ] **Step 4: Rewrite `AssistantApp.kt`** with the bottom-nav shell

```kotlin
package com.just.assistant

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.assistant.deeplink.DeepLinkRouter
import com.just.assistant.ui.component.AssistantBottomBar
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.BottomNavItem
import com.just.assistant.ui.component.theme.AssistantTheme
import com.just.feature.capture.CaptureScene
import com.just.feature.chat.ChatScene
import com.just.feature.memo.MemoScene
import com.just.feature.onboarding.OnboardingScene
import com.just.feature.settings.SettingsScene
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Composable
fun AssistantApp() {
    AssistantTheme {
        Surface {
            val context = LocalContext.current
            val deepLinkRouter = remember {
                EntryPointAccessors
                    .fromApplication(context.applicationContext, DeepLinkEntryPoint::class.java)
                    .deepLinkRouter()
            }

            val bootVm: AssistantBootViewModel = hiltViewModel()
            val initial by bootVm.initialRoute.collectAsState()
            val backStack = rememberNavBackStack(AssistantRoute.Onboarding)
            var pendingDeepLinkNoteId by remember { mutableStateOf<Long?>(null) }
            var memoDetailOpen by remember { mutableStateOf(false) }

            LaunchedEffect(initial) {
                if (initial != null && backStack.lastOrNull() != initial) {
                    backStack.clear()
                    backStack.add(initial!!)
                }
            }

            LaunchedEffect(Unit) {
                deepLinkRouter.events.collect { noteId ->
                    pendingDeepLinkNoteId = noteId
                    if (backStack.lastOrNull() != AssistantRoute.Memo) {
                        backStack.clear()
                        backStack.add(AssistantRoute.Memo)
                    }
                }
            }

            val current = backStack.lastOrNull()
            fun selectTab(route: AssistantRoute) {
                if (backStack.lastOrNull() != route) {
                    backStack.clear()
                    backStack.add(route)
                }
            }

            val topLevelTabs = setOf(
                AssistantRoute.Capture, AssistantRoute.Chat, AssistantRoute.Memo, AssistantRoute.Settings,
            )
            val showBottomBar = current in topLevelTabs && !(current == AssistantRoute.Memo && memoDetailOpen)

            // Back from a non-Capture tab returns to Capture; don't intercept while memo detail is open.
            BackHandler(enabled = current in topLevelTabs && current != AssistantRoute.Capture && !memoDetailOpen) {
                selectTab(AssistantRoute.Capture)
            }

            AssistantScaffold(
                bottomBar = {
                    if (showBottomBar) {
                        AssistantBottomBar(
                            items = listOf(
                                BottomNavItem(stringResource(R.string.nav_capture), Icons.Filled.Edit, current == AssistantRoute.Capture) { selectTab(AssistantRoute.Capture) },
                                BottomNavItem(stringResource(R.string.nav_chat), Icons.AutoMirrored.Filled.Chat, current == AssistantRoute.Chat) { selectTab(AssistantRoute.Chat) },
                                BottomNavItem(stringResource(R.string.nav_memo), Icons.Filled.Notes, current == AssistantRoute.Memo) { selectTab(AssistantRoute.Memo) },
                                BottomNavItem(stringResource(R.string.nav_settings), Icons.Filled.Settings, current == AssistantRoute.Settings) { selectTab(AssistantRoute.Settings) },
                            ),
                        )
                    }
                },
            ) { padding ->
                NavDisplay(
                    modifier = androidx.compose.ui.Modifier.padding(padding),
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<AssistantRoute.Onboarding> {
                            OnboardingScene(
                                onDone = { selectTab(AssistantRoute.Capture) },
                            )
                        }
                        entry<AssistantRoute.Capture> { CaptureScene() }
                        entry<AssistantRoute.Memo> {
                            val deepLinkId = pendingDeepLinkNoteId
                            MemoScene(
                                onBackToApp = { selectTab(AssistantRoute.Capture) },
                                initialDetailNoteId = deepLinkId,
                                onDetailOpenChange = { memoDetailOpen = it },
                            )
                            LaunchedEffect(deepLinkId) {
                                if (deepLinkId != null) pendingDeepLinkNoteId = null
                            }
                        }
                        entry<AssistantRoute.Chat> {
                            ChatScene(onBack = { selectTab(AssistantRoute.Capture) })
                        }
                        entry<AssistantRoute.Settings> {
                            SettingsScene(
                                onBack = { selectTab(AssistantRoute.Capture) },
                                appVersion = BuildConfig.VERSION_NAME,
                            )
                        }
                    },
                )
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DeepLinkEntryPoint {
    fun deepLinkRouter(): DeepLinkRouter
}
```
> `import androidx.compose.foundation.layout.padding` is required for `Modifier.padding(padding)`. Add it.
> The `activity-compose` dependency (already used by the app) provides `androidx.activity.compose.BackHandler`.

- [ ] **Step 5: Compile whole app** — `./gradlew compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`. Fix any missing imports (`padding`).

- [ ] **Step 6: Verify existing tests still compile/pass** — `./gradlew testDebugUnitTest`. Expected: `BUILD SUCCESSFUL`. (Instrumented `CaptureScreenTest` references the removed top-bar buttons — see note below.)

> **CaptureScreenTest impact:** `feature_capture/src/androidTest/.../CaptureScreenTest.kt` may assert on the old top-bar nav buttons / call `CaptureScreen(onOpenMemo=…)`. Update that test to the new no-arg `CaptureScreen()` signature and drop assertions about chat/list/settings buttons. Keep all other testTag assertions. Run `./gradlew :feature_capture:compileDebugAndroidTestKotlin` to confirm it compiles.

- [ ] **Step 7: Commit** — `git commit -am "feat(app): bottom navigation shell; remove capture top-bar nav buttons"`

---

## PHASE 4 — Screen Refactors

> Each screen swaps stock components for the shared ones and replaces hardcoded values with tokens. **Preserve all existing `testTag`s and all ViewModel calls / string resources.** After each task: `./gradlew :<module>:compileDebugKotlin` then commit.

### Task 18: Chat screen — bubbles

**Files:**
- Modify: `feature_chat/src/main/java/com/just/feature/chat/chat/ChatScreen.kt`

- [ ] **Step 1:** Replace the message rendering and shell. Swap `Scaffold`→`AssistantScaffold`, `TopAppBar`→`AssistantTopBar` (keep the two `TextButton` actions for TTS/New), and replace the `items` block:

```kotlin
items(state.messages) { msg ->
    ChatBubble(
        text = msg.text,
        isUser = msg.role == ChatMessage.Role.USER,
    )
}
```
Wrap error/model-not-ready `Text` in a tinted inline surface (use `MaterialTheme.colorScheme.errorContainer` background + `Spacing`), and replace `12.dp`/`8.dp` literals with `Spacing.md`/`Spacing.sm`. Imports to add: `com.just.assistant.ui.component.AssistantScaffold`, `AssistantTopBar`, `ChatBubble`, `com.just.assistant.ui.component.theme.Spacing`. Remove now-unused `Scaffold`, `TopAppBar`, `TextAlign` imports.

- [ ] **Step 2: Compile** — `./gradlew :feature_chat:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Run chat tests** — `./gradlew :feature_chat:testDebugUnitTest`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 4: Commit** — `git commit -am "feat(chat): message bubbles + tokenized styling"`

---

### Task 19: Memo list — cards + empty state

**Files:**
- Modify: `feature_memo/src/main/java/com/just/feature/memo/memoList/MemoListScreen.kt`

- [ ] **Step 1:** Replace shell and list body. `Scaffold`→`AssistantScaffold`, `TopAppBar`→`AssistantTopBar`. Keep `testTag("memo_list")`. Replace each item's `Column…HorizontalDivider()` with `MemoCard`, and the empty branch with `EmptyState`:

```kotlin
LazyColumn(
    Modifier.fillMaxSize(),
    contentPadding = PaddingValues(Spacing.lg),
    verticalArrangement = Arrangement.spacedBy(Spacing.md),
) {
    items(s.notes, key = { it.id }) { note ->
        MemoCard(
            title = note.title,
            body = note.body,
            isCompleted = note.isCompleted,
            thumbnail = note.imageUri?.let { uri ->
                { m -> AsyncImage(model = uri, contentDescription = null, modifier = m, contentScale = ContentScale.Crop) }
            },
            onClick = { onOpenDetail(note.id) },
        )
    }
}
```
Empty branch:
```kotlin
EmptyState(
    icon = Icons.Filled.Notes,
    title = stringResource(R.string.memo_list_empty),
    modifier = Modifier.align(Alignment.Center),
)
```
Imports to add: `com.just.assistant.ui.component.{AssistantScaffold,AssistantTopBar,MemoCard,EmptyState}`, `theme.Spacing`, `androidx.compose.foundation.layout.{Arrangement,PaddingValues}`, `androidx.compose.ui.layout.ContentScale`, `androidx.compose.material.icons.Icons`, `androidx.compose.material.icons.filled.Notes`. Remove `HorizontalDivider`, old `Column`/`padding` usages, `TextDecoration` (moved into MemoCard).

- [ ] **Step 2: Compile** — `./gradlew :feature_memo:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Run memo tests** — `./gradlew :feature_memo:testDebugUnitTest`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 4: Commit** — `git commit -am "feat(memo): card list + empty state"`

---

### Task 20: Memo detail — grouped section + chips

**Files:**
- Modify: `feature_memo/src/main/java/com/just/feature/memo/memoDetail/MemoDetailScreen.kt`

- [ ] **Step 1:** `Scaffold`→`AssistantScaffold`, `TopAppBar`→`AssistantTopBar(title, onBack = onBack)` (detail gets a back arrow). Replace the two `AssistChip`s with `AssistantChip` (calendar → `ChipTone.Success` + `Icons.Filled.Event`; alarm → `ChipTone.Primary` + `Icons.Filled.Notifications`). Wrap the schedule chips+buttons in a `SectionCard`. Replace `OutlinedButton`/`Button` with `SecondaryButton`/`PrimaryButton`. Clip the image with `RoundedCornerShape(Radius.lg)`. Replace `Spacer(height=…)` literals with `Spacing`.

Chips example:
```kotlin
if (hasCalendar) AssistantChip(stringResource(R.string.memo_detail_chip_calendar), leadingIcon = Icons.Filled.Event, tone = ChipTone.Success)
if (hasAlarm) AssistantChip(stringResource(R.string.memo_detail_chip_alarm), leadingIcon = Icons.Filled.Notifications, tone = ChipTone.Primary)
```
Image:
```kotlin
AsyncImage(
    model = uri,
    contentDescription = stringResource(R.string.memo_detail_image_desc),
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(Radius.lg)),
)
```
Imports to add: `ui.component.{AssistantScaffold,AssistantTopBar,SectionCard,AssistantChip,ChipTone,PrimaryButton,SecondaryButton}`, `theme.{Spacing,Radius}`, `androidx.compose.ui.draw.clip`, `androidx.compose.foundation.shape.RoundedCornerShape`, `androidx.compose.ui.layout.ContentScale`, `androidx.compose.material.icons.Icons`, `androidx.compose.material.icons.filled.{Event,Notifications}`. Remove `AssistChip`, `AssistChipDefaults`, `Button`, `OutlinedButton`, `Scaffold`, `TopAppBar` imports.

- [ ] **Step 2: Compile** — `./gradlew :feature_memo:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git commit -am "feat(memo): detail section card + styled chips/buttons"`

---

### Task 21: Capture screen — restyle (keep testTags)

**Files:**
- Modify: `feature_capture/src/main/java/com/just/feature/capture/capture/CaptureScreen.kt`

- [ ] **Step 1:** With nav buttons already removed (Task 17), restyle the body:
  - `Scaffold`→`AssistantScaffold`, `TopAppBar`→`AssistantTopBar(title)` with empty actions.
  - Convert the three capture `Button`s (gallery/camera/voice) to `IconButton`s with `Icons.Filled.{PhotoLibrary, PhotoCamera, Mic}` in a `Row(spacedBy(Spacing.sm))`. **Keep the exact onClick logic.**
  - Replace the "Prepare" `Button` with `PrimaryButton(stringResource(R.string.capture_prepare), …, modifier = Modifier.fillMaxWidth().testTag("capture_prepare"))`. **Keep `testTag("capture_prepare")`, `testTag("capture_input")`, `testTag("capture_thumbnail")`.**
  - Clip the thumbnail: add `.clip(RoundedCornerShape(Radius.lg))`.
  - Replace `Spacer`/`padding` `.dp` literals with `Spacing`.
  - Error text: wrap in an `errorContainer`-tinted box.
  - Leave `PreviewSheet`, `AlertDialog`, and all permission logic untouched.

- [ ] **Step 2: Compile** — `./gradlew :feature_capture:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Instrumented test compiles** — `./gradlew :feature_capture:compileDebugAndroidTestKotlin`. Expected: `BUILD SUCCESSFUL` (testTags preserved).
- [ ] **Step 4: Commit** — `git commit -am "feat(capture): restyled inputs (icon actions, primary button, tokens)"`

---

### Task 22: Settings — grouped section cards

**Files:**
- Modify: `feature_settings/src/main/java/com/just/feature/settings/settings/SettingsScreen.kt`

- [ ] **Step 1:** `Scaffold`→`AssistantScaffold`, `TopAppBar`→`AssistantTopBar(title)`. Wrap the briefing switch row and the About row in a single `SectionCard` with a `HorizontalDivider` between them. Add a leading icon to each row (`Icons.Filled.Notifications`, `Icons.Filled.Info`) and a trailing chevron (`Icons.AutoMirrored.Filled.KeyboardArrowRight`) on About. Use `Spacing` for padding. Keep `viewModel::onToggleBriefing`, `onOpenAbout`, and all string resources.

```kotlin
AssistantScaffold(topBar = { AssistantTopBar(stringResource(R.string.settings_title)) }) { padding ->
    Column(Modifier.padding(padding).padding(Spacing.lg)) {
        SectionCard {
            Row(Modifier.fillMaxWidth().padding(vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.weight(1f).padding(start = Spacing.md)) {
                    Text(stringResource(R.string.settings_briefing_label), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.settings_briefing_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = enabled, onCheckedChange = viewModel::onToggleBriefing)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(Modifier.fillMaxWidth().clickable(onClick = onOpenAbout).padding(vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.about_open_label), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f).padding(start = Spacing.md))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
```
Imports: add `ui.component.{AssistantScaffold,AssistantTopBar,SectionCard}`, `theme.Spacing`, `material3.{HorizontalDivider,Icon}`, `material.icons.Icons`, `material.icons.filled.{Notifications,Info}`, `material.icons.automirrored.filled.KeyboardArrowRight`. Remove `Scaffold`, `TopAppBar` imports.

- [ ] **Step 2: Compile** — `./gradlew :feature_settings:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Commit** — `git commit -am "feat(settings): grouped section card with icons + chevron"`

---

### Task 23: Onboarding — brand mark + styled progress

**Files:**
- Modify: `feature_onboarding/src/main/java/com/just/feature/onboarding/screen/OnboardingScreen.kt`

- [ ] **Step 1:** Keep the centered layout. Add a brand mark above the title: a `Box` (96.dp, `primaryContainer` background, `CircleShape`) containing `Icon(Icons.Filled.AutoAwesome, tint = primary)`. Replace state `Button`s with `PrimaryButton`. In `ProgressBlock`, round the bar with `strokeCap`/clip and use `Spacing`. Error branch: prefix with `Icon(Icons.Filled.ErrorOutline, tint = error)` and color the text `MaterialTheme.colorScheme.error`. Replace `Spacer` `.dp` ladder with `Spacing` values. Use `AssistantScaffold` for the background color.

```kotlin
Box(
    Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
    contentAlignment = Alignment.Center,
) { Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp)) }
Spacer(Modifier.height(Spacing.xl))
Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineSmall)
```
Progress bar:
```kotlin
LinearProgressIndicator(
    progress = { ratio },
    strokeCap = StrokeCap.Round,
    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(Radius.sm)),
)
```
Imports: add `ui.component.{AssistantScaffold,PrimaryButton}`, `theme.{Spacing,Radius}`, `material.icons.Icons`, `material.icons.filled.{AutoAwesome,ErrorOutline}`, `foundation.background`, `foundation.shape.{CircleShape,RoundedCornerShape}`, `ui.draw.clip`, `ui.graphics.StrokeCap`, `foundation.layout.size`, `material3.Icon`.

- [ ] **Step 2: Compile** — `./gradlew :feature_onboarding:compileDebugKotlin`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Run onboarding tests** — `./gradlew :feature_onboarding:testDebugUnitTest`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 4: Commit** — `git commit -am "feat(onboarding): brand mark, primary button, styled progress"`

---

## PHASE 5 — Full Verification

### Task 24: Whole-app build, tests, and manual light/dark check

**Files:** none (verification only)

- [ ] **Step 1: Full debug build** — `./gradlew assembleDebug`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 2: All unit tests** — `./gradlew testDebugUnitTest`. Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 3: Instrumented test compile** — `./gradlew compileDebugAndroidTestKotlin`. Expected: `BUILD SUCCESSFUL`. (Run `connectedDebugAndroidTest` if a device/emulator is available.)
- [ ] **Step 4: Manual check** (emulator/device, per `superpowers:verify` or `/run`): launch the app and confirm on each screen, in **both** system light and dark mode:
  - Onboarding: brand mark, primary button, progress bar render.
  - Bottom nav appears on Capture/Chat/Memo(list)/Settings; hidden on Onboarding and Memo detail; selected tab tinted.
  - Chat: user bubbles right/primary, assistant bubbles left/surfaceVariant.
  - Memo list: cards with rounded thumbnails; empty state when no notes.
  - Memo detail: section card, chips, back arrow works.
  - Capture: icon action row, primary "Prepare" button; preview sheet + permission dialogs still work.
  - Settings: grouped card, switch toggles, About opens.
  - No stray white-on-white or black-on-black; no leftover default-blue Material surfaces.
- [ ] **Step 5: Final commit (if any manual fixes)** — `git commit -am "fix(ui): light/dark polish from manual verification"`

---

## Self-Review Notes (author)

- **Spec coverage:** color/type/spacing/shape/elevation tokens (T1–6); all 9 components (T7–15); bottom nav restructure incl. hide-on-detail + remove capture buttons (T17); all 6 screens (T18–23); light+dark (T6 + T24 manual). Success role via CompositionLocal (T5, used in T10/T20). ✓
- **testTag preservation** is called out in T17/T19/T21 and the Testing Strategy. ✓
- **Known deviations from strict TDD:** visual composables are verified by compile + `@Preview` + existing regression tests rather than new failing unit tests — appropriate for presentation-only work (see Testing Strategy). ChatBubble/BottomBar behavior is implicitly covered by the manual pass in T24; add Compose UI tests later if desired.
- **Coil isolation:** `MemoCard` uses a `thumbnail` slot so `ui_component` needs no Coil dependency. ✓
- **Deferred-commit caveat:** Tasks 1–6 are committed together (T6 Step 4) because T1 intentionally breaks `AssistantTheme.kt` until T6.
