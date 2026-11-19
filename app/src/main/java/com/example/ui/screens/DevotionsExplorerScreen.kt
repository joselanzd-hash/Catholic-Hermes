package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.models.AppLanguage
import com.example.data.models.ChristManifestation
import com.example.data.models.LocalizedText
import com.example.data.models.MarianApparition
import com.example.data.repository.CatholicDataRepository
import com.example.data.repository.DevotionImageCredit
import com.example.data.repository.DevotionImageCredits
import com.example.ui.design.AppEmptyState
import com.example.ui.design.AppFilterChips
import com.example.ui.design.AppFilterMenu
import com.example.ui.design.AppHeroImage
import com.example.ui.design.AppInlineTitle
import com.example.ui.design.AppSearchBar
import com.example.ui.design.AppSecondaryCapsule
import com.example.ui.design.AppSegmentedTabs
import com.example.ui.design.CompactHeader
import com.example.ui.design.GroupPosition
import com.example.ui.design.LandscapeGap
import com.example.ui.design.LandscapeTwoPane
import com.example.ui.design.PaneSurface
import com.example.ui.design.appScreenInsets
import com.example.ui.design.rememberLandscapeForm
import com.example.ui.theme.LocalAppDesign
import com.example.ui.theme.LocalAppTextStyles

// -----------------------------------------------------------------------------
// Mary & Jesus: Marian advocations and devotions to Christ, a searchable list
// grouped by region, and a reading page for each devotion.
// -----------------------------------------------------------------------------

/** Sentinel for "every region". */
private const val EVERY_REGION = "ALL"

/** Regions in the order they appear; any other value in the data follows. */
private val REGION_ORDER = listOf("Americas", "Europe", "Asia_Africa", "Universal")

private fun regionLabel(region: String, language: AppLanguage): String = when (region) {
    EVERY_REGION -> language.tr("Todas", "All", "Todas")
    "Americas" -> language.tr("Américas", "Americas", "Américas")
    "Europe" -> language.tr("Europa", "Europe", "Europa")
    "Asia_Africa" -> language.tr("Asia y África", "Asia & Africa", "Ásia e África")
    "Universal" -> language.tr("Universales", "Universal", "Universais")
    else -> region.replace('_', ' ')
}

/** One row of the list, whichever category it comes from. */
private data class DevotionRowData(
    val id: String,
    val region: String,
    val title: String,
    val subtitle: String,
    val year: String?
)

