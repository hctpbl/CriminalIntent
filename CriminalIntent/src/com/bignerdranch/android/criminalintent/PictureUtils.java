package com.bignerdranch.android.criminalintent;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

public class PictureUtils {
	/**
	 * Get a BitmapDrawable from a local file that is scaled down
	 * to fit the current window size
	 */
	
	private static final String TAG = "PictureUtils";
	
	@SuppressWarnings("deprecation")
	public static BitmapDrawable getScaledDrawable(Activity a, String path) {
		Display display = a.getWindowManager().getDefaultDisplay();
		float destWidth = display.getWidth();
		float destHeight = display.getHeight();
		
		// Read in the dimensions of the image on the disk
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;
		
		int inSampleSize = 1;
		if (srcHeight > destHeight || srcWidth > destWidth) {
			if (srcWidth > srcHeight) {
				inSampleSize = Math.round(srcHeight / destHeight);
			} else {
				inSampleSize = Math.round(srcWidth / destWidth);
			}
		}
		
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		
		try {		
			ExifInterface exifInterface = new ExifInterface(path);
			int exiforientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			
			Matrix matrix = new Matrix();
			
			if (exiforientation == ExifInterface.ORIENTATION_ROTATE_90) {
			       matrix.postRotate(90);
			} else if (exiforientation == ExifInterface.ORIENTATION_ROTATE_180) {
			       matrix.postRotate(180);
			} else if (exiforientation == ExifInterface.ORIENTATION_ROTATE_270) {
			       matrix.postRotate(270);
			}
			
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);
		} catch (IOException e) {
			Log.e(TAG, "Unable to rotate thumbnail.", e);
		}
		
		return new BitmapDrawable(a.getResources(), bitmap);
	}
	
	public static void cleanImageView(ImageView imageView) {
		if (!(imageView.getDrawable() instanceof BitmapDrawable)) 
			return ;
		
		// Clean up the view's image for the sake of memory
		BitmapDrawable b = (BitmapDrawable)imageView.getDrawable();
		b.getBitmap().recycle();
		imageView.setImageDrawable(null);
	}

}
