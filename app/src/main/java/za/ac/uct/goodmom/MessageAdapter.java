package za.ac.uct.goodmom;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item, parent, false);
        }

        TextView messageTextView = (TextView) convertView.findViewById(R.id.message_text_view);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.name_text_view);

        Message message = getItem(position);

        messageTextView.setText(message.getText());
        authorTextView.setText(message.getName());

        return convertView;
    }

}
