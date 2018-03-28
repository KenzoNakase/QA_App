package jp.techacademy.kenzou.nakase.qa_app;

import java.io.Serializable;


public class Favourite implements Serializable {
    private String mUid;
    private String mQuestionUid;
    private int mGenre;


    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }
    public int getGenre() {
        return mGenre;
    }


    public Favourite(String uid, String questionUid, int genre) {
        mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;

    }
}