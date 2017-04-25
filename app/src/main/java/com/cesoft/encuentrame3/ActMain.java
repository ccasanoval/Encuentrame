package com.cesoft.encuentrame3;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;


//https://guides.codepath.com/android/Handling-Scrolls-with-CoordinatorLayout
//https://developer.android.com/training/permissions/requesting.html
//MOCK LOCATIONS ON DEVICE : http://stackoverflow.com/questions/2531317/android-mock-location-on-device

//http://www.vogella.com/tutorials/Dagger/article.html

//TODO: Servicio solo arrancado si hay avisos activos!!!!!!!!!!!!!!!
//TODO: CESoft:getLista:onCancelled:DatabaseError: Permission denied  -----> Mostrar ventana de login...


//TODO: Avisar con TextToVoice y permitir no hacerlo mediante las opciones....
//TODO: Cambiar ListView por recyclerview
//TODO: comprobar cuando dos moviles funcionan con la misma clave, hay problema? o solo sandras's
//TODO: AVISO: no molestar mas por hoy
//TODO: main window=> Number or routes, places and geofences...
//TODO: Egg?
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: Preparar para tablet
//TODO: Opcion que diga no preguntar por activar GPS (en tablet que no tiene gps... es un coñazo)
//http://developer.android.com/intl/es/training/basics/supporting-devices/screens.html
// small, normal, large, xlarge   ///  low (ldpi), medium (mdpi), high (hdpi), extra high (xhdpi)

