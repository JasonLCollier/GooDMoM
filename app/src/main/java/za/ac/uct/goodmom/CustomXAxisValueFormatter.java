package za.ac.uct.goodmom;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class CustomXAxisValueFormatter implements IAxisValueFormatter {

    public CustomXAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int day = (int) Math.ceil(value / 24);
        return Integer.toString(day);
    }
}
