package com.smarthome.benz.airremotecontroller

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_cron_editor.*
import org.jetbrains.anko.toast
import org.json.JSONObject

/**
 * Created by benz on 17.8.2.
 */

class CronEditor: AppCompatActivity(), View.OnClickListener {
    val TAG = "editorAty"
    val url = "http://www.benzweb.tech:28234"
    val url_set_cron = url + "/set_cron"

    var mQueue: RequestQueue? = null

    var newCron = CmdCron(0, 1, 12, 0, AirCmd(0, true))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cron_editor)
        setSupportActionBar(cron_editor_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        init()
    }

    override fun onResume() {
        super.onResume()
        refreshViewParams()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.confirm_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_confirm -> {
                send_cron()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun init() {
        hour_picker.minValue = 0
        hour_picker.maxValue = 23
        minute_picker.minValue = 0
        minute_picker.maxValue = 59

        btn_degree_down.setOnClickListener(this)
        btn_degree_up.setOnClickListener(this)
        btn_toggle.setOnClickListener(this)
        btn_work_mode.setOnClickListener(this)
        btn_wind_speed.setOnClickListener(this)

        mQueue = Volley.newRequestQueue(this)
    }

    fun send_cron() {
        newCron.hour = hour_picker.value
        newCron.minute = minute_picker.value

        val request = JsonObjectRequest(Request.Method.POST, url_set_cron, newCron.buildJson(),
                Response.Listener<JSONObject> { response ->
                    toast(response.toString())

                },
                Response.ErrorListener { err ->
                    err.printStackTrace()
                    toast("request#${newCron.id} failed with errMsg: " + err.message.toString())
                })

        mQueue!!.add(request)
    }

    override fun onClick(v: View?) {
        Log.i(TAG, "onClick")
        when(v!!) {
            btn_work_mode -> {
                newCron.airCmd!!.workMode += 1
                newCron.airCmd!!.workMode %= 2
                when (newCron.airCmd!!.workMode) {
                    0 -> tv_work_mode.text = "冷风"
                    1 -> tv_work_mode.text = "热风"
                }
            }

            btn_degree_down -> {
                newCron.airCmd!!.degree -= 1
                when {
                    newCron.airCmd!!.degree < 23 -> {newCron.airCmd!!.degree = 28}
                }
                tv_degree.text = newCron.airCmd!!.degree.toString()
            }

            btn_degree_up -> {
                newCron.airCmd!!.degree += 1
                when {
                    newCron.airCmd!!.degree > 28 -> {newCron.airCmd!!.degree = 23}
                }
                tv_degree.text = newCron.airCmd!!.degree.toString()
            }

            btn_wind_speed -> {
                newCron.airCmd!!.windSpeed += 1
                newCron.airCmd!!.windSpeed %= 4
                when (newCron.airCmd!!.windSpeed) {
                    0 -> tv_wind_speed.text = "自动"
                    1 -> tv_wind_speed.text = "低"
                    2 -> tv_wind_speed.text = "中"
                    3 -> tv_wind_speed.text = "高"
                }
            }

            btn_toggle -> {
                newCron.airCmd!!.toggle = !newCron.airCmd!!.toggle
                when (newCron.airCmd!!.toggle) {
                    true -> tv_toggle.text = "ON"
                    false -> tv_toggle.text = "OFF"
                }
            }
        }
    }

    fun refreshViewParams() {
        Log.i(TAG, "refreshViewParams")
        when (newCron.airCmd!!.workMode) {
            0 -> tv_work_mode.text = "冷风"
            1 -> tv_work_mode.text = "热风"
        }
        tv_degree.text = newCron.airCmd!!.degree.toString()
        when (newCron.airCmd!!.windSpeed) {
            0 -> tv_wind_speed.text = "自动"
            1 -> tv_wind_speed.text = "低"
            2 -> tv_wind_speed.text = "中"
            3 -> tv_wind_speed.text = "高"
        }
        when (newCron.airCmd!!.toggle) {
            true -> tv_toggle.text = "ON"
            false -> tv_toggle.text = "OFF"
        }

        hour_picker.value = newCron.hour
        minute_picker.value = newCron.minute
    }

}

