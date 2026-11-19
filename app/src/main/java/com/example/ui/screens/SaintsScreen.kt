package com.example.ui.screens

import com.example.data.repository.DevotionImageCredits
import com.example.data.repository.DevotionImageCredit
import androidx.activity.compose.BackHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import com.example.data.audio.LiturgicalTtsManager
import com.example.ui.design.AppSecondaryCapsule
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.example.ui.design.AppBackHandler
import com.example.ui.design.AppDetailHeader
import com.example.ui.design.AppEdgeToEdge
import com.example.ui.design.appScreenInsets
import com.example.ui.design.appDockInset
import com.example.ui.design.AppHeroHeader
import com.example.ui.design.AppHeroImage
import com.example.ui.design.CompactHeader
import com.example.ui.design.LandscapeTwoPane
import com.example.ui.design.PaneSurface
import com.example.ui.design.rememberLandscapeForm
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.liturgy.GeneralCelebration
import com.example.data.liturgy.GeneralRomanSanctoral
import com.example.data.models.AppLanguage
import com.example.data.models.SaintProfile
import com.example.data.repository.SaintsCatalog
import com.example.ui.design.AppCard
import com.example.ui.design.AppChip
import com.example.ui.design.AppDivider
import com.example.ui.design.AppEmptyState
import com.example.ui.design.AppListGroup
import com.example.ui.design.AppListRow
import com.example.ui.design.AppMenuChip
import com.example.ui.design.AppMenuItem
import com.example.ui.design.AppScreenTitle
import com.example.ui.design.AppSearchField
import com.example.ui.design.AppSecondaryButton
import com.example.ui.design.AppSectionHeader
import com.example.ui.theme.LocalAppDesign
import com.example.ui.theme.LocalAppTextStyles
import java.text.Normalizer

private val monthsEs = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
private val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
private val monthsPt = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")

internal fun saintMonthName(month: Int, language: AppLanguage): String {
    val name = when (language) { AppLanguage.ES -> monthsEs; AppLanguage.EN -> monthsEn; AppLanguage.PT -> monthsPt }[month - 1]
    return name.replaceFirstChar { it.uppercase() }
}

internal fun saintDateLabel(month: Int, day: Int, language: AppLanguage): String = when (language) {
    AppLanguage.ES -> "$day de ${monthsEs[month - 1]}"
    AppLanguage.EN -> "${monthsEn[month - 1]} $day"
    AppLanguage.PT -> "$day de ${monthsPt[month - 1]}"
}

