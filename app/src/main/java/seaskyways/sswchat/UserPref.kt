package seaskyways.sswchat

import com.chibatching.kotpref.KotprefModel

/**
 * Created by Ahmad on 11/02 Feb/2017.
 */
object UserPref : KotprefModel() {
    var token by stringPref()
    var name: String = ""
    var id: Int = 0
    
    override fun toString(): String {
        return """
token = $token
name = $name
id = $id
"""
    }
}