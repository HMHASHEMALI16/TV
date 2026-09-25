package com.example.player

import com.example.data.Channel

data class StreamCandidate(
    val url: String,
    val mimeType: String? = null,
    val label: String = ""
)

object StreamResolver {

    // Direct online streams for channels when outside the local 10.6.6.2 network or as fallbacks
    private val directStreams: Map<String, List<String>> = mapOf(
        "channel_31" to listOf(
            "https://boishakhi.sonarbanglatv.com/boishakhi/boishakhitv/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_32" to listOf(
            "http://103.99.249.139/anandatv/index.m3u8",
            "https://app.ncare.live/c3VydmVyX8RpbEU9Mi8xNy8yMDE0GIDU6RgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcGVMZEJCTEFWeVN3PTOmdFsaWRtaW51aiPhnPTI2/anandatv.stream/live-orgin/anandatv.stream/playlist.m3u8"
        ),
        "channel_33" to listOf(
            "https://amg13737-amg13737c1-amgplt0016.playout.now3.amagi.tv/playlist/amg13737-amg13737c1-amgplt0016/playlist.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_34" to listOf(
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8",
            "https://tvsen5.aynaott.com/atnbangla/index.m3u8"
        ),
        "channel_35" to listOf(
            "https://app.ncare.live/c3VydmVyX8RpbEU9Mi8xNy8yMDE0GIDU6RgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcGVMZEJCTEFWeVN3PTOmdFsaWRtaW51aiPhnPTI/atnmusic.stream/playlist.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_36" to listOf(
            "http://103.190.133.68:1935/news21live/live/playlist.m3u8",
            "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/ruposhibangla.stream/playlist.m3u8"
        ),
        "channel_37" to listOf(
            "https://tvsen5.aynaott.com/banglavision/index.m3u8",
            "https://boishakhi.sonarbanglatv.com/boishakhi/boishakhitv/index.m3u8"
        ),
        "channel_38" to listOf(
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8",
            "https://tvsen5.aynaott.com/banglavision/index.m3u8"
        ),
        "channel_39" to listOf(
            "https://boishakhi.sonarbanglatv.com/boishakhi/boishakhitv/index.m3u8",
            "https://tvsen5.aynaott.com/banglavision/index.m3u8"
        ),
        "channel_40" to listOf(
            "https://boishakhi.sonarbanglatv.com/boishakhi/boishakhitv/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_41" to listOf(
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8",
            "https://tplay.live/out/bangladesh/ekhontv.index.m3u8"
        ),
        "channel_42" to listOf(
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8",
            "https://boishakhi.sonarbanglatv.com/boishakhi/boishakhitv/index.m3u8"
        ),
        "channel_43" to listOf(
            "https://tvsen5.aynaott.com/banglavision/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_44" to listOf(
            "https://app.ncare.live/c3VydmVyX8RpbEU9Mi8xNy8yMDE0GIDU6RgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcGVMZEJCTEFWeVN3PTOmdFsaWRtaW51aiPhnPTI2/channels.stream/live-orgin/channels.stream/playlist.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_45" to listOf(
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8",
            "https://tplay.live/out/bangladesh/ekhontv.index.m3u8"
        ),
        "channel_46" to listOf(
            "https://tvsen6.aynaott.com/6xyZ3N4oHv2KBJdB6W4p/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_47" to listOf(
            "https://deshitv.deshitv24.net/live/myStream/playlist.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_48" to listOf(
            "https://tvsen6.aynaott.com/6xyZ3N4oHv2KBJdB6W4p/index.m3u8",
            "https://tvsen5.aynaott.com/banglavision/index.m3u8"
        ),
        "channel_49" to listOf(
            "https://tplay.live/out/bangladesh/ekhontv.index.m3u8",
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8"
        ),
        "channel_50" to listOf(
            "https://ekusheyserver.com/etvlivesn.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_51" to listOf(
            "https://server.livelegitpro.in/globalpunjab/globalpunjab/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_52" to listOf(
            "http://tvn1.chowdhury-shaheb.com/gazitv/index.m3u8",
            "https://app.ncare.live/c3VydmVyX8RpbEU9Mi8xNy8yMDE0GIDU6RgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcGVMZEJCTEFWeVN3PTOmdFsaWRtaW51aiPhnPTI2/gazibdz.stream/live-orgin/gazibdz.stream/playlist.m3u8"
        ),
        "channel_53" to listOf(
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8",
            "https://tplay.live/out/bangladesh/ekhontv.index.m3u8"
        ),
        "channel_54" to listOf(
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8",
            "https://tplay.live/out/bangladesh/ekhontv.index.m3u8"
        ),
        "channel_55" to listOf(
            "http://tvsen5.aynascope.net/maasrangatv/index.m3u8",
            "https://tvsen5.aynaott.com/maasrangatv/index.m3u8"
        ),
        "channel_56" to listOf(
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8",
            "https://boishakhi.sonarbanglatv.com/boishakhi/boishakhitv/index.m3u8"
        ),
        "channel_57" to listOf(
            "http://alvetv.com/moviebanglatv/8080/index.m3u8",
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8"
        ),
        "channel_58" to listOf(
            "https://app.ncare.live/c3VydmVyX8RpbEU9Mi8xNy8yMDE0GIDU6RgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcGVMZEJCTEFWeVN3PTOmdFsaWRtaW51aiPhnPTI2/mytv-up-off.stream/live-orgin/mytv-up-off.stream/playlist.m3u8"
        ),
        "channel_59" to listOf(
            "https://tvsen5.aynaott.com/banglavision/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_60" to listOf(
            "http://103.190.133.68:1935/news21live/live/playlist.m3u8",
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8"
        ),
        "channel_61" to listOf(
            "https://tvsen6.aynaott.com/Epm7WrFa/index.m3u8",
            "https://boishakhi.sonarbanglatv.com/boishakhi/boishakhitv/index.m3u8"
        ),
        "channel_62" to listOf(
            "https://tvsen5.aynaott.com/xV4jEKf3D9zc/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_63" to listOf(
            "http://tvsen5.aynascope.net/RtvHD/index.m3u8",
            "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/rtv-sg.stream/index.m3u8"
        ),
        "channel_64" to listOf(
            "https://tvsen5.aynaott.com/banglavision/index.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_65" to listOf(
            "http://tvn3.chowdhury-shaheb.com/dbc/index.m3u8",
            "https://tplay.live/out/bangladesh/ekhontv.index.m3u8"
        ),
        "channel_66" to listOf(
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_67" to listOf(
            "http://103.165.93.31:8095/colorsBangla/index.m3u8",
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8"
        ),
        "channel_68" to listOf(
            "http://103.165.93.31:8095/colorsBangla/index.m3u8",
            "https://d1g8wgjurz8via.cloudfront.net/bpk-tv/ColorsHD/default/ColorsHD.m3u8"
        ),
        "channel_69" to listOf(
            "https://live-bangla.akamaized.net/liveabr/playlist.m3u8",
            "https://mumt07.tangotv.in/zHjX9OFlENTERR10BANGLA/index.m3u8"
        ),
        "channel_70" to listOf(
            "https://da86m1sqpm3o0.cloudfront.net/28072023/smil:starjalsha.smil/chunklist_b1928000.m3u8",
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8"
        ),
        "channel_71" to listOf(
            "https://server.thelegitpro.in/rongeentv/rongeentv/index.m3u8",
            "https://mumt05.tangotv.in/87NeALx2RONGEENTV/index.m3u8"
        ),
        "channel_72" to listOf(
            "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/ruposhibangla.stream/playlist.m3u8"
        ),
        "channel_73" to listOf(
            "http://103.165.93.31:8095/sonyAath/index.m3u8",
            "https://cloudplay-sonyliv.pages.dev/aath.m3u8"
        ),
        "channel_74" to listOf(
            "https://da86m1sqpm3o0.cloudfront.net/28072023/smil:starjalsha.smil/chunklist_b1928000.m3u8",
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8"
        ),
        "channel_75" to listOf(
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8",
            "http://tvsen5.aynascope.net/atnbangla/index.m3u8"
        ),
        "channel_76" to listOf(
            "https://d2dsoyvkr33m05.cloudfront.net/index_4.m3u8",
            "https://tvsen6.aynaott.com/DpPnXP9r/index.m3u8"
        ),
        "channel_77" to listOf(
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8"
        ),
        "channel_78" to listOf(
            "https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/ZeeBanglaHD.m3u8"
        )
    )

    // NOTE: Many entries below currently reuse the same ATN-Bangla / Zee fallback URL
    // for different channels. That means channel X may play channel Y's content when
    // off the ISP (10.6.6.2) network. Replace each list with that channel's REAL
    // official HLS URL before public release, or leave only ISP + error (no fake fallback).
    // Universal fallback DISABLED on purpose: showing the SAME Zee stream on every
    // failed channel confuses 70+ users more than a clear "Not available / RETRY" screen.
    // If you want a last-resort stream, re-enable UNIVERSAL_FALLBACK_STREAM below.

    /**
     * Resolves a prioritized list of candidate stream configurations for playback.
     * Order of preference:
     * 1. ISP local network stream (http://10.6.6.2/play.php?id={channel.id}) in Auto-detect mode
     * 2. ISP local network stream with explicit HLS M3U8 MIME type
     * 3. ISP local network stream with explicit MPEG-TS MIME type
     * 4. Direct online HLS stream backup (per-channel, if correct URL is configured)
     * No universal fallback: wrong content is worse than a clear error for elderly users.
     */
    fun resolveCandidateStreams(channel: Channel): List<StreamCandidate> {
        val list = mutableListOf<StreamCandidate>()
        val primaryIptvUrl = if (channel.streamUrl.isNotBlank()) {
            channel.streamUrl.trim()
        } else {
            "http://10.6.6.2/play.php?id=${channel.id}"
        }

        // 1. ISP local stream: Auto-detect extractor (sniffs MPEG-TS, FLV, MP4, etc., and follows redirects)
        list.add(StreamCandidate(url = primaryIptvUrl, mimeType = null, label = "ISP Direct"))

        // 2. ISP local stream: Explicit HLS in case play.php delivers HLS playlist without m3u8 header
        list.add(StreamCandidate(url = primaryIptvUrl, mimeType = "application/x-mpegURL", label = "ISP HLS"))

        // 3. ISP local stream: Explicit MPEG-TS in case it is a raw transport stream
        list.add(StreamCandidate(url = primaryIptvUrl, mimeType = "video/mp2t", label = "ISP MPEG-TS"))

        // 4. Online direct streams fallback (if user is off ISP network)
        directStreams[channel.id]?.forEach { onlineUrl ->
            list.add(StreamCandidate(url = onlineUrl, mimeType = "application/x-mpegURL", label = "Online Backup"))
        }

        return list
    }

    fun resolveCandidateUrls(channel: Channel): List<String> {
        return resolveCandidateStreams(channel).map { it.url }.distinct()
    }
}
