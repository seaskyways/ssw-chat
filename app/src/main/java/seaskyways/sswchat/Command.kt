package seaskyways.sswchat

import org.json.JSONObject

/**
 * Created by Ahmad on 11/02 Feb/2017.
 */
class Command(val commandTag: String, val data: String) {
    constructor(commandTag: String, data: JSONObject) : this(commandTag, data.toString())
    constructor(json: JSONObject) : this(json.getString("command"), json.getString("data"))
    constructor(json: String) : this(JSONObject(json))
    
    val jsonData: JSONObject by lazy { JSONObject(data) }
    
    override fun toString(): String {
        return JSONObject()
                .run {
                    put("command", commandTag)
                    put("data", data)
                    toString()
                }
    }
    
    fun toStringHeavy() : String{
        return JSONObject()
                .run {
                    put("command", commandTag)
                    put("data", jsonData)
                    toString()
                }
    }
}