package com.example.myapplication
import android.Manifest
import com.example.myapplication.R.drawable.ic_play_arrow_white_24dp
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import co.metalab.asyncawait.async
import com.mtechviral.mplaylib.MusicFinder
import com.mtechviral.mplaylib.R.drawable
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import kotlin.coroutines.coroutineContext
import java.util.*

class MainActivity : AppCompatActivity() {


    var albumArt: ImageView? = null

    var playButton: ImageButton? = null
    var shuffleButton: ImageButton? = null

    var songTitle: TextView? = null
    var songArtist: TextView? = null

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE //app gets read permissons to /storage or sd card
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        } else {
            createPlayer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { //if permission granted then create player
            createPlayer()
        } else {
            longToast("You do not have permission. Goodbye now.") //if not end program
            finish()
        }
    }

    private fun createPlayer() {


            //Because using musicPlayer library variables have to be the same
            val songFinder = MusicFinder(contentResolver) //songFinder same as music class
            songFinder.prepare() //removed async
            songFinder.allSongs //find all the songs
        var songsJob: MutableList<MusicFinder.Song> = songFinder.allSongs

        //In async it tries to find all the songs


        GlobalScope.launch(Dispatchers.IO) {
            var songs = songsJob //load the app ui and wait for something to happen
        }


        val playerUI = object : AnkoComponent<MainActivity> { //Anko UI creation
            override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {

                relativeLayout {
                    backgroundColor = Color.BLACK

                    albumArt = imageView {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }.lparams(matchParent, matchParent)

                    verticalLayout {
                        backgroundColor = Color.parseColor("#99000000")
                        songTitle = textView {
                            textColor = Color.WHITE
                            typeface = Typeface.DEFAULT_BOLD
                            textSize = 18f
                        }

                        songArtist = textView {
                            textColor = Color.WHITE

                        }

                        linearLayout {
                            playButton = imageButton {
                                imageResource = ic_play_arrow_white_24dp
                                onClick {
                                    playOrPause()
                                }
                            }.lparams(0, wrapContent, 0.5f)

                            shuffleButton = imageButton {
                                imageResource = R.drawable.ic_shuffle_black_24dp
                                onClick {
                                    playShuffle()
                                }
                            }.lparams(0, wrapContent, 0.5f)
                        }.lparams(matchParent, wrapContent) {
                            topMargin = dip(5)
                        }


                    }.lparams(matchParent, wrapContent) {
                        alignParentBottom()
                    }
                }

            }

            fun playShuffle() {
                songsJob.shuffle() //dont need collections
                val song = songsJob[0] //Takes first song
                mediaPlayer?.reset() //if not null then reset it
                mediaPlayer = MediaPlayer.create(ctx, song.uri)
                mediaPlayer?.setOnCompletionListener {
                    //When song is done
                    playShuffle()
                }
                albumArt?.imageURI = song.albumArt
                songTitle?.text = song.title
                songArtist?.text = song.artist
                mediaPlayer?.start() //Start player
                playButton?.imageResource = R.drawable.ic_pause_black_24dp
            }

            fun playOrPause() {
                var songPlaying: Boolean? = mediaPlayer?.isPlaying

                if (songPlaying == true) {
                    mediaPlayer?.pause()
                    playButton?.imageResource = ic_play_arrow_white_24dp
                } else {
                    mediaPlayer?.start()
                    playButton?.imageResource = R.drawable.ic_pause_black_24dp
                }
            }
        }
        playerUI.setContentView(this@MainActivity) //Set UI
        playerUI.playShuffle()


    }
    override fun onDestroy() { //When its done stop player
        mediaPlayer?.release()
        super.onDestroy()
    }

}


