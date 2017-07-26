package com.smarthome.benz.airremotecontroller

import org.json.JSONObject

/**
 * Created by benz on 17.6.11.
 */
class AirCmd(val id: Int, val workMode: Int, val windSpeed: Int, val degree: Int) {
    var workModeStr = "COOL"
    var cmdStr: String
    var status: String? = null
    var result: String? = null

    init {
        when (workMode) {
            0 -> {workModeStr = "COOL"}
            1 -> {workModeStr = "HOT"}
        }
        cmdStr = "HAIER_${this.workModeStr}_${this.degree}_${this.windSpeed}"
    }

    fun buildCmdJson() : JSONObject {
        var json = JSONObject()
        json.put("mode", workModeStr)
        json.put("degree", degree)
        json.put("wind", windSpeed)
        return json
    }

    fun buildCmdStr() : String{
        cmdStr = "HAIER_${this.workModeStr}_${this.degree}_${this.windSpeed}"
        return cmdStr
    }
}