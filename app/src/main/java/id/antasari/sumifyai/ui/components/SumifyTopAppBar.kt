package id.antasari.sumifyai.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.antasari.sumifyai.ui.theme.BorderLight
import id.antasari.sumifyai.ui.theme.SurfaceLight
import id.antasari.sumifyai.ui.theme.TextPrimary

val SumifyTopBarColor = SurfaceLight
val SumifyTopBarContentColor = TextPrimary
private val SumifyTopBarBottomLineColor = BorderLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SumifyTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SumifyTopBarColor,
            titleContentColor = SumifyTopBarContentColor,
            navigationIconContentColor = SumifyTopBarContentColor,
            actionIconContentColor = SumifyTopBarContentColor
        ),
        modifier = modifier.drawBehind {
            drawLine(
                color = SumifyTopBarBottomLineColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    )
}
