package br.com.mural.feature.calendar

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mural.core.date.DateUtils
import br.com.mural.core.permissions.PhotoAccess
import br.com.mural.core.ui.theme.SerifFontFamily
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onDayClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.updatePermissionState()
    }

    LaunchedEffect(Unit) {
        viewModel.updatePermissionState()
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CalendarUiEvent.ShowSnackbar -> {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoCoverChange()
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Month Header with Prev/Next Arrows and Config 3-Dots Menu
            MonthHeader(
                month = uiState.month,
                canGoForward = uiState.month.isBefore(uiState.maxMonth),
                currentAspectRatio = uiState.cellAspectRatio,
                onPrevClick = {
                    val prev = uiState.month.minusMonths(1)
                    if (!prev.isBefore(uiState.minMonth)) {
                        viewModel.onMonthChanged(prev)
                    }
                },
                onNextClick = {
                    val next = uiState.month.plusMonths(1)
                    if (!next.isAfter(uiState.maxMonth)) {
                        viewModel.onMonthChanged(next)
                    }
                },
                onAspectRatioSelected = { ratio ->
                    viewModel.setCellAspectRatio(ratio)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday Headers (D S T Q Q S S)
            WeekdayHeaders()

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.access) {
                PhotoAccess.Denied -> {
                    PermissionDeniedCard(
                        onRequestPermission = {
                            val permissions = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            permissionLauncher.launch(permissions.toTypedArray())
                        },
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                else -> {
                    if (uiState.access == PhotoAccess.Partial) {
                        PartialAccessBanner(
                            onManage = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Month Grid (7 columns x 6 rows)
                    MonthGrid(
                        cells = uiState.cells,
                        aspectRatio = uiState.cellAspectRatio,
                        onCellClick = { cell ->
                            if (cell.date != LocalDate.MIN) {
                                onDayClick(DateUtils.toDateKey(cell.date))
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // "Hoje" Card (US-11)
                    TodayCard(
                        today = uiState.today,
                        photoCount = uiState.todayPhotoCount,
                        coverUri = uiState.todayCoverUri,
                        onClick = {
                            onDayClick(DateUtils.toDateKey(uiState.today))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    month: YearMonth,
    canGoForward: Boolean,
    currentAspectRatio: Float,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onAspectRatioSelected: (Float) -> Unit
) {
    val monthName = month.format(DateTimeFormatter.ofPattern("MMMM", Locale("pt", "BR")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
    val yearText = month.year.toString()
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = monthName,
                fontFamily = SerifFontFamily,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = yearText,
                fontFamily = SerifFontFamily,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPrevClick),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Mês anterior",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(enabled = canGoForward, onClick = onNextClick),
                color = if (canGoForward) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Próximo mês",
                        tint = if (canGoForward) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            // 3-Dots Config Button & Aspect Ratio Modal Menu
            Box {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { showMenu = true }),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Configuração do formato das células",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    Text(
                        text = "Formato dos blocos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    listOf(
                        "1:1 Quadrado" to 1.0f,
                        "3:4 Retrato" to (3f / 4f),
                        "2:3 Retrato alto" to (2f / 3f)
                    ).forEach { (label, ratio) ->
                        val isSelected = abs(currentAspectRatio - ratio) < 0.01f
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onAspectRatioSelected(ratio)
                                showMenu = false
                            },
                            trailingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekdayHeaders() {
    val weekdays = listOf("D", "S", "T", "Q", "Q", "S", "S")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekdays.forEach { day ->
            Text(
                text = day,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MonthGrid(
    cells: List<DayCellUi>,
    aspectRatio: Float,
    onCellClick: (DayCellUi) -> Unit
) {
    // 6 rows x 7 columns
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until 6) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    if (index < cells.size) {
                        val cellUi = cells[index]
                        DayCell(
                            cellUi = cellUi,
                            aspectRatio = aspectRatio,
                            onClick = { onCellClick(cellUi) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun PartialAccessBanner(
    onManage: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Você compartilhou algumas fotos. O mural mostra só elas.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Gerenciar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onManage)
            )
        }
    }
}

@Composable
fun PermissionDeniedCard(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Photo,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Suas fotos, organizadas por dia",
                fontFamily = SerifFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Precisamos da sua permissão para ler a galeria e montar o calendário de recordações. Tudo fica no seu aparelho.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Permitir acesso às fotos")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Abrir configurações do app",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onOpenSettings)
            )
        }
    }
}
