package com.new9.recogbusinesscard.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;
import com.bartoszlipinski.flippablestackview.utilities.ValueInterpolator;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.new9.recogbusinesscard.Adapter.ColorFragmentAdapter;
import com.new9.recogbusinesscard.Class.Card;
import com.new9.recogbusinesscard.Class.User;
import com.new9.recogbusinesscard.Fragment.ColorFragment;
import com.new9.recogbusinesscard.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by SEGU on 2017-05-27.
 */

public class MainActivity extends AppCompatActivity {

    private FlippableStackView mFlippableStack;

    private ColorFragmentAdapter mPageAdapter;

    private TextView cardName;
    private TextView cardContact;
    private TextView cardEmail;
    private TextView cardCompany;
    private TextView cardNameEng;
    private TextView cardNickname;
    private TextView cardAddress;
    private ImageView cardFace;

    private LinearLayout loginNeedLayout;
    private LinearLayout myCardLayout;
    private CoordinatorLayout floatingLayout;

    private final int REGISTER_CARD = 11;

    private boolean loginState = false;
    private User user;
    private List<Card> cards = new LinkedList<Card>();

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private List<Fragment> mViewPagerFragments = new ArrayList<>();
    private FirebaseStorage storage;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginNeedLayout = (LinearLayout) findViewById(R.id.loginNeedLayout);
        myCardLayout = (LinearLayout) findViewById(R.id.myCardLayout);
        floatingLayout = (CoordinatorLayout) findViewById(R.id.samples_fab);

        cardName = (TextView) findViewById(R.id.nameTextView);
        cardNameEng = (TextView) findViewById(R.id.nameEngTextView);
        cardContact = (TextView) findViewById(R.id.contactTextView);
        cardEmail = (TextView) findViewById(R.id.emailTextView);
        cardCompany = (TextView) findViewById(R.id.companyTextView);
        cardNickname = (TextView) findViewById(R.id.nicknameTextView);
        cardAddress = (TextView) findViewById(R.id.addressTextView);
        cardFace = (ImageView) findViewById(R.id.faceImageView);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loginState) {
                    Intent intent = new Intent(getApplicationContext(), RegistercardActivity.class);
                    intent.putExtra("CURRENTUSER", user);
                    startActivityForResult(intent, REGISTER_CARD);
                }
                else{
                    Snackbar.make(view,"로그인이 필요합니다.", Snackbar.LENGTH_LONG).setAction("이동", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
                }
            }
        });


        loginState = getIntent().getBooleanExtra("LOGINSTATE",false);
        if(loginState){
            user = (User) getIntent().getSerializableExtra("CURRENTUSER");
            loginNeedLayout.setVisibility(View.GONE);
            myCardLayout.setVisibility(View.VISIBLE);
            storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl("gs://recogbusinesscard.appspot.com").child(user.getId());

            cardName.setText(user.getCard().getName());
            cardNameEng.setText(user.getCard().getEngname());
            cardContact.setText(user.getCard().getPhone());
            cardEmail.setText(user.getCard().getEmail());
            cardCompany.setText(user.getCard().getCompany());
            cardNickname.setText(user.getCard().getPosition());
            cardAddress.setText(user.getCard().getAddress());
            String filename = generateFileNameWithEmail(user.getCard().getEmail());
            StorageReference imageReference = storageReference.child(filename+".jpg");
            imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(cardFace);
                    Toast.makeText(getApplicationContext(), "다운로드 성공 : "+ uri, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "다운로드 실패 : ", Toast.LENGTH_SHORT).show();
                }
            });
            //사진
            flippableCardLoad();
        }
    }
    public void flippableCardLoad(){
        loadCards();
        createViewPagerFragments();
        cardAdapterInit();
    }

    public void loadCards(){
        cards.clear();
        Iterator<String> keys = user.getUserlist().keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            cards.add(user.getUserlist().get(key));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REGISTER_CARD){
            if(resultCode == RESULT_OK){
                Snackbar.make(floatingLayout, "명함추가에 성공하셨습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Card newCard = (Card) data.getSerializableExtra("ADDCARD");
                user.getUserlist().put(generateFileNameWithEmail(newCard.getEmail()), newCard);
                flippableCardLoad();
            }else{
                Snackbar.make(floatingLayout, "명함추가에 실패하셨습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(user != null)
            flippableCardLoad();
    }

    public void onLoginRequireClicked(View view){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("로그인 요청");
        Snackbar.make(floatingLayout, "로그인 페이지로 이동합니다.", Snackbar.LENGTH_LONG).setAction("이동", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }).show();
    }

    private void cardAdapterInit(){
        mPageAdapter = new ColorFragmentAdapter(getSupportFragmentManager(), mViewPagerFragments);

        boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        mFlippableStack = (FlippableStackView) findViewById(R.id.flippable_stack_view);
        mFlippableStack.initStack(4, portrait ?
                        StackPageTransformer.Orientation.VERTICAL :
                        StackPageTransformer.Orientation.HORIZONTAL,
                0.9f,0.7f,0.4f,StackPageTransformer.Gravity.CENTER
        );
        mFlippableStack.setAdapter(mPageAdapter);
    }
    private void createViewPagerFragments() {
        mViewPagerFragments.clear();

        int startColor = getResources().getColor(R.color.white);
        int startR = Color.red(startColor);
        int startG = Color.green(startColor);
        int startB = Color.blue(startColor);

        int endColor = getResources().getColor(R.color.white);
        int endR = Color.red(endColor);
        int endG = Color.green(endColor);
        int endB = Color.blue(endColor);

        ValueInterpolator interpolatorR = new ValueInterpolator(0, cards.size() - 1, endR, startR);
        ValueInterpolator interpolatorG = new ValueInterpolator(0, cards.size() - 1, endG, startG);
        ValueInterpolator interpolatorB = new ValueInterpolator(0, cards.size() - 1, endB, startB);

        for (int i = 0; i < cards.size(); ++i) {
            mViewPagerFragments.add(ColorFragment.newInstance(Color.argb(255, (int) interpolatorR.map(i), (int) interpolatorG.map(i), (int) interpolatorB.map(i)),cards.get(i),user.getId()));
        }
    }
    public static String generateFileNameWithEmail(String email){
        int param = email.indexOf("@");
        return email.substring(0,param);
    }

}
