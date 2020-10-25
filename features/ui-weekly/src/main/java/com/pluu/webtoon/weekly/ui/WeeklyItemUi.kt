package com.pluu.webtoon.weekly.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import androidx.ui.tooling.preview.PreviewParameter
import androidx.ui.tooling.preview.PreviewParameterProvider
import com.pluu.webtoon.model.Status
import com.pluu.webtoon.model.ToonInfo
import com.pluu.webtoon.ui.compose.foundation.backgroundCorner
import com.pluu.webtoon.ui.compose.graphics.toColor
import com.pluu.webtoon.utils.userAgent
import com.pluu.webtoon.weekly.R
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun WeeklyItemUi(
    item: ToonInfo,
    onClicked: (ToonInfo) -> Unit
) {
    Card(modifier = Modifier.padding(2.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth().height(100.dp)
                .clickable { onClicked(item) }
        ) {
            CoilImage(
                data = item.image,
                requestBuilder = {
                    addHeader("User-Agent", userAgent)
                },
                fadeIn = true,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colors.secondary
                        )
                    }
                },
                error = {
                    Image(asset = vectorResource(id = R.drawable.ic_sentiment_very_dissatisfied_48))
                }
            )

            previewWeeklyItemOverlayUi(item)
        }
    }
}

@Composable
fun previewWeeklyItemOverlayUi(
    item: ToonInfo,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    ConstraintLayout(modifier = modifier) {
        val (title, status,
            regDate, favorite
        ) = createRefs()

        Text(
            text = item.title,
            maxLines = 1,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .background(0x66000000.toColor())
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .wrapContentWidth(align = Alignment.Start)
                .constrainAs(title) {
                    width = Dimension.fillToConstraints
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    // TODO: goneMarginEnd=0.dp 대응 필요
                    end.linkTo(status.start, margin = 8.dp)
                }
        )

        WeeklyStatusUi(
            modifier = Modifier.constrainAs(status) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            },
            isUpdate = item.status == Status.UPDATE,
            isAdultLimit = item.isAdult,
            isRest = item.status == Status.BREAK
        )

        if (item.updateDate.isNotBlank()) {
            Text(
                text = item.updateDate,
                color = Color.White,
                modifier = Modifier
                    .backgroundCorner(color = 0x66000000.toColor(), size = 4.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .constrainAs(regDate) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }

        if (item.isFavorite) {
            Image(
                asset = vectorResource(id = R.drawable.ic_favorite_black_36),
                colorFilter = ColorFilter.tint(0xFFF44336.toColor()),
                modifier = Modifier.constrainAs(favorite) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }
            )
        }
    }

}

class FakeWeeklyItemProvider : PreviewParameterProvider<ToonInfo> {
    override val values = sequenceOf(
        ToonInfo(
            id = "",
            title = "타이틀",
            image = "",
            updateDate = "1234.56.78",
            status = Status.UPDATE,
        ),
        ToonInfo(
            id = "",
            title = "타이틀 타이틀 타이틀 타이틀 타이틀 타이틀 타이틀 타이틀",
            image = "",
            updateDate = "1234.56.78",
            status = Status.BREAK,
            isAdult = true,
            isFavorite = true
        )
    )
    override val count: Int = values.count()
}

@Preview(
    group = "Weekly Component",
    widthDp = 240,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun previewWeeklyItemUi(
    @PreviewParameter(FakeWeeklyItemProvider::class) info: ToonInfo,
) {
    WeeklyItemUi(item = info, onClicked = { })
}

@Composable
private fun WeeklyStatusUi(
    modifier: Modifier = Modifier,
    isUpdate: Boolean,
    isAdultLimit: Boolean,
    isRest: Boolean
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        if (isUpdate) {
            Text(
                text = "UP",
                color = Color.White,
                modifier = Modifier
                    .backgroundCorner(color = Color.Red, size = 4.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        if (isAdultLimit) {
            Text(
                text = "19", color = Color.White,
                modifier = Modifier
                    .padding(
                        top = if (isUpdate) {
                            3.dp
                        } else {
                            0.dp
                        }
                    )
                    .backgroundCorner(color = Color.Red, size = 4.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        if (isRest) {
            Text(
                text = "휴재",
                color = Color.White,
                modifier = Modifier
                    .padding(
                        top = if (isUpdate or isAdultLimit) {
                            3.dp
                        } else {
                            0.dp
                        }
                    )
                    .backgroundCorner(color = 0x66000000.toColor(), size = 4.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Preview("Weekly Status Component", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun previewWeeklyStatusUi() {
    WeeklyStatusUi(isUpdate = true, isAdultLimit = true, isRest = true)
}

@Composable
fun WeeklyEmptyUi() {
    Image(asset = vectorResource(R.drawable.ic_sentiment_very_dissatisfied_48))
}

@Preview("EmptyView", widthDp = 100, heightDp = 100)
@Composable
fun previewWeeklyEmptyUi() {
    Box(
        modifier = Modifier.fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        WeeklyEmptyUi()
    }
}