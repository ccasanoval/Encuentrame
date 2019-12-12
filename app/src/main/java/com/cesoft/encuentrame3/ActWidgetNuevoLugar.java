package com.cesoft.encuentrame3;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova
public class ActWidgetNuevoLugar extends Activity//AppCompatActivity porque se muestra como dialogo
{
	private static final String TAG = ActWidgetNuevoLugar.class.getSimpleName();
	@Inject	Util util;
	@Inject	Login login;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_widget_nuevo);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		((App)getApplication()).getGlobalComponent().inject(this);
		progressBar = findViewById(R.id.progressBar);
	}
	@Override
	public void onPause()
	{
		super.onPause();
		progressBar.setVisibility(View.INVISIBLE);
	}
	@Override
	public void onResume()
	{
		super.onResume();
		progressBar.setVisibility(View.INVISIBLE);
		//
		if( ! login.isLogged())
		{
			Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
			finish();
		}
		//
		ImageButton btnSave = findViewById(R.id.btnSave);
		btnSave.setOnClickListener(saveClickListener);
	}

	private final View.OnClickListener saveClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			final EditText txtNombre = findViewById(R.id.txtNombre);
			if(txtNombre.getText().length() < 1)
			{
				Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.sin_nombre), Toast.LENGTH_SHORT).show();
				return;
			}
			progressBar.setVisibility(View.VISIBLE);

			Location pos = util.getLocation();
			if(pos == null)
			{
				Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.sin_posicion), Toast.LENGTH_SHORT).show();
				Log.e(TAG, "ActWidgetNuevoLugar:onResume:btnSave:onClick: pos == null");
				progressBar.setVisibility(View.INVISIBLE);
				return;
			}
			final int[] flag = new int[]{0};
			final Lugar l = new Lugar();
			l.setLatLon(pos.getLatitude(), pos.getLongitude());
			l.setNombre(txtNombre.getText().toString());
			l.setDescripcion("Widget");
			l.guardar(new Fire.CompletadoListener() {
				@Override
				protected void onDatos(String id)
				{
					progressBar.setVisibility(View.INVISIBLE);
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
					Log.e(TAG, "addNuevo:onError:----------------------------------------------"+err);
					progressBar.setVisibility(View.INVISIBLE);
					Toast.makeText(ActWidgetNuevoLugar.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
				}
			});
		}
	};
}