private fun fold(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()

/** Non-Lord celebrations of the General Roman Calendar, in calendar order. */
internal fun saintCelebrations(): List<GeneralCelebration> =
    GeneralRomanSanctoral.fixed.filter { !it.isLordFeast }.sortedWith(compareBy({ it.month }, { it.day }, { it.rank.ordinal }))

/** The calendar filtered by month, profile availability and the folded query. */
private fun visibleSaints(
    all: List<GeneralCelebration>,
    profiles: Map<String, SaintProfile>,
    folded: String,
    month: Int,
    profilesOnly: Boolean,
    language: AppLanguage
): List<GeneralCelebration> = all.filter { c ->
    (month == 0 || c.month == month) &&
        (!profilesOnly || c.key in profiles) &&
        (folded.isEmpty() || fold(c.title.get(language)).contains(folded) ||
            profiles[c.key]?.let { fold(it.bio.get(language)).contains(folded) } == true)
}

/**
 * Saints of the General Roman Calendar. Every celebration is listed; those
 * with an editorial profile open a detail page, the others show title and date.
 */
@Composable
fun SaintsScreen(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    /** The app's narrator: the profile can be read aloud (notification, focus, sleep timer). */
    ttsManager: LiturgicalTtsManager? = null,
    /** Opens this celebration's profile on arrival (the global search); an unknown key shows the list. */
    initialKey: String? = null,
    /** Back from the profile opened by [initialKey] leaves to the caller (the global search) instead of the list. */
    onExitContext: (() -> Unit)? = null
) {
    val catalog = remember { SaintsCatalog.default }
    val profiles = catalog.byKey
    var selectedKey by rememberSaveable { mutableStateOf<String?>(initialKey?.takeIf { it in profiles }) }
    val selected = selectedKey?.let { k -> saintCelebrations().firstOrNull { it.key == k } }

    // Filters live here so they survive the rotation between the portrait list
    // and the landscape master-detail; both branches render the same controls.
    var query by rememberSaveable { mutableStateOf("") }
    var month by rememberSaveable { mutableIntStateOf(0) }
    var profilesOnly by rememberSaveable { mutableStateOf(false) }

    val form = rememberLandscapeForm()
    if (form.isLandscape && form.heightDp < 500.dp) {
        SaintsLandscapeScreen(
            profiles = profiles,
            language = currentLanguage,
            selectedKey = selectedKey,
            initialKey = initialKey,
            onSelect = { selectedKey = it },
            onDeselect = { selectedKey = null },
            onExitContext = onExitContext,
            query = query,
            onQueryChange = { query = it },
            month = month,
            onMonthChange = { month = it },
            profilesOnly = profilesOnly,
            onProfilesOnlyChange = { profilesOnly = it },
            ttsManager = ttsManager,
            modifier = modifier
        )
        return
    }

    if (selected != null) {
        val back = { if (onExitContext != null && selectedKey == initialKey) onExitContext() else selectedKey = null }
        AppBackHandler { back() }
        SaintDetail(selected, profiles[selected.key], currentLanguage, { back() }, modifier, ttsManager)
    } else {
        SaintsList(
            profiles, currentLanguage, { selectedKey = it }, modifier,
            query, { query = it }, month, { month = it }, profilesOnly, { profilesOnly = it }
        )
    }
}

// ------------------------------------------------------- shared blocks
// Portrait and landscape render these same blocks, so the two form factors
// cannot drift apart (the PrayerDetailView precedent).

/** Search field + month picker + "with profile only" chip. Tags unchanged. */
@Composable
private fun SaintsFilterControls(
    query: String,
    onQueryChange: (String) -> Unit,
    month: Int,
    onMonthChange: (Int) -> Unit,
    profilesOnly: Boolean,
    onProfilesOnlyChange: (Boolean) -> Unit,
    hasProfiles: Boolean,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    val gutter = design.spacing.gutter
    fun tr(es: String, en: String, pt: String) = when (language) { AppLanguage.ES -> es; AppLanguage.EN -> en; AppLanguage.PT -> pt }
    val monthLabel = if (month == 0) tr("Todos los meses", "All months", "Todos os meses") else saintMonthName(month, language)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = tr("Buscar un santo", "Search for a saint", "Buscar um santo"),
            modifier = Modifier.fillMaxWidth().testTag("saints_search")
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = gutter),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppMenuChip(
                label = monthLabel,
                tag = "saints_month_picker",
                items = listOf(AppMenuItem(tr("Todos los meses", "All months", "Todos os meses"), selected = month == 0, tag = "saints_month_0") { onMonthChange(0) }) +
                    (1..12).map { m -> AppMenuItem(saintMonthName(m, language), selected = month == m, tag = "saints_month_$m") { onMonthChange(m) } }
            )
            if (hasProfiles) {
                AppChip(
                    label = tr("Solo con ficha", "With profile only", "Só com ficha"),
                    selected = profilesOnly,
                    onClick = { onProfilesOnlyChange(!profilesOnly) },
                    modifier = Modifier.testTag("saints_profiles_only")
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

/** One month of the calendar: section header + grouped rows. Tags unchanged. */
@Composable
private fun SaintsMonthGroup(
    month: Int,
    entries: List<GeneralCelebration>,
    profiles: Map<String, SaintProfile>,
    language: AppLanguage,
    selectedKey: String?,
    onOpen: (String) -> Unit
) {
    val design = LocalAppDesign.current
    AppSectionHeader(title = saintMonthName(month, language), modifier = Modifier.testTag("saints_month_header_$month"))
    AppListGroup {
        entries.forEach { c ->
            val has = c.key in profiles
            AppListRow(
                title = c.title.get(language),
                subtitle = saintDateLabel(c.month, c.day, language) + " · " + c.rank.displayName.get(language),
                showsNavigationAffordance = has,
                selected = c.key == selectedKey,
                onClick = if (has) ({ onOpen(c.key) }) else null,
                modifier = Modifier.testTag("saint_row_${c.key}")
            )
        }
    }
    Spacer(Modifier.height(design.spacing.cardGap))
}

/** «Imagen: autor · licencia · fuente», tappable when the source is https. */
@Composable
private fun SaintImageCredit(credit: DevotionImageCredit, language: AppLanguage, modifier: Modifier = Modifier) {
    val styles = LocalAppTextStyles.current
    val palette = LocalAppDesign.current.palette
    val uriHandler = LocalUriHandler.current
    val url = credit.safeUrl
    Text(
        imageCreditLine(credit, language), style = styles.footnote, color = palette.textSecondary,
        modifier = modifier
            .then(if (url != null) Modifier.clickable(role = Role.Button) { runCatching { uriHandler.openUri(url) } } else Modifier)
            .testTag("saint_image_credit")
    )
}

/** Listen/stop capsule for the narrator. The TTS state lives in the caller. */
@Composable
private fun SaintListenCapsule(listening: Boolean, language: AppLanguage, onToggle: () -> Unit) {
    fun tr(es: String, en: String, pt: String) = when (language) { AppLanguage.ES -> es; AppLanguage.EN -> en; AppLanguage.PT -> pt }
    AppSecondaryCapsule(
        label = if (listening) tr("Detener", "Stop", "Parar") else tr("Escuchar", "Listen", "Ouvir"),
        icon = if (listening) Icons.Outlined.Stop else Icons.Outlined.VolumeUp,
        onClick = onToggle,
        tag = "saint_listen_button"
    )
}

/** Biography, patronage, quote and sources: the reading cards of a profile. */
@Composable
private fun SaintProfileCards(profile: SaintProfile?, language: AppLanguage, card: Modifier, gutter: Dp) {
    val design = LocalAppDesign.current
    val styles = LocalAppTextStyles.current
    val palette = design.palette
    fun tr(es: String, en: String, pt: String) = when (language) { AppLanguage.ES -> es; AppLanguage.EN -> en; AppLanguage.PT -> pt }
    val bio = profile?.bio?.get(language).orEmpty()
    val patronages = profile?.patronages?.get(language).orEmpty()
    val quote = profile?.quote
    val quoteText = quote?.text?.get(language).orEmpty()
    val sources = profile?.sources.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(design.spacing.cardGap)) {
        if (bio.isNotBlank()) {
            AppCard(card) { Text(bio, style = styles.body, color = palette.textPrimary) }
        }
        if (patronages.isNotEmpty()) {
            AppCard(card) {
                Text(tr("Patronazgos", "Patronage", "Padroados"), style = styles.headline, color = palette.textPrimary, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(6.dp))
                Text(patronages.joinToString(" · "), style = styles.callout, color = palette.textSecondary, modifier = Modifier.testTag("saint_patronages"))
            }
        }
        if (quote != null && quoteText.isNotBlank()) {
            AppCard(card.testTag("saint_quote")) {
                Text("\u201C", style = styles.display, color = palette.accent, modifier = Modifier.height(34.dp).clearAndSetSemantics { })
                Text(quoteText, style = styles.quote, color = palette.textPrimary)
                Spacer(Modifier.height(10.dp))
                Text(quote.source, style = styles.footnote, color = palette.textSecondary)
            }
        }
        if (sources.isNotEmpty()) {
            Column(Modifier.padding(horizontal = gutter + 4.dp).padding(top = 6.dp).widthIn(max = design.spacing.readingMaxWidth)) {
                Text(tr("Fuentes", "Sources", "Fontes"), style = styles.eyebrow, color = palette.textSecondary, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(6.dp))
                Column(Modifier.testTag("saint_sources"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sources.forEach { Text(it, style = styles.footnote, color = palette.textSecondary) }
                }
            }
        }
    }
}

/** True when [profile] has at least one reading card to show. */
private fun hasProfileCards(profile: SaintProfile?, language: AppLanguage): Boolean {
    val bio = profile?.bio?.get(language).orEmpty()
    val patronages = profile?.patronages?.get(language).orEmpty()
    val quote = profile?.quote
    val quoteText = quote?.text?.get(language).orEmpty()
    return bio.isNotBlank() || patronages.isNotEmpty() ||
        (quote != null && quoteText.isNotBlank()) || profile?.sources.orEmpty().isNotEmpty()
}

// ------------------------------------------------------- landscape phone

/**
 * Landscape phone (844x390, 915x412, 740x360): the calendar becomes a fixed
 * left index — compact title, search and filters always visible, the month
 * list scrolling inside its own pane — and the saint's profile fills the
 * right pane with the only reading scroll. The portrait 300dp art header is
 * replaced by a 88-104dp thumbnail beside the title, so the biography starts
 * above the fold. Portrait and tablet keep the original list/detail screens.
 */
@Composable
private fun SaintsLandscapeScreen(
    profiles: Map<String, SaintProfile>,
    language: AppLanguage,
    selectedKey: String?,
    initialKey: String?,
    onSelect: (String) -> Unit,
    onDeselect: () -> Unit,
    onExitContext: (() -> Unit)?,
    query: String,
    onQueryChange: (String) -> Unit,
    month: Int,
    onMonthChange: (Int) -> Unit,
    profilesOnly: Boolean,
    onProfilesOnlyChange: (Boolean) -> Unit,
    ttsManager: LiturgicalTtsManager?,
    modifier: Modifier
) {
    AppEdgeToEdge()
    val form = rememberLandscapeForm()
    val design = LocalAppDesign.current
    val styles = LocalAppTextStyles.current
    val palette = design.palette
    val insets = appScreenInsets()
    fun tr(es: String, en: String, pt: String) = when (language) { AppLanguage.ES -> es; AppLanguage.EN -> en; AppLanguage.PT -> pt }

    val all = remember { saintCelebrations() }
    val folded = fold(query.trim())
    val visible = remember(folded, month, profilesOnly, language, profiles) {
        visibleSaints(all, profiles, folded, month, profilesOnly, language)
    }
    val byMonth = remember(visible) { visible.groupBy { it.month } }

    val selected = selectedKey?.let { k -> all.firstOrNull { it.key == k } }
    val profile = selected?.let { profiles[it.key] }
    val back: () -> Unit = { if (onExitContext != null && selectedKey == initialKey) onExitContext() else onDeselect() }
    // System back deselects; the visible "All saints" capsule does the same.
    // No glass back override here: its 52dp bar row would eat the short height.
    BackHandler(enabled = selected != null) { back() }

    // Read aloud: same contract as the portrait detail (name, then biography).
    val saintTitle = selected?.title?.get(language).orEmpty()
    val bio = profile?.bio?.get(language).orEmpty()
    val speakText = if (bio.isBlank()) "" else "$saintTitle.\n\n$bio"
    val narrating by (ttsManager?.currentlySpeaking ?: remember { kotlinx.coroutines.flow.MutableStateFlow<String?>(null) }).collectAsState()
    val listening = speakText.isNotEmpty() && narrating == speakText
    DisposableEffect(selected?.key, ttsManager) { onDispose { if (ttsManager != null && narrating == speakText) ttsManager.stop() } }

    val context = LocalContext.current
    val images = remember(context) { DevotionImageCredits.install(context); DevotionImageCredits.saints }
    val imagePath = remember(images, selected?.key) { selected?.let { images.imagePath(it.key) } }
    val credit = remember(images, imagePath, selected?.key) {
        if (imagePath != null && selected != null) images.credit(selected.key) else null
    }

    LandscapeTwoPane(
        form = form,
        modifier = modifier.fillMaxSize().testTag("saints_screen"),
        leftWidthFraction = 0.40f,
        left = {
            Column(
                Modifier.fillMaxSize()
                    .padding(top = insets.calculateTopPadding(), bottom = insets.calculateBottomPadding())
            ) {
                CompactHeader(
                    title = tr("Santos", "Saints", "Santos"),
                    form = form,
                    subtitle = tr("Calendario Romano General", "General Roman Calendar", "Calendário Romano Geral")
                )
                SaintsFilterControls(
                    query = query,
                    onQueryChange = onQueryChange,
                    month = month,
                    onMonthChange = onMonthChange,
                    profilesOnly = profilesOnly,
                    onProfilesOnlyChange = onProfilesOnlyChange,
                    hasProfiles = profiles.isNotEmpty(),
                    language = language
                )
                // The index: fixed controls above, the month list scrolls here.
                LazyColumn(
                    Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (visible.isEmpty()) {
                        item(key = "empty") {
                            AppEmptyState(
                                title = tr("Sin resultados", "No results", "Sem resultados"),
                                body = tr("Prueba con otro nombre o mes.", "Try another name or month.", "Tente outro nome ou mês."),
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        byMonth.forEach { (m, entries) ->
                            item(key = "month_$m") {
                                SaintsMonthGroup(m, entries, profiles, language, selectedKey, onSelect)
                            }
                        }
                    }
                }
            }
        },
        right = {
            if (selected == null) {
                Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.Center) {
                    AppEmptyState(
                        title = tr("Elige un santo", "Choose a saint", "Escolha um santo"),
                        body = tr(
                            "Toca un nombre del calendario para leer su ficha.",
                            "Tap a name in the calendar to read its profile.",
                            "Toque um nome do calendário para ler a sua ficha."
                        ),
                        icon = Icons.Outlined.AutoStories
                    )
                }
            } else {
                PaneSurface(
                    modifier = Modifier.padding(top = insets.calculateTopPadding(), bottom = insets.calculateBottomPadding()),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(Modifier.fillMaxSize().testTag("saint_detail")) {
                        // Fixed controls: back and listen never scroll away.
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            AppSecondaryButton(
                                tr("Todos los santos", "All saints", "Todos os santos"),
                                back,
                                leadingIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                                modifier = Modifier.testTag("saint_detail_back")
                            )
                            Spacer(Modifier.weight(1f))
                            if (ttsManager != null && speakText.isNotEmpty()) {
                                SaintListenCapsule(listening, language) {
                                    if (listening) ttsManager.stop()
                                    else { ttsManager.setLanguage(language); ttsManager.speakSingle(speakText, title = saintTitle) }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        // The only reading scroll of the screen.
                        LazyColumn(
                            Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(design.spacing.cardGap),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "saint_header") {
                                Row(Modifier.fillMaxWidth()) {
                                    Box(
                                        Modifier
                                            .width(128.dp)
                                            .height(if (form.isShortLandscape) 88.dp else 104.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                    ) {
                                        AppHeroImage(
                                            imagePath ?: "heroes/lumen/saints.jpg",
                                            Modifier.fillMaxSize(),
                                            fadeToCanvas = false,
                                            topVeil = false
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            saintTitle,
                                            style = styles.title2,
                                            color = palette.textPrimary,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            saintDateLabel(selected.month, selected.day, language) + " · " + selected.rank.displayName.get(language),
                                            style = styles.footnote,
                                            color = palette.textSecondary
                                        )
                                        if (credit != null) {
                                            Spacer(Modifier.height(4.dp))
                                            SaintImageCredit(credit, language, Modifier.fillMaxWidth())
                                        }
                                    }
                                }
                            }
                            if (hasProfileCards(profile, language)) {
                                item(key = "saint_cards") {
                                    SaintProfileCards(
                                        profile, language,
                                        card = Modifier.fillMaxWidth().widthIn(max = design.spacing.readingMaxWidth),
                                        gutter = 0.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// ------------------------------------------------------- portrait / tablet

@Composable
private fun SaintsList(
    profiles: Map<String, SaintProfile>,
    language: AppLanguage,
    onOpen: (String) -> Unit,
    modifier: Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    month: Int,
    onMonthChange: (Int) -> Unit,
    profilesOnly: Boolean,
    onProfilesOnlyChange: (Boolean) -> Unit
) {
    AppEdgeToEdge()
    val design = LocalAppDesign.current
    val gutter = design.spacing.gutter
    val all = remember { saintCelebrations() }

    val folded = fold(query.trim())
    val visible = remember(folded, month, profilesOnly, language, profiles) {
        visibleSaints(all, profiles, folded, month, profilesOnly, language)
    }
    fun tr(es: String, en: String, pt: String) = when (language) { AppLanguage.ES -> es; AppLanguage.EN -> en; AppLanguage.PT -> pt }

    LazyColumn(
        // The header reaches the top edge; scroll-to and keyboard focus stop
        // above the floating dock, where rows can be tapped.
        modifier = modifier.fillMaxSize().padding(bottom = appScreenInsets().calculateBottomPadding()).testTag("saints_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "title") {
            AppHeroHeader(
                assetPath = "heroes/lumen/saints.jpg",
                title = tr("Santos", "Saints", "Santos"),
                eyebrow = tr("Calendario Romano General", "General Roman Calendar", "Calendário Romano Geral")
            )
            Spacer(Modifier.height(6.dp))
        }
        item(key = "filters") {
            SaintsFilterControls(
                query = query,
                onQueryChange = onQueryChange,
                month = month,
                onMonthChange = onMonthChange,
                profilesOnly = profilesOnly,
                onProfilesOnlyChange = onProfilesOnlyChange,
                hasProfiles = profiles.isNotEmpty(),
                language = language
            )
        }
        if (visible.isEmpty()) {
            item(key = "empty") {
                AppEmptyState(
                    title = tr("Sin resultados", "No results", "Sem resultados"),
                    body = tr("Prueba con otro nombre o mes.", "Try another name or month.", "Tente outro nome ou mês."),
                    modifier = Modifier.padding(horizontal = gutter, vertical = 32.dp)
                )
            }
        } else {
            val byMonth = visible.groupBy { it.month }
            byMonth.forEach { (m, entries) ->
                item(key = "month_$m") {
                    SaintsMonthGroup(m, entries, profiles, language, null, onOpen)
                }
            }
        }
    }
}

@Composable
private fun SaintDetail(
    celebration: GeneralCelebration,
    profile: SaintProfile?,
    language: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier,
    ttsManager: LiturgicalTtsManager?
) {
    val design = LocalAppDesign.current
    fun tr(es: String, en: String, pt: String) = when (language) { AppLanguage.ES -> es; AppLanguage.EN -> en; AppLanguage.PT -> pt }
    val bio = profile?.bio?.get(language).orEmpty()

    // Read aloud: the name, then the biography, through the shared narrator.
    val saintTitle = celebration.title.get(language)
    val speakText = if (bio.isBlank()) "" else "$saintTitle.\n\n$bio"
    val narrating by (ttsManager?.currentlySpeaking ?: remember { kotlinx.coroutines.flow.MutableStateFlow<String?>(null) }).collectAsState()
    val listening = speakText.isNotEmpty() && narrating == speakText
    DisposableEffect(celebration.key, ttsManager) { onDispose { if (ttsManager != null && narrating == speakText) ttsManager.stop() } }

    val gutter = design.spacing.gutter
    val card = Modifier.fillMaxWidth().padding(horizontal = gutter).widthIn(max = design.spacing.readingMaxWidth + gutter * 2)
    val context = LocalContext.current
    val images = remember(context) { DevotionImageCredits.install(context); DevotionImageCredits.saints }
    val imagePath = remember(images, celebration.key) { images.imagePath(celebration.key) }
    val credit = remember(images, imagePath) { imagePath?.let { images.credit(celebration.key) } }
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("saint_detail"),
        contentPadding = PaddingValues(bottom = appScreenInsets().calculateBottomPadding() + 32.dp),
        verticalArrangement = Arrangement.spacedBy(design.spacing.cardGap)
    ) {
        item {
            AppDetailHeader(
                title = celebration.title.get(language),
                assetPath = imagePath ?: "heroes/lumen/saints.jpg",
                eyebrow = saintDateLabel(celebration.month, celebration.day, language) + " · " + celebration.rank.displayName.get(language)
            )
            if (credit != null) {
                SaintImageCredit(credit, language, Modifier.fillMaxWidth().padding(horizontal = gutter).padding(bottom = 6.dp))
            }
            Row(Modifier.padding(horizontal = gutter).padding(top = 4.dp)) {
                AppSecondaryButton(tr("Todos los santos", "All saints", "Todos os santos"), onBack, leadingIcon = Icons.AutoMirrored.Outlined.ArrowBack, modifier = Modifier.testTag("saint_detail_back"))
            }
            if (ttsManager != null && speakText.isNotEmpty()) {
                Row(Modifier.padding(horizontal = gutter).padding(top = 10.dp)) {
                    SaintListenCapsule(listening, language) {
                        if (listening) ttsManager.stop()
                        else { ttsManager.setLanguage(language); ttsManager.speakSingle(speakText, title = saintTitle) }
                    }
                }
            }
        }
        if (hasProfileCards(profile, language)) {
            item(key = "profile_cards") {
                SaintProfileCards(profile, language, card, gutter)
            }
        }
    }
}
