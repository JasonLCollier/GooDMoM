package za.ac.uct.goodmom;

public class Message {

    private String mText;
    private String mName;

    public Message() {
    }

    public Message(String text, String name) {
        this.mText = text;
        this.mName = name;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }
}
