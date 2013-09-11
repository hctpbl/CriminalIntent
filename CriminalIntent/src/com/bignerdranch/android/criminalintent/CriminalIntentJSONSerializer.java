package com.bignerdranch.android.criminalintent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class CriminalIntentJSONSerializer {
	
	private static final String TAG = "CriminalIntentJSONSerializer";

	private Context mContext;
	private String mFilename;
	
	public CriminalIntentJSONSerializer(Context c, String f) {
		mContext = c;
		mFilename = f;
	}
	
	public ArrayList<Crime> loadCrimes() throws IOException, JSONException {
		ArrayList<Crime> crimes = new ArrayList<Crime>();
		BufferedReader reader = null;
		try {
			InputStream in = null;
			// Open and read the file into a StringBuilder
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File file = new File(mContext.getExternalFilesDir(null), mFilename);
				in = new FileInputStream(file);
				Log.d(TAG, mContext.getExternalFilesDir(null).getAbsolutePath());
				Log.i(TAG, "Reading from SD card");
			} else {
				in = mContext.openFileInput(mFilename);
				Log.i(TAG, "Reading from internal storage");
			}
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Line breaks are irrelevant
				jsonString.append(line);
			}
			// Parse the JSON using JSONTokener
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			// Build the array of crimes from JSONObjects
			for (int i = 0; i < array.length(); i++) {
				crimes.add(new Crime(array.getJSONObject(i)));
			}
		} catch (FileNotFoundException e) {
			// Ignore this one; it happens when starting fresh
		} finally {
			if (reader != null)
				reader.close();
		}
		return crimes;
	}
	
	public void saveCrimes(ArrayList<Crime> crimes) 
			throws JSONException, IOException {

		// Build an array in JSON
		JSONArray array = new JSONArray();
		for (Crime c : crimes)
			array.put(c.toJSON());
		
		//Write the file to disk
		Writer writer = null;
		try {
			OutputStream out = null;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File file = new File(mContext.getExternalFilesDir(null), mFilename);
				Log.i(TAG, "Writing to SD card");
				out = new FileOutputStream(file);
			} else {
				out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
				Log.i(TAG, "Writing to internal storage");
			}
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
		
	}
	
}
