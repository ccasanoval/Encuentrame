package com.cesoft.encuentrame3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.bumptech.glide.Glide;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.util.Log;

import javax.microedition.khronos.opengles.GL10;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActImagen extends AppCompatActivity
{
	private static final String TAG = ActImagen.class.getSimpleName();
	public static final String PARAM_LUGAR = "lugar";
	public static final String PARAM_IMG_PATH = "img_path";
	public static final String FILE_STR = "file:";

	public static final int IMAGE_CAPTURE = 6969;

	private static final int REQUEST_ACTION_IMAGE_CAPTURE = 6970;
	private static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 6971;
	private static final int REQUEST_PERMISSION_CAMERA = 6972;

	private Lugar lugar;
	private ImageView imageView;
	private String imgURLnew;

	private enum ESTADO {SIN_IMG, OLD_IMG, NEW_IMG}
	private ESTADO estado;


	// * Whether or not the system UI should be auto-hidden after
	// * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	private static final boolean AUTO_HIDE = true;

	// * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the system UI.
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	// * Some older devices needs a small delay between UI widget updates and a change of the status and navigation bar.
	private static final int UI_ANIMATION_DELAY = 300;
	private final Handler mHideHandler = new Handler();
	private View mContentView;
	private final Runnable mHidePart2Runnable = new Runnable()
	{
		@SuppressLint("InlinedApi")
		@Override
		public void run()
		{
			// Delayed removal of status and navigation bar
			// Note that some of these constants are new as of API 16 (Jelly Bean) and API 19 (KitKat).
			// It is safe to use them, as they are inlined at compile-time and do nothing on earlier devices.
			mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};
	private View mControlsView;
	private final Runnable mShowPart2Runnable = new Runnable()
	{
		@Override
		public void run()
		{
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null)
				actionBar.show();
			mControlsView.setVisibility(View.VISIBLE);
		}
	};
	private boolean mVisible;
	private final Runnable mHideRunnable = this::hide;
	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system UI.
	 * This is to prevent the jarring behavior of controls going away while interacting with activity UI.
	 */
	private final View.OnClickListener mHideClickListener = view ->
	{
		if(AUTO_HIDE) delayedHide(AUTO_HIDE_DELAY_MILLIS);
	};

	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_imagen);
		setTitle(getString(R.string.imagen));

		mVisible = true;
		mControlsView = findViewById(R.id.fullscreen_content_controls);
		mContentView = findViewById(R.id.imagen);
		imageView = (ImageView)mContentView;

		// Set up the user interaction to manually show or hide the system UI.
		mContentView.setOnClickListener(v -> toggle());

		// Upon interacting with UI controls, delay any scheduled hide() operations
		// to prevent the jarring behavior of controls going away while interacting with the UI.
		findViewById(R.id.tomar_foto).setOnClickListener(mHideClickListener);
		Button b = findViewById(R.id.tomar_foto);
		b.setOnClickListener(view -> dispatchTakePictureIntent());
		//----
		int[] maxTextureSize = new int[1];
		GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);

		//------------------------------------------------------------------------------------------
		try
		{
			imgURLnew = getIntent().getStringExtra(PARAM_IMG_PATH);
			lugar = getIntent().getParcelableExtra(PARAM_LUGAR);
			if(imgURLnew != null)
			{
				File file = new File(imgURLnew);
				if(file.exists())
				{
					try{
						Glide.with(this).load(imgURLnew).into(imageView);
						refreshMenu(ESTADO.NEW_IMG);
					}
					catch(OutOfMemoryError e)
					{
						//TODO: por que las imagenes no se ven en todos los dispositivos, HACER COMPATIBLES!!!!!!!!
						//TODO: guardar imagen reducida a 1Mb
						Log.e(TAG, "onCreate:BitmapFactory.decodeFile:e"+e,e);
					}
				}
				else
					imgURLnew = null;
			}
			if(imgURLnew == null)
			{
				if(lugar != null && lugar.getId() != null)
				{
					lugar.downloadImg(imageView, this, new Fire.SimpleListener<String>()
					{
						@Override
						public void onDatos(String[] aData)
						{
							Log.e(TAG, "onCreate: tiene imagen y se establecio----------------------- OK: ");
							refreshMenu(ESTADO.OLD_IMG);
						}
						@Override
						public void onError(String err)
						{
							Log.e(TAG, "onCreate: no tiene imagen o error---------------------------: "+err);
							refreshMenu(ESTADO.SIN_IMG);
							takePhoto();
						}
					});
				}
				else
				{
					Log.e(TAG, "onCreate: no existe url ni Lugar -----------------------------------");
					refreshMenu(ESTADO.SIN_IMG);
					takePhoto();
				}
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, "onCreate:e:-----------------------------------------------------------------",e);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been created, to briefly hint to the user that UI controls are available.
		delayedHide(100);
	}

	private void toggle()
	{
		if(mVisible)	hide();
		else			show();
	}

	private void hide()
	{
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)actionBar.hide();
		mControlsView.setVisibility(View.GONE);
		mVisible = false;
		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	@SuppressLint("InlinedApi")
	private void show()
	{
		// Show the system bar
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;
		// Schedule a runnable to display UI elements after a delay
		mHideHandler.removeCallbacks(mHidePart2Runnable);
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	// Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
	private void delayedHide(int delayMillis)
	{
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}



	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	private Menu menu = null;
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_img, menu);
		this.menu = menu;
		refreshMenu(estado);
		return true;
	}
	private void refreshMenu(ESTADO v)
	{
		estado = v;
		if(menu == null)return;
		if(estado == ESTADO.SIN_IMG)
		{
			menu.findItem(R.id.menu_eliminar).setVisible(false);
			menu.findItem(R.id.menu_guardar).setVisible(false);
			show();
		}
		else if(estado == ESTADO.NEW_IMG)
		{
			menu.findItem(R.id.menu_eliminar).setVisible(true);
			menu.findItem(R.id.menu_guardar).setVisible(true);
		}
		else if(estado == ESTADO.OLD_IMG)
		{
			menu.findItem(R.id.menu_eliminar).setVisible(true);
			menu.findItem(R.id.menu_guardar).setVisible(false);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			eliminar();
		return super.onOptionsItemSelected(item);
	}
	/// MENU

	private String currentPhotoPath;
	private File createImageFile() {
		// Create an image file name
		@SuppressLint("SimpleDateFormat")
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		///File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		try {
			path.mkdirs();// Make sure the Pictures directory exists.
			File image = File.createTempFile(imageFileName,".jpg", path);
			currentPhotoPath = FILE_STR + image.getAbsolutePath();
			Log.e(TAG, "createImageFile:----------------------------currentPhotoPath="+currentPhotoPath);
			return image;
		}
		catch (Exception e) {
			Log.e(TAG, "createImageFile:e:---------------------------- ",e);
			return null;
		}
	}

	//______________________________________________________________________________________________
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			try {
				File photoFile = createImageFile();
				if(photoFile != null) {
					Uri photoURI = FileProvider.getUriForFile(
							this,
							BuildConfig.APPLICATION_ID + ".provider",
							photoFile);
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
					startActivityForResult(takePictureIntent, REQUEST_ACTION_IMAGE_CAPTURE);
				}
			}
			catch(Exception e) {
				Log.e(TAG, "dispatchTakePictureIntent:e:-------------------------------------",e);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ACTION_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			refreshMenu(ESTADO.NEW_IMG);
			show();

			// Variable to store the img
			if (currentPhotoPath.contains(FILE_STR))
				imgURLnew = currentPhotoPath.substring(FILE_STR.length());

			// Show the thumbnail on ImageView
			try {
				Glide.with(this).load(imgURLnew).into(imageView);
				refreshMenu(ESTADO.NEW_IMG);
			} catch (Exception e) {
				Log.e(TAG, "onActivityResult:e:----------------------------------", e);
				Toast.makeText(this, R.string.error_img_path, Toast.LENGTH_LONG).show();
				return;
			}

			// ScanFile so it will be appeared on Gallery
			Uri imageUri = Uri.parse(currentPhotoPath);
			MediaScannerConnection.scanFile(this,
					new String[]{imageUri.getPath()},
					null,
					(path, uri) -> {
					});
		} else {
			finish();
			Log.e(TAG, "onActivityResult:---------else finish");
		}
	}


	//______________________________________________________________________________________________

	private void guardar()
	{
		Intent i = new Intent();
		i.putExtra(PARAM_IMG_PATH, imgURLnew);
		setResult(RESULT_OK, i);
		finish();
	}
	private void eliminar()
	{
		switch(estado)
		{
		case SIN_IMG:
			break;
		case NEW_IMG:
			imgURLnew = null;
			guardar();
			break;
		case OLD_IMG:
			lugar.delImg();
			finish();
			break;
		}
	}


	private boolean isCameraHardware() {
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}
	private boolean isNoStoragePermission() {
		int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		return permission != PackageManager.PERMISSION_GRANTED;
	}
	private boolean isNoCamaraPermission()
	{
		int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
		return permission != PackageManager.PERMISSION_GRANTED;
	}
	private void requestCameraPermission()
	{
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.CAMERA},
				REQUEST_PERMISSION_CAMERA);
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
	{
		//Log.e(TAG, "------------------------------------- requestCode = "+requestCode+" : ");
		//for(String s : permissions)Log.e(TAG, "------------------------------------- permissions = "+s);
		//for(int i : grantResults)Log.e(TAG, "------------------------------------- granted = "+i);
		switch(requestCode) {
			case REQUEST_PERMISSION_CAMERA:
			case REQUEST_PERMISSION_EXTERNAL_STORAGE:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
					takePhoto();
				break;
			default:break;
		}
	}


	private void requestStoragePermission() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
				REQUEST_PERMISSION_EXTERNAL_STORAGE);
	}


	private void takePhoto() {
		if( ! isCameraHardware()) {
			Toast.makeText(this, R.string.no_camera_hardware, Toast.LENGTH_LONG).show();
		}
		else if(isNoCamaraPermission()) {
			requestCameraPermission();
		}
		else if(isNoStoragePermission()) {
			requestStoragePermission();
		}
		else
			dispatchTakePictureIntent();
	}
}
