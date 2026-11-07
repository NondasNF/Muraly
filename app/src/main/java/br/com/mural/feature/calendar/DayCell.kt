package br.com.mural.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.mural.core.ui.theme.MuralTheme
import coil3.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayCell(
    cellUi: DayCellUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cellUi.date == LocalDate.MIN) {
        // Ghost padding cell for 6-row alignment
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MuralTheme.extraColors.emptyCell.copy(alpha = 0.3f))
        )
        return
    }

    val date = cellUi.date
    val dayNumber = date.dayOfMonth.toString()
    val formattedDate = date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("pt", "BR")))
    val accessDescription = when {
        cellUi.coverUri != null -> "$formattedDate, ${cellUi.photoCount} fotos, capa definida"
        cellUi.photoCount > 0 -> "$formattedDate, ${cellUi.photoCount} fotos, sem capa"
        else -> "$formattedDate, sem fotos"
    }

    val extraColors = MuralTheme.extraColors
    val backgroundColor = when {
        cellUi.isFuture -> extraColors.futureCell
        else -> extraColors.emptyCell
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = accessDescription
                role = Role.Button
            }
    ) {
        if (cellUi.coverUri != null && cellUi.isCoverAvailable) {
            // Cover Photo
            AsyncImage(
                model = cellUi.coverUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim gradient for contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.70f)
                            ),
                            startY = 0f
                        )
                    )
            )

            // White day number with shadow
            Text(
                text = dayNumber,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 3.dp)
            )
        } else if (cellUi.coverUri != null && !cellUi.isCoverAvailable) {
            // Broken Cover Image Placeholder
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "Capa indisponível",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center)
            )

            Text(
                text = dayNumber,
                style = TextStyle(
                    color = extraColors.dayNumberEmpty,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 3.dp)
            )
        } else {
            // Empty / Future cell without cover
            val numberColor = when {
                cellUi.isFuture -> extraColors.dayNumberEmpty.copy(alpha = 0.40f)
                else -> extraColors.dayNumberEmpty
            }

            Text(
                text = dayNumber,
                style = TextStyle(
                    color = numberColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 3.dp)
            )
        }

        // Today highlight: 2 dp terracotta ring with 2 dp inset, 4 dp top-left dot
        if (cellUi.isToday) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .padding(start = 5.dp, top = 5.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
