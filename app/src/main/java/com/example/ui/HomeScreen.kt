package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Channel

@Composable
fun HomeScreen(
    channels: List<Channel>,
    selectedIndex: Int,
    onChannelClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Keep the focused channel visible as user moves through the list
    LaunchedEffect(selectedIndex) {
        if (channels.isNotEmpty() && selectedIndex in channels.indices) {
            // scrollOffset must be >= 0 (negative crashes). 0 = snap to top.
            listState.animateScrollToItem(
                index = selectedIndex,
                scrollOffset = 0
            )
        }
    }

    // Super simple list: ONLY 1,2,3... No names, no headings. For 70+ user.
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("channel_list")
    ) {
        itemsIndexed(
            items = channels,
            key = { _, channel -> channel.id }
        ) { index, channel ->
            val isSelected = (index == selectedIndex)
            val serialNumber = channel.serialNumber(index)

            ChannelNumberRow(
                serialNumber = serialNumber,
                isSelected = isSelected,
                onClick = { onChannelClick(index) }
            )
        }
    }
}

@Composable
private fun ChannelNumberRow(
    serialNumber: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Ultra simple: Black = selected, White = normal. Only BIG number.
    val backgroundColor = if (isSelected) Color.Black else Color.White
    val textColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp)
            .testTag("channel_row_$serialNumber"),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = serialNumber,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = textColor,
            modifier = Modifier.testTag("channel_number_$serialNumber")
        )
    }

    // Subtle divider between rows
    HorizontalDivider(
        thickness = 1.dp,
        color = if (isSelected) Color.Black else Color(0xFFEEEEEE)
    )
}
