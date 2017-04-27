package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActWidgetNuevoLugar extends Activity//AppCompatActivity porque se muestra como dialogo
{
	private static final String TAG = ActWidgetNuevoLugar.class.getSimpleName();
	@Inject
	Util _util;
	@Inject	Login _login;
	private ProgressDialog _progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_widget_nuevo);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
		//Util.setSvcContext(this);
		if( ! _login.isLogged())
		{
			Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
			finish();
		}
		//
		final EditText txtNombre = (EditText)findViewById(R.id.txtNombre);
		ImageButton btnSave = (ImageButton)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(txtNombre.getText().length() < 1)
				{
					Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.sin_nombre), Toast.LENGTH_SHORT).show();
					return;
				}
				_progressDialog.show();//runOnUiThread(new Runnable()

				Location pos = _util.getLocation();
				if(pos == null)
				{
					Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.sin_posicion), Toast.LENGTH_SHORT).show();
					Log.e(TAG, "ActWidgetNuevoLugar:onResume:btnSave:onClick: pos == null");
					return;
				}
				final int[] flag = new int[]{0};
				final Lugar l = new Lugar();
				l.setLatitud(pos.getLatitude());l.setLongitud(pos.getLongitude());
				l.setNombre(txtNombre.getText().toString());
				l.setDescripcion("Widget");
				l.guardar(new Fire.CompletadoListener() {
					@Override
					protected void onDatos(String id)
					{
						_progressDialog.dismiss();
						Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.ok_guardar_lugar), Toast.LENGTH_SHORT).show();
						ActWidgetNuevoLugar.this.finish();
					}
					@Override
					protected void onError(String err, int code)
					{
						if(flag[0] == 0)
						{
							flag[0]++;
							l.guardar(this);
							return;
						}
						Log.e(TAG, "ActWidgetNuevoLugar:addNuevo:backendlessFault: "+err);
						_progressDialog.hide();//if(_progressDialog.isShowing())
						Toast.makeText(ActWidgetNuevoLugar.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
					}
				});
  		  	}
		});
	}
}
