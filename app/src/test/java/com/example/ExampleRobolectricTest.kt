package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Channel
import com.example.data.ChannelRepository
import com.example.player.StreamResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Live TV", appName)
    }

    @Test
    fun `verify channels list is populated`() {
        val channels = ChannelRepository.getHardcodedChannels()
        assertEquals(48, channels.size)
        assertEquals("71", channels[0].name)
        assertEquals("Zee Bangla HD", channels[47].name)
    }

    @Test
    fun `verify serial number formatting`() {
        val channel = Channel("channel_31", "71", "http://10.6.6.2/play.php?id=channel_31")
        assertEquals("1", channel.serialNumber(0))
        assertEquals("10", channel.serialNumber(9))
        assertEquals("21", channel.serialNumber(20))
    }
}
