package com.smarthome.benz.airremotecontroller

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
    val url_update_cron = url + "/update_cron"

    var mQueue: RequestQueue? = null
    var isAddingNew : Boolean = true

    var newCron = CmdCron(0, 0, 12, 0, AirCmd(0, true))
    var weekdays_mask: Int = 31 shl 1
    var weekends_mask: Int = 3 shl 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cron_editor)
        setSupportActionBar(cron_editor_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        var isNew = intent.getBooleanExtra("is_new", true)
        if (!isNew) {
            isAddingNew = false
            val jsonStr = intent.getStringExtra("cron_json")
            Log.i(TAG, "jsonStr  " + jsonStr)
            val json = JSONObject(jsonStr)
            newCron = CmdCron(json)
            Log.i(TAG, "built cron  " + newCron.toString())
        }

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
        day_1.setOnClickListener(this)
        day_2.setOnClickListener(this)
        day_3.setOnClickListener(this)
        day_4.setOnClickListener(this)
        day_5.setOnClickListener(this)
        day_6.setOnClickListener(this)
        day_7.setOnClickListener(this)
        day_option_weekday.setOnClickListener(this)
        day_option_weekend.setOnClickListener(this)

        tintAllWeekBtns()

        mQueue = Volley.newRequestQueue(this)
    }

    fun tintAllWeekBtns() {
        tintWeekBtn(1, day_1)
        tintWeekBtn(2, day_2)
        tintWeekBtn(3, day_3)
        tintWeekBtn(4, day_4)
        tintWeekBtn(5, day_5)
        tintWeekBtn(6, day_6)
        tintWeekBtn(7, day_7)
        tintBriefWeekOptionBtns()
    }

    fun send_cron() {
        newCron.hour = hour_picker.value
        newCron.minute = minute_picker.value
        if (isAddingNew) {
             val request = JsonObjectRequest(Request.Method.POST, url_set_cron, newCron.buildJson(),
                    Response.Listener<JSONObject> { response ->
                        toast(response.toString())
                        this.finish()
                    },
                    Response.ErrorListener { err ->
                        err.printStackTrace()
                        toast("request#${newCron.id} failed with errMsg: " + err.message.toString())
                    })
            mQueue!!.add(request)
        } else {
            val request = JsonObjectRequest(Request.Method.POST, url_update_cron, newCron.buildJson(),
                    Response.Listener<JSONObject> { response ->
                        toast(response.toString())
                        this.finish()
                    },
                    Response.ErrorListener { err ->
                        err.printStackTrace()
                        toast("request#${newCron.id} failed with errMsg: " + err.message.toString())
                    })
            mQueue!!.add(request)
        }
    }

    override fun onBackPressed() {
        if (!isAddingNew) {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("You will lose your change.")
                    .setPositiveButton("Confirm", { dialog, which ->
                        dialog.dismiss()
                        this.finish()
            })
                    .setNegativeButton("Cancel", { dialog, which ->
                        dialog.dismiss()
            })
            dialogBuilder.create().show()
        } else {
            super.onBackPressed()
        }
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

            day_1 -> {
                Log.i(TAG, "onClick day1")
                toggleWeek(1, day_1)
            }

            day_2 -> {
                toggleWeek(2, day_2)
            }

            day_3 -> {
                toggleWeek(3, day_3)
            }

            day_4 -> {
                toggleWeek(4, day_4)
            }

            day_5 -> {
                toggleWeek(5, day_5)
            }

            day_6 -> {
                toggleWeek(6, day_6)
            }

            day_7 -> {
                toggleWeek(7, day_7)
            }

            day_option_weekday -> {
                toggleBriefWeekOption(1)
            }

            day_option_weekend -> {
                toggleBriefWeekOption(2)
            }
        }
    }

    fun tintWeekBtn(week:Int, v:TextView?) {
        Log.i(TAG, "tintWeekBtn, week: " + week)
        var week_flag = 1 shl week
        if (week <= 5) {
            if (this.newCron.day == 0 || (this.newCron.day and week_flag > 0)) {
                v!!.setBackgroundColor(getColor(R.color.SpringGreen))
            } else {
                v!!.setBackgroundColor(getColor(R.color.lavender))
            }
        } else {
            if (this.newCron.day == 0 || (this.newCron.day and week_flag > 0)) {
                v!!.setBackgroundColor(getColor(R.color.Goldenrod))
            } else {
                v!!.setBackgroundColor(getColor(R.color.lavender))
            }
        }
    }

    fun tintBriefWeekOptionBtns() {
        Log.i(TAG, "tintBriefWeekOptionBtns, day: " + this.newCron.day)
        if (this.newCron.day == 0) {
            day_option_weekday.setBackgroundColor(getColor(R.color.SpringGreen))
            day_option_weekend.setBackgroundColor(getColor(R.color.Goldenrod))
            return
        }

        val weekday_flag = this.newCron.day and weekdays_mask
        if (weekday_flag == weekdays_mask) {
            day_option_weekday.setBackgroundColor(getColor(R.color.SpringGreen))
        } else if (weekday_flag > 0) {
            day_option_weekday.setBackgroundColor(getColor(R.color.LightGray))
        } else {
            day_option_weekday.setBackgroundColor(getColor(R.color.lavender))
        }

        val weekend_flag = this.newCron.day and weekends_mask
        if (weekend_flag == weekends_mask) {
            day_option_weekend.setBackgroundColor(getColor(R.color.Goldenrod))
        } else if (weekend_flag > 0) {
            day_option_weekend.setBackgroundColor(getColor(R.color.LightGoldenrod))
        } else {
            day_option_weekend.setBackgroundColor(getColor(R.color.lavender))
        }
    }

    fun toggleBriefWeekOption(tag: Int) {
        if (this.newCron.day == 0) {
            Log.i(TAG, "toggleWeek, day_flag day is 0")
            this.newCron.day = weekdays_mask or weekends_mask
        }
        if (tag == 1) {
            val weekday_flag = this.newCron.day and weekdays_mask
            if (weekday_flag > 0) {
                // clear weekday
                this.newCron.day = this.newCron.day and weekdays_mask.inv()
            } else {
                // set weekdays_mask
                this.newCron.day = this.newCron.day or weekdays_mask
            }
        } else if (tag == 2) {
            val weekend_flag = this.newCron.day and weekends_mask
            if (weekend_flag > 0) {
                this.newCron.day = this.newCron.day and weekends_mask.inv()
            } else {
                this.newCron.day = this.newCron.day or weekends_mask
            }
        }
        tintAllWeekBtns()
    }

    fun toggleWeek(week:Int, v:TextView) {
        Log.i(TAG, "toggleWeek, week: " + week)
        if (this.newCron.day == 0) {
            Log.i(TAG, "toggleWeek, day_flag day is 0")
            this.newCron.day = weekdays_mask or weekends_mask
        }
        Log.i(TAG, "toggleWeek, day_flag before: " + this.newCron.day)
        var week_flag = 1 shl week
        this.newCron.day = this.newCron.day xor week_flag;
        Log.i(TAG, "toggleWeek, day_flag after: " + this.newCron.day)
        tintWeekBtn(week, v)
        tintBriefWeekOptionBtns()
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

