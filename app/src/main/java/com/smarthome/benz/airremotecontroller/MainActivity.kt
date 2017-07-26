package com.smarthome.benz.airremotecontroller

import android.content.Context
import android.content.SharedPreferences
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.jetbrains.anko.toast
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {
    val url = "http://www.benzweb.tech:28234"
    val url_shut_down = url + "/air_ctl_close"
    val url_ircmd = url + "/ircmd"

    var mQueue: RequestQueue? = null
    var mWorkMode = 0
    var mWindSpeed = 0
    var mDegree = 26
    var mLastCmd: AirCmd = AirCmd(0, 0, 0, 26)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    override fun onClick(v: View?) {
        toast("clicked")
        when (v!!) {
            btn_work_mode -> {
                mWorkMode += 1
                mWorkMode %= 2
                refreshViewParams(1)
                doSendCmd()
            }
            btn_degree_down -> {
                mDegree -= 1
                when {
                    mDegree < 23 -> {mDegree = 28}
                    mDegree > 28 -> {mDegree = 23}
                }
                refreshViewParams(2)
                doSendCmd()
            }
            btn_degree_up -> {
                mDegree += 1
                when {
                    mDegree < 23 -> {mDegree = 28}
                    mDegree > 28 -> {mDegree = 23}
                }
                refreshViewParams(2)
                doSendCmd()
            }
            btn_wind_speed -> {
                mWindSpeed += 1
                mWindSpeed %= 4
                refreshViewParams(4)
                doSendCmd()
            }
            btn_send_cmd -> {
                doSendCmd()
            }
            btn_shut_down -> {
                doShutDown()
            }
        }
    }

    fun init() {
        toast("initing...")
        btn_send_cmd.setOnClickListener(this)
        btn_shut_down.setOnClickListener(this)
        btn_wind_speed.setOnClickListener(this)
        btn_work_mode.setOnClickListener(this)
        btn_degree_up.setOnClickListener(this)
        btn_degree_down.setOnClickListener(this)

        // fetch history params from shared preferences.
        var sp = getSharedPreferences("AirCmd", Context.MODE_PRIVATE)
        mWorkMode = sp.getInt("workMode", 0)
        mWindSpeed = sp.getInt("windSpeed", 0)
        mDegree = sp.getInt("degree", 26)
        var id = sp.getInt("id", -1)
        mLastCmd = AirCmd(id, mWorkMode, mWindSpeed, mDegree)

        refreshViewParams(7)

        mQueue = Volley.newRequestQueue(this)
    }

    fun doSendCmd() {
        var cmd = AirCmd(mLastCmd.id + 1, mWorkMode, mWindSpeed, mDegree)
        toast("btn_send_cmd clicked!   Cmd: ${cmd.cmdStr}")
        cmd.status = "Sending"
        val request = JsonObjectRequest(Request.Method.POST, url_ircmd, cmd.buildCmdJson(),
                Response.Listener<JSONObject> {response ->
                    toast(response.toString())
                    cmd.status = "Success"
                    cmd.result = response.toString()
                },
                Response.ErrorListener { err ->
                    err.printStackTrace()
                    toast("request#${cmd.id} failed with errMsg: " + err.message.toString())
                    cmd.status = "Failed"
                    cmd.result = err.message.toString()
                })
        mQueue!!.add(request)
        mQueue!!.start()

        mLastCmd = cmd

        var history = tv_cmd_history.text.toString() + mLastCmd.cmdStr + "\n"
        tv_cmd_history.text = history
        if (tv_cmd_history.lineCount > 5) {
            tv_cmd_history.scrollTo(0, tv_cmd_history.lineHeight * (tv_cmd_history.lineCount - 5))
        }
//        var offset = tv_cmd_history.lineCount * tv_cmd_history.lineHeight
//        if (offset> tv_cmd_history.height) {
//            tv_cmd_history.scrollTo(0, offset - tv_cmd_history.height)
//        }
    }

    override fun onStop() {
        var sp = getSharedPreferences("AirCmd", Context.MODE_PRIVATE)
        sp.edit()
                .putInt("id", mLastCmd.id)
                .putInt("workMode", mWorkMode)
                .putInt("windSpeed", mWindSpeed)
                .putInt("degree", mDegree)
                .commit();
        super.onStop()
    }

    fun doShutDown() {
        toast("btn_shut_down clicked!")
        val request = JsonObjectRequest(Request.Method.GET, url_shut_down, null,
                Response.Listener<JSONObject> { response ->
                    toast(response.toString())
                },
                Response.ErrorListener {
                    toast("request not working...")
                })
        mQueue!!.add(request)
        mQueue!!.start()
    }

    // use bit to indicate whom to refresh, 1 workMode, 2 degree, 4 windSpeed
    fun refreshViewParams(toRefresh: Int) {
        when {
            (toRefresh.and(1) != 0) -> {
                when (mWorkMode) {
                    0 -> tv_work_mode.setText(R.string.work_mode_cool)
                    1 -> tv_work_mode.setText(R.string.work_mode_hot)
                }
            }

            (toRefresh.and(2) != 0) -> {
                tv_degree.text = mDegree.toString()
            }

            (toRefresh.and(4) != 0) -> {
                when (mWindSpeed) {
                    0 -> tv_wind_speed.setText(R.string.wind_speed_0)
                    1 -> tv_wind_speed.setText(R.string.wind_speed_1)
                    2 -> tv_wind_speed.setText(R.string.wind_speed_2)
                    3 -> tv_wind_speed.setText(R.string.wind_speed_3)
                }
            }

            toRefresh == -1 -> {
                // this means the air controller is shut down.
            }
        }
    }

}
