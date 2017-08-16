package com.smarthome.benz.airremotecontroller

import org.json.JSONObject

/**
 * Created by benz on 17.7.30.
 */

class CmdCron() {
    var id: Int = 0
    var day: Int = 0
    var hour: Int = 0
    var minute: Int = 0

    var enabled: Boolean = true
    var cmdType: Int = 0
    var cmdStrForShell: String? = null
    var airCmd: AirCmd? = null

    init { }

    constructor(id: Int,
                day: Int,
                hour: Int,
                minute: Int,
                airCmd: AirCmd): this() {
        this.id = id
        this.day = day
        this.hour = hour
        this.minute = minute
        this.cmdType = 2
        this.airCmd = airCmd
        this.cmdStrForShell = "irsend SEND_ONCE haierac " + airCmd.cmdStr
    }

    constructor(id: Int,
                day: Int,
                hour: Int,
                minute: Int,
                cmdStr: String): this() {
        this.id = id
        this.day = day
        this.hour = hour
        this.minute = minute
        this.cmdType = 1
        this.cmdStrForShell = cmdStr
    }

    constructor(json: JSONObject): this() {
        this.id = json.getInt("id")
        this.day = json.getInt("day")
        this.hour = json.getInt("hour")
        this.minute = json.getInt("minute")

        this.cmdType = 2
        val toggle = json.getBoolean("toggle")

        if (toggle) {
            val modeStr = json.getString("mode")
            var mode = 0
            if (modeStr != "COOL") {
                mode = 1
            }
            val wind = json.getInt("wind")
            val degree = json.getInt("degree")
            this.airCmd = AirCmd(id, toggle, mode, wind, degree)
        } else {
            this.airCmd = AirCmd(id, toggle, 0, 0, 26)
        }

        this.cmdStrForShell = "irsend SEND_ONCE haierac " + this.airCmd!!.cmdStr
    }

    fun buildJson() : JSONObject {
        val json = JSONObject()
        json.put("id", this.id)
        json.put("day", this.day)
        json.put("hour", this.hour)
        json.put("minute", this.minute)
        if (this.cmdType == 2) {
            json.put("toggle", this.airCmd!!.toggle)
            json.put("mode", this.airCmd!!.workModeStr)
            json.put("degree", this.airCmd!!.degree)
            json.put("wind", this.airCmd!!.windSpeed)
        } else {
            json.put("cmd", this.cmdStrForShell)
        }
        return json
    }

}


