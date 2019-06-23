package za.ac.uct.goodmom;

public class HpSpecifiedRanges {
    private String mGlucMax;
    private String mGlucMin;
    private String mWeightMax;
    private String mWeightMin;
    private String mActMax;
    private String mActMin;

    public HpSpecifiedRanges() {
    }

    public String getActMax() {
        return mActMax;
    }

    public void setActMax(String actMax) {
        mActMax = actMax;
    }

    public String getActMin() {
        return mActMin;
    }

    public void setActMin(String actMin) {
        mActMin = actMin;
    }

    public String getGlucMax() {
        return mGlucMax;
    }

    public void setGlucMax(String glucMax) {
        mGlucMax = glucMax;
    }

    public String getGlucMin() {
        return mGlucMin;
    }

    public void setGlucMin(String glucMin) {
        mGlucMin = glucMin;
    }

    public String getWeightMax() {
        return mWeightMax;
    }

    public void setWeightMax(String weightMax) {
        mWeightMax = weightMax;
    }

    public String getWeightMin() {
        return mWeightMin;
    }

    public void setWeightMin(String weightMin) {
        mWeightMin = weightMin;
    }
}