////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity implements FrgMain.MainIterface
{
	private static final String TAG = ActMain.class.getSimpleName();

	private FrgMain[] _aFrg = new FrgMain[3];
	private ViewPager _viewPager;
	private Login _login;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		//_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_content);

		_login = ((App)getApplication()).getGlobalComponent().login();
		//if(!_login.isLogged())gotoLogin();

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setSubtitle(Login.getCurrentUserName());
Log.e(TAG, "----------- MAIN CREATE");

		createViews();
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_viewPager = null;
//Log.e(TAG, "----------- MAIN DESTROY");
	}
	@Override
	public void onStart()
	{
//Log.e(TAG, "----------- MAIN START");
		super.onStart();
		pideGPS();
		if(!_login.isLogged())gotoLogin();
		//createViews();
	}
	@Override
	public void onStop()
	{
//Log.e(TAG, "----------- MAIN STOP");
		super.onStop();
	}

	private void createViews()
	{
//Log.e(TAG, "-------------------- CREATE VIEWS -------------------");

		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		_viewPager = (ViewPager)findViewById(R.id.container);
		_viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(_viewPager);
		tabLayout.setSelectedTabIndicatorHeight(10);

		try
		{
			Integer nPagina = getIntent().getIntExtra(Constantes.WIN_TAB, -1);
			if(nPagina >= Constantes.LUGARES && nPagina <= Constantes.AVISOS)
				_viewPager.setCurrentItem(nPagina);
			String sMensaje = getIntent().getStringExtra(Constantes.MENSAJE);
			if(sMensaje != null && !sMensaje.isEmpty())
				Toast.makeText(ActMain.this, sMensaje, Toast.LENGTH_LONG).show();
		}
		catch(Exception e){Log.e(TAG, "onCreate:e:--------------------------------------------------", e);}
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_act_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will automatically handle clicks on the
		// Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
		Intent i;
		int id = item.getItemId();
		switch(id)
		{
			case R.id.action_config:
				startActivityForResult(new Intent(this, ActConfig.class), Constantes.CONFIG);
				return true;
			case R.id.action_mapa:
				i = new Intent(this, ActMaps.class);
				i.putExtra(Util.TIPO, _viewPager.getCurrentItem());//_sectionNumber
				startActivity(i);
				return true;
			case R.id.action_buscar:
				FrgMain frg = _aFrg[_viewPager.getCurrentItem()];// frg==null cuando se libero mem y luego se activó app...
				if(frg == null)new SectionsPagerAdapter(getSupportFragmentManager()).getItem(_viewPager.getCurrentItem());
				buscar(_aFrg[_viewPager.getCurrentItem()]);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != RESULT_OK)return;
		if(requestCode == Constantes.CONFIG)gotoLogin();
		else super.onActivityResult(requestCode, resultCode, data);
	}

	//----------------------------------------------------------------------------------------------
	/// FrgMain : MainIterface
	public void gotoLogin()
	{
		_login.logout();
		Intent intent = new Intent(getBaseContext(), ActLogin.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	//---
	public void onLugar()
	{
		startActivityForResult(new Intent(this, ActLugar.class), Constantes.LUGARES);
	}
	public void onAviso()
	{
		startActivityForResult(new Intent(this, ActAviso.class), Constantes.AVISOS);
	}
	public void onRuta()
	{
		startActivityForResult(new Intent(this, ActRuta.class), Constantes.RUTAS);
	}
	//---
	public void goLugar(Objeto obj)
	{
		Intent i = new Intent(this, ActLugar.class);
		i.putExtra(Lugar.NOMBRE, obj);
		startActivityForResult(i, Constantes.LUGARES);
	}
	public void goAviso(Objeto obj)
	{
		Intent i = new Intent(this, ActAviso.class);
		i.putExtra(Aviso.NOMBRE, obj);
		startActivityForResult(i, Constantes.AVISOS);
	}
	public void goRuta(Objeto obj)
	{
		try
		{
			Intent i = new Intent(this, ActRuta.class);
			i.putExtra(Ruta.NOMBRE, obj);
			startActivityForResult(i, Constantes.RUTAS);
		}
		catch(Exception e){Log.e(TAG, "------------------goRuta:onItemEdit:e:-----------------------", e);}
	}
	//---
	public void goLugarMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Lugar.NOMBRE, obj);
		startActivityForResult(i, Constantes.LUGARES);
	}
	public void goAvisoMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Aviso.NOMBRE, obj);
		startActivityForResult(i, Constantes.AVISOS);
	}
	public void goRutaMap(Objeto obj)
	{
		try
		{
			Intent i = new Intent(this, ActMaps.class);
			i.putExtra(Ruta.NOMBRE, obj);
			startActivityForResult(i, Constantes.RUTAS);
		}
		catch(Exception e){Log.e(TAG, "goRutaMap:RUTAS:e:-------------------------------------------", e);}
	}
	//---
	public void buscar(final FrgMain frg)
	{
		try
		{
			if( ! frg.isAdded())
			{
				Log.e(TAG, "buscar: *************** frg is not ADDED *****************");
				getSupportFragmentManager().beginTransaction().add(frg, String.valueOf(frg._sectionNumber)).commit();
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						buscar(frg);
					}
				}, 100);
				return;
			}

			Filtro fil = frg.getFiltro();
			Intent i = new Intent(/*frg.getContext()*/this, ActBuscar.class);//_main.getApplicationContext
			i.putExtra(Filtro.FILTRO, fil);
			frg.startActivityForResult(i, Constantes.BUSCAR);
		}
		catch(Exception e){Log.e(TAG, "buscar:e:----------------------------------------------------", e);}
	}
	//---
	public int getCurrentItem()
	{
		if(_viewPager == null)return Constantes.NADA;
		return _viewPager.getCurrentItem();
	}



	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////
	// FRAGMEN PAGER ADAPTER
	////////////////////////////////////////////////////////////////////////////////////////////////
	private class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			_aFrg[position] = FrgMain.newInstance(position);
Log.e(TAG, "getItem--------------------["+position+"]="+_aFrg[position]+" : "+_aFrg[position]._sectionNumber);
			return _aFrg[position];
		}
		@Override
		public int getCount()
		{
			return 3;
		}
		@Override
		public CharSequence getPageTitle(int position)
		{
			switch(position)
			{
			case Constantes.LUGARES:	return getString(R.string.lugares);
			case Constantes.RUTAS:	return getString(R.string.rutas);
			case Constantes.AVISOS:	return getString(R.string.avisos);
			}
			return null;
		}
	}


	//----------------------------------------------------------------------------------------------
	public void pideGPS()//TODO: a Util.pideGPS(Activity a)
	{
		//if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ! ha.canAccessLocation())activarGPS(true);
		int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
		if(permissionCheck == PackageManager.PERMISSION_DENIED)
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 6969);
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
	{
		Log.e(TAG, "-------------------------------------------------------------------------------- requestCode = "+requestCode+" : ");
		/*if(requestCode == 6969)
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				try{_Map.setMyLocationEnabled(true);}catch(SecurityException ignore){}*/
	}

}