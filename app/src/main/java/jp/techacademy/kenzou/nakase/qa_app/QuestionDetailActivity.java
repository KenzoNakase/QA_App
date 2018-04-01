package jp.techacademy.kenzou.nakase.qa_app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import android.util.Log;

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

import static jp.techacademy.kenzou.nakase.qa_app.MainActivity.map;


public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;
    private DatabaseReference mFavouriteRef;
    private DatabaseReference mFavouriteGenreRef;
    private DatabaseReference mIsFavouriteRef;
    private DatabaseReference mGenreRef;

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

    private ChildEventListener mFavouriteListener = new ChildEventListener() {
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
            mFavouriteRef.addChildEventListener(mFavouriteListener);
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

                    mGenreRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre()));
                    String genre = mGenreRef.getKey();
                    data.put("genre", genre);
                    mFavouriteRef = dataBaseReference.child(Const.FavouritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                    mFavouriteGenreRef = dataBaseReference.child(Const.FavouritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid()).child(String.valueOf(mQuestion.getGenre()));
                    mFavouriteRef.setValue(data);
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