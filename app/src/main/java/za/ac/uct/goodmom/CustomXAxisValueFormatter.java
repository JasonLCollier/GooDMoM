package za.ac.uct.goodmom;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomXAxisValueFormatter implements IAxisValueFormatter {

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");


    public CustomXAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return sdf.format(new Date((long) value));
    }
}
