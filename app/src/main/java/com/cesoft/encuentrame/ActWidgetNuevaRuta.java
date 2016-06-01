package com.cesoft.encuentrame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.cesoft.encuentrame.models.Ruta;

public class ActWidgetNuevaRuta extends Activity//AppCompatActivity porque se muestra como dialogo
{
	private ProgressDialog _progressDialog;
	private EditText _txtNombre;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_widget_nuevo);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		_txtNombre = (EditText)findViewById(R.id.txtNombre);
System.err.println("----------------------_txtNombre="+_txtNombre);
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
System.err.println("----------------------5");
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);//_progressDialog.setIcon(R.mipmap.ic_launcher);//funcionaria si dialogo tuviese titulo
		_progressDialog.hide();
System.err.println("----------------------6");
		//
		//Util.setSvcContext(this);
		Util.initBackendless(this);
		if( ! Util.isLogged())
		{
			Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
			finish();
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
				r.guardar(new AsyncCallback<Ruta>()
				{
					@Override
					public void handleResponse(Ruta ruta)
					{
						Util.setTrackingRoute(ActWidgetNuevaRuta.this, ruta.getObjectId());
						//System.err.println("--------------A----------------------------------Ruta:addNuevo:ruta: "+ruta);
						_progressDialog.dismiss();
						Toast.makeText(ActWidgetNuevaRuta.this, getString(R.string.ok_guardar_ruta), Toast.LENGTH_SHORT).show();
						ActWidgetNuevaRuta.this.finish();
					}
					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						//TODO: Why?
						//For some fucking reason it always fails the first time!!!
						//BackendlessFault{ code: 'Server.Processing', message: 'java.lang.RuntimeException: java.lang.RuntimeException: java.lang.RuntimeException: com.mysql.jdbc.MysqlDataTruncation: Data truncation: Incorrect string value: '\xAC\xED\x00\x05sr...' for column 'DATE_FORMAT.2296E689-85BC-7C4A-FF39-CA9471B53B00' at row 1' }
						if(flag[0] == 0)
						{
							//System.err.println("--------------B--------------------------Lugar:addNuevo:lugar:backendlessFault: "+backendlessFault);
							flag[0]++;
							r.guardar(this);
							return;
						}
						System.err.println("ActWidgetNuevaRuta:addNuevo:backendlessFault: "+backendlessFault);
						_progressDialog.hide();//if(_progressDialog.isShowing())
						Toast.makeText(ActWidgetNuevaRuta.this, String.format(getString(R.string.error_guardar), backendlessFault), Toast.LENGTH_LONG).show();
					}
				});
    		}
		});
	}
}
