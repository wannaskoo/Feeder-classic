package com.nononsenseapps.feeder.ui.compose.navdrawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedUnreadCount

const val EXPAND_ANIMATION_DURATION = 300
const val COLLAPSE_ANIMATION_DURATION = 300

@ExperimentalAnimationApi
@Composable
fun ListOfFeedsAndTags() {
    val feedListViewModel: FeedListViewModel = DIAwareViewModel()
    val feedsAndTags by feedListViewModel.liveFeedsAndTagsWithUnreadCounts
        .observeAsState(initial = emptyList())

    ListOfFeedsAndTags(
        feedsAndTags = feedsAndTags,
        onItemClick = { /* TODO in old UI */ }
    )
}

@ExperimentalAnimationApi
@Composable
@Preview
private fun ListOfFeedsAndTagsPreview() {
    ListOfFeedsAndTags(
        listOf(
            FeedUnreadCount(id = ID_ALL_FEEDS, unreadCount = 100),
            FeedUnreadCount(tag = "News tag", unreadCount = 3),
            FeedUnreadCount(id = 1, title = "Times", tag = "News tag", unreadCount = 1),
            FeedUnreadCount(id = 2, title = "Post", tag = "News tag", unreadCount = 2),
            FeedUnreadCount(tag = "Funny tag", unreadCount = 6),
            FeedUnreadCount(id = 3, title = "Hidden", tag = "Funny tag", unreadCount = 6),
            FeedUnreadCount(id = 4, title = "Top Dog", unreadCount = 99)
        )
    ) {}
}

@ExperimentalAnimationApi
@Composable
fun ListOfFeedsAndTags(
    feedsAndTags: List<FeedUnreadCount>,
    onItemClick: (FeedUnreadCount) -> Unit
) {
    var expandedTags by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    LazyColumn(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
    ) {
        items(feedsAndTags) { item ->
            when {
                item.isTag -> ExpandableTag(
                    item = item,
                    expanded = item.tag in expandedTags,
                    onExpand = { expandedTags = expandedTags + item.tag },
                    onContract = { expandedTags = expandedTags - item.tag },
                    onItemClick = onItemClick
                )
                item.isTop -> TopLevelFeed(item = item, onItemClick = onItemClick)
                item.tag.isEmpty() -> TopLevelFeed(item = item, onItemClick = onItemClick)
                else -> ChildFeed(
                    item = item,
                    visible = item.tag in expandedTags,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
private fun ExpandableTag(
    item: FeedUnreadCount = FeedUnreadCount(tag = "News tag", unreadCount = 3),
    expanded: Boolean = true,
    onExpand: (String) -> Unit = {},
    onContract: (String) -> Unit = {},
    onItemClick: (FeedUnreadCount) -> Unit = {},
) {
    val transitionState = remember {
        MutableTransitionState(expanded).apply {
            targetState = !expanded
        }
    }
    val transition = updateTransition(transitionState)

    val arrowRotationDegree by transition.animateFloat({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }) {
        if (expanded) 0f else 180f
    }
    ConstraintLayout(
        modifier = Modifier
            .clickable(onClick = { onItemClick(item) })
            .padding(top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
    ) {
        val (expandButton, text, unreadCount, childItems) = createRefs()
        ExpandArrow(
            degrees = arrowRotationDegree,
            onClick = {
                if (expanded) {
                    onContract(item.tag)
                } else {
                    onExpand(item.tag)
                }
            },
            modifier = Modifier
                .constrainAs(expandButton) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )
        Text(
            text = item.unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp, end = 4.dp)
                .constrainAs(unreadCount) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    centerVerticallyTo(expandButton)
                }
        )
        Text(
            text = item.tag,
            maxLines = 1,
            modifier = Modifier
                .padding(end = 2.dp)
                .constrainAs(text) {
                    top.linkTo(parent.top)
                    start.linkTo(expandButton.end)
                    end.linkTo(unreadCount.start)
                    width = Dimension.fillToConstraints
                    centerVerticallyTo(expandButton)
                }
        )
    }
}

@Composable
private fun ExpandArrow(
    degrees: Float,
    modifier: Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_expand_less_24),
            contentDescription = stringResource(id = R.string.toggle_tag_expansion),
            modifier = Modifier.rotate(degrees = degrees)
        )
    }
}

@Preview
@Composable
private fun TopLevelFeed(
    item: FeedUnreadCount = FeedUnreadCount(title = "A feed", unreadCount = 999),
    onItemClick: (FeedUnreadCount) -> Unit = {}
) = Feed(
    title = if (item.isTop) stringResource(id = R.string.all_feeds) else item.displayTitle,
    unreadCount = item.unreadCount,
    startPadding = 16.dp,
    onItemClick = { onItemClick(item) }
)

@ExperimentalAnimationApi
@Preview
@Composable
private fun ChildFeed(
    item: FeedUnreadCount = FeedUnreadCount(title = "Some feed", unreadCount = 21),
    visible: Boolean = true,
    onItemClick: (FeedUnreadCount) -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Feed(
            title = item.displayTitle,
            unreadCount = item.unreadCount,
            startPadding = 48.dp,
            onItemClick = { onItemClick(item) }
        )
    }
}

@Composable
private fun Feed(
    title: String,
    unreadCount: Int,
    startPadding: Dp,
    onItemClick: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(start = startPadding, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val (refText, refCount) = createRefs()
        Text(
            text = unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp)
                .constrainAs(refCount) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    centerVerticallyTo(parent)
                }
        )
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .padding(end = 2.dp)
                .constrainAs(refText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(refCount.start)
                    width = Dimension.fillToConstraints
                    centerVerticallyTo(parent)
                }
        )
    }
}