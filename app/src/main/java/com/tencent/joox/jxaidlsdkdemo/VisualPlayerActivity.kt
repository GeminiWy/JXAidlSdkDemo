package com.tencent.joox.jxaidlsdkdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.tencent.joox.openapisdk.JXOpenApi
import com.tencent.joox.openapisdk.JXOpenApiCallBack
import com.tencent.joox.openapisdk.JXOpenApiEventListener
import com.tencent.joox.openapisdk.constract.*
import com.tencent.joox.openapisdk.model.JXOpenSongModel
import com.tencent.joox.openapisdk.util.JXOpenClient
import kotlinx.android.synthetic.main.activity_visual_player.*

class VisualPlayerActivity : AppCompatActivity(), View.OnClickListener, ServiceConnection {
    var jxOpenApi: JXOpenApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visual_player)
        btnActive.setOnClickListener(this)
        btnPlayPre.setOnClickListener(this)
        btnPlay.setOnClickListener(this)
        btnPause.setOnClickListener(this)
        btnPlayNext.setOnClickListener(this)
        getCurSongInfo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnActive -> {
                var isBindSuc = JXOpenClient.bindService(this, this)
                if (!isBindSuc) {
                    Toast.makeText(this@VisualPlayerActivity, "failed to connect", Toast.LENGTH_SHORT).show()
                }
            }
            btnPlayPre -> {
                jxOpenApi?.executeAsync(JXOpenApiAction.API_ACTION_SKIP_TO_PRE, null, object : JXOpenApiCallBack.Stub() {
                    override fun onResp(result: Bundle?) {
                        disposeResult(result)
                    }
                })
            }
            btnPause -> {
                jxOpenApi?.executeAsync(JXOpenApiAction.API_ACTION_STOP, null, object : JXOpenApiCallBack.Stub() {
                    override fun onResp(result: Bundle?) {
                        disposeResult(result)
                    }
                })
            }
            btnPlay -> {
                jxOpenApi?.executeAsync(JXOpenApiAction.API_ACTION_PLAY, null, object : JXOpenApiCallBack.Stub() {
                    override fun onResp(result: Bundle?) {
                        disposeResult(result)
                    }
                })
            }
            btnPlayNext -> {
                jxOpenApi?.executeAsync(JXOpenApiAction.API_ACTION_SKIP_TO_NEXT, null, object : JXOpenApiCallBack.Stub() {
                    override fun onResp(result: Bundle?) {
                        disposeResult(result)
                    }
                })
            }
            getCurSongInfo -> {
                var respBundle = jxOpenApi?.execute(JXOpenApiAction.API_ACTION_CUR_SONG_INFO, null)
                disposeResult(respBundle)
                var songInfostr = respBundle?.getString(JXOpenApiRespParamsKey.API_RESP_KEY_DATA)
                var songInfo = Gson().fromJson(songInfostr, JXOpenSongModel::class.java)
                tv_song_info.text = songInfostr
            }
        }
    }


    private fun disposeResult(result: Bundle?) {
        var resultCode = result?.get(JXOpenApiRespParamsKey.API_RESP_KEY_CODE)
        var resultMessage = result?.get(JXOpenApiRespParamsKey.API_RESP_KEY_ERROR_MESSAGE)

        runOnUiThread {
            when (resultCode) {
                JXOpenApiErrorCode.ERROR_AD_CANNOT_TOUCH -> {
                    //正在播放广告无法操作
                    Toast.makeText(this, "正在播放广告无法操作" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_FREE_CANNOT_TOUCH -> {
                    //免费用户无法操作
                    Toast.makeText(this, "免费用户无法操作" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_NEED_USER_AUTHENTICATION -> {
                    //用户需要授权
                    Toast.makeText(this, "用户需要授权" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_NOT_LOGIN -> {
                    //需要登陆
                    Toast.makeText(this, "需要登陆" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_OK -> {
                    //成功
                    Toast.makeText(this, "成功" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_PARAMS -> {
                    //参数错误
                    Toast.makeText(this, "参数错误" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_UNKNOWN -> {
                    //未知
                    Toast.makeText(this, "未知" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_PLAY_LIST_EMPTY -> {
                    //未知
                    Toast.makeText(this, "列表为空" + resultMessage, Toast.LENGTH_SHORT).show()
                }
                JXOpenApiErrorCode.ERROR_NET -> {
                    //未知
                    Toast.makeText(this, "无网络" + resultMessage, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun disposeRegisterAppResult(result: Bundle?) {
        var resultCode = result?.get(JXOpenApiRespParamsKey.API_RESP_KEY_CODE)
        var resultMessage = result?.get(JXOpenApiRespParamsKey.API_RESP_KEY_ERROR_MESSAGE)
        when (resultCode) {
            JXOpenApiErrorCode.ERROR_NEED_USER_AUTHENTICATION -> {
                //用户需要授权
                Toast.makeText(this, "授权失败" + resultMessage, Toast.LENGTH_SHORT).show()
            }
            JXOpenApiErrorCode.ERROR_NOT_LOGIN -> {
                //需要登陆
                Toast.makeText(this, "需要登陆" + resultMessage, Toast.LENGTH_SHORT).show()
            }
            JXOpenApiErrorCode.ERROR_OK -> {
                //成功
                Toast.makeText(this, "授权成功" + resultMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        //绑定成功
        Toast.makeText(this@VisualPlayerActivity, "suc to connect", Toast.LENGTH_SHORT).show()
        jxOpenApi = JXOpenApi.Stub.asInterface(service)
        var resultParam = jxOpenApi?.registerApp("jx_13110539617")
        disposeRegisterAppResult(resultParam)
        jxOpenApi?.register(JXOpenApiEvent.EVENT_PLAY_SONG_CHANGED, eventListener)
        jxOpenApi?.register(JXOpenApiEvent.EVENT_PLAY_STATE_CHANGED, eventListener)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        jxOpenApi = null
    }

    private val eventListener = object : JXOpenApiEventListener.Stub() {
        override fun onEvent(event: String, extra: Bundle) {
            when (event) {
                JXOpenApiEvent.EVENT_PLAY_SONG_CHANGED -> {
                    disposePlaySongChanged()
                }
                JXOpenApiEvent.EVENT_PLAY_STATE_CHANGED -> {
                    disposePlayStateChanged(extra)
                }
            }
        }

    }

    private fun disposePlayStateChanged(extra: Bundle?) {
        runOnUiThread {
            extra?.let {
                var playState = it.get(JXOpenApiEventResp.EVENT_RESP_PLAY_STATE)
                when (playState) {
                    JXOpenApiEventResp.PlayState.PLAY_STATE_PASUED -> {
                        playPauseState.text = "暂停中"
                    }
                    JXOpenApiEventResp.PlayState.PLAY_STATE_PLAYING -> {
                        playPauseState.text = "播放中"
                    }
                    JXOpenApiEventResp.PlayState.PLAY_STATE_BUFFERING -> {
                        playPauseState.text = "缓冲中"
                    }
                }
            }
        }
    }

    private fun disposePlaySongChanged() {
        runOnUiThread {
            var respBundle = jxOpenApi?.execute(JXOpenApiAction.API_ACTION_CUR_SONG_INFO, null)
            var songInfostr = respBundle?.getString(JXOpenApiRespParamsKey.API_RESP_KEY_DATA)
            var songInfo = Gson().fromJson(songInfostr, JXOpenSongModel::class.java)
            txtSongInfos.text = songInfo.songName
            txtAlbum.text = songInfo.singerName
            var playTimeRespBundle = jxOpenApi?.execute(JXOpenApiAction.API_ACTION_TOTAL_PLAY_TIME, null)
            txtPlayTime.text = ((playTimeRespBundle?.getLong(JXOpenApiRespParamsKey.API_RESP_KEY_DATA)
                    ?: 0) / 1000).toString()
            Glide.with(this).load(songInfo.albumUrl).into(SongPic)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            jxOpenApi?.unRegister(JXOpenApiEvent.EVENT_PLAY_SONG_CHANGED,eventListener)
            jxOpenApi?.unRegister(JXOpenApiEvent.EVENT_PLAY_STATE_CHANGED,eventListener)
        } catch (ignored: Throwable) {
        }
        unbindService(this)
    }
}
