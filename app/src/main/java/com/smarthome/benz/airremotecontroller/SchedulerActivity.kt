package com.smarthome.benz.airremotecontroller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_scheduler.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by benz on 17.7.30.
 */

class SchedulerActivity: AppCompatActivity() {
    val TAG = "SchedulerAty"
    val url_root = "http://www.benzweb.tech:28234"
    val url_get_cron = url_root + "/get_cron"
    val url_clear_all = url_root + "/clear_cron"
    var mQueue: RequestQueue? = null

    val mCronList: ArrayList<CmdCron>? = ArrayList()
    var mListAdapter: CronListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduler)
        setSupportActionBar(scheduler_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mQueue = Volley.newRequestQueue(this)

        recycler_view.layoutManager = LinearLayoutManager(this)
        this.mListAdapter = CronListAdapter(mCronList!!, R.layout.cron_list_item)
        recycler_view.adapter = mListAdapter

        fab_add.setOnClickListener({v:View ->
            val intent = Intent(this, CronEditor::class.java)
            startActivityForResult(intent, 100)
        })
    }

    fun prepareData() {
        val request = JsonArrayRequest(Request.Method.GET, url_get_cron, null,
                Response.Listener { response: JSONArray ->
                    Log.i(TAG, "get_cron_rep: " + response.toString())
                    mCronList!!.clear()
                    for (item: JSONObject in response) {
                        Log.i(TAG, "cron_item: " + item.toString())
                        mCronList!!.add(CmdCron(item))
                    }
                    var c1 : Comparator<CmdCron> = Comparator{ o1, o2 ->
                        if (o1.hour == o2.hour) {
                            o1.minute.compareTo(o2.minute)
                        } else {
                            o1.hour - o2.hour
                        }
                    }
                    mCronList.sortWith(c1)

                    mListAdapter!!.notifyDataSetChanged()
                },
                Response.ErrorListener { error ->
                    Log.i(TAG, "get_cron, VolleyError: " + error.toString())
                    toast("request failed...")
                }
        )
        mQueue!!.add(request)
        Log.i(TAG, "get_cron request sent!")
    }


    operator fun JSONArray.iterator(): Iterator<JSONObject>
            = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    override fun onResume() {
        super.onResume()
        prepareData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.scheduler_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.i(TAG, "SchedulerAction pressed: " + item!!.toString())
        when (item!!.itemId) {
            R.id.actions_item_clear_all -> {
                val request = JsonObjectRequest(Request.Method.GET, url_clear_all, null,
                        Response.Listener<JSONObject> { response ->
                            toast(response.toString())
                        },
                        Response.ErrorListener { error ->
                            toast(error.toString())
                        })
                mQueue!!.add(request)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class CronListAdapter(var mItems: ArrayList<CmdCron>,
                          val layoutId: Int)
        : RecyclerView.Adapter<CronListAdapter.ViewHolder>() {

        class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val mModeView: TextView = view.find(R.id.tv_item_mode)
            val mDegreeView: TextView = view.find(R.id.tv_item_degree)
            val mDayTypeView: TextView = view.find(R.id.tv_item_day_type)
            val mTimeView: TextView = view.find(R.id.tv_item_time)

            fun bindItem(item: CmdCron) {
                if (item.airCmd!!.toggle) {
                    when (item.airCmd!!.workMode) {
                        0 -> mModeView.backgroundColor = Color.rgb(0,255,255)
                        1 -> mModeView.backgroundColor = Color.rgb(255,165,0)
                    }
                    mModeView.text = item.airCmd!!.windSpeed.toString()
                    mDegreeView.text = item.airCmd!!.degree.toString()
                } else {
                    mModeView.text = "OFF"
                    mDegreeView.text = "--"
                }

                when (item.day) {
                    1 -> mDayTypeView.text = "workday"
                    2 -> mDayTypeView.text = "weekend"
                }
                val time = item.hour.toString() + " : " + item.minute.toString()
                mTimeView.text = time
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mItems[position]
            holder.bindItem(item)
        }

        override fun getItemCount(): Int {
            return mItems.size
        }
    }

}