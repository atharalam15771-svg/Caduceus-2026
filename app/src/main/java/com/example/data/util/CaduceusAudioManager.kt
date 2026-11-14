package com.example.data.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import com.example.R
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

object CaduceusAudioManager {
    private const val PREFS_NAME = "caduceus_audio_prefs"
    private const val KEY_AUDIO_ENABLED = "audio_enabled"

    // Effective BGM gain boosted ~19% from 0.08f -> 0.095f while staying safely below clipping
    internal const val BGM_VOLUME = 0.095f
    internal const val DUCKED_BGM_VOLUME = 0.035f
    private const val SFX_TAP_VOLUME = 0.16f
    private const val SFX_ALERT_VOLUME = 0.25f
    private const val SFX_REVEAL_VOLUME = 0.20f

    // Single serial executor prevents overlapping MediaPlayer creation and UI-thread buffer stalls
    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "CaduceusAudioThread").apply { isDaemon = true }
    }

    @Volatile
    private var mediaPlayer: MediaPlayer? = null

    @Volatile
    private var isPlayerPrepared = false

    @Volatile
    private var isDesiredPlaying = false

    @Volatile
    private var pausedByTransientFocusLoss = false

    @Volatile
    private var currentDucked = false

    @Volatile
    private var audioManager: AudioManager? = null

    @Volatile
    private var focusRequest: AudioFocusRequest? = null

    @Volatile
    private var soundPool: SoundPool? = null

    @Volatile
    private var tapSoundId: Int = 0

    @Volatile
    private var alertSoundId: Int = 0

    @Volatile
    private var revealSoundId: Int = 0

    private val loadedSamples = ConcurrentHashMap.newKeySet<Int>()
    private val isSfxInitialized = AtomicBoolean(false)

    private val musicAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        audioExecutor.execute {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    currentDucked = false
                    val player = mediaPlayer
                    if (player != null && isPlayerPrepared) {
                        if (pausedByTransientFocusLoss && isDesiredPlaying) {
                            pausedByTransientFocusLoss = false
                            if (!safeIsPlaying(player)) {
                                fadeAndStartInternal(player)
                            }
                        } else {
                            safeSetVolume(player, BGM_VOLUME)
                        }
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    currentDucked = true
                    mediaPlayer?.let { if (isPlayerPrepared) safeSetVolume(it, DUCKED_BGM_VOLUME) }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    val player = mediaPlayer
                    if (player != null && isPlayerPrepared && safeIsPlaying(player)) {
                        pausedByTransientFocusLoss = true
                        safePause(player)
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    pausedByTransientFocusLoss = false
                    currentDucked = false
                    mediaPlayer?.let { if (isPlayerPrepared && safeIsPlaying(it)) safePause(it) }
                }
            }
        }
    }

    fun isAudioEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUDIO_ENABLED, true)
    }

    fun toggleAudio(context: Context): Boolean {
        val next = !isAudioEnabled(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUDIO_ENABLED, next)
            .apply()
        if (next) {
            startBgm(context.applicationContext)
        } else {
            pauseBgm()
        }
        return next
    }

    fun initIfNeeded(context: Context) {
        if (!isSfxInitialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        audioExecutor.execute {
            try {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val pool = SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(attrs)
                    .build()

                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        loadedSamples.add(sampleId)
                    }
                }

                tapSoundId = pool.load(appContext, R.raw.sfx_tap, 1)
                alertSoundId = pool.load(appContext, R.raw.sfx_alert, 1)
                revealSoundId = pool.load(appContext, R.raw.sfx_reveal, 1)
                soundPool = pool
            } catch (_: Exception) {
                isSfxInitialized.set(false)
            }
        }
    }

    fun startBgm(context: Context) {
        initIfNeeded(context)
        val appContext = context.applicationContext
        if (!isAudioEnabled(appContext)) return
        isDesiredPlaying = true
        audioExecutor.execute {
            startBgmInternal(appContext)
        }
    }

    fun pauseBgm() {
        isDesiredPlaying = false
        pausedByTransientFocusLoss = false
        audioExecutor.execute {
            val player = mediaPlayer ?: return@execute
            if (isPlayerPrepared && safeIsPlaying(player)) {
                safeSetVolume(player, 0.01f)
                safePause(player)
            }
            abandonAudioFocusInternal()
        }
    }

    fun playTap(context: Context) {
        if (!isAudioEnabled(context)) return
        initIfNeeded(context)
        val id = tapSoundId
        if (id != 0 && loadedSamples.contains(id)) {
            soundPool?.play(id, SFX_TAP_VOLUME, SFX_TAP_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playAlert(context: Context) {
        if (!isAudioEnabled(context)) return
        initIfNeeded(context)
        val id = alertSoundId
        if (id != 0 && loadedSamples.contains(id)) {
            soundPool?.play(id, SFX_ALERT_VOLUME, SFX_ALERT_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playReveal(context: Context) {
        if (!isAudioEnabled(context)) return
        initIfNeeded(context)
        val id = revealSoundId
        if (id != 0 && loadedSamples.contains(id)) {
            soundPool?.play(id, SFX_REVEAL_VOLUME, SFX_REVEAL_VOLUME, 1, 0, 1.0f)
        }
    }

    fun release() {
        isDesiredPlaying = false
        pausedByTransientFocusLoss = false
        audioExecutor.execute {
            releaseMediaPlayerInternal()
            abandonAudioFocusInternal()
            try {
                soundPool?.release()
            } catch (_: Exception) {}
            soundPool = null
            loadedSamples.clear()
            tapSoundId = 0
            alertSoundId = 0
            revealSoundId = 0
            isSfxInitialized.set(false)
        }
    }

    private fun startBgmInternal(appContext: Context) {
        if (!isDesiredPlaying || !isAudioEnabled(appContext)) return

        val existing = mediaPlayer
        if (existing != null && isPlayerPrepared) {
            if (!safeIsPlaying(existing)) {
                if (requestAudioFocusInternal(appContext)) {
                    fadeAndStartInternal(existing)
                }
            } else {
                val target = if (currentDucked) DUCKED_BGM_VOLUME else BGM_VOLUME
                safeSetVolume(existing, target)
            }
            return
        }

        // Guarantee single instance: dispose any partially initialized/stale player first
        releaseMediaPlayerInternal()

        val player = createPreparedPlayer(appContext) ?: return
        mediaPlayer = player
        isPlayerPrepared = true

        if (isDesiredPlaying && isAudioEnabled(appContext) && requestAudioFocusInternal(appContext)) {
            fadeAndStartInternal(player)
        }
    }

    private fun createPreparedPlayer(appContext: Context): MediaPlayer? {
        return try {
            val afd = appContext.resources.openRawResourceFd(R.raw.caduceus_bgm) ?: return null
            val mp = MediaPlayer()
            try {
                mp.setAudioAttributes(musicAttributes)
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            } finally {
                try {
                    afd.close()
                } catch (_: Exception) {}
            }
            mp.isLooping = true
            mp.setOnErrorListener { _, _, _ ->
                audioExecutor.execute {
                    releaseMediaPlayerInternal()
                    if (isDesiredPlaying && isAudioEnabled(appContext)) {
                        startBgmInternal(appContext)
                    }
                }
                true
            }
            mp.prepare()
            mp
        } catch (_: Exception) {
            null
        }
    }

    private fun fadeAndStartInternal(player: MediaPlayer) {
        try {
            val targetVolume = if (currentDucked) DUCKED_BGM_VOLUME else BGM_VOLUME
            safeSetVolume(player, 0.005f)
            player.start()
            // Gentle 160ms soft fade-in prevents speaker click/pop when starting or resuming
            val steps = 8
            for (i in 1..steps) {
                if (!isDesiredPlaying) break
                val v = 0.005f + (targetVolume - 0.005f) * (i / steps.toFloat())
                safeSetVolume(player, v)
                try {
                    Thread.sleep(20)
                } catch (_: InterruptedException) {
                    break
                }
            }
        } catch (_: Exception) {}
    }

    private fun requestAudioFocusInternal(appContext: Context): Boolean {
        return try {
            val am = audioManager ?: (appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager).also {
                audioManager = it
            } ?: return true

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = focusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(musicAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build()
                    .also { focusRequest = it }
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } catch (_: Exception) {
            true
        }
    }

    private fun abandonAudioFocusInternal() {
        try {
            val am = audioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(focusChangeListener)
            }
        } catch (_: Exception) {}
    }

    private fun releaseMediaPlayerInternal() {
        val player = mediaPlayer
        mediaPlayer = null
        isPlayerPrepared = false
        if (player != null) {
            try {
                if (safeIsPlaying(player)) {
                    player.stop()
                }
            } catch (_: Exception) {}
            try {
                player.reset()
            } catch (_: Exception) {}
            try {
                player.release()
            } catch (_: Exception) {}
        }
    }

    private fun safeIsPlaying(player: MediaPlayer): Boolean {
        return try {
            player.isPlaying
        } catch (_: Exception) {
            false
        }
    }

    private fun safeSetVolume(player: MediaPlayer, volume: Float) {
        try {
            val clamped = volume.coerceIn(0f, 1f)
            player.setVolume(clamped, clamped)
        } catch (_: Exception) {}
    }

    private fun safePause(player: MediaPlayer) {
        try {
            player.pause()
        } catch (_: Exception) {}
    }
}
