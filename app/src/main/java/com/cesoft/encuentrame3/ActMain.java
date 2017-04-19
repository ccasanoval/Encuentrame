package com.cesoft.encuentrame3;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;


//https://guides.codepath.com/android/Handling-Scrolls-with-CoordinatorLayout
//https://developer.android.com/training/permissions/requesting.html
//MOCK LOCATIONS ON DEVICE : http://stackoverflow.com/questions/2531317/android-mock-location-on-device

//TODO: cuando matas X solo queda fragment con patalla lugares, tanto para rutas como para avisos...?
//TODO: Servicio solo arrancado si hay avisos activos!!!!!!!!!!!!!!!

//TODO: Avisar con TextToVoice y permitir no hacerlo mediante las opciones....
//TODO: Cambiar ListView por recyclerview
//TODO: comprobar cuando dos moviles funcionan con la misma clave, hay problema? o solo sandras's
//TODO: AVISO: no molestar mas por hoy
//TODO: main window=> Number or routes, places and geofences...
//TODO: Egg?
//TODO: Google auth?
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: Develop a web app for points management : connect to backendless by REST API...
//TODO: Preparar para tablet
//TODO: Opcion que diga no preguntar por activar GPS (en tablet que no tiene gps... es un coñazo)
//http://developer.android.com/intl/es/training/basics/supporting-devices/screens.html
// small, normal, large, xlarge   ///  low (ldpi), medium (mdpi), high (hdpi), extra high (xhdpi)

////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity implements FrgMain.MainIterface
{
	private static final String TAG = ActMain.class.getSimpleName();
	public static final String PAGINA = "pagina", MENSAJE = "mensaje", DIRTY = "dirty";

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

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setSubtitle(Login.getCurrentUserName());
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_viewPager = null;
	}
	@Override
	public void onStart()
	{
		super.onStart();
		pideGPS();
		createViews();
	}
	@Override
	public void onStop()
	{
		super.onStop();
	}

	private void createViews()
	{
		Log.e(TAG, "-------------------- CREATE VIEWS -------------------");

		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		_viewPager = (ViewPager)findViewById(R.id.container);
		if(_viewPager != null)_viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
		if(tabLayout != null)
		{
			tabLayout.setupWithViewPager(_viewPager);
			tabLayout.setSelectedTabIndicatorHeight(10);
		}
		//tabLayout.setSelectedTabIndicatorColor();
		//tabLayout.setTabTextColors();

		try
		{
			Integer nPagina = getIntent().getIntExtra(PAGINA, -1);
			if(nPagina >= Util.LUGARES && nPagina <= Util.AVISOS)
				_viewPager.setCurrentItem(nPagina);
			String sMensaje = getIntent().getStringExtra(MENSAJE);
			if(sMensaje != null && !sMensaje.isEmpty())
				Toast.makeText(ActMain.this, sMensaje, Toast.LENGTH_LONG).show();
		}
		catch(Exception e){Log.e(TAG, String.format("onCreate:e:%s",e), e);}
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
				startActivityForResult(new Intent(this, ActConfig.class), Util.CONFIG);
				return true;
			case R.id.action_mapa:
				i = new Intent(this, ActMaps.class);
				i.putExtra(Util.TIPO, _viewPager.getCurrentItem());//_sectionNumber
				startActivity(i);
				return true;
			case R.id.action_buscar:
			//	FrgMain._apf[_viewPager.getCurrentItem()].buscar();
				FrgMain f = _aFrg[_viewPager.getCurrentItem()];
		for(int j=0; j < _aFrg.length; j++)Log.e(TAG, "********************** "+j+" : "+_aFrg[j]);
				if(f == null)
				{
					Log.e(TAG, "---------------------------------------------------- F = NULL  "+_viewPager.getCurrentItem() + "  :: "+_aFrg.length);
					return false;
				}
				buscar(f, f.getFiltro());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
Log.e(TAG, "onActivityResult------------------------------requestCode="+requestCode+" :: resultCode="+resultCode);
		if(resultCode != RESULT_OK)return;
		if(requestCode == Util.CONFIG)gotoLogin();
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
		startActivityForResult(new Intent(this, ActLugar.class), Util.LUGARES);
	}
	public void onAviso()
	{
		startActivityForResult(new Intent(this, ActAviso.class), Util.AVISOS);
	}
	public void onRuta()
	{
		startActivityForResult(new Intent(this, ActRuta.class), Util.RUTAS);
	}
	//---
	public void goLugar(Objeto obj)
	{
		Intent i = new Intent(this, ActLugar.class);
		i.putExtra(Lugar.NOMBRE, obj);
		startActivityForResult(i, Util.LUGARES);
	}
	public void goAviso(Objeto obj)
	{
		Intent i = new Intent(this, ActAviso.class);
		i.putExtra(Aviso.NOMBRE, obj);
		startActivityForResult(i, Util.AVISOS);
	}
	public void goRuta(Objeto obj)
	{
		try
		{
			Intent i = new Intent(this, ActRuta.class);
			i.putExtra(Ruta.NOMBRE, obj);
			startActivityForResult(i, Util.RUTAS);
		}
		catch(Exception e){Log.e(TAG, "------------------goRuta:onItemEdit:e:-----------------------", e);}
	}
	//---
	public void goLugarMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Lugar.NOMBRE, obj);
		startActivityForResult(i, Util.LUGARES);
	}
	public void goAvisoMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Aviso.NOMBRE, obj);
		startActivityForResult(i, Util.AVISOS);
	}
	public void goRutaMap(Objeto obj)
	{
		try
		{
			Intent i = new Intent(this, ActMaps.class);
			i.putExtra(Ruta.NOMBRE, obj);
			startActivityForResult(i, Util.RUTAS);
		}
		catch(Exception e){Log.e(TAG, "goRutaMap:RUTAS:e:-------------------------------------------", e);}
	}
	//---
	public void buscar(Fragment f, Filtro filtro)
	{
		try
		{
			Intent i = new Intent(f.getContext(), ActBuscar.class);//_main.getApplicationContext
			i.putExtra(Filtro.FILTRO, filtro);
			f.startActivityForResult(i, Util.BUSCAR);
		}
		catch(Exception e){Log.e(TAG, "buscar:e:----------------------------------------------------", e);}
	}
	//---
	public int getCurrentItem()
	{
		if(_viewPager == null)return Util.NADA;
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
Log.e(TAG, "--------------SectionsPagerAdapter:getItem---------"+position+"-------------------------"+_aFrg[position]);
			return _aFrg[position];//TODO : enhance
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
			case Util.LUGARES:	return getString(R.string.lugares);
			case Util.RUTAS:	return getString(R.string.rutas);
			case Util.AVISOS:	return getString(R.string.avisos);
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