@Composable
internal fun DevotionsExplorerScreen(
    repository: CatholicDataRepository,
    language: AppLanguage,
    nav: DoctrineNav,
    spec: RootSpec
) {
    val state = rememberLazyListState()
    val styles = LocalAppTextStyles.current
    val p = LocalAppDesign.current.palette
    var category by rememberSaveable { mutableStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf(EVERY_REGION) }

    val marian = repository.marianApparitions
    val christ = repository.christManifestations
    val q = query.trim().lowercase()

    fun hit(vararg fields: String) = q.isEmpty() || fields.any { it.lowercase().contains(q) }

    val rows: List<DevotionRowData> = remember(category, q, language, marian, christ) {
        if (category == 0) {
            marian.filter {
                hit(
                    it.title.get(language), it.country.get(language), it.location.get(language),
                    it.patronageOf.get(language), it.primarySanctuary.get(language), it.recordedMiracles.get(language)
                )
            }.map {
                DevotionRowData(
                    it.id, it.region, splitAlias(it.title.get(language)).title,
                    it.location.get(language), devotionYear(it.year, language)
                )
            }
        } else {
            christ.filter {
                hit(
                    it.title.get(language), it.country.get(language), it.primarySanctuary.get(language),
                    it.originHistory.get(language), it.recordedMiracles.get(language)
                )
            }.map {
                DevotionRowData(
                    it.id, it.region, splitAlias(it.title.get(language)).title, it.country.get(language),
                    it.year.takeIf { y -> y.isNotBlank() }?.let { y -> devotionYear(y, language) }
                )
            }
        }
    }
    val inRegion = if (region == EVERY_REGION) rows else rows.filter { it.region == region }

    val regionKeys = listOf(EVERY_REGION) + REGION_ORDER
    val regionIndex = regionKeys.indexOf(region).coerceAtLeast(0)
    val grouped: List<Pair<String, List<DevotionRowData>>> =
        if (region == EVERY_REGION) {
            val known = REGION_ORDER.map { r -> r to rows.filter { it.region == r } }
            val others = rows.filter { it.region !in REGION_ORDER }.groupBy { it.region }.toList()
            (known + others).filter { it.second.isNotEmpty() }
        } else {
            listOf(region to inRegion).filter { it.second.isNotEmpty() }
        }

    val form = rememberLandscapeForm()

    // Shared blocks: portrait and landscape render exactly the same controls,
    // rows and empty state, so the two form factors cannot drift apart.
    val controlsBlock: @Composable () -> Unit = {
        AppSegmentedTabs(
            options = listOf(
                language.tr("Nuestra Señora", "Our Lady", "Nossa Senhora"),
                language.tr("Cristo", "Christ", "Cristo")
            ),
            selectedIndex = category,
            onSelect = { category = it },
            modifier = Modifier.testTag("devotions_category_picker")
        )
        Spacer(Modifier.height(14.dp))
        AppSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = language.tr("Nombre, país o santuario", "Name, country or shrine", "Nome, país ou santuário"),
            tag = "devotions_search_input"
        )
        Spacer(Modifier.height(12.dp))
        AppFilterMenu(
            prefix = language.tr("Región", "Region", "Região"),
            options = regionKeys.map { regionLabel(it, language) },
            selectedIndex = regionIndex,
            onSelect = { region = regionKeys[it] },
            tag = "devotions_region_picker"
        )
    }
    val emptyBlock: @Composable () -> Unit = {
        AppEmptyState(
            title = language.tr("Sin resultados", "Nothing found", "Sem resultados"),
            body = language.tr(
                "Prueba otro nombre o elige otra región.",
                "Try another name, or choose a different region.",
                "Tente outro nome ou escolha outra região."
            ),
            icon = Icons.Outlined.Search
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AppSecondaryCapsule(
                label = language.tr("Ver todas", "Show all", "Ver todas"),
                onClick = { query = ""; region = EVERY_REGION }
            )
        }
    }
    val headerBlock: @Composable (String, Int, Dp) -> Unit = { key, count, topGap ->
        SectionHeader(
            if (region == EVERY_REGION) regionLabel(key, language)
            else if (category == 0) language.tr("Advocaciones marianas", "Marian titles", "Advocações marianas")
            else language.tr("Devociones a Cristo", "Devotions to Christ", "Devoções a Cristo"),
            trailing = "$count",
            topGap = topGap
        )
    }
    val rowBlock: @Composable (DevotionRowData, GroupPosition?) -> Unit = { m, position ->
        LeadRow(
            title = m.title,
            subtitle = m.subtitle,
            trailingText = m.year,
            position = position,
            onClick = { nav.open("devotion:${m.id}") },
            tag = "devotion_card_${m.id}"
        )
    }

    // Landscape phone: category, search and region stay pinned in the left
    // pane; the devotions spread into a 2-3 column grid on the right, which
    // owns the only scroll. The 232dp hero becomes a 44-52dp compact header.
    if (form.isLandscape && form.heightDp < 500.dp) {
        val insets = appScreenInsets()
        LandscapeTwoPane(
            form = form,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = insets.calculateTopPadding(), bottom = insets.calculateBottomPadding())
                .testTag("devotions_explorer_screen"),
            leftWidthFraction = 0.34f,
            left = {
                Column(Modifier.fillMaxSize()) {
                    CompactHeader(title = spec.title, form = form, subtitle = spec.eyebrow)
                    LandscapeGap(form)
                    controlsBlock()
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${inRegion.size} " + language.tr("resultados", "results", "resultados"),
                        style = styles.footnote,
                        color = p.textSecondary,
                        modifier = Modifier.padding(horizontal = form.gutter, vertical = 8.dp)
                    )
                }
            },
            right = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (form.panes >= 3) 3 else 2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 0.dp, end = 8.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (inRegion.isEmpty()) {
                        item(key = "empty", span = { GridItemSpan(maxLineSpan) }) { emptyBlock() }
                    } else {
                        grouped.forEach { (key, members) ->
                            item(key = "h_$key", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                                headerBlock(key, members.size, 8.dp)
                            }
                            gridItemsIndexed(members, key = { _, m -> "d_${m.id}" }, contentType = { _, _ -> "devotion" }) { _, m ->
                                rowBlock(m, GroupPosition.Single)
                            }
                        }
                    }
                }
            }
        )
        return
    }

    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize().testTag("devotions_explorer_screen"),
        contentPadding = PaddingValues(bottom = LocalBottomPad.current)
    ) {
        item(key = "hero", contentType = "hero") { RootHeader(spec) }
        item(key = "controls") {
            Spacer(Modifier.height(4.dp))
            controlsBlock()
        }

        if (inRegion.isEmpty()) {
            item(key = "empty") { emptyBlock() }
        } else {
            grouped.forEach { (key, members) ->
                item(key = "h_$key", contentType = "header") { headerBlock(key, members.size, 24.dp) }
                itemsIndexed(members, key = { _, m -> "d_${m.id}" }, contentType = { _, _ -> "devotion" }) { i, m ->
                    rowBlock(m, groupPosition(i, members.size))
                }
            }
        }
    }
}

