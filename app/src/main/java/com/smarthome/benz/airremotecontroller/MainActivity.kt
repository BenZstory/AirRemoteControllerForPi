package com.smarthome.benz.airremotecontroller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.jetbrains.anko.toast
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {
    val TAG = "mainAty"
    val url = "http://www.benzweb.tech:28234"
    val url_shut_down = url + "/air_ctl_close"
    val url_ircmd = url + "/ircmd"

    var mSp: SharedPreferences? = null
    var mQueue: RequestQueue? = null
    var mWorkMode = 0
    var mWindSpeed = 0
    var mDegree = 26
    var mLastCmd: AirCmd = AirCmd(0, true, 0, 0, 26)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)

        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.i(TAG, item.toString())
        when (item!!.itemId) {
            R.id.action_item_scheduler -> {
                val intent = Intent(this, SchedulerActivity::class.java)
                startActivity(intent)
            }

            R.id.action_item_settings -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        refreshViewParams(7)
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
        mSp = getSharedPreferences("AirCmd", Context.MODE_PRIVATE)
        mWorkMode = mSp!!.getInt("workMode", 0)
        mWindSpeed = mSp!!.getInt("windSpeed", 0)
        mDegree = mSp!!.getInt("degree", 26)
        var id = mSp!!.getInt("id", -1)
        val toggle = mSp!!.getBoolean("toggle", true)
        mLastCmd = AirCmd(id, toggle, mWorkMode, mWindSpeed, mDegree)
        Log.i(TAG, "init lastCmd: " + mLastCmd.cmdStr)

        mQueue = Volley.newRequestQueue(this)
    }

    fun doSaveCmd() {
        mSp!!.edit()
                .putInt("id", mLastCmd.id)
                .putBoolean("toggle", mLastCmd.toggle)
                .putInt("workMode", mWorkMode)
                .putInt("windSpeed", mWindSpeed)
                .putInt("degree", mDegree)
                .apply()
        Log.i(TAG, "cmd saved: " + mLastCmd.cmdStr)
    }

    fun doSendCmd() {
        val cmd = AirCmd(mLastCmd.id + 1, true, mWorkMode, mWindSpeed, mDegree)
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

        mLastCmd = cmd
        doSaveCmd()

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

    fun doShutDown() {
        val cmd = AirCmd(mLastCmd.id + 1, false, mWorkMode, mWindSpeed, mDegree)
        toast("btn_shut_down clicked!")
        val request = JsonObjectRequest(Request.Method.GET, url_shut_down, null,
                Response.Listener<JSONObject> { response ->
                    toast(response.toString())
                },
                Response.ErrorListener {
                    toast("request not working...")
                })
        mQueue!!.add(request)
        doSaveCmd()
    }

    // use bit to indicate whom to refresh, 1 workMode, 2 degree, 4 windSpeed
    fun refreshViewParams(toRefresh: Int) {
        Log.i(TAG, "refreshViewParams, int: " + toRefresh)

        if (toRefresh.and(1) != 0) {
            when (mWorkMode) {
                0 -> tv_work_mode.setText(R.string.work_mode_cool)
                1 -> tv_work_mode.setText(R.string.work_mode_hot)
            }
        }

        if (toRefresh.and(2) != 0) {
            Log.i(TAG, "setting degreeText : " + mDegree.toString())
            tv_degree.text = mDegree.toString()
        }

        if (toRefresh.and(4) != 0) {
            when (mWindSpeed) {
                0 -> tv_wind_speed.setText(R.string.wind_speed_0)
                1 -> tv_wind_speed.setText(R.string.wind_speed_1)
                2 -> tv_wind_speed.setText(R.string.wind_speed_2)
                3 -> tv_wind_speed.setText(R.string.wind_speed_3)
            }
        }

        if (toRefresh == -1) {
            Log.i(TAG, "shutting down...")
        }
    }

}
