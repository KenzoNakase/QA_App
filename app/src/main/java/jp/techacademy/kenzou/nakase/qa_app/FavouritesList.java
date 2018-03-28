package jp.techacademy.kenzou.nakase.qa_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FavouritesList extends AppCompatActivity {

    private Toolbar mToolbar_favourite;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFavouriteRef;


    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

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
        setContentView(R.layout.activity_favourites_list);
        mToolbar_favourite = (Toolbar) findViewById(R.id.toolbar_favourite);
        setSupportActionBar(mToolbar_favourite);

        mToolbar_favourite.setTitle("お気に入り");

        ArrayList<Map<String, String>> list =  new ArrayList<Map<String, String>>();

        Map<String, String> map = new HashMap<String, String>();
        map.put("title", "タイトル");
        map.put("body", "質問内容" );
        list.add(map);


        SimpleAdapter adapter = new SimpleAdapter(
                this,
                list,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "body"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("UI_PARTS", "クリック " + String.valueOf(position));
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Firebase
            mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFavouriteRef = mDatabaseReference.child(Const.FavouritesPATH).child(uid);

            // 選択したジャンルにリスナーを登録する
            if (mFavouriteRef != null) {
                mFavouriteRef.removeEventListener(mEventListener);
            }

            mFavouriteRef.addChildEventListener(mEventListener);
        }
    }

}