// ------------------------------------------------------------------ detail

@Composable
internal fun DevotionDetail(
    id: String,
    repository: CatholicDataRepository,
    language: AppLanguage,
    nav: DoctrineNav
) {
    val marian = repository.marianApparitions.firstOrNull { it.id == id }
    val christ = repository.christManifestations.firstOrNull { it.id == id }
    val context = LocalContext.current
    val credits = remember(context) { DevotionImageCredits.install(context); DevotionImageCredits.default }
    val credit = remember(credits, id) { credits.imagePath(id)?.let { credits.credit(id) } }
    // The devotion's own picture heads its page; without one, the section's art stays.
    CompositionLocalProvider(LocalReaderArt provides (credit?.let { credits.imagePath(id) } ?: LocalReaderArt.current)) {
        when {
            marian != null -> MarianPage(marian, language, nav, credit)
            christ != null -> ChristPage(christ, language, nav, credit)
        }
    }
}

@Composable
private fun MarianPage(item: MarianApparition, language: AppLanguage, nav: DoctrineNav, credit: DevotionImageCredit?) {
    val ta = splitAlias(item.title.get(language))
    AppInlineTitle(ta.title)
    val year = devotionYear(item.year, language)
    DevotionPage(
        language = language,
        nav = nav,
        id = item.id,
        title = ta.title,
        eyebrow = language.tr("Advocación mariana", "Marian title", "Advocação mariana") + if (year.isNotBlank()) " · $year" else "",
        alias = ta.alias,
        facts = listOf(
            language.tr("Fiesta", "Feast day", "Festa") to item.feastDay.get(language),
            language.tr("País", "Country", "País") to item.country.get(language),
            language.tr("Lugar", "Place", "Lugar") to item.location.get(language),
            language.tr("Santuario", "Shrine", "Santuário") to item.primarySanctuary.get(language),
            language.tr("Patronazgo", "Patronage", "Padroado") to item.patronageOf.get(language)
        ),
        historyLabel = language.tr("Historia", "History", "História"),
        history = item.historyAndApparition.get(language),
        meaningLabel = null,
        meaning = null,
        promises = item.promises.get(language),
        miraclesLabel = language.tr("Milagros destacados", "Recorded miracles", "Milagres registados"),
        miracles = item.recordedMiracles.get(language),
        prayerTitle = language.tr(
            "Oración de veneración e intercesión",
            "Prayer of veneration and intercession",
            "Oração de veneração e intercessão"
        ),
        prayer = item.venerationPrayer.get(language),
        extras = item.extras(language),
        credit = credit
    )
}

@Composable
private fun ChristPage(item: ChristManifestation, language: AppLanguage, nav: DoctrineNav, credit: DevotionImageCredit?) {
    val ta = splitAlias(item.title.get(language))
    AppInlineTitle(ta.title)
    DevotionPage(
        language = language,
        nav = nav,
        id = item.id,
        title = ta.title,
        eyebrow = language.tr("Devoción a Cristo", "Devotion to Christ", "Devoção a Cristo") +
            devotionYear(item.year, language).let { if (it.isNotBlank()) " · $it" else "" },
        alias = ta.alias,
        facts = listOf(
            language.tr("Fiesta", "Feast day", "Festa") to item.feastDay.get(language),
            language.tr("País", "Country", "País") to item.country.get(language),
            language.tr("Santuario principal", "Principal shrine", "Santuário principal") to item.primarySanctuary.get(language)
        ),
        historyLabel = language.tr("Historia", "History", "História"),
        history = item.originHistory.get(language),
        meaningLabel = language.tr("Sentido teológico", "Theological meaning", "Sentido teológico"),
        meaning = item.theologicalMeaning.get(language),
        promises = item.promises.get(language),
        miraclesLabel = language.tr("Milagros", "Miracles", "Milagres"),
        miracles = item.recordedMiracles.get(language),
        prayerTitle = language.tr("Oración devocional", "Devotional prayer", "Oração devocional"),
        prayer = item.officialPrayer.get(language),
        extras = item.extras(language),
        credit = credit
    )
}

