package com.cesoft.encuentrame3.views;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Login;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.svc.GeotrackingService;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import com.cesoft.encuentrame3.widget.WidgetRutaJobService;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActWidgetNuevaRuta extends Activity
{
	private static final String TAG = ActWidgetNuevaRuta.class.getSimpleName();

	@Inject	Util util;
	@Inject Login login;

	private EditText txtNombre;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_widget_nuevo);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		App.getComponent().inject(this);
		if(!login.isLogged())gotoLogin();

		txtNombre = findViewById(R.id.txtNombre);
		txtNombre.setHint(R.string.nueva_ruta);
		txtNombre.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                save();
                return true;
            }
            return false;
        });
	}
	public void gotoLogin()
	{
		login.logout(this);
		Intent intent = new Intent(getBaseContext(), ActLogin.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if( ! login.isLogged())
		{
			login.login(new Fire.AuthListener() {
				@Override
				public void onExito(FirebaseUser usr) {
					Log.e(TAG, "ActWidgetNuevRuta:Login:OK:usr="+usr.getEmail());
				}
				@Override
				public void onFallo(Exception e) {
					Log.e(TAG, "ActWidgetNuevRuta:Login:KO:e:---------------------------------------", e);
					Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
					finish();
				}
			});
		}
		//
		ImageButton btnSave = findViewById(R.id.btnSave);
		btnSave.setOnClickListener(v -> save());
	}

	private boolean oncePideActivarGPS = true;
	private boolean oncePideActivarBateria = true;
	private boolean oncePideActivarBateria2 = true;
	private void save() {
		if(oncePideActivarBateria && util.pideBateria(this)) {
			oncePideActivarBateria = false;
			return;
		}
		if(oncePideActivarBateria2 && util.pideBateriaDeNuevoSiEsNecesario(this)) {
			oncePideActivarBateria2 = false;
			return;
		}

        if(pidePermisosGPS()) {
            return;
        }
		if(oncePideActivarGPS) {
			util.pideActivarGPS(this, 6868);
			oncePideActivarGPS = false;
			return;
		}

		if(txtNombre.getText().length() < 1) {
			Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.sin_nombre), Toast.LENGTH_SHORT).show();
			return;
		}

		final int[] flag = new int[]{0};
		final Ruta r = new Ruta();
		r.setNombre(txtNombre.getText().toString());
		r.setDescripcion("Widget");
		r.guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				util.setTrackingRoute(id, txtNombre.getText().toString());
				Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.ok_guardar_ruta), Toast.LENGTH_SHORT).show();
				ActWidgetNuevaRuta.this.finish();
				//
				GeotrackingService.start(getApplicationContext());
				WidgetRutaJobService.start(getApplicationContext());
			}
			@Override
			protected void onError(String err, int code)
			{
				if(flag[0] == 0)
				{
					flag[0]++;
					r.guardar(this);
					return;
				}
				Log.e(TAG, "ActWidgetNuevaRuta:addNuevo:e:----------------------------------"+err);
				Toast.makeText(ActWidgetNuevaRuta.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
				//
				WidgetRutaJobService.start(getApplicationContext());
			}
		});
	}


	// Permisos de GPS
	private boolean pidePermisosGPS()
	{
		int pFine = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
		int pCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
		if(pFine == PackageManager.PERMISSION_DENIED && pCoarse == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{
					android.Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
			}, 6969);
			return true;
		}
		return false;
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		try {
			for(int i=0; i < permissions.length; i++)
				Log.e(TAG, "onRequestPermissionsResult------------------- requestCode = "
					+ requestCode + " : " + permissions[i] + " = " + grantResults[i]);
		}
		catch(Exception e) {
			Log.e(TAG, "onRequestPermissionsResult:e:------------------------------------------",e);
		}
	}
}