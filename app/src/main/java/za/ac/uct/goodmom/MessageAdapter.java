package za.ac.uct.goodmom;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<Message> mMessageList;

    public MessageAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);

        return new MessageAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.MyViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        holder.messageTextView.setText(message.getText());
        holder.authorTextView.setText(message.getName());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView, authorTextView;

        public MyViewHolder(View view) {
            super(view);
            messageTextView = view.findViewById(R.id.message_text_view);
            authorTextView = view.findViewById(R.id.name_text_view);
        }
    }

}
