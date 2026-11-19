package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.SanctaFidesDao
import com.example.data.models.AppLanguage
import com.example.data.prefs.UserPreferencesStore
import com.example.data.repository.CatholicDataRepository
import com.example.data.repository.ReadingPlan
import com.example.data.repository.ReadingPlansCatalog
import com.example.ui.design.AppEdgeToEdge
import com.example.ui.design.AppHeroHeader
import com.example.ui.design.AppIconSlot
import com.example.ui.design.AppIcons
import com.example.ui.design.AppListGroup
import com.example.ui.design.AppListRow
import com.example.ui.design.AppProgressBar
import com.example.ui.design.AppSectionHeader
import com.example.ui.design.CompactHeader
import com.example.ui.design.LandscapeTwoPane
import com.example.ui.design.PaneSurface
import com.example.ui.design.rememberFirstAsset
import com.example.ui.design.rememberLandscapeForm
import com.example.ui.theme.LocalAppDesign
import com.example.ui.theme.LocalAppTextStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The places inside «Mi camino». [HUB] is its own start page; the other four are the screens that used to be two
 * Explore destinations (Hábitos y Virtudes, Vida espiritual), shown unchanged. The old destinations still exist and
 * open the matching section.
 */
enum class MyPathSection { HUB, HABITS, DIARY, TEMPTATIONS, PRACTICES }

/**
 * Start page of «Mi camino»: the shared header, «Hoy en mi camino» (today's habits, the fidelity run, the reading plan
 * and the novena in progress, each opening its own place) and the four sections plus Confession.
 *
 * Everything shown is read from the records the sections already keep; opening a row never writes anything.
 */
