package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.titleFontWeight

@Composable
fun FeedItemSuperCompact(
    item: FeedListItem,
    showThumbnail: Boolean,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleBookmarked: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    newIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(IntrinsicSize.Min),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(weight = 1.0f, fill = true)
                .requiredHeightIn(min = minimumTouchSize)
                .padding(vertical = 4.dp),
        ) {
            val opacity = if (item.unread) 1.0f else 0.7f;
            WithBidiDeterminedLayoutDirection(paragraph = item.title) {
                Text(
                    text = item.title,
                    style = FeedListItemTitleTextStyle(),
                    fontWeight = titleFontWeight(item.unread),
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .fillMaxWidth()
                        .alpha(opacity),
                )
            }
            // Want the dropdown to center on the middle text row
            Box {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        val text = buildAnnotatedString {
                            if (item.pubDate.isNotBlank()) {
                                append("${item.pubDate} ‧ ")
                            }
                            withStyle(FeedListItemFeedTitleStyle().toSpanStyle()) {
                                append(item.feedTitle)
                            }
                        }
                        WithBidiDeterminedLayoutDirection(paragraph = text.text) {
                            Text(
                                text = text,
                                style = FeedListItemDateStyle(),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, end = 4.dp)
                                    .alpha(opacity),
                            )
                        }
                    }
                }
                DropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = onDismissDropdown,
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onTogglePinned()
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    when (item.pinned) {
                                        true -> R.string.unpin_article
                                        false -> R.string.pin_article
                                    },
                                ),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onToggleBookmarked()
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    when (item.bookmarked) {
                                        true -> R.string.remove_bookmark
                                        false -> R.string.bookmark_article
                                    },
                                ),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkAboveAsRead()
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.mark_items_above_as_read),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkBelowAsRead()
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.mark_items_below_as_read),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onShareItem()
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.share),
                            )
                        },
                    )
                }
            }
        }

        if (showThumbnail && (item.imageUrl != null || item.feedImageUrl != null) || item.unread || item.bookmarked || item.pinned) {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.TopEnd,
            ) {
                (item.imageUrl ?: item.feedImageUrl?.toString())?.let { imageUrl ->
                    if (showThumbnail) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .listener(
                                    onError = { a, b ->
                                        Log.e("FEEDER_SUPERCOMPACT", "error ${a.data}", b.throwable)
                                    },
                                )
                                .scale(Scale.FIT)
                                .size(200)
                                .precision(Precision.INEXACT)
                                .build(),
                            placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
                            error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                            contentDescription = stringResource(id = R.string.article_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight(),
                        )
                    }
                }
                FeedItemIndicatorColumn(
                    unread = item.unread && newIndicator,
                    bookmarked = item.bookmarked,
                    pinned = item.pinned,
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 4.dp,
                        end = 4.dp,
                    ),
                    spacing = 4.dp,
                    iconSize = 8.dp,
                )
            }
        } else {
            // Taking Row spacing into account
            Spacer(modifier = Modifier.width(LocalDimens.current.margin - 4.dp))
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewRead() {
    FeederTheme {
        Surface {
            FeedItemSuperCompact(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = false,
                    imageUrl = null,
                    link = null,
                    id = ID_UNSET,
                    pinned = false,
                    bookmarked = false,
                    feedImageUrl = null,
                ),
                showThumbnail = true,
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onTogglePinned = {},
                onToggleBookmarked = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                newIndicator = true,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewUnread() {
    FeederTheme {
        Surface {
            FeedItemSuperCompact(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    imageUrl = null,
                    link = null,
                    id = ID_UNSET,
                    pinned = false,
                    bookmarked = false,
                    feedImageUrl = null,
                ),
                showThumbnail = true,
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onTogglePinned = {},
                onToggleBookmarked = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                newIndicator = true,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewWithImage() {
    FeederTheme {
        Surface {
            FeedItemSuperCompact(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    imageUrl = "blabla",
                    link = null,
                    id = ID_UNSET,
                    pinned = true,
                    bookmarked = false,
                    feedImageUrl = null,
                ),
                showThumbnail = true,
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onTogglePinned = {},
                onToggleBookmarked = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                newIndicator = true,
            )
        }
    }
}
