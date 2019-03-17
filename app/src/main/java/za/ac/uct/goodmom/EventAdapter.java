package za.ac.uct.goodmom;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {

    private List<Event> mEventsList;

    public EventAdapter(List<Event> eventsList) {
        mEventsList = eventsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Event event = mEventsList.get(position);

        long startDateTime = event.getStartDateTime();
        long endDateTime = event.getEndDateTime();
        SimpleDateFormat day = new SimpleDateFormat("dd");
        SimpleDateFormat month = new SimpleDateFormat("MMM");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");

        String dayStr = day.format(new Date(startDateTime));
        String monthStr = month.format(new Date(startDateTime));
        String timeStr = time.format(new Date(startDateTime)) + " - " + time.format(new Date(endDateTime));

        holder.dayTextView.setText(dayStr);
        holder.monthTextView.setText(monthStr);
        holder.timeTextView.setText(timeStr);
        holder.titleTextView.setText(event.getTitle());
    }

    @Override
    public int getItemCount() {
        return mEventsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView, dayTextView, monthTextView, timeTextView;

        public MyViewHolder(View view) {
            super(view);
            timeTextView = view.findViewById(R.id.event_time);
            titleTextView = view.findViewById(R.id.event_title);
            dayTextView = view.findViewById(R.id.event_day);
            monthTextView = view.findViewById(R.id.event_month);
        }
    }
}
