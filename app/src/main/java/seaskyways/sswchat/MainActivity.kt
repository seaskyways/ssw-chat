package seaskyways.sswchat

import android.os.Bundle
import android.support.design.widget.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.widget.EditText
import com.neovisionaries.ws.client.*
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    
    
    val messagesList = arrayListOf<Message>()
    val messageSubject = PublishSubject.create<String>()!!
    val commandObservable: Observable<Command>
        get() = messageSubject.subscribeOn(Schedulers.io()).map(::Command)
    val nameSubject = BehaviorSubject.create<String>()!!
    
    val messagesAdapter = MessagesAdapter(messagesList)
    
    lateinit var websocket: WebSocket
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        val fab = find<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Do you want to rename ?", Snackbar.LENGTH_LONG)
                    .setAction("Change name", {
                        alert("", title = "Change name") {
                            var nameEditText: EditText? = null
                            customView {
                                verticalLayout {
                                    nameEditText = editText {
                                        hint = "Enter new name"
                                    }
                                }
                            }
                            okButton {
                                val text = nameEditText?.text?.toString()
                                if (!text.isNullOrBlank()) {
                                    nameSubject.onNext(text)
                                }
                            }
                        }.show()
                    }).show()
        }
        
        bindRecycler()
        subscribeToMessages()
        setupWebsocketConnection()
        setupSendingLogic()
    }
    
    private fun setupSendingLogic() {
        send_button.onClick {
            val text = message_input.text.toString()
            if (text.isNotBlank()) {
                val command = JSONObject(
                        mapOf(
                                "command" to "send_message",
                                "data" to text
                        )
                )
                websocket.sendText(command.toString())
                message_input.text.clear()
            }
        }
    }
    
    private fun bindRecycler() {
        messages_recycler.adapter = messagesAdapter
        messages_recycler.layoutManager = LinearLayoutManager(ctx)
    }
    
    private fun setupWebsocketConnection() {
        Completable.create {
            val websocketFactory = WebSocketFactory()
            websocket = websocketFactory.createSocket("ws://192.168.0.100/chat/ws")
            
            websocket.addListener(object : WebSocketAdapter(), AnkoLogger {
                override fun onConnected(websocket: WebSocket, headers: MutableMap<String, MutableList<String>>?) {
                    val cmd = Command("get_id", UserPref.token).toString()
                    websocket.sendText(cmd)
                }
                
                
                override fun onTextMessage(websocket: WebSocket, text: String) {
                    messageSubject.onNext(text)
                }
            })
            
            websocket.connect()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    toast("Connected")
                }, {
                    it.printStackTrace()
                    toast("Check the logs , crash was handales")
                })
    }
    
    private fun subscribeToMessages() {
        commandObservable
                .filter { it.commandTag == "message" }
                .map(Command::jsonData)
                .map { Message(it.getString("sender_name"), it.getString("message")) }
                .doOnNext { messagesList.add(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    messagesAdapter.notifyItemInserted(messagesList.size)
                }, { error ->
                    error.printStackTrace()
                    alert(message = "An Error has occurred").show()
                })
        
        messageSubject
                .subscribe({ }, {
                    websocket.clearListeners()
                    websocket.sendClose()
                }, {
                    websocket.clearListeners()
                    websocket.sendClose()
                })
        
        commandObservable
                .filter { it.commandTag == "connection_info" }
                .map(Command::jsonData)
                .subscribe({ cmdJson ->
                    UserPref.beginBulkEdit()
                    UserPref.id = cmdJson.getInt("connection_id")
                    UserPref.name = cmdJson.optString("name", UserPref.id.toString())
                    UserPref.token = cmdJson.getString("token")
                    UserPref.commitBulkEdit()
                    runOnUiThread {
                        longToast(UserPref.toString())
                    }
                }, {
                    
                })
        
        nameSubject.subscribe { UserPref.name = it }
        
        nameSubject.observeOn(Schedulers.io())
                .map {
                    Command("save_name", """{"id" : ${UserPref.id}, "name" : $it}""")
                }
                .subscribe {
                    websocket.sendText(it.toStringHeavy())
                }
        
        
    }
    
    override fun onDestroy() {
        super.onDestroy()
        messageSubject.onComplete()
    }
}
