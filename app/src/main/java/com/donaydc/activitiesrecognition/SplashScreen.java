package com.donaydc.activitiesrecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Person;
import com.squareup.picasso.Picasso;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class SplashScreen extends AppCompatActivity {

    Button BTLogIn, BTTra, BTRec;
    ImageView ProI;
    String UserEmail = "null@uninorte.edu.co";
    int RC_SIGN_IN = 1;
    GoogleApiClient mGoogleApiClient;
    TextView txtNombre;
    ProgressDialog progressDialog;
    String profileGender = null;
    String profileBirthday = null;
    private static final String TAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_image);
        ProI =(ImageView)findViewById(R.id.ProfeileImag);

        setContentView(R.layout.activity_splash_screen);
        BTLogIn = (Button)findViewById(R.id.BTL);
        BTTra=(Button)findViewById(R.id.Training);
        BTRec=(Button)findViewById(R.id.Recognition);
        txtNombre = (TextView)findViewById(R.id.TXNombre);

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope("profile"))
                        .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed: ");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        BTLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                updateUI(false);
                            }
                        });
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        BTTra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Intent1 = new Intent(SplashScreen.this, Main2Activity.class);
                Intent1.putExtra("Email",UserEmail);
                startActivity(Intent1);
            }
        });

        BTRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Intent2 = new Intent(SplashScreen.this, MainActivity.class);
                Intent2.putExtra("Email",UserEmail);
                startActivity(Intent2);
            }
        });
    }

    public void setUp() throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        String clientId = "913147898105-s4avqcb6vgnftgi4vceau95tq0hdam2m.apps.googleusercontent.com";
        String clientSecret = "1cOneoSGc_CjQEkmQEjswEr5";

        String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
        String scope = "https://www.googleapis.com/auth/contacts.readonly";

        String authorizationUrl =
                new GoogleBrowserClientRequestUrl(clientId, redirectUrl, Arrays.asList(scope)).build();

        System.out.println("Go to the following link in your browser:");
        System.out.println(authorizationUrl);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("What is the authorization code?");
        String code = in.readLine();

        GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(
                        httpTransport, jsonFactory, clientId, clientSecret, code, redirectUrl)
                        .execute();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setFromTokenResponse(tokenResponse);

        PeopleService peopleService =
                new PeopleService.Builder(httpTransport, jsonFactory, credential).build();

        Person profile = peopleService.people().get("people/me")
                .setPersonFields("names,emailAddress,genders,birthdays")
                .execute();

        onPostExecute(profile);

    }

    protected void onPostExecute(Person person) {
        if (person != null) {
            if (person.getGenders() != null && person.getGenders().size() > 0) {
                profileGender = person.getGenders().get(0).getValue();
            }
            if (person.getBirthdays() != null && person.getBirthdays().get(0).size() > 0) {
                Date dobDate = person.getBirthdays().get(0).getDate();
                if (dobDate.getYear() != null) {
                    profileBirthday = dobDate.getYear() + "-" + dobDate.getMonth() + "-" + dobDate.getDay();
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();
            txtNombre.setText(acct.getDisplayName());
            UserEmail = acct.getEmail();
            LoadImage(acct.getPhotoUrl());

            BTRec.setEnabled(true);
            BTTra.setEnabled(true);

            updateUI(true);
        } else {
            updateUI(false);
        }
    }

    private void LoadImage (Uri Uimage) {
        Picasso.with(this)
                .load(Uimage)
                .placeholder(R.drawable.circlerun)
                .error(R.drawable.circlerun)
                .into(ProI);
        Drawable originalDrawable = ProI.getDrawable();
        Bitmap originalBitmap = ((BitmapDrawable) originalDrawable).getBitmap();
        Bitmap resized = Bitmap.createScaledBitmap(originalBitmap, 150, 150, true);
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), resized);
        roundedDrawable.setCornerRadius(resized.getHeight());
        ProI.setImageDrawable(roundedDrawable);
        BTLogIn.setBackground(roundedDrawable);
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            //Toast.makeText(this, "Connection Sucessfull", Toast.LENGTH_SHORT).show();
        } else {
            ProI.setImageDrawable(getResources().getDrawable(R.drawable.circlerun));
            BTLogIn.setBackground(getResources().getDrawable(R.drawable.circlerun));
            txtNombre.setText("Pick a new user!");
            BTRec.setEnabled(false);
            BTTra.setEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
                private void hideProgressDialog() {
                    progressDialog.hide();
                }
            });
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Looking for user");
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }
}
