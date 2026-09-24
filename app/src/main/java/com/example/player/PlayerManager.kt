package com.example.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlaybackState {
    object Idle : PlaybackState
    data class Loading(val message: String = "Starting TV...") : PlaybackState
    object Playing : PlaybackState
    object NoStream : PlaybackState
    data class Error(val message: String = "Channel unavailable") : PlaybackState
}

@OptIn(UnstableApi::class)
class PlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var currentChannel: Channel? = null
    private var currentCandidates: List<StreamCandidate> = emptyList()
    private var currentCandidateIndex: Int = 0

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var candidateTimeoutJob: Job? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun getOrCreatePlayer(): ExoPlayer {
        val existing = exoPlayer
        if (existing != null) return existing

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("VLC/3.0.18 LibVLC/3.0.18 (Android; TV)")
            .setConnectTimeoutMs(10000)
            .setReadTimeoutMs(12000)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
            .setDefaultRequestProperties(
                mapOf(
                    "Accept" to "*/*",
                    "Connection" to "keep-alive"
                )
            )

        val extractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory, extractorsFactory)

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                if (_playbackState.value !is PlaybackState.Playing &&
                                    _playbackState.value !is PlaybackState.Error) {
                                    _playbackState.value = PlaybackState.Loading()
                                }
                            }
                            Player.STATE_READY -> {
                                candidateTimeoutJob?.cancel()
                                candidateTimeoutJob = null
                                _playbackState.value = PlaybackState.Playing
                            }
                            Player.STATE_ENDED -> {
                                // Live stream loop or restart
                                seekToDefaultPosition()
                                prepare()
                                play()
                            }
                            Player.STATE_IDLE -> {
                                // Handled in onPlayerError
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.w("PlayerManager", "Stream error on candidate $currentCandidateIndex: ${error.message}")
                        candidateTimeoutJob?.cancel()
                        candidateTimeoutJob = null
                        tryNextCandidate()
                    }
                })
            }

        exoPlayer = player
        return player
    }

    fun playChannel(channel: Channel) {
        currentChannel = channel
        currentCandidates = StreamResolver.resolveCandidateStreams(channel)
        currentCandidateIndex = 0

        if (currentCandidates.isEmpty()) {
            exoPlayer?.stop()
            _playbackState.value = PlaybackState.NoStream
            return
        }

        tryPlayCurrentCandidate()
    }

    fun retryCurrentChannel() {
        currentChannel?.let { playChannel(it) }
    }

    private fun tryPlayCurrentCandidate() {
        if (currentCandidateIndex >= currentCandidates.size) {
            exoPlayer?.stop()
            _playbackState.value = PlaybackState.Error("Channel unavailable")
            return
        }

        val candidate = currentCandidates[currentCandidateIndex]
        _playbackState.value = PlaybackState.Loading()

        val player = getOrCreatePlayer()
        try {
            val builder = MediaItem.Builder().setUri(candidate.url)
            if (candidate.mimeType != null) {
                builder.setMimeType(candidate.mimeType)
            } else if (candidate.url.contains(".m3u8", ignoreCase = true)) {
                builder.setMimeType(MimeTypes.APPLICATION_M3U8)
            } else if (candidate.url.contains(".ts", ignoreCase = true)) {
                builder.setMimeType(MimeTypes.VIDEO_MP2T)
            }

            val mediaItem = builder.build()
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            // Guard against silent socket hangs with generous 10s window for ISP buffer
            candidateTimeoutJob?.cancel()
            candidateTimeoutJob = scope.launch {
                delay(10000)
                if (_playbackState.value !is PlaybackState.Playing) {
                    Log.i("PlayerManager", "Candidate $currentCandidateIndex (${candidate.label}) timed out, switching to next...")
                    tryNextCandidate()
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerManager", "Exception preparing candidate $currentCandidateIndex: ${e.message}")
            tryNextCandidate()
        }
    }

    private fun tryNextCandidate() {
        currentCandidateIndex++
        if (currentCandidateIndex < currentCandidates.size) {
            tryPlayCurrentCandidate()
        } else {
            exoPlayer?.stop()
            _playbackState.value = PlaybackState.Error("Channel unavailable")
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun stop() {
        candidateTimeoutJob?.cancel()
        candidateTimeoutJob = null
        exoPlayer?.stop()
        _playbackState.value = PlaybackState.Idle
    }

    fun release() {
        candidateTimeoutJob?.cancel()
        candidateTimeoutJob = null
        exoPlayer?.release()
        exoPlayer = null
        _playbackState.value = PlaybackState.Idle
    }
}
