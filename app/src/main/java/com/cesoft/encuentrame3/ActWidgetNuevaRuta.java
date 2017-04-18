package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.widget.WidgetRutaService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import javax.inject.Inject;

public class ActWidgetNuevaRuta extends Activity//AppCompatActivity porque se muestra como dialogo
{
	private static final String TAG = "CESoft:";
	@Inject
	Util _util;
	@Inject Login _login;
	private ProgressDialog _progressDialog;
	private EditText _txtNombre;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_widget_nuevo);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		_txtNombre = (EditText)findViewById(R.id.txtNombre);
		_txtNombre.setHint(R.string.nueva_ruta);
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
		//Login _login = ((App)getApplication()).getGlobalComponent().login();
		if( ! _login.isLogged())
		{
			_login.login(new Login.AuthListener()
			{
				@Override
				public void onExito(FirebaseUser usr)
				{
					Log.w(TAG, "ActWidgetNuevRuta:Login:OK:usr="+usr.getEmail());
				}
				@Override
				public void onFallo(Exception e)
				{
					Log.e(TAG, "ActWidgetNuevRuta:Login:KO:e="+e, e);
					Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
					finish();
				}
			});
		}
		//
		//Util.setTrackingRoute("");
		//_txtNombre.setText("");
		//TODO: ruta_activa = false
		//TODO: desactiva boton stop
		//
		ImageButton btnSave = (ImageButton)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(_txtNombre.getText().length() < 1)
				{
					Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.sin_nombre), Toast.LENGTH_SHORT).show();
					return;
				}
				_progressDialog.show();//runOnUiThread(new Runnable()

				// if(ruta_activa)"quiere parar la ruta actual y crear una nueva?"
				//TODO: ruta_activa = true
				//TODO: activa boton stop

				final int[] flag = new int[]{0};
				final Ruta r = new Ruta();
				r.setNombre(_txtNombre.getText().toString());
				r.setDescripcion("Widget");
				r.guardar(new DatabaseReference.CompletionListener()
				{
					@Override
					public void onComplete(DatabaseError err, DatabaseReference data)
					{
						if(err == null)
						{
							_util.setTrackingRoute(data.getKey());
							_progressDialog.dismiss();
							Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.ok_guardar_ruta), Toast.LENGTH_SHORT).show();
							ActWidgetNuevaRuta.this.finish();

							WidgetRutaService.startServ(ActWidgetNuevaRuta.this.getApplicationContext());
						}
						else
						{
							if(flag[0] == 0)
							{
								flag[0]++;
								r.guardar(this);
								return;
							}
							Log.e(TAG, "ActWidgetNuevaRuta:addNuevo:backendlessFault: "+err);
							_progressDialog.hide();//if(_progressDialog.isShowing())
							Toast.makeText(ActWidgetNuevaRuta.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();

							Intent i = new Intent(ActWidgetNuevaRuta.this, WidgetRutaService.class);
							ActWidgetNuevaRuta.this.startService(i);
						}
					}
				});
		  	}
		});
	}
}
