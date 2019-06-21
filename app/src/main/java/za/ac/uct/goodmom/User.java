package za.ac.uct.goodmom;

import java.io.Serializable;

public class User implements Serializable {
    private String mName;
    private String mPhone;
    private String mId;
    private String mEmail;
    private String mAddress;
    private String mHpSurname;
    private String mHpNumber;
    private long mDueDate;
    private String mHpType;
    private int mHeight;
    private double mPrePregWeight;
    private String mDiabetesType;

    public User() {
    }

    public User(String name, String phone, String id, String email, String address, String hpSurname, String hpNumber, long dueDate, String hpType, int height, double prePregWeight, String diabetesType) {
        mName = name;
        mPhone = phone;
        mId = id;
        mEmail = email;
        mAddress = address;
        mHpSurname = hpSurname;
        mHpNumber = hpNumber;
        mDueDate = dueDate;
        mHpNumber = hpNumber;
        mHpType = hpType;
        mHeight = height;
        mPrePregWeight = prePregWeight;
        mDiabetesType = diabetesType;
    }

    public void setAddress(int no, String street, String city, String province, int postCode) {
        mAddress = no + ", " + street + ", " + city + ", " + province + ", " + postCode;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getHpSurname() {
        return mHpSurname;
    }

    public void setHpSurname(String hpSurname) {
        mHpSurname = hpSurname;
    }

    public String getHpNumber() {
        return mHpNumber;
    }

    public void setHpNumber(String hpNumber) {
        mHpNumber = hpNumber;
    }

    public long getDueDate() {
        return mDueDate;
    }

    public void setDueDate(long dueDate) {
        mDueDate = dueDate;
    }

    public String getHpType() {
        return mHpType;
    }

    public void setHpType(String hpType) {
        mHpType = hpType;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public double getPrePregWeight() {
        return mPrePregWeight;
    }

    public void setPrePregWeight(double prepregWeight) {
        mPrePregWeight = prepregWeight;
    }

    public String getDiabetesType() {
        return mDiabetesType;
    }

    public void setDiabetesType(String diabetesType) {
        mDiabetesType = diabetesType;
    }
}
