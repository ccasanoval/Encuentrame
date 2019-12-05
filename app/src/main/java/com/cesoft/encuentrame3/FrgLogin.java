package com.cesoft.encuentrame3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cesoft.encuentrame3.di.components.GlobalComponent;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.cesoft.encuentrame3.ActLogin.ENTER;
import static com.cesoft.encuentrame3.ActLogin.RECOVER;
import static com.cesoft.encuentrame3.ActLogin.REGISTER;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class FrgLogin extends Fragment //implements GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = FrgLogin.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Login login;

    //------------------------------------------------------------------------------------------
    //GOOGLE LOG IN
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult();
            if (task.isSuccessful()) {
Log.e(TAG, "onActivityResult---------------------------------------------------------");
                if(account != null)
                    firebaseAuthWithGoogle(account);

            } else {
                //ERROR en SHA1 y google-services.json?
                Log.e(TAG, "onActivityResult: Google Sign In failed: "+task.toString()+" --- "+task.getException());
                Toast.makeText(getContext(), R.string.login_error, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        final String usr = acct.getEmail();
        Log.e(TAG, "firebaseAuthWithGoogle:"+acct.getId()+" ::: "+acct.getEmail());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Activity activity = getActivity();
        if(activity == null) return;
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(
            activity,
            task -> {
                if(task.getException() != null) {
                    Log.e(TAG, "----------------------------------------**********************----------exception= " + task.getException());
                }
                AuthResult authResult = task.getResult();
                if(authResult != null) {
                    Log.e(TAG, "----------------------------------------**********************----------result= " + authResult.getUser());
                    Log.e(TAG, "----------------------------------------**********************----------result= " + authResult.getCredential());
                    Log.e(TAG, "----------------------------------------**********************----------result= " + authResult.getAdditionalUserInfo());
                }
                logginOk(usr);
            });
    }

    private void logginOk(String usr) {
        ActLogin activity = (ActLogin)getActivity();
        if(activity == null) return;
        ((App)activity.getApplication()).iniServicesDependantOnLogin();
        Toast.makeText(activity, String.format(getString(R.string.login_ok), usr), Toast.LENGTH_LONG).show();
        activity.goMain();
    }


    // Returns a new instance of this fragment for the given section number.
    public static FrgLogin newInstance(int sectionNumber)
    {
        FrgLogin fragment = new FrgLogin();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        GlobalComponent c = App.getComponent();
        if(c != null) login = c.login();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ActLogin activity = (ActLogin)getActivity();
        if(activity == null) return null;
        if(login == null || login.isLogged()) activity.goMain();

        final int sectionNumber = getArguments()!=null ? getArguments().getInt(ARG_SECTION_NUMBER) : 0;
        final View rootView = inflater.inflate(R.layout.act_login_frag, container, false);

        switch(sectionNumber)
        {
            default:
            case ENTER:
                enter(rootView);
                break;
            case REGISTER:
                register(rootView);
                break;
            case RECOVER:
                recover(rootView);
                break;
        }

        return rootView;
    }

    private void enter(View rootView) {
        final TextView lblTitulo = rootView.findViewById(R.id.lblTitulo);
        final EditText txtPassword = rootView.findViewById(R.id.txtPassword);
        final EditText txtEmail = rootView.findViewById(R.id.txtEmail);
        final Button btnSend = rootView.findViewById(R.id.btnSend);
        final Button btnPrivacyPolicy = rootView.findViewById(R.id.btnPrivacyPolicy);
        final SignInButton btnGoogle = rootView.findViewById(R.id.btnGoogle);
        final TextInputLayout lblPassword2 = rootView.findViewById(R.id.lblPassword2);

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
            //main.iniEsperaLogin();
            login.login(txtEmail.getText().toString(), txtPassword.getText().toString(),
                    new Fire.AuthListener()
                    {
                        @Override
                        public void onExito(FirebaseUser usr) {
                            logginOk(usr.getEmail());
                        }
                        @Override
                        public void onFallo(Exception e) {
                            //main.finEsperaLogin();
                            Toast.makeText(getContext(), getString(R.string.login_error), Toast.LENGTH_LONG).show();
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

        firebaseAuth = FirebaseAuth.getInstance();

        Activity activity = getActivity();
        if(activity != null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, gso);
            login.setGoogleSignInClient(signInClient);

            btnGoogle.setOnClickListener(view -> {
                Intent intent = signInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            });
        }
    }


    private void register(View rootView) {
        final TextView lblTitulo = rootView.findViewById(R.id.lblTitulo);
        final EditText txtPassword = rootView.findViewById(R.id.txtPassword);
        final EditText txtPassword2 = rootView.findViewById(R.id.txtPassword2);
        final EditText txtEmail = rootView.findViewById(R.id.txtEmail);
        final Button btnSend = rootView.findViewById(R.id.btnSend);
        final SignInButton btnGoogle = rootView.findViewById(R.id.btnGoogle);

        btnGoogle.setVisibility(View.GONE);
        lblTitulo.setText(getString(R.string.register_lbl));
        btnSend.setOnClickListener(v -> {
            if( ! txtPassword.getText().toString().equals(txtPassword2.getText().toString()))
            {
                Toast.makeText(getContext(), getString(R.string.register_bad_pass), Toast.LENGTH_LONG).show();
                return;
            }
            //main.iniEsperaLogin();
            login.addUser(txtEmail.getText().toString(), txtPassword.getText().toString(),
                    new Fire.AuthListener()
                    {
                        @Override
                        public void onExito(FirebaseUser usr)
                        {
                            //main.finEsperaLogin();
                            if(usr != null)
                                Toast.makeText(getContext(), getString(R.string.register_ok)+"  "+usr.getEmail(), Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onFallo(Exception e)
                        {
                            //main.finEsperaLogin();//TODO: Añadir %s en la cadena
                            Toast.makeText(getContext(), getString(R.string.register_ko)+"  "+e, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void recover(View rootView) {
        final TextView lblTitulo = rootView.findViewById(R.id.lblTitulo);
        final EditText txtEmail = rootView.findViewById(R.id.txtEmail);
        final Button btnSend = rootView.findViewById(R.id.btnSend);
        final SignInButton btnGoogle = rootView.findViewById(R.id.btnGoogle);
        final TextInputLayout lblPassword = rootView.findViewById(R.id.lblPassword);
        final TextInputLayout lblPassword2 = rootView.findViewById(R.id.lblPassword2);

        btnGoogle.setVisibility(View.GONE);
        lblPassword.setVisibility(View.GONE);
        lblPassword2.setVisibility(View.GONE);
        lblTitulo.setText(getString(R.string.recover_lbl));
        btnSend.setOnClickListener(v -> login.restoreUser(txtEmail.getText().toString(),
                new Fire.AuthListener()
                {
                    @Override
                    public void onExito(FirebaseUser usr)
                    {
                        Toast.makeText(rootView.getContext(), R.string.recover_ok, Toast.LENGTH_LONG).show();
                        ActLogin activity = (ActLogin)getActivity();
                        if(activity != null)
                            activity.selectTabEnter();
                    }
                    @Override
                    public void onFallo(Exception e)
                    {
                        Log.e(TAG, String.format("RECOVER:e:%s",e), e);
                        Toast.makeText(rootView.getContext(), R.string.recover_ko +"  "+ e.toString(), Toast.LENGTH_LONG).show();
                    }
                }));
    }
}
