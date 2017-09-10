package com.new9.recogbusinesscard.Fragment;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.new9.recogbusinesscard.Activity.MainActivity;
import com.new9.recogbusinesscard.Class.Card;
import com.new9.recogbusinesscard.R;

/**
 * Created by SEGU on 2017-05-27.
 */


public class ColorFragment extends Fragment {
    private TextView cardName;
    private TextView cardContact;
    private TextView cardEmail;
    private TextView cardCompany;
    private TextView cardNameEng;
    private TextView cardNickname;
    private ImageView cardFace;
    private TextView cardAddress;

    private FirebaseStorage storage;

    private static final String EXTRA_COLOR = "com.new9.recogbussinesscard.Fragment.ColorFragment.EXTRA_COLOR";
    private static final String CARD = "CARD";
    private static final String USERID = "USERID";
    FrameLayout mMainLayout;
    Card card;
    String userId;

    public static ColorFragment newInstance(int backgroundColor, Card card, String userId) {
        ColorFragment fragment = new ColorFragment();
        Bundle bdl = new Bundle();
        bdl.putInt(EXTRA_COLOR, backgroundColor);
        bdl.putSerializable(CARD, card);
        bdl.putString(USERID, userId);
        fragment.setArguments(bdl);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        card = (Card) (getArguments() != null ? getArguments().getSerializable(CARD) : null);
        userId = (String)(getArguments() != null ? getArguments().getString(USERID) : 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dummy, container, false);
        Bundle bdl = getArguments();

        mMainLayout = (FrameLayout) v.findViewById(R.id.main_layout);

        cardName = (TextView) v.findViewById(R.id._nameTextView);
        cardContact = (TextView) v.findViewById(R.id._contactTextView);
        cardEmail = (TextView) v.findViewById(R.id._emailTextView);
        cardCompany = (TextView) v.findViewById(R.id._companyTextView);
        cardNameEng = (TextView) v.findViewById(R.id._nameEngTextView);
        cardNickname = (TextView) v.findViewById(R.id._nicknameTextView);
        cardAddress = (TextView) v.findViewById(R.id._addressTextView);
        cardFace = (ImageView) v.findViewById(R.id._faceImageView);
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://recogbusinesscard.appspot.com").child(userId);

        LayerDrawable bgDrawable = (LayerDrawable) mMainLayout.getBackground();
        GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.background_shape);
        shape.setColor(bdl.getInt(EXTRA_COLOR));

        cardName.setText(card.getName());
        cardContact.setText(card.getPhone());
        cardEmail.setText(card.getEmail());
        cardCompany.setText(card.getCompany());
        cardNameEng.setText(card.getEngname());
        cardNickname.setText(card.getPosition());
        cardAddress.setText(card.getAddress());

        String filename = MainActivity.generateFileNameWithEmail(card.getEmail());
        StorageReference imageReference = storageReference.child(filename+".jpg");
        Log.d("log","eh");
        if(imageReference != null) {
            imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(mMainLayout.getContext()).load(uri).into(cardFace);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    cardFace.setImageResource(R.drawable.boy);
                }
            });
        }
        return v;
    }
}
