package com.new9.recogbusinesscard.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonParser;
import com.new9.recogbusinesscard.Class.Card;
import com.new9.recogbusinesscard.Class.User;
import com.new9.recogbusinesscard.Class.Word;
import com.new9.recogbusinesscard.Class.WordParser;
import com.new9.recogbusinesscard.R;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by SEGU on 2017-05-27.
 */

public class RegistercardActivity extends AppCompatActivity {

    private final int SELECT_PICTURE = 1;
    private final int RECOG_CARD = 2;
    private ImageView faceImageView;
    private User user;

    private EditText name;
    private EditText engname;
    private EditText company;
    private EditText position;
    private EditText email;
    private EditText phone;
    private EditText address;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseStorage storage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registercard);

        user = (User) getIntent().getSerializableExtra("CURRENTUSER");
        faceImageView = (ImageView) findViewById(R.id.faceImageView);

        name = (EditText) findViewById(R.id.name);
        engname = (EditText) findViewById(R.id.engname);
        company = (EditText) findViewById(R.id.company);
        position = (EditText) findViewById(R.id.position);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        address = (EditText) findViewById(R.id.address);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user").child(user.getId());

    }
    public void onRecogBusinessCard(View view){
        Intent intent = new Intent(getApplicationContext(), RecogActivity.class);
        startActivityForResult(intent,RECOG_CARD);
    }


    public void pickImage(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onRegisterClicked(View view){
        if(name.getText().toString().equals("")) {
            name.setError(getString(R.string.name_error));
            return;
        }
        if(company.getText().toString().equals("")) {
            company.setError(getString(R.string.company_error));
            return;
        }
        if(email.getText().toString().equals("")){
            email.setError(getString(R.string.email_error));
            return;
        }
        if(phone.getText().toString().equals("")){
            phone.setError(getString(R.string.phone_error));
            return;
        }
        final View tempView = view;
        final Card newCard = new Card(
                name.getText().toString(),
                engname.getText().toString(),
                address.getText().toString(),
                phone.getText().toString(),
                company.getText().toString(),
                position.getText().toString(),
                email.getText().toString());
        final String keyValue = MainActivity.generateFileNameWithEmail(email.getText().toString());
        myRef.child("userlist").child(keyValue).setValue(newCard)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReferenceFromUrl("gs://recogbusinesscard.appspot.com");
                        StorageReference fileReference = storageReference.child(user.getId()).child(keyValue+".jpg");
                        faceImageView.setDrawingCacheEnabled(true);
                        faceImageView.buildDrawingCache();
                        if(faceImageView.getDrawable() != null){
                            Bitmap bitmap =faceImageView.getDrawingCache();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask uploadTask = fileReference.putBytes(data);
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Intent intent = new Intent();
                                    intent.putExtra("ADDCARD",newCard);
                                    setResult(RESULT_OK,intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    setResult(RESULT_CANCELED);
                                    finish();;
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setResult(RESULT_CANCELED);
                        finish();;
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURE){
            if(resultCode == RESULT_OK){
                Glide.with(this).load(data.getData()).centerCrop().into(faceImageView);
            }
        }else if(requestCode == RECOG_CARD){
            if(resultCode ==RESULT_OK){
                String t = data.getStringExtra("RESULT");

                try {
                    JSONParser jsonParser = new JSONParser();
                    org.json.simple.JSONObject result = (org.json.simple.JSONObject) jsonParser.parse(t);
                    JSonControl(result);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void JSonControl(org.json.simple.JSONObject result){
        JSONArray regionsArray = (JSONArray) result.get("regions");
        ArrayList<Word> wordsList = new ArrayList<>();
        for(int i=0;i<regionsArray.size();i++){
            org.json.simple.JSONObject tempRegion = (org.json.simple.JSONObject) regionsArray.get(i);
            JSONArray tempLinesArray = (JSONArray) tempRegion.get("lines");
            for(int j=0;j<tempLinesArray.size();j++){
                org.json.simple.JSONObject tempLine = (org.json.simple.JSONObject) tempLinesArray.get(j);
                JSONArray tempWordArray = (JSONArray) tempLine.get("words");
                Word word = new Word();
                for(int k=0;k<tempWordArray.size();k++){
                    org.json.simple.JSONObject tempWord = (org.json.simple.JSONObject) tempWordArray.get(k);
                    String parsedWord = (String) tempWord.get("text");
                    word.addString(parsedWord);
                }
                wordsList.add(word);
            }
        }

        for(int i=0;i<wordsList.size();i++){
            Word w = wordsList.get(i);
            for(int j=0;j<w.getSize();j++){
                Log.d("word"+j+" : ",w.get(j));
            }
        }

        WordParser wp = new WordParser(wordsList);
        wp.bcr();

        name.setText(wp.getName());
        address.setText(wp.getAddress());
        position.setText(wp.getPosition());
        company.setText(wp.getCompany());
        phone.setText(wp.getPhone());
        email.setText(wp.getEmail());


        Toast.makeText(this,result.toString(),Toast.LENGTH_LONG);
    }
}


