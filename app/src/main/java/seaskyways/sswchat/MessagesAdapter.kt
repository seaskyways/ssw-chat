package seaskyways.sswchat

import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.single_message.view.*
import org.jetbrains.anko.layoutInflater
import java.util.*

/**
 * Created by Ahmad on 11/02 Feb/2017.
 */
class MessagesAdapter(val dataList : List<Message>) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {
    
    override fun getItemCount() = dataList.size
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val curr = dataList[position]
        holder.apply {
            nameTextView.text = curr.senderName + ":"
            messageTextView.text = curr.message
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(parent.context.layoutInflater.inflate(R.layout.single_message, parent, false))
    }
    
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.sender_name
        val messageTextView: TextView = itemView.message_text
    }
}