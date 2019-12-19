package com.cesoft.encuentrame3.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.presenters.PreMain;
import com.cesoft.encuentrame3.util.Login;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.adapters.SectionsPagerAdapter;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.svc.ActividadIntentService;
import com.cesoft.encuentrame3.svc.GeofenceStore;
import com.cesoft.encuentrame3.svc.GeofencingService;
import com.cesoft.encuentrame3.svc.GeotrackingService;
import com.cesoft.encuentrame3.svc.ServiceNotifications;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by César Casanova
public class ActMain extends AppCompatActivity implements
		PreMain.IVista,
		FrgMain.MainIterface,
		NavigationView.OnNavigationItemSelectedListener {

	private static final String TAG = ActMain.class.getSimpleName();
	private static final int ASK_GPS_PERMISSION = 6969;
	private static final int ASK_GPS_ACTIVATION = 6968;

	private DrawerLayout drawerLayout;
	private ViewPager viewPager;
	private MenuItem vozMenuItem;
	private SectionsPagerAdapter sectionsPagerAdapter;

	@Inject	Login login;
	@Inject	Util util;
	@Inject	Voice voice;
	@Inject	PreMain presenter;

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		App.getComponent().inject(this);

		presenter.onCreate(this);

		if(!login.isLogged())gotoLogin();

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitleTextAppearance(this, R.style.toolbarTextStyle);
		setSupportActionBar(toolbar);

		createViews();
		processIntent(getIntent());

		if(!login.isLogged())gotoLogin();

		util.pideBateria(this);

		if(util.compruebaPermisosGPS(this, ASK_GPS_PERMISSION)) {
			util.pideActivarGPS(this, ASK_GPS_ACTIVATION);
			startServices();
		}

		/// Drawer Menu
		drawerLayout = findViewById(R.id.main_content);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.lugares, R.string.rutas);
		toggle.syncState();
		drawerLayout.addDrawerListener(toggle);
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		///
		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		View headerView = navigationView.getHeaderView(0);
		TextView userName = headerView.findViewById(R.id.userName);
		TextView userEmail = headerView.findViewById(R.id.userEmail);
		ImageView userImage = headerView.findViewById(R.id.userImage);

		userName.setText(Login.getCurrentUserName());
		userEmail.setText(Login.getCurrentUserEmail());
		Login.getCurrentUserImage(new CustomTarget<Bitmap>() {
			@Override
			public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
				userImage.setImageBitmap(resource);
			}
			@Override
			public void onLoadCleared(@Nullable Drawable placeholder) {
				//Log.e(TAG, "onLoadCleared------------------------------------------------------");
			}
		});
	}
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		processIntent(intent);
	}
	private void processIntent(Intent intent) {
		if(intent == null) return;
		gotoPage(intent);
		showMensaje(intent);
		int nPagina = intent.getIntExtra(Constantes.WIN_TAB, Constantes.NADA);
		boolean stop = intent.getBooleanExtra(ServiceNotifications.ACTION_STOP, false);
		String msg = intent.getStringExtra(Constantes.MENSAJE);
		if(msg != null)
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		if(stop) {
			switch(nPagina) {
				case Constantes.AVISOS:
					GeofencingService.stop();
					break;
				case Constantes.RUTAS:
					GeotrackingService.stop(this);
					break;
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		if(drawerLayout.isDrawerOpen(GravityCompat.END))
			drawerLayout.closeDrawer(GravityCompat.END);
		else
			super.onBackPressed();
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public void onPause()
	{
		voice.stopListening();
		super.onPause();
	}
	//----------------------------------------------------------------------------------------------
	@Override
	public void onResume()
	{
		super.onResume();
		if(voice.isListening()) {
			voice.startListening();
		}
	}
	//----------------------------------------------------------------------------------------------
	@Override
	public void onStart()
	{
		super.onStart();
		EventBus.getDefault().register(this);
	}
	//----------------------------------------------------------------------------------------------
	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
		if(menu != null) menu.close();
	}
	//----------------------------------------------------------------------------------------------
	@Override
	public void onDestroy()
	{
		viewPager = null;
		super.onDestroy();
	}

	//----------------------------------------------------------------------------------------------
	private void createViews()
	{
		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		viewPager = findViewById(R.id.main_container);
		viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
	}
	private void gotoPage(Intent intent)
	{
		int nPagina = intent.getIntExtra(Constantes.WIN_TAB, Constantes.NADA);
		if(nPagina == Constantes.LUGARES || nPagina == Constantes.RUTAS || nPagina == Constantes.AVISOS)
			viewPager.setCurrentItem(nPagina);
	}
	private void showMensaje(Intent intent)
	{
		String sMensaje = intent.getStringExtra(Constantes.MENSAJE);
		if(sMensaje != null && !sMensaje.isEmpty())
			Toast.makeText(this, sMensaje, Toast.LENGTH_LONG).show();
	}

	private void refreshVoiceIcon() {
		if(vozMenuItem != null)
			vozMenuItem.setIcon(voice.isListening() ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_white_24dp);
	}

	//----------------------------------------------------------------------------------------------
	private Menu menu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.menu = menu;
		getMenuInflater().inflate(R.menu.menu_act_main, menu);
		vozMenuItem = menu.findItem(R.id.action_voz);
		refreshVoiceIcon();
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will automatically handle clicks on the
		// Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id)
		{
			case R.id.action_mapa:
				goMap();
				return true;
			case R.id.action_buscar:
				buscar(sectionsPagerAdapter.getPage(viewPager.getCurrentItem()));
				return true;
			case R.id.action_voz:
				voice.checkPermissions(this);
				voice.toggleListening();
				refreshVoiceIcon();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Subscribe(sticky = true, threadMode = ThreadMode.POSTING)
	public void onVoiceEvent(Voice.VoiceStatusEvent event) {
		refreshVoiceIcon();
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onCommandEvent(Voice.CommandEvent event) {
		Toast.makeText(this, event.getText(), Toast.LENGTH_LONG).show();

		switch(event.getCommand()) {
			case R.string.voice_new_point:
				viewPager.setCurrentItem(Constantes.LUGARES);
				onLugar(true);
				voice.speak(event.getText());
				break;
			case R.string.voice_new_route:
			case R.string.voice_new_route2:
				viewPager.setCurrentItem(Constantes.RUTAS);
				onRuta(true);
				voice.speak(event.getText());
				break;
			case R.string.voice_stop_route:
				util.setTrackingRoute("", "");
				voice.speak(event.getText());
				break;
			case R.string.voice_new_alert:
				viewPager.setCurrentItem(Constantes.AVISOS);
				onAviso(true);
				voice.speak(event.getText());
				break;
			case R.string.voice_map:
				goMap();
				voice.speak(event.getText());
				break;
			case R.string.voice_stop_listening:
				voice.turnOffListening();
				voice.speak(event.getText());
				break;

			/*
			case R.string.voice_name:
				break;
			case R.string.voice_description:
				break;
			case R.string.voice_radious:
				break;
			case R.string.voice_metres:
				break;
			case R.string.voice_kilometers:
				break;*/

			default:
				break;
		}
	}

	//----------------------------------------------------------------------------------------------
	/// FrgMain : MainIterface
	public void gotoLogin()
	{
		Intent intent = new Intent(getBaseContext(), ActLogin.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	//---
	public void onLugar(boolean isVoiceCommand)
	{
		Intent intent = new Intent(this, ActLugar.class);
		intent.putExtra(Voice.NAME, isVoiceCommand);
		startActivityForResult(intent, Constantes.LUGARES);
	}
	public void onAviso(boolean isVoiceCommand)
	{
		Intent intent = new Intent(this, ActAviso.class);
		intent.putExtra(Voice.NAME, isVoiceCommand);
		startActivityForResult(intent, Constantes.AVISOS);
	}
	public void onRuta(boolean isVoiceCommand)
	{
		Intent intent = new Intent(this, ActRuta.class);
		intent.putExtra(Voice.NAME, isVoiceCommand);
		startActivityForResult(intent, Constantes.RUTAS);
	}
	//---
	public void goLugar(Objeto obj)
	{
		Intent intent = new Intent(this, ActLugar.class);
		intent.putExtra(Objeto.NOMBRE, obj);
		startActivityForResult(intent, Constantes.LUGARES);
	}
	public void goAviso(Objeto obj)
	{
		Intent intent = new Intent(this, ActAviso.class);
		intent.putExtra(Objeto.NOMBRE, obj);
		startActivityForResult(intent, Constantes.AVISOS);
	}
	public void goRuta(Objeto obj)
	{
		try
		{
			Intent intent = new Intent(this, ActRuta.class);
			intent.putExtra(Objeto.NOMBRE, obj);
			startActivityForResult(intent, Constantes.RUTAS);
		}
		catch(Exception e)
		{
			Log.e(TAG, "goRuta:onItemEdit:e:--------------------------------------------------", e);
		}
	}
	//---
	public void goMap() {
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Util.TIPO, viewPager.getCurrentItem());
		startActivity(i);
	}
	public void goLugarMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Objeto.NOMBRE, obj);
		i.putExtra(Util.TIPO, Constantes.LUGARES);
		startActivityForResult(i, Constantes.LUGARES);
	}
	public void goAvisoMap(Objeto obj)
	{
		Intent i = new Intent(this, ActMaps.class);
		i.putExtra(Objeto.NOMBRE, obj);
		i.putExtra(Util.TIPO, Constantes.AVISOS);
		startActivityForResult(i, Constantes.AVISOS);
	}
	public void goRutaMap(Objeto obj)
	{
		try
		{
			Intent i = new Intent(this, ActMaps.class);
			i.putExtra(Objeto.NOMBRE, obj);
			i.putExtra(Util.TIPO, Constantes.RUTAS);
			startActivityForResult(i, Constantes.RUTAS);
		}
		catch(Exception e)
		{
			Log.e(TAG, "goRutaMap:RUTAS:e:----------------------------------------------------", e);
		}
	}
	//---
	public void buscar(final FrgMain frg)
	{
		if(frg == null)return;
		try
		{
			if( ! frg.isAdded())
			{
				Log.e(TAG, "buscar: ********************* frg is not ADDED *********************");
				getSupportFragmentManager().beginTransaction().add(frg, String.valueOf(frg.getSectionNumber())).commit();
				util.exeDelayed(100, () -> buscar(frg));
				return;
			}

			Filtro fil = frg.getFiltro();
			Intent intent = new Intent(this, ActBuscar.class);
			intent.putExtra(Filtro.FILTRO, fil);
			frg.startActivityForResult(intent, Constantes.BUSCAR);
		}
		catch(Exception e)
		{
			Log.e(TAG, "buscar:e:-------------------------------------------------------------", e);
		}
	}
	//---
	public int getCurrentItem()
	{
		if(viewPager == null)return Constantes.NADA;
		return viewPager.getCurrentItem();
	}

	private void startServices() {
		GeotrackingService.start(this);
		GeofencingService.start();
		ActividadIntentService.start(this);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == ASK_GPS_PERMISSION) {// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				util.pideActivarGPS(this, ASK_GPS_ACTIVATION);
				//TODO: Ahora puedes activar servicios localizacion y geofence
				startServices();
			} else {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
				dialogBuilder.setNegativeButton(getString(R.string.cancelar), (dlg, which) -> finish());
				dialogBuilder.setPositiveButton(getString(R.string.ok), (dialog1, which) ->
						util.compruebaPermisosGPS(this, ASK_GPS_PERMISSION));
				final AlertDialog dlgEliminar = dialogBuilder.create();
				dlgEliminar.setTitle(R.string.permission_required_title);
				dlgEliminar.setMessage(getString(R.string.permission_required));
				dlgEliminar.show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ASK_GPS_ACTIVATION) {
			Log.e(TAG, "onActivityResult: ASK_GPS_ACTIVATION: resultCode=" + resultCode);
		}
		else {
			Log.e(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onGeofenceStoreEvent(GeofenceStore.Event event) {
		NavigationView navigationView = findViewById(R.id.nav_view);
		MenuItem item = navigationView.getMenu().findItem(R.id.nav_geofencing_onoff);
//Log.e(TAG, "onGeofenceStoreEvent--------------------------------------------------------------------item="+item+" : "+GeofencingService.isOn()+" : "+event.isOn());
		//TODO: si event.isOn == false ---> el servicio deberia estar intentando arrancar? o dejar todo desactivado?
		if(GeofencingService.isOn()) {
			if(item != null)item.setTitle(R.string.stop_geofencing);
		}
		else {
			if(item != null)item.setTitle(R.string.start_geofencing);
		}
	}

	/// Implements NavigationView.OnNavigationItemSelectedListener
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		Intent intent;
		int id = item.getItemId();
		switch(id) {
			case R.id.nav_geofencing_onoff:
				if(GeofencingService.isOn())
					GeofencingService.turnOff();
				else
					GeofencingService.turnOn();
				return true;

			case R.id.nav_config:
			case R.id.nav_voice:
			case R.id.nav_about:
				intent = new Intent(this, ActSettings.class);
				intent.putExtra(Constantes.SETTINGS_PAGE, id);
				startActivity(intent);
				return true;

			case R.id.nav_privacy_policy:
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cesweb-ef91a.firebaseapp.com"));
				startActivity(intent);
				return true;

			case R.id.nav_logout:
				gotoLogin();
				return true;
			case R.id.nav_exit:
				System.exit(0);
				return true;

			default:
				return false;
		}
	}


	/// Implements PreMain.IVista
	@Override
	public Activity getAct() {
		return null;
	}

	@Override
	public void iniEspera() {

	}

	@Override
	public void finEspera() {

	}

	@Override
	public void toast(int msg) {

	}

	@Override
	public void toast(int msg, @org.jetbrains.annotations.Nullable String err) {

	}
}