/** «Imagen: autor · licencia · fuente», with the author left out when unknown. */
internal fun imageCreditLine(c: DevotionImageCredit, language: AppLanguage): String {
    if (c.referential) {
        val prefix = language.tr("Imagen referencial: ", "Reference image: ", "Imagem de referência: ")
        return prefix + c.license.removePrefix("Derechos: ").trim()
    }
    if (c.userSupplied) return language.tr("Foto aportada por el usuario", "Photo supplied by the user", "Foto enviada pelo usuário")
    val parts = listOf(c.author, c.license, c.source).map { it.trim() }.filter { it.isNotEmpty() }
    return language.tr("Imagen: ", "Image: ", "Imagem: ") + parts.joinToString(" · ")
}

/** Optional editorial material for a devotion; blank pieces are not shown. */
private data class DevotionExtras(
    val recognition: String = "",
    val practice: String = "",
    val promiseSource: String = "",
    val prayerSource: String = "",
    val sources: List<String> = emptyList()
)

private fun promiseCredit(attribution: LocalizedText?, source: String?, language: AppLanguage) =
    listOfNotNull(attribution?.get(language)?.trim(), source?.trim()).filter { it.isNotEmpty() }.joinToString(" — ")

private fun MarianApparition.extras(language: AppLanguage) = DevotionExtras(
    recognition?.get(language).orEmpty(), practice?.get(language).orEmpty(),
    promiseCredit(promiseAttribution, promiseSource, language), prayerSource.orEmpty(), sources
)

private fun ChristManifestation.extras(language: AppLanguage) = DevotionExtras(
    recognition?.get(language).orEmpty(), practice?.get(language).orEmpty(),
    promiseCredit(promiseAttribution, promiseSource, language), prayerSource.orEmpty(), sources
)

