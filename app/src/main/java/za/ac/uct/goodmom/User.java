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
    private String mDueDate;

    public User() {
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

    public String getDueDate() {
        return mDueDate;
    }

    public void setDueDate(String dueDate) {
        mDueDate = dueDate;
    }
}