@Composable
fun MyPathHub(
    repository: CatholicDataRepository,
    dao: SanctaFidesDao,
    store: UserPreferencesStore,
    language: AppLanguage,
    onOpenSection: (MyPathSection) -> Unit,
    onOpenConfession: () -> Unit,
    onOpenPlan: (planId: String) -> Unit,
    onOpenNovena: (novenaId: String) -> Unit
) {
    val insets = AppEdgeToEdge()
    val design = LocalAppDesign.current
    val context = LocalContext.current
    fun tr(es: String, en: String, pt: String) = language.pick3(es, en, pt)

    val today = rememberToday()
    val todayKey = today.toString()
    val todayCompletions by remember(dao, todayKey) { dao.getCompletionsForDate(todayKey) }
        .collectAsState(initial = null)
    val allCompletions by remember(dao) { dao.getAllCompletions() }.collectAsState(initial = emptyList())
    val planProgress by store.planProgress.collectAsState(initial = emptyMap())
    val date by repository.currentDate.collectAsState()
    val plans by produceState<List<ReadingPlan>?>(null, ReadingPlansCatalog.default) {
        value = withContext(Dispatchers.IO) { ReadingPlansCatalog.default.load() }
    }
    val novenaCatalog by repository.novenaCatalog.collectAsState()
    val novenaProgress by remember(dao) { dao.getAllNovenaProgress() }.collectAsState(initial = emptyList())
    val novenaSnapshot by remember(dao) { dao.novenaDaySnapshot() }.collectAsState(initial = emptyList())
    val legacyExtras = remember(context) { NovenaExtraDaysStore(context.applicationContext).all() }

    val habits = todayCompletions?.let { rows ->
        MyPathSummaryMath.habitsToday(DailyRule.habits.map { it.id }, rows.map { it.habitId }.toSet())
    }
    val streak = MyPathSummaryMath.fidelityStreak(DailyRule.countsFrom(allCompletions), today)
    val plansNow = plans?.let { MyPathSummaryMath.plansInProgress(it, planProgress, date.toEpochDay()) }.orEmpty()
    val novenasNow = remember(novenaCatalog, novenaProgress, novenaSnapshot, legacyExtras) {
        MyPathSummaryMath.novenasInProgress(
            novenaCatalog.orEmpty(),
            novenaDaysPrayed(novenaProgress, novenaSnapshot, legacyExtras),
            novenaProgress.filter { it.startDate > 0L }.associate { it.novenaId to it.startDate }
        )
    }

    val art = rememberFirstAsset("heroes/lumen/habits.jpg", "heroes/lumen/spiritual.jpg")
    val title = tr("Mi camino", "My path", "Meu caminho")
    val form = rememberLandscapeForm()

    // Shared blocks so portrait and landscape cannot drift apart.
    val todayHeader: @Composable () -> Unit = {
        AppSectionHeader(tr("Hoy en mi camino", "Today on my path", "Hoje no meu caminho"), modifier = Modifier.testTag("my_path_today_header"))
    }
    val todayBlock: @Composable () -> Unit = {
        AppListGroup(Modifier.testTag("my_path_today")) {
            if (habits != null) AppListRow(
                title = tr("Hábitos de hoy", "Today's habits", "Hábitos de hoje"),
                subtitle = if (habits.pending == 0)
                    tr("Todo hecho. Gracias a Dios.", "All done. Thanks be to God.", "Tudo feito. Graças a Deus.")
                else tr(
                    "${habits.done} ${if (habits.done == 1) "hecho" else "hechos"} · ${habits.pending} ${if (habits.pending == 1) "pendiente" else "pendientes"}",
                    "${habits.done} done · ${habits.pending} to go",
                    "${habits.done} ${if (habits.done == 1) "feito" else "feitos"} · ${habits.pending} ${if (habits.pending == 1) "pendente" else "pendentes"}"
                ),
                leadingIcon = AppIcons.pair(AppIconSlot.HABITS).first,
                onClick = { onOpenSection(MyPathSection.HABITS) },
                modifier = Modifier.testTag("my_path_today_habits")
            )
            AppListRow(
                title = tr("Fidelidad", "Fidelity", "Fidelidade"),
                subtitle = if (streak > 0) tr(
                    if (streak == 1) "1 día seguido" else "$streak días seguidos",
                    if (streak == 1) "1 day in a row" else "$streak days in a row",
                    if (streak == 1) "1 dia seguido" else "$streak dias seguidos"
                ) else tr("Empieza hoy, a tu ritmo.", "Begin today, at your own pace.", "Comece hoje, no seu ritmo."),
                leadingIcon = Icons.Outlined.CalendarMonth,
                onClick = { onOpenSection(MyPathSection.HABITS) },
                modifier = Modifier.testTag("my_path_today_fidelity")
            )
            plansNow.forEach { now ->
                AppListRow(
                    title = now.plan.title.get(language),
                    subtitle = tr("Día ${now.day} de ${now.plan.totalDays} · ${now.percent} %", "Day ${now.day} of ${now.plan.totalDays} · ${now.percent}%", "Dia ${now.day} de ${now.plan.totalDays} · ${now.percent} %"),
                    leadingIcon = Icons.AutoMirrored.Outlined.MenuBook,
                    onClick = { onOpenPlan(now.plan.id) },
                    modifier = Modifier.testTag("my_path_today_plan_${now.plan.id}")
                )
            }
            novenasNow.take(2).forEach { now ->
                AppListRow(
                    title = now.novena.title.get(language),
                    subtitle = tr("Novena · día ${now.nextDay} de ${now.total}", "Novena · day ${now.nextDay} of ${now.total}", "Novena · dia ${now.nextDay} de ${now.total}"),
                    leadingIcon = Icons.Outlined.VolunteerActivism,
                    onClick = { onOpenNovena(now.novena.id) },
                    modifier = Modifier.testTag("my_path_today_novena_${now.novena.id}")
                )
            }
        }
    }
    val sectionsHeader: @Composable () -> Unit = {
        AppSectionHeader(tr("Secciones", "Sections", "Seções"), modifier = Modifier.padding(top = design.spacing.sectionGap - 8.dp))
    }
    val sectionsBlock: @Composable () -> Unit = {
        AppListGroup(Modifier.testTag("my_path_sections")) {
            AppListRow(
                title = tr("Hábitos", "Habits", "Hábitos"),
                subtitle = tr("Tu regla de vida, día a día", "Your rule of life, day by day", "Sua regra de vida, dia a dia"),
                leadingIcon = AppIcons.pair(AppIconSlot.HABITS).first,
                onClick = { onOpenSection(MyPathSection.HABITS) },
                modifier = Modifier.testTag("my_path_section_habits")
            )
            AppListRow(
                title = tr("Diario", "Journal", "Diário"),
                subtitle = tr("Intenciones, gratitud y gracias recibidas", "Intentions, gratitude and graces received", "Intenções, gratidão e graças recebidas"),
                leadingIcon = Icons.Outlined.AutoStories,
                onClick = { onOpenSection(MyPathSection.DIARY) },
                modifier = Modifier.testTag("my_path_section_diary")
            )
            AppListRow(
                title = tr("Tentaciones y victorias", "Temptations and victories", "Tentações e vitórias"),
                subtitle = tr("Ayuda en el momento y tu registro privado", "Help in the moment and your private record", "Ajuda na hora e seu registro privado"),
                leadingIcon = Icons.Outlined.Shield,
                onClick = { onOpenSection(MyPathSection.TEMPTATIONS) },
                modifier = Modifier.testTag("my_path_section_temptations")
            )
            AppListRow(
                title = tr("Prácticas", "Practices", "Práticas"),
                subtitle = tr("Vía Crucis, preguntas de fe y canto gregoriano", "Stations of the Cross, faith quiz and Gregorian chant", "Via-Sacra, quiz da fé e canto gregoriano"),
                leadingIcon = Icons.Outlined.Spa,
                onClick = { onOpenSection(MyPathSection.PRACTICES) },
                modifier = Modifier.testTag("my_path_section_practices")
            )
        }
    }
    val confessionBlock: @Composable () -> Unit = {
        AppListGroup {
            AppListRow(
                title = tr("Confesión", "Confession", "Confissão"),
                subtitle = tr("Examen de conciencia y guía del sacramento", "Examination of conscience and guide to the sacrament", "Exame de consciência e guia do sacramento"),
                leadingIcon = Icons.Outlined.Church,
                onClick = onOpenConfession,
                modifier = Modifier.testTag("my_path_confession_entry")
            )
        }
    }

    // Landscape phone: the illustrated header plus two stacked lists cannot fit
    // ~360dp of height. The numbers (habits kept, fidelity run, plan progress)
    // become a fixed left pane of metric tiles; every row that opens a place
    // stays in the right pane, which owns the only scroll.
    if (form.isLandscape && form.heightDp < 500.dp) {
        val firstPlan = plansNow.firstOrNull()
        LandscapeTwoPane(
            form = form,
            modifier = Modifier.fillMaxSize(),
            leftWidthFraction = 0.38f,
            left = {
                PaneSurface {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CompactHeader(title = title, form = form, modifier = Modifier.testTag("my_path_title"))
                        PathMetricTile(
                            value = habits?.let { "${it.done}/${it.total}" } ?: "–",
                            caption = tr("Hábitos de hoy", "Today's habits", "Hábitos de hoje"),
                            progress = habits?.let { if (it.total == 0) 0f else it.done.toFloat() / it.total },
                            onClick = { onOpenSection(MyPathSection.HABITS) },
                            modifier = Modifier.weight(1f)
                        )
                        PathMetricTile(
                            value = "$streak",
                            caption = tr("Fidelidad · días seguidos", "Fidelity · days in a row", "Fidelidade · dias seguidos"),
                            progress = null,
                            onClick = { onOpenSection(MyPathSection.HABITS) },
                            modifier = Modifier.weight(1f)
                        )
                        if (firstPlan != null) {
                            PathMetricTile(
                                value = "${firstPlan.percent} %",
                                caption = firstPlan.plan.title.get(language) + " · " +
                                    tr("Día ${firstPlan.day} de ${firstPlan.plan.totalDays}", "Day ${firstPlan.day} of ${firstPlan.plan.totalDays}", "Dia ${firstPlan.day} de ${firstPlan.plan.totalDays}"),
                                progress = firstPlan.percent / 100f,
                                onClick = { onOpenPlan(firstPlan.plan.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            right = {
                LazyColumn(
                    Modifier.fillMaxSize().testTag("my_path_screen"),
                    contentPadding = PaddingValues(top = 6.dp, bottom = insets.calculateBottomPadding() + 16.dp)
                ) {
                    item(key = "today_header") { todayHeader() }
                    item(key = "today") { todayBlock() }
                    item(key = "sections_header") { sectionsHeader() }
                    item(key = "sections") { sectionsBlock() }
                    item(key = "confession") {
                        Column {
                            Spacer(Modifier.height(design.spacing.sectionGap - 8.dp))
                            confessionBlock()
                        }
                    }
                }
            }
        )
        return
    }

    LazyColumn(
        Modifier.fillMaxSize().testTag("my_path_screen"),
        contentPadding = PaddingValues(bottom = insets.calculateBottomPadding() + 24.dp)
    ) {
        item(key = "header") { AppHeroHeader(assetPath = art, title = title, titleTag = "my_path_title") }

        item(key = "today_header") { todayHeader() }
        item(key = "today") { todayBlock() }

        item(key = "sections_header") { sectionsHeader() }
        item(key = "sections") { sectionsBlock() }
        item(key = "confession") {
            Column {
                Spacer(Modifier.height(design.spacing.sectionGap - 8.dp))
                confessionBlock()
            }
        }
    }
}

/**
 * One landscape summary number: value, caption and (optionally) a progress bar,
 * on a raised mini-card. The whole tile opens the place that owns the record.
 * Tiles share the pane height with `weight`, so no dead stripe is left below.
 */
@Composable
private fun PathMetricTile(
    value: String,
    caption: String,
    progress: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalAppDesign.current.palette
    val styles = LocalAppTextStyles.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(p.raised)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(value, style = styles.title2, color = p.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(caption, style = styles.footnote, color = p.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (progress != null) {
            Spacer(Modifier.height(8.dp))
            AppProgressBar(progress, height = 4.dp)
        }
    }
}

/**
 * Shows one section of «Mi camino» at a time. [sectionId] changes every time the reader opens a section from the start
 * page, so a section always opens on its own first page and never on whatever it showed last time.
 */
@Composable
fun MyPathSwitcher(
    section: MyPathSection,
    sectionId: Int,
    content: @Composable (MyPathSection) -> Unit
) {
    val reduceMotion = LocalAppDesign.current.reduceMotion
    AnimatedContent(
        targetState = section to sectionId,
        transitionSpec = {
            val forward = targetState.first != MyPathSection.HUB
            val dir = if (forward) 1 else -1
            val ms = if (reduceMotion) 0 else 260
            (slideInHorizontally(tween(ms)) { dir * it / 4 } + fadeIn(tween(ms))) togetherWith
                (slideOutHorizontally(tween(ms)) { -dir * it / 6 } + fadeOut(tween(if (reduceMotion) 0 else 160)))
        },
        label = "myPathSection"
    ) { (shown, _) -> content(shown) }
}
