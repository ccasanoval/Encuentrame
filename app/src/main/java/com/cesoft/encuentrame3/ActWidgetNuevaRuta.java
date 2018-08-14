package com.cesoft.encuentrame3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.svc.GeoTrackingJobService;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.widget.WidgetRutaService;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActWidgetNuevaRuta extends Activity
{
	private static final String TAG = ActWidgetNuevaRuta.class.getSimpleName();

	@Inject	Util _util;
	@Inject Login _login;
	@Inject Preferencias _pref;

	private ProgressDialog _progressDialog;
	private EditText _txtNombre;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_widget_nuevo);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		_txtNombre = findViewById(R.id.txtNombre);
		_txtNombre.setHint(R.string.nueva_ruta);
		_txtNombre.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                save();
                return true;
            }
            return false;
        });
		//_util = ((App)getApplication()).getGlobalComponent().util();
		((App)getApplication()).getGlobalComponent().inject(this);
	}
	@Override
	public void onPause()
	{
		super.onPause();
		_progressDialog.dismiss();
	}
	@Override
	public void onResume()
	{
		super.onResume();
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);//_progressDialog.setIcon(R.mipmap.ic_launcher);//funcionaria si dialogo tuviese titulo
		_progressDialog.hide();
		//
		if( ! _login.isLogged())
		{
			_login.login(new Fire.AuthListener() {
				@Override
				public void onExito(FirebaseUser usr) {
					Log.w(TAG, "ActWidgetNuevRuta:Login:OK:usr="+usr.getEmail());
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
		//TODO: ruta_activa = false
		//TODO: desactiva boton stop
		//
		ImageButton btnSave = findViewById(R.id.btnSave);
		btnSave.setOnClickListener(v -> save());
	}

	//private boolean oncePermisos = false;
	private boolean oncePideActivarGPS = true;
	private boolean oncePideActivarBateria = true;
	private boolean oncePideActivarBateria2 = true;
	private void save() {
		if(oncePideActivarBateria && _util.pideBateria(this)) {
			oncePideActivarBateria = false;
			return;
		}
		if(oncePideActivarBateria2 && _util.pideBateriaDeNuevoSiEsNecesario(this)) {
			oncePideActivarBateria2 = false;
			return;
		}

        if(pidePermisosGPS()) {
            return;
        }
		if(oncePideActivarGPS && _util.pideActivarGPS(this)) {
			oncePideActivarGPS = false;
			return;
		}

		if(_txtNombre.getText().length() < 1) {
			Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.sin_nombre), Toast.LENGTH_SHORT).show();
			return;
		}

		_progressDialog.show();

		// if(ruta_activa)"quiere parar la ruta actual y crear una nueva?"
		//TODO: ruta_activa = true
		//TODO: activa boton stop

		//TODO:añadir flags: ignore_battery_optimization, delay, etc
		final int[] flag = new int[]{0};
		final Ruta r = new Ruta();
		r.setNombre(_txtNombre.getText().toString());
		r.setDescripcion("Widget");
		r.guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				_util.setTrackingRoute(id);
				_progressDialog.dismiss();
				Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.ok_guardar_ruta), Toast.LENGTH_SHORT).show();
				ActWidgetNuevaRuta.this.finish();
				//
				refreshWidget();
				// Start tracking
				GeoTrackingJobService.start(getApplicationContext(), _pref.getTrackingDelay());
				//ActWidgetNuevaRuta.this.finish();
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
				_progressDialog.hide();
				Toast.makeText(ActWidgetNuevaRuta.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
				//
				refreshWidget();
			}
		});
	}


	private ServiceConnection _sc = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.e(TAG, "---------------------------- onServiceConnected --------------------------------------------");
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			WidgetRutaService.LocalBinder binder = (WidgetRutaService.LocalBinder)service;
			WidgetRutaService _svcWidget = binder.getService();
			if(_svcWidget != null) _svcWidget.refresh();
			WidgetRutaService.unbindSvc(_sc, getApplicationContext());
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) { }
	};
	//
	private void refreshWidget() {
		WidgetRutaService.bindSvc(_sc, getApplicationContext());
	}

	// Permisos de GPS
	private boolean pidePermisosGPS()
	{
		//if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ! ha.canAccessLocation())activarGPS(true);
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
	{
		try {
			for(int i=0; i < permissions.length; i++)
				Log.e(TAG, "onRequestPermissionsResult------------------- requestCode = "
					+ requestCode + " : " + permissions[i] + " = " + grantResults[i]);
		}catch(Exception ignore){}
	}
}
