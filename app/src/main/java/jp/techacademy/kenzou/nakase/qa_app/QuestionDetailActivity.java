package jp.techacademy.kenzou.nakase.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Button;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import static jp.techacademy.kenzou.nakase.qa_app.MainActivity.map;


public class QuestionDetailActivity extends AppCompatActivity {


    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private ListView mListView;

    private DatabaseReference mAnswerRef;
    private DatabaseReference mFavouriteAnswerRef;
    private DatabaseReference mAnswerArrayListRef;
    private DatabaseReference mFavouriteRef;
    private DatabaseReference mFavouriteGenreRef;
    private DatabaseReference mIsFavouriteRef;
    private DatabaseReference mBodyRef;
    private DatabaseReference mNameRef;
    private DatabaseReference mTitleRef;
    private DatabaseReference mUidRef;
    private DatabaseReference mAnswerBody;
    private DatabaseReference mAnswerName;
    private DatabaseReference mAnswerUid;

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, @StyleRes int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
    }

    @Override
    public PackageManager getPackageManager() {
        return super.getPackageManager();
    }

    private ImageView mImageView;
    private EditText mAnswerEditText;

    boolean mIsFavourite = false;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mIsFavouriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mIsFavourite = true;
            Button btn =(Button) findViewById(R.id.button);
            Drawable orange_btn = ResourcesCompat.getDrawable(getResources(),R.drawable.orange_button, null);
            btn.setBackground(orange_btn);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_question_detail);

        Button btn =(Button) findViewById(R.id.button);
        Drawable default_btn = ResourcesCompat.getDrawable(getResources(),R.drawable.default_button, null);
        btn.setBackground(default_btn);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 要件1. ログインしている場合に質問詳細画面に「お気に入り」ボタンを表示
        final Button button = (Button) findViewById(R.id.button);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            mFavouriteRef = dataBaseReference.child(Const.FavouritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            mFavouriteRef.addChildEventListener(mIsFavouriteListener);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mIsFavourite = !mIsFavourite;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                mIsFavouriteRef = dataBaseReference.child(Const.FavouritesPATH).child(mQuestion.getUid());

                if (mIsFavourite) {

                    Map<String, String> data = new HashMap<String, String>();

                    mBodyRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getBody()));

                    mImageView = (ImageView) findViewById(R.id.imageView);

                    // 添付画像を取得する
                    BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

                    // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
                    if (drawable != null) {
                        Bitmap bitmap = drawable.getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                        data.put("image", bitmapString);
                    }

                    mNameRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getName()));
                    mTitleRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getTitle()));
                    mUidRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getUid()));

                    String body = mBodyRef.getKey();
                    String name = mNameRef.getKey();
                    String title = mTitleRef.getKey();
                    String uid = mUidRef.getKey();

                    data.put("body", body);
                    data.put("name", name);
                    data.put("title", title);
                    data.put("uid", uid);

                    mFavouriteRef = dataBaseReference.child(Const.FavouritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                    mFavouriteRef.setValue(data);


                    Map<String, String> data2 = new HashMap<String, String>();

                    ArrayList<Answer> answer = mQuestion.getAnswers();

                    /*

                    String answerBody = (String) answer.get("body");
                    String answerName = (String) answer.get("name");
                    String answerUid = (String) answer.get("uid");
                    
                    data2.put("body", answerBody);
                    data2.put("name", answerName);
                    data2.put("uid", answerUid);

                    */

                    mFavouriteAnswerRef = dataBaseReference.child(Const.FavouritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
                    mFavouriteAnswerRef.setValue(data2);

                    mFavouriteGenreRef = dataBaseReference.child(Const.FavouritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid()).child(String.valueOf(mQuestion.getGenre()));
                    map.put(mFavouriteRef.getKey(), mFavouriteGenreRef.getKey());

                } else {
                    mFavouriteRef = dataBaseReference.child(Const.FavouritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                    mFavouriteRef.removeValue();
                    map.remove(mFavouriteRef.getKey());
                }

                if(map.containsKey(mFavouriteRef.getKey())) {
                    Button btn =(Button) findViewById(R.id.button);
                    Drawable orange_btn = ResourcesCompat.getDrawable(getResources(),R.drawable.orange_button, null);
                    btn.setBackground(orange_btn);
                } else {
                    Button btn =(Button) findViewById(R.id.button);
                    Drawable default_btn = ResourcesCompat.getDrawable(getResources(),R.drawable.default_button, null);
                    btn.setBackground(default_btn);
                }

            }

        });

    }

}