package org.svij.taskwarriorapp;

import org.svij.taskwarriorapp.data.Task;
import org.svij.taskwarriorapp.db.TaskDataSource;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ArrayListFragment extends SherlockListFragment {

	/**
	 * 
	 * This class is based on GnuCash Mobiles "AccountsActivity.java" by Ngewi
	 * Fet <ngewif@gmail.com>
	 * 
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may
	 * not use this file except in compliance with the License. You may obtain a
	 * copy of the License at
	 * 
	 * http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License.
	 */

	TaskDataSource datasource;
	ArrayAdapter<Task> adapter = null;
	private boolean inEditMode = false;
	private ActionMode actionMode = null;
	private int selectedViewPosition = -1;
	private long selectedItemId = -1;
	private MenuListFragment menuFragment;

	private ActionMode.Callback actionModeCallbacks = new Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);
			mode.setTitle(getString(R.string.task_selected, 1));
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// nothing to see here, move along
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.context_menu_edit_task:
				showAddTaskActivity(getTaskWithId(selectedItemId));
				return true;

			case R.id.context_menu_delete_task:
				deleteTask(getTaskWithId(selectedItemId));
				mode.finish();
				return true;

			case R.id.context_menu_done_task:
				doneTask(getTaskWithId(selectedItemId));
				mode.finish();
				return true;

			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			finishEditMode();
		}

		private void showAddTaskActivity(long selectedItemId) {
			Intent intent = new Intent(getActivity(), TaskAddActivity.class);
			intent.putExtra("taskID", selectedItemId);
			startActivity(intent);
		}

		private void deleteTask(long selectedItemId) {
			datasource.deleteTask(selectedItemId);
			menuFragment = (MenuListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.menu_frame);
			menuFragment.setMenuList();
			menuFragment.setTaskList();
		}

		private void doneTask(long selectedItemId) {
			datasource.doneTask(selectedItemId);
			menuFragment = (MenuListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.menu_frame);
			menuFragment.setMenuList();
			menuFragment.setTaskList();
		}

		private long getTaskWithId(long selectedItemId) {
			return ((Task) getListAdapter().getItem((int) selectedItemId - 1))
					.getId();
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView listview = getListView();
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				inEditMode = true;
				selectedItemId = id + 1;
				// Start the CAB using the ActionMode.Callback defined
				// above
				actionMode = getSherlockActivity().startActionMode(
						actionModeCallbacks);

				selectItem(position);
			}
		});

		menuFragment = (MenuListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		menuFragment.setTaskList();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (datasource != null) {
			datasource.open();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (datasource != null) {
			datasource.close();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (datasource == null) {
			datasource = new TaskDataSource(getActivity());
			datasource.open();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (datasource != null) {
			datasource.close();
			datasource = null;
		}
	}

	public void finishEditMode() {
		inEditMode = false;
		deselectPreviousSelectedItem();
		actionMode = null;
	}

	private void selectItem(int position) {
		deselectPreviousSelectedItem();
		ListView lv = getListView();
		lv.setItemChecked(position, true);
		View v = lv.getChildAt(position - lv.getFirstVisiblePosition());
		v.setSelected(true);
		v.setBackgroundColor(getResources().getColor(
				R.color.abs__holo_blue_light));
		selectedViewPosition = position;
	}

	private void deselectPreviousSelectedItem() {
		if (selectedViewPosition >= 0) {
			ListView lv = getListView();
			lv.setItemChecked(selectedViewPosition, false);
			View v = getListView().getChildAt(
					selectedViewPosition - lv.getFirstVisiblePosition());
			if (v == null) {
				// if we just deleted a row, then the previous position is
				// invalid
				return;
			}
			v.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			v.setSelected(false);
		}
	}
}
