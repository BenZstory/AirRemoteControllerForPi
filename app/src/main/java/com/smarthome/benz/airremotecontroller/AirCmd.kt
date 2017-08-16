package com.smarthome.benz.airremotecontroller

import org.json.JSONObject

/**
 * Created by benz on 17.6.11.
 */
class AirCmd(val id: Int,
             var toggle: Boolean) {
    var workModeStr: String = "COOL"
    var cmdStr: String? = null
    var degree: Int = 26
    var workMode: Int = 0
    var windSpeed: Int = 0
    var status: String? = null
    var result: String? = null

    init {
        if (!toggle) {
            cmdStr = "HAIER_CLOSE"
        }
    }

    constructor(id : Int,
                toggle: Boolean,
                workMode: Int,
                windSpeed: Int,
                degree: Int) :this(id, toggle) {
        when(workMode) {
            0 -> {workModeStr = "COOL"}
            1 -> {workModeStr = "HOT"}
        }
        this.workMode = workMode
        this.windSpeed = windSpeed
        this.degree = degree
        if (toggle) {
            cmdStr = "HAIER_${this.workModeStr}_${this.degree}_${this.windSpeed}"
        }
    }

    fun buildCmdJson() : JSONObject {
        val json = JSONObject()
        if (!toggle) return json
        json.put("mode", workModeStr)
        json.put("degree", degree)
        json.put("wind", windSpeed)
        return json    }
}