@Composable
private fun DevotionPage(
    language: AppLanguage,
    nav: DoctrineNav,
    id: String,
    title: String,
    eyebrow: String,
    alias: String?,
    facts: List<Pair<String, String>>,
    historyLabel: String,
    history: String,
    meaningLabel: String?,
    meaning: String?,
    promises: String,
    miraclesLabel: String,
    miracles: String,
    prayerTitle: String,
    prayer: String,
    extras: DevotionExtras = DevotionExtras(),
    credit: DevotionImageCredit? = null
) {
    val design = LocalAppDesign.current
    val styles = LocalAppTextStyles.current
    val p = design.palette
    val form = rememberLandscapeForm()
    LaunchedEffect(Unit) { nav.voice.prepare() }
    // The devotion's story, read aloud; the prayer below keeps its own button.
    val storyText = listOfNotNull(title, history, meaning?.takeIf { it.isNotBlank() }).joinToString("\n\n")

    // Shared blocks: portrait and landscape render exactly these pieces.
    val creditBlock: @Composable () -> Unit = {
        if (credit != null) {
            val uriHandler = LocalUriHandler.current
            val url = credit.safeUrl
            Text(
                imageCreditLine(credit, language), style = styles.footnote, color = p.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = design.spacing.gutter)
                    .then(if (url != null) Modifier.clickable(role = Role.Button) { runCatching { uriHandler.openUri(url) } } else Modifier)
                    .testTag("devotion_image_credit")
            )
            BlockGap(20.dp)
        }
    }
    val listenBlock: @Composable () -> Unit = {
        ReaderActionRow(language, nav.voice, storyText, null, "devotion_listen_button", null, title = title)
    }
    val factsBlock: @Composable () -> Unit = {
        FactsGroup(facts)
        BlockGap(28.dp)
    }
    val recognitionBlock: @Composable () -> Unit = {
        NoteCard(
            language.tr("Reconocimiento eclesial", "Church recognition", "Reconhecimento eclesial"),
            extras.recognition, Modifier.testTag("devotion_recognition")
        )
        BlockGap(28.dp)
    }
    val historyBlock: @Composable () -> Unit = {
        SectionHeader(historyLabel, topGap = 0.dp)
        ReadingParagraphs(history)
        if (meaningLabel != null && !meaning.isNullOrBlank()) {
            SectionHeader(meaningLabel, topGap = 28.dp)
            ReadingParagraphs(meaning)
        }
        BlockGap(28.dp)
    }
    val practiceBlock: @Composable () -> Unit = {
        NoteCard(
            language.tr("Cómo se vive", "How it is lived", "Como se vive"),
            extras.practice, Modifier.testTag("devotion_practice")
        )
        BlockGap(28.dp)
    }
    val promisesBlock: @Composable () -> Unit = {
        GlassCard {
            Text(
                language.tr("Promesas", "Promises", "Promessas"), style = styles.headline,
                color = p.textPrimary, modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(8.dp))
            Text(promises, style = styles.reading.copy(fontStyle = FontStyle.Italic), color = p.textPrimary)
            if (extras.promiseSource.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    extras.promiseSource, style = styles.footnote, color = p.textSecondary,
                    modifier = Modifier.testTag("devotion_promise_source")
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
    val miraclesBlock: @Composable () -> Unit = {
        NoteCard(miraclesLabel, miracles)
        BlockGap(28.dp)
    }
    val prayerBlock: @Composable () -> Unit = {
        GlassCard {
            Text(prayerTitle, style = styles.headline, color = p.textPrimary, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(12.dp))
            Text(
                prayer,
                style = styles.reading.copy(fontStyle = FontStyle.Italic),
                color = p.textPrimary
            )
            if (extras.prayerSource.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    extras.prayerSource, style = styles.footnote, color = p.textSecondary,
                    modifier = Modifier.testTag("devotion_prayer_source")
                )
            }
            Spacer(Modifier.height(16.dp))
            val playing = nav.voice.isReading(prayer)
            AppSecondaryCapsule(
                label = if (playing) language.tr("Detener", "Stop", "Parar") else language.tr("Escuchar", "Listen", "Ouvir"),
                icon = if (playing) Icons.Outlined.Stop else Icons.Outlined.VolumeUp,
                onClick = { if (playing) nav.voice.stop() else nav.voice.speak(prayer, title = prayerTitle) },
                tag = "devotion_prayer_listen"
            )
        }
    }
    val sourcesBlock: @Composable () -> Unit = {
        BlockGap(28.dp)
        NoteCard(
            language.tr("Fuentes", "Sources", "Fontes"),
            extras.sources.joinToString("\n") { "• $it" }, Modifier.testTag("devotion_sources")
        )
    }

    // Landscape phone: art (78-104dp), title and the listen control stay
    // pinned in the left pane; the reading blocks scroll on the right at a
    // <=640dp measure. The portrait art header never appears here.
    if (form.isLandscape && form.heightDp < 500.dp) {
        val insets = appScreenInsets()
        LandscapeTwoPane(
            form = form,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = insets.calculateTopPadding(), bottom = insets.calculateBottomPadding())
                .testTag("devotion_detail_$id"),
            leftWidthFraction = 0.36f,
            left = {
                PaneSurface {
                    Column(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(if (form.isShortLandscape) 78.dp else 104.dp)
                                .clip(RoundedCornerShape(18.dp))
                        ) {
                            AppHeroImage(
                                LocalReaderArt.current,
                                Modifier.fillMaxSize(),
                                fadeToCanvas = false,
                                topVeil = false
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(title, style = styles.title2, color = p.textPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(3.dp))
                        Text(eyebrow, style = styles.footnote, color = p.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (alias != null) {
                            Spacer(Modifier.height(3.dp))
                            Text(alias, style = styles.footnote, color = p.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { listenBlock() }
                    }
                }
            },
            right = {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
                ) {
                    if (credit != null) item(key = "image_credit") { creditBlock() }
                    item(key = "facts") { factsBlock() }
                    if (extras.recognition.isNotBlank()) item(key = "recognition") { recognitionBlock() }
                    item(key = "history") { historyBlock() }
                    if (extras.practice.isNotBlank()) item(key = "practice") { practiceBlock() }
                    if (promises.isNotBlank()) item(key = "promises") { promisesBlock() }
                    if (miracles.isNotBlank()) item(key = "miracles") { miraclesBlock() }
                    if (prayer.isNotBlank()) item(key = "prayer") { prayerBlock() }
                    if (extras.sources.isNotEmpty()) item(key = "sources") { sourcesBlock() }
                }
            }
        )
        return
    }

    ReaderPage(
        title = title,
        eyebrow = eyebrow,
        meta = alias,
        pageTag = "devotion_detail_$id"
    ) {
        if (credit != null) item(key = "image_credit") { creditBlock() }
        // The devotion's story, read aloud; the prayer below keeps its own button.
        item(key = "listen") {
            listenBlock()
            BlockGap(20.dp)
        }
        item(key = "facts") { factsBlock() }
        if (extras.recognition.isNotBlank()) item(key = "recognition") { recognitionBlock() }
        item(key = "history") { historyBlock() }
        if (extras.practice.isNotBlank()) item(key = "practice") { practiceBlock() }
        if (promises.isNotBlank()) item(key = "promises") { promisesBlock() }
        if (miracles.isNotBlank()) item(key = "miracles") { miraclesBlock() }
        if (prayer.isNotBlank()) item(key = "prayer") { prayerBlock() }
        if (extras.sources.isNotEmpty()) item(key = "sources") { sourcesBlock() }
    }
}
