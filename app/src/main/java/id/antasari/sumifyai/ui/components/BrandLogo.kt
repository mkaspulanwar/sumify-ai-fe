package id.antasari.sumifyai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import id.antasari.sumifyai.R

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    heightDp: Int = 32,
    widthDp: Int = 108
) {
    Image(
        painter = painterResource(id = R.drawable.sumifyai_logo_horizontal),
        contentDescription = "Sumify AI Logo",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(heightDp.dp)
            .width(widthDp.dp)
    )
}
