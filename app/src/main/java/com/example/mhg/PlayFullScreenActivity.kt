package com.example.mhg

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.mhg.VO.ExerciseViewModel
import com.example.mhg.VO.UserViewModel
import com.example.mhg.databinding.ActivityPlayFullScreenBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class PlayFullScreenActivity : AppCompatActivity() {
        lateinit var binding: ActivityPlayFullScreenBinding
        private var videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        val viewModel: ExerciseViewModel by viewModels()
        private var simpleExoPlayer: SimpleExoPlayer? = null
        private var player : SimpleExoPlayer? = null
        private var playWhenReady = true
        private var currentWindow = 0
        private var playbackPosition = 0L
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            binding = ActivityPlayFullScreenBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // -----! landscape로 방향 설정 & 재생시간 받아오기 !-----
            videoUrl = intent.getStringExtra("video_url").toString()
            playbackPosition = intent.getLongExtra("current_position", 0L)
            initPlayer()


            // -----! 재생목록이 있을 경우 가져오기 시작 !-----
            val resourceList = listOf(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            )

            // -----! 재생목록이 있을 경우 가져오기 끝 !-----

            // -----! 원래 화면으로 돌아감 !-----
            val fullscreenButton = binding.pvFullScreen.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_fullscreen)
            fullscreenButton.setOnClickListener {
                onBackPressed()
            }

        }
        private fun fullScreen(fullScreenOption : Int) {
            window.decorView.systemUiVisibility = (
                    fullScreenOption
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN )
        }
        override fun onWindowFocusChanged(hasFocus : Boolean) {
            super.onWindowFocusChanged(hasFocus)
            if(hasFocus) fullScreen(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        private fun initPlayer(){
            simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
            binding.pvFullScreen.player = simpleExoPlayer
            buildMediaSource().let {
                simpleExoPlayer?.prepare(it)
            }
            simpleExoPlayer?.seekTo(playbackPosition)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        // -----! 동영상 재생목록에 넣기 !-----
        private fun buildMediaSource() : MediaSource {
            val dataSourceFactory = DefaultDataSourceFactory(this, "sample")
            val concatenatingMediaSource = ConcatenatingMediaSource()
            val resourceList = listOf(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            )
            resourceList.forEach { url ->
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(url))
                concatenatingMediaSource.addMediaSource(mediaSource)
            }
            return concatenatingMediaSource

        }
        // 일시중지
        override fun onResume() {
            super.onResume()
            simpleExoPlayer?.playWhenReady = true
        }

        override fun onStop() {
            super.onStop()
            simpleExoPlayer?.stop()
            simpleExoPlayer?.playWhenReady = false
        }

        override fun onDestroy() {
            super.onDestroy()
            simpleExoPlayer?.release()
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playbackPosition", simpleExoPlayer?.currentPosition ?: 0L)
        outState.putInt("currentWindow", simpleExoPlayer?.currentWindowIndex ?: 0)
        outState.putBoolean("playWhenReady", simpleExoPlayer?.playWhenReady ?: true)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        val currentPosition = simpleExoPlayer?.currentPosition
        val video_url = videoUrl
        val intent = Intent(this,PlayActivity::class.java)
        intent.putExtra("current_position", currentPosition)
        intent.putExtra("video_url", video_url)
        setResult(Activity.RESULT_OK, intent)

        simpleExoPlayer?.seekTo(currentPosition ?: 0)
        super.onBackPressed()
    }


}