package com.bignerdranch.android.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class CrimeListFragment extends SherlockListFragment {
	
	private static final String TAG = "CrimeListFragment";
	private boolean mSubtitleVisible;
	private ArrayList<Crime> mCrimes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		mSubtitleVisible = false;
		getActivity().setTitle(R.string.crimes_title);
		mCrimes = CrimeLab.get(getActivity()).getCrimes();
		
		CrimeAdapter adapter = new CrimeAdapter(mCrimes);
		setListAdapter(adapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Crime c = ((CrimeAdapter)getListAdapter()).getItem(position);
		//Log.d(TAG, c.getTitle() + " was clicked");
		
		// Start crimeActivity with this crime
		Intent i = new Intent(getActivity(), CrimePagerActivity.class);
		i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
		startActivity(i);
	}
	
	private class CrimeAdapter extends ArrayAdapter<Crime> {
		public CrimeAdapter(ArrayList<Crime> crimes) {
			super(getActivity(), 0, crimes);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// If we weren't given a view, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_crime, null);
			}
			
			//Configure the view for this Crime
			Crime c = getItem(position);
			
			TextView titleTextView =
					(TextView)convertView.findViewById(R.id.crime_list_item_titleTextView);
			titleTextView.setText(c.getTitle());
			
			TextView dateTextView =
					(TextView)convertView.findViewById(R.id.crime_list_item_dateTextView);
			dateTextView.setText(c.getDate().toString());
			
			CheckBox solvedCheckBox =
					(CheckBox)convertView.findViewById(R.id.crime_list_item_solvedCheckBox);
			solvedCheckBox.setChecked(c.isSolved());
			
			return convertView;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_list, menu);
		com.actionbarsherlock.view.MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
		if (mSubtitleVisible && showSubtitle != null) {
			showSubtitle.setTitle(R.string.hide_subtitle);
		}
	}
	
	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_new_crime:
				Crime crime = new Crime();
				CrimeLab.get(getActivity()).addCrime(crime);
				Intent i = new Intent(getActivity(), CrimePagerActivity.class);
				i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
				startActivityForResult(i, 0);
				return true;
			case R.id.menu_item_show_subtitle:
				if (getSherlockActivity().getSupportActionBar().getSubtitle() == null) {
					getSherlockActivity().getSupportActionBar().setSubtitle(R.string.subtitle);
					mSubtitleVisible = true;
					item.setTitle(R.string.hide_subtitle);
				} else {
					getSherlockActivity().getSupportActionBar().setSubtitle(null);
					mSubtitleVisible = false;
					item.setTitle(R.string.show_subtitle);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		getSherlockActivity().getSupportActionBar().setSubtitle(R.string.subtitle);
		
		ListView listView = (ListView)v.findViewById(android.R.id.list);
		
		// Use contextual action bar on Honeycomb and higher
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				getSherlockActivity().startActionMode(new com.actionbarsherlock.view.ActionMode.Callback() {
					@Override
					public boolean onPrepareActionMode(
							com.actionbarsherlock.view.ActionMode mode,
							com.actionbarsherlock.view.Menu menu) {
						// Required, but not used in this implementation
						return false;
					}
					
					@Override
					public void onDestroyActionMode(com.actionbarsherlock.view.ActionMode mode) {
						// Required, but not used in this implementation
					}
					
					@Override
					public boolean onCreateActionMode(
							com.actionbarsherlock.view.ActionMode mode,
							com.actionbarsherlock.view.Menu menu) {
						com.actionbarsherlock.view.MenuInflater inflater = mode.getMenuInflater();
						inflater.inflate(R.menu.crime_list_item_context, menu);
						return true;
					}
					
					@Override
					public boolean onActionItemClicked(
							com.actionbarsherlock.view.ActionMode mode,
							com.actionbarsherlock.view.MenuItem item) {
						switch (item.getItemId()) {
							case R.id.menu_item_delete_crime:
								CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
								CrimeLab crimeLab = CrimeLab.get(getActivity());
								for (int i = adapter.getCount() - 1; i >= 0; i--) {
									if (getListView().isItemChecked(i)) {
										crimeLab.deleteCrime(adapter.getItem(i));
									}
								}
								mode.finish();
								adapter.notifyDataSetChanged();
								return true;
							default:
								return false;
						}
					}
				});
				return true;
			}
			
		});
		
		/*listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// Required, but not used in this implementation
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.crime_list_item_context, menu);
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
					case R.id.menu_item_delete_crime:
						CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
						CrimeLab crimeLab = CrimeLab.get(getActivity());
						for (int i = adapter.getCount() - 1; i >= 0; i--) {
							if (getListView().isItemChecked(i)) {
								crimeLab.deleteCrime(adapter.getItem(i));
							}
						}
						mode.finish();
						adapter.notifyDataSetChanged();
						return true;
					default:
						return false;
				}
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				// Required but not used in this implementation
			}
		});*/
		
		return v;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int position = info.position;
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		Crime crime = adapter.getItem(position);
		
		switch (item.getItemId()) {
			case R.id.menu_item_delete_crime:
				CrimeLab.get(getActivity()).deleteCrime(crime);
				adapter.notifyDataSetChanged();
				return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onPause() {
		CrimeLab.get(getActivity()).saveCrimes();
		super.onPause();
	}

}
