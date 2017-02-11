package seaskyways.sswchat

import android.app.Application
import com.chibatching.kotpref.Kotpref

/**
 * Created by Ahmad on 12/02 Feb/2017.
 */
class SSWChat : Application() {
    
    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
    }
}