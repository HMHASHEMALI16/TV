package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.example.data.Channel
import com.example.data.ChannelRepository
import com.example.player.PlayerManager
import com.example.ui.HomeScreen
import com.example.ui.PlayerScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class AppState {
    CHANNEL_LIST,
    VIDEO_PLAYER
}

class MainActivity : ComponentActivity() {

    private lateinit var channelRepository: ChannelRepository
    private lateinit var playerManager: PlayerManager

    private var currentAppState by mutableStateOf(AppState.CHANNEL_LIST)
    private var selectedIndex by mutableIntStateOf(0)
    private var channelList by mutableStateOf<List<Channel>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Restore last-watched channel for 70+ user: reopen where they left off.
        val prefs = getSharedPreferences("sn_tv_prefs", MODE_PRIVATE)
        selectedIndex = prefs.getInt("last_channel_index", 0)

        channelRepository = ChannelRepository(applicationContext)
        playerManager = PlayerManager(applicationContext)

        // Initialize channels synchronously with default 48 channels first
        channelList = ChannelRepository.getHardcodedChannels()

        // Then asynchronously check cache and remote API
        lifecycleScope.launch {
            channelRepository.initialize()
            channelRepository.channels.collect { channels ->
                if (channels.isNotEmpty()) {
                    channelList = channels
                }
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val playbackState by playerManager.playbackState.collectAsState()

                    // Handle Back action gracefully
                    BackHandler(enabled = currentAppState == AppState.VIDEO_PLAYER) {
                        returnToChannelList()
                    }

                    when (currentAppState) {
                        AppState.CHANNEL_LIST -> {
                            HomeScreen(
                                channels = channelList,
                                selectedIndex = selectedIndex,
                                onChannelClick = { index ->
                                    playChannelAtIndex(index)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .safeDrawingPadding()
                            )
                        }

                        AppState.VIDEO_PLAYER -> {
                            val activeChannel = channelList.getOrNull(selectedIndex)
                                ?: Channel("unknown", "TV", "")

                            PlayerScreen(
                                channel = activeChannel,
                                channelIndex = selectedIndex,
                                exoPlayer = playerManager.getOrCreatePlayer(),
                                playbackState = playbackState,
                                onBack = {
                                    returnToChannelList()
                                },
                                onPrevChannel = {
                                    changeChannel(isNext = false)
                                },
                                onNextChannel = {
                                    changeChannel(isNext = true)
                                },
                                onRetry = {
                                    playerManager.retryCurrentChannel()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun playChannelAtIndex(index: Int) {
        if (channelList.isEmpty()) return
        selectedIndex = index.coerceIn(0, channelList.size - 1)
        // Remember for next app start (elderly user should not lose place)
        getSharedPreferences("sn_tv_prefs", MODE_PRIVATE)
            .edit().putInt("last_channel_index", selectedIndex).apply()
        val channel = channelList[selectedIndex]
        currentAppState = AppState.VIDEO_PLAYER
        playerManager.playChannel(channel)
    }

    private fun returnToChannelList() {
        playerManager.stop()
        currentAppState = AppState.CHANNEL_LIST
    }

    private fun changeChannel(isNext: Boolean) {
        if (channelList.isEmpty()) return
        val count = channelList.size
        selectedIndex = if (isNext) {
            (selectedIndex + 1) % count
        } else {
            (selectedIndex - 1 + count) % count
        }
        getSharedPreferences("sn_tv_prefs", MODE_PRIVATE)
            .edit().putInt("last_channel_index", selectedIndex).apply()
        val nextChannel = channelList[selectedIndex]
        playerManager.playChannel(nextChannel)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (channelList.isEmpty()) {
            return super.onKeyDown(keyCode, event)
        }
        val count = channelList.size

        when (currentAppState) {
            AppState.CHANNEL_LIST -> {
                when (keyCode) {
                    // UP or LEFT or CHANNEL_UP -> Previous Channel (wrapping 01 -> 48)
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_CHANNEL_UP,
                    KeyEvent.KEYCODE_PAGE_UP,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        selectedIndex = (selectedIndex - 1 + count) % count
                        return true
                    }

                    // DOWN or RIGHT or CHANNEL_DOWN -> Next Channel (wrapping 48 -> 01)
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_CHANNEL_DOWN,
                    KeyEvent.KEYCODE_PAGE_DOWN,
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        selectedIndex = (selectedIndex + 1) % count
                        return true
                    }

                    // OK / ENTER -> Play selected channel immediately
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                        playChannelAtIndex(selectedIndex)
                        return true
                    }
                }
            }

            AppState.VIDEO_PLAYER -> {
                when (keyCode) {
                    // UP or LEFT or CHANNEL_UP -> Switch to Previous Channel immediately
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_CHANNEL_UP,
                    KeyEvent.KEYCODE_PAGE_UP,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        changeChannel(isNext = false)
                        return true
                    }

                    // DOWN or RIGHT or CHANNEL_DOWN -> Switch to Next Channel immediately
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_CHANNEL_DOWN,
                    KeyEvent.KEYCODE_PAGE_DOWN,
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        changeChannel(isNext = true)
                        return true
                    }

                    // BACK / ESCAPE -> Return to Channel list, preserving selected index
                    KeyEvent.KEYCODE_BACK,
                    KeyEvent.KEYCODE_ESCAPE -> {
                        returnToChannelList()
                        return true
                    }
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        if (currentAppState == AppState.VIDEO_PLAYER) {
            playerManager.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentAppState == AppState.VIDEO_PLAYER) {
            playerManager.resume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}
