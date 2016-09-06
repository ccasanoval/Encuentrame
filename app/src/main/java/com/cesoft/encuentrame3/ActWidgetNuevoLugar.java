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

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame3.models.Lugar;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;


public class ActWidgetNuevoLugar extends Activity//AppCompatActivity porque se muestra como dialogo
{
	private ProgressDialog _progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_widget_nuevo);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
		if( ! Login.isLogged())
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

				Location pos = Util.getLocation(ActWidgetNuevoLugar.this);
				final int[] flag = new int[]{0};
				final Lugar l = new Lugar();
				l.setLatitud(pos.getLatitude());l.setLongitud(pos.getLongitude());
				l.setNombre(txtNombre.getText().toString());
				l.setDescripcion("Widget");
				l.guardar(new DatabaseReference.CompletionListener()
				{
					@Override
					public void onComplete(DatabaseError err, DatabaseReference data)
					{
						if(err == null)
						{
							_progressDialog.dismiss();
							Toast.makeText(ActWidgetNuevoLugar.this, getString(R.string.ok_guardar_lugar), Toast.LENGTH_SHORT).show();
							//TODO: send mesage to ActMain para que refresque lista? Tampoco importa, el widget tiene sentido si no tienes app avierta...
							ActWidgetNuevoLugar.this.finish();
						}
						else
						{
							if(flag[0] == 0)
							{
								flag[0]++;
								l.guardar(this);
								return;
							}
							System.err.println("ActWidgetNuevoLugar:addNuevo:backendlessFault: "+err);
							_progressDialog.hide();//if(_progressDialog.isShowing())
							Toast.makeText(ActWidgetNuevoLugar.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
						}
					}
				});
    		}
		});
	}
}
