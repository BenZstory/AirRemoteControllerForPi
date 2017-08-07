package com.smarthome.benz.airremotecontroller

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_cron_editor.*

/**
 * Created by benz on 17.8.2.
 */

class CronEditor: AppCompatActivity() {
    val TAG = "editorAty"
    val url = "www.benzweb.tech:28234"
    val url_set_cron = url + "/set_cron"

    var myCron = CmdCron()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cron_editor)
        setSupportActionBar(cron_editor_toolbar)

        hour_picker.minValue = 0
        hour_picker.maxValue = 23
        minute_picker.minValue = 0
        minute_picker.maxValue = 59
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

    fun send_cron() {

    }


}

