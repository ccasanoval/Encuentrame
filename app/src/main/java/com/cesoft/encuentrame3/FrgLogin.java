package com.cesoft.encuentrame3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.cesoft.encuentrame3.ActLogin.ENTER;
import static com.cesoft.encuentrame3.ActLogin.RECOVER;
import static com.cesoft.encuentrame3.ActLogin.REGISTER;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class FrgLogin extends Fragment implements GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = FrgLogin.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ActLogin main;
    private Login login;

    //------------------------------------------------------------------------------------------
    //GOOGLE LOG IN
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onPause() {
        super.onPause();
        if(googleApiClient != null && getActivity() != null)
            googleApiClient.stopAutoManage(getActivity());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.e(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this.getActivity(), "Google Play Services Error", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                if(account != null)
                    firebaseAuthWithGoogle(account);
            }
            else
            {
                //ERROR en SHA1 y google-services.json?
                Log.e(TAG, "onActivityResult: Google Sign In failed: "+result.toString()+" --- "+result.getStatus()+" --- "+result.getSignInAccount());
                Toast.makeText(getContext(), R.string.login_error, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        final String usr = acct.getEmail();
        Log.e(TAG, "firebaseAuthWithGoogle:"+acct.getId()+" ::: "+acct.getEmail());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(main,
                task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(FrgLogin.this.main, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        logginOk(usr);
                    }
                });
    }

    private void logginOk(String usr) {
        ((App) main.getApplication()).iniServicesDependantOnLogin();
        main.finEsperaLogin();
        Toast.makeText(main, String.format(getString(R.string.login_ok), usr), Toast.LENGTH_LONG).show();
        main.goMain();
    }


    // Returns a new instance of this fragment for the given section number.
    public static FrgLogin newInstance(int sectionNumber, ActLogin main)
    {
        FrgLogin fragment = new FrgLogin();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.main = main;

        return fragment;
    }

    public FrgLogin(){}

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        login = App.getComponent(main).login();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(login.isLogged()) main.goMain();

        final int sectionNumber = getArguments()!=null ? getArguments().getInt(ARG_SECTION_NUMBER) : 0;
        final View rootView = inflater.inflate(R.layout.act_login_frag, container, false);
        final TextView lblTitulo = rootView.findViewById(R.id.lblTitulo);
        final EditText txtPassword = rootView.findViewById(R.id.txtPassword);
        final EditText txtPassword2 = rootView.findViewById(R.id.txtPassword2);
        final EditText txtEmail = rootView.findViewById(R.id.txtEmail);
        final Button btnSend = rootView.findViewById(R.id.btnSend);
        final Button btnPrivacyPolicy = rootView.findViewById(R.id.btnPrivacyPolicy);
        final SignInButton btnGoogle = rootView.findViewById(R.id.btnGoogle);
        final TextInputLayout lblPassword = rootView.findViewById(R.id.lblPassword);
        final TextInputLayout lblPassword2 = rootView.findViewById(R.id.lblPassword2);

        switch(sectionNumber)
        {
            case ENTER://----------------------- ----------------------------------------------------
                lblPassword2.setVisibility(View.GONE);
                lblTitulo.setText(getString(R.string.enter_lbl));
                //
                btnPrivacyPolicy.setOnClickListener(v -> {
                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://cesweb-ef91a.firebaseapp.com"));
                    startActivity(browserIntent);
                });
                //
                btnSend.setOnClickListener(v -> {
                    main.iniEsperaLogin();
                    login.login(txtEmail.getText().toString(), txtPassword.getText().toString(),
                            new Fire.AuthListener()
                            {
                                @Override
                                public void onExito(FirebaseUser usr) {
                                    logginOk(usr.getEmail());
                                }
                                @Override
                                public void onFallo(Exception e) {
                                    main.finEsperaLogin();
                                    Toast.makeText(main, getString(R.string.login_error), Toast.LENGTH_LONG).show();
                                }
                            });
                });
                txtPassword.setOnEditorActionListener((v, actionId, event) -> {
                    if((event != null && (event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER))
                            || (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE))
                    {
                        btnSend.callOnClick();
                    }
                    return false;
                });
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                googleApiClient = new GoogleApiClient.Builder(main)
                        .enableAutoManage(main/* FragmentActivity */, this/* OnConnectionFailedListener */)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
                firebaseAuth = FirebaseAuth.getInstance();
                btnGoogle.setOnClickListener(view -> {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });
                break;

            case REGISTER://------------------------------------------------------------------------
                btnGoogle.setVisibility(View.GONE);
                lblTitulo.setText(getString(R.string.register_lbl));
                btnSend.setOnClickListener(v -> {
                    if( ! txtPassword.getText().toString().equals(txtPassword2.getText().toString()))
                    {
                        Toast.makeText(main, getString(R.string.register_bad_pass), Toast.LENGTH_LONG).show();
                        return;
                    }
                    main.iniEsperaLogin();
                    Login.addUser(txtEmail.getText().toString(), txtPassword.getText().toString(),
                            new Fire.AuthListener()
                            {
                                @Override
                                public void onExito(FirebaseUser usr)
                                {
                                    main.finEsperaLogin();
                                    if(usr != null)
                                        Toast.makeText(main, getString(R.string.register_ok)+"  "+usr.getEmail(), Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onFallo(Exception e)
                                {
                                    main.finEsperaLogin();//TODO: Añadir %s en la cadena
                                    Toast.makeText(main, getString(R.string.register_ko)+"  "+e, Toast.LENGTH_LONG).show();
                                }
                            });
                });
                break;

            case RECOVER://-------------------------------------------------------------------------
                btnGoogle.setVisibility(View.GONE);
                lblPassword.setVisibility(View.GONE);
                lblPassword2.setVisibility(View.GONE);
                lblTitulo.setText(getString(R.string.recover_lbl));
                btnSend.setOnClickListener(v -> Login.restoreUser(txtEmail.getText().toString(),
                        new Fire.AuthListener()
                        {
                            @Override
                            public void onExito(FirebaseUser usr)
                            {
                                Toast.makeText(rootView.getContext(), R.string.recover_ok, Toast.LENGTH_LONG).show();
                                main.selectTabEnter();
                            }
                            @Override
                            public void onFallo(Exception e)
                            {
                                Log.e(TAG, String.format("RECOVER:e:%s",e), e);
                                Toast.makeText(rootView.getContext(), R.string.recover_ko +"  "+ e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }));
                break;
        }

        return rootView;
    }
}
