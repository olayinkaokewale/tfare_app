package com.wwwebteam.clients.tfare.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerEngine {
	
	//Declare static variables and methods
//	public static final String URL = "http://192.168.173.1:80/tfare/index.php";
	public static final String URL = "http://192.168.43.102:80/tfare/index.php";
//	public static final String URL = "http://tfare.ng/app/index.php";
	public static final String FEEDBACK = "feedback", MESSAGE = "message", DETAILS = "details";
	//End statics
	
	public JSONDecoder jDecoder;
	HttpURLConnection httpconn;
	
	//Constructor
	public ServerEngine() {
		jDecoder = new JSONDecoder();
	}
	
	// METHOD TO GET THE DISTANCE OF A RESTAURANT
	public static String getDistance(String USER_GPS_COORD, String GPS_COORD) {
		final double coordToMeters = 0.00000714285714;
		String finalDistance = null;
		if (!USER_GPS_COORD.equals("")  && !GPS_COORD.equals("")) { //Find the distance.
			String[] userGpsCoords = USER_GPS_COORD.split(",");
			String[] gpsCoords = GPS_COORD.split(",");
			
			double latDiff = Math.abs(Double.valueOf(userGpsCoords[0]) - Double.valueOf(gpsCoords[0]));
			double longDiff = Math.abs(Double.valueOf(userGpsCoords[1]) - Double.valueOf(gpsCoords[1]));
			
			double latMetersDiff = latDiff/coordToMeters;
			double longMetersDiff = longDiff/coordToMeters;
			
			double straightDistance = Math.sqrt((latMetersDiff*latMetersDiff)+(longMetersDiff*longMetersDiff));
			
			if (straightDistance < 1000) {
				finalDistance = ((int) straightDistance) + "m";
			} else if (straightDistance >= 1000) {
				finalDistance = ((int) straightDistance/1000) + "km";
			}
		}
		return finalDistance;
	}
	// DONE WITH THIS METHOD
	
	// METHOD TO HASH ANY STRING TO MD5
	public static String stringHash(String text) {
		String hashed = null;
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(text.getBytes(), 0, text.length());
			hashed = new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		return hashed;
	}

	// METHOD TO GENERATE BARCODES WITH ZXING LIBRARY
	public static Bitmap generateQRBitMap(final String content) {

		Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();

		hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);

		QRCodeWriter qrCodeWriter = new QRCodeWriter();

		try {
			BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints);

			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();

			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {

					bmp.setPixel(x , y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
				}
			}

			return bmp;
		} catch (WriterException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	// METHOD TO ENCODE IMAGE STRING FOR IMAGE UPLOAD
	public static String encodeBitmapToBase64(Bitmap image) {
		System.gc();
		if (image == null) return null;
		Bitmap imagex = image;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		imagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		
		byte[] b = baos.toByteArray();
		String imageEncode = Base64.encodeToString(b, Base64.DEFAULT);
		return imageEncode;
	}
	
	//METHOD TO DECODE IMAGE STRING AND STORE INTO FILE
	public static String decodeAndSaveFile(String encodedImgStr, String foodPictureLink, Context context) {
		byte[] b = Base64.decode(encodedImgStr, Base64.DEFAULT);
		
		//Do the file saving things.
		String path = "/Android/data/"+ context.getPackageName() +"/";
		File main_folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android");
		File data_folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data");
		File app_folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path);
		if (!main_folder.exists()) {
			main_folder.mkdir();
		}
		if (!data_folder.exists()) {
			data_folder.mkdir();
		}
		if (!app_folder.exists()) {
			app_folder.mkdir();
		}
		File imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path + foodPictureLink); //.split(".")[0]
		InputStream in = null;
	    OutputStream out = null;
	    try {
	        in = new ByteArrayInputStream(b);
	        out = new FileOutputStream(imageFile);
	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        System.out.println("Exception in copyFile"+e);
	    }
		//Done.
		
		return imageFile.getAbsolutePath();
	}
	
	// Other methods to do specific job off the server.
	public JSONObject performServerOperation(List<NameValuePair> params) {
		/*
		 * USE: List<NameValuePair> params = new ArrayList<NameValuePair>();
		 */
		JSONObject jsonResponce = jDecoder.makeHttpRequest(URL, "POST", params);
		return jsonResponce;
	}
	
	// METHOD TO CHECK FOR INTERNET CONNECTIVITY.
	public static boolean CheckInternet(Context context) {
	    ConnectivityManager connectivity = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (connectivity != null) {
	          NetworkInfo[] info = connectivity.getAllNetworkInfo();
	          if (info != null) 
	              for (int i = 0; i < info.length; i++) 
	                  if (info[i].getState() == NetworkInfo.State.CONNECTED) {
	                      return true;
	                  }
	    	   }
	      return false;
	}
	
	//METHOD TO GET THE IMAGE BITMAP
	public Bitmap GetBitmap(Context ctx, File file, int w, int h) {
		Context context = ctx;
		File fileName = file;
		int reqHeight = h;
		int reqWidth = w;
		
		String fName = fileName.getPath();
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			//Image = BitmapFactory.decodeFile(this.fileEntries.get(no), options);
			Bitmap Image = BitmapFactory.decodeFile(fName, options);
			
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inJustDecodeBounds = false;
			//bm = BitmapFactory.decodeFile(this.fileEntries.get(no), options);
			return BitmapFactory.decodeFile(fileName.getPath(), options);
			
			
		} catch (OutOfMemoryError e) {
			Toast.makeText(context, "Out of Memory!", Toast.LENGTH_SHORT).show();
			return null;
		}
	}
		
	public Bitmap GetBitmap(Context ctx, FileDescriptor fd, int w, int h) {
		Context context = ctx;
		FileDescriptor fileDescriptor = fd;
		int reqHeight = h;
		int reqWidth = w;
		
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			//Image = BitmapFactory.decodeFile(this.fileEntries.get(no), options);
			Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inJustDecodeBounds = false;
			//bm = BitmapFactory.decodeFile(this.fileEntries.get(no), options);
			return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
			
			
		} catch (OutOfMemoryError e) {
			Toast.makeText(context, "Out of Memory!", Toast.LENGTH_SHORT).show();
			return null;
		}
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		//Get raw values of the image width and height
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1; //First initialization
		if (height > reqHeight || width > reqWidth) {
			final int halfWidth = width/2;
			final int halfHeight = height/2;
			//Calculate the largest in sample size that is a power of 2 and keeps both height and width larger than the requested height and width
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
		//Get raw values of the image width and height
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1; //First initialization
		int reqHeight = (height*reqWidth)/width;
		if (height > reqHeight || width > reqWidth) {
			final int halfWidth = width/2;
			final int halfHeight = height/2;
			//Calculate the largest in sample size that is a power of 2 and keeps both height and width larger than the requested height and width
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
	//End Image Bitmap get
}