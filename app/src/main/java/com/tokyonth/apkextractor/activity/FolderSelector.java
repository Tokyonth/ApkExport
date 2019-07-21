package com.tokyonth.apkextractor.activity;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.adapter.FileListAdapter;
import com.tokyonth.apkextractor.data.Constants;
import com.tokyonth.apkextractor.data.FileItemInfo;
import com.tokyonth.apkextractor.utils.StorageUtil;

public class FolderSelector extends BaseActivity implements Runnable {
	
	private File path = new File(savepath);
	private List <FileItemInfo> filelist = new ArrayList<FileItemInfo>();
		
	private ListView listview;
	private RelativeLayout rl_load,rl_face;
	private SwipeRefreshLayout swl;
	
	private FileListAdapter listadapter;
	private Thread thread_refreshlist;
	private Toolbar toolbar;
	private Spinner spinner;

	private boolean isInterrupted = false;
	private String currentSelectedStoragePath = new String(storage_path);
	
	public static final int MESSAGE_REFRESH_FILELIST_COMPLETE = 0x0050;

	private void initView() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		listview = (ListView) findViewById(R.id.folderselector_filelist);
		rl_load = (RelativeLayout) findViewById(R.id.folderselector_refresharea);
		rl_face = (RelativeLayout) findViewById(R.id.folderselector_facearea);
		swl = (SwipeRefreshLayout) findViewById(R.id.folderselector_swiperefreshlayout);
		spinner = (Spinner)findViewById(R.id.folderselector_spinner);
	}

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.layout_folderselector);
		initView();
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getResources().getString(R.string.activity_folder_selector_title));

		swl.setSize(SwipeRefreshLayout.DEFAULT);
		swl.setDistanceToTriggerSync(100);
		swl.setProgressViewEndTarget(false, 200);
		listadapter = new FileListAdapter(filelist,this);
		
		try{
			final List<String> storages = StorageUtil.getAvailableStoragePaths();
			spinner.setAdapter(new ArrayAdapter<String>(FolderSelector.this,R.layout.layout_item_spinner_text,
					R.id.item_storage_text,storages));
			OUT:
			for(int i=0; i<storages.size(); i++){
				try{
					if(path.getAbsolutePath().toLowerCase(Locale.getDefault()).trim().equals(storages.get(i)
							.toLowerCase(Locale.getDefault()).trim())){
						spinner.setSelection(i);
						break;
					}else{
						File file = new File(path.getAbsolutePath());
						while((file = file.getParentFile()) != null){
							if(file.getAbsolutePath().toLowerCase(Locale.getDefault()).trim()
									.equals(storages.get(i).toLowerCase(Locale.getDefault()).trim())){
								spinner.setSelection(i);								
								break OUT;								
							}
						}
					}
					
				}catch(Exception e){}
			}
			spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					Log.d("Spinner", "position is " + position);
					try{
						if(currentSelectedStoragePath.toLowerCase(Locale.getDefault())
								.trim().equals(((String)spinner.getSelectedItem()).toLowerCase(Locale.getDefault()).trim()))
						path = new File((String)spinner.getSelectedItem());
						currentSelectedStoragePath = (String)spinner.getSelectedItem();
						refreshList(true);	
					}catch(Exception e){
						e.printStackTrace();
						Toast.makeText(FolderSelector.this, e.toString(), Toast.LENGTH_SHORT).show();
					}											
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) { }
				
			});
		} catch (Exception e){
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		}
		
		this.path = new File (savepath);
		try {
			if (!path.exists()) {
				if (!path.mkdirs()) {
					Toast.makeText(this, getResources().getString(R.string.activity_folder_selector_initial_failed), Toast.LENGTH_SHORT).show();
				}
			}
			
		} catch (Exception e){
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		}
				
		refreshList(true);
		swl.setOnRefreshListener(() -> FolderSelector.this.refreshList(false));
	}

	public void refreshList(boolean isShowProgressBar){
		if(listview!=null){
			listview.setAdapter(null);
		}
		if(thread_refreshlist!=null){
			thread_refreshlist.interrupt();
			isInterrupted=true;
			thread_refreshlist=null;
		}
		
		thread_refreshlist=new Thread(this);
		isInterrupted=false;
		thread_refreshlist.start();
		if(rl_face!=null){
			rl_face.setVisibility(View.GONE);
		}
		if(rl_load!=null&&isShowProgressBar){
			rl_load.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void processMessage(Message msg) {
		switch(msg.what){
			default:
				break;
			case MESSAGE_REFRESH_FILELIST_COMPLETE:{				
				FolderSelector.this.setInfoAtt(path.getAbsolutePath());
				listadapter = new FileListAdapter(filelist,this);
				if(listview != null){
					listview.setAdapter(listadapter);
					thread_refreshlist=null;
					listview.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
						FolderSelector.this.path = FolderSelector.this.filelist.get(arg2).file;
						FolderSelector.this.setInfoAtt(FolderSelector.this.path.getAbsolutePath());
						FolderSelector.this.refreshList(true);
					});
					
					this.listadapter.setOnRadioButtonClickListener(position -> {
						path = filelist.get(position).file;
						setInfoAtt(path.getAbsolutePath());
						listadapter.setSelected(position);
					});
				}									
								
				if(rl_load != null){
					rl_load.setVisibility(View.GONE);
				}
				if(rl_face != null){
					if(filelist.size() <= 0){
						rl_face.setVisibility(View.VISIBLE);
					}
					else{
						rl_face.setVisibility(View.GONE);
					}
				}
				if(swl != null){
					swl.setRefreshing(false);
				}
			}
			break;
		}
		
	}
	
	private void setInfoAtt(String att){
		if(att.length() > 50){
			att = "/..." + att.substring(att.length() - 50);
		}
		((TextView)findViewById(R.id.folderselector_pathname)).setText(getResources().getString(R.string.activity_folder_selector_current) + att);
	}

	@Override
	public void run() {
		try{
			if(path.isDirectory()){				
				File[] files = FolderSelector.this.path.listFiles();
				FolderSelector.this.filelist = new ArrayList<FileItemInfo>();
				
				if(files != null&&files.length>0){
					
					for(int i = 0; i < files.length; i++){
						if(!this.isInterrupted){
							if(files[i].isDirectory()&&files[i].getName().indexOf(".") !=0 ){
								FileItemInfo fileitem = new FileItemInfo(files[i]);
								FolderSelector.this.filelist.add(fileitem);
							}
						}
						else{
							break;
						}
					}
					Collections.sort(FolderSelector.this.filelist);
				}
			}
			if(!this.isInterrupted)
			sendEmptyMessage(MESSAGE_REFRESH_FILELIST_COMPLETE);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.folderselector, menu);		
		return true;
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu)  {  
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {  
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {  
                try {  
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);  
                    m.setAccessible(true);  
                    m.invoke(menu, true);  
                } catch (Exception e) {  
                }  
            }  
        }  
        return super.onMenuOpened(featureId, menu);  
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
			default:break;
			case R.id.folderselector_action_confirm:{								
				savepath = path.getAbsolutePath();
				try{
					storage_path = ((String)((Spinner)findViewById(R.id.folderselector_spinner)).getSelectedItem());
				}catch(Exception e){e.printStackTrace();}
				editor.putString(Constants.PREFERENCE_SAVE_PATH, savepath);
				editor.putString(Constants.PREFERENCE_STORAGE_PATH, storage_path);
				editor.apply();
				Toast.makeText(this, getResources().getString(R.string.activity_folder_selector_saved_font)+savepath, Toast.LENGTH_SHORT).show();
				finish();								
			}
			break;
			case R.id.folderselector_action_cancel:{
				finish();
			}
			break;
			case R.id.folderselector_action_sort_ascending:{
				FileItemInfo.SortConfig = 1;
				if(this.filelist!=null) Collections.sort(filelist);
				if(this.listadapter!=null){
					this.listadapter.setSelected(-1);
				}
			}
			break;
			case R.id.folderselector_action_sort_descending:{
				FileItemInfo.SortConfig=2;
				if(this.filelist!=null) Collections.sort(filelist);
				if(this.listadapter!=null){
					this.listadapter.setSelected(-1);
				}
			}
			break;
			case R.id.folderselector_action_newfolder:{
				LayoutInflater inflater = LayoutInflater.from(this);
				View dialog_view = inflater.inflate(R.layout.layout_dialog_newfolder, null);
				final AlertDialog newfolder = new AlertDialog.Builder(this)
						.setTitle(getResources().getString(R.string.new_folder))
						.setIcon(R.drawable.ic_newfolder)
						.setView(dialog_view)
						.setPositiveButton(getResources().getString(R.string.dialog_button_positive), null)
						.setNegativeButton(getResources().getString(R.string.dialog_button_negative), null)
						.create();
				newfolder.show();
				
				final EditText edittext = (EditText)dialog_view.findViewById(R.id.dialog_newfolder_edittext);
				newfolder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
					try{
						String folder_name = edittext.getText().toString().trim();
						File new_file = new File(FolderSelector.this.path.getAbsolutePath() + "/" + folder_name);
						if(folder_name.length() ==0 ||folder_name.equals("")){
							Toast.makeText(FolderSelector.this, getResources().getString(R.string.activity_folder_selector_invalid_pathname), Toast.LENGTH_SHORT).show();
							return;
						}else if(folder_name.contains("?")||folder_name.contains("\\")||folder_name.contains("/")||folder_name.contains(":")
								||folder_name.contains("*")||folder_name.contains("\"")||folder_name.contains("<")||folder_name.contains(">")
								||folder_name.contains("|")){
							Toast.makeText(FolderSelector.this, getResources().getString(R.string.activity_folder_selector_invalid_foldername), Toast.LENGTH_SHORT).show();
							return;
						}else if(new_file.exists()){
							Toast.makeText(FolderSelector.this, getResources().getString(R.string.activity_folder_selector_folder_already_exists)+folder_name, Toast.LENGTH_SHORT).show();
							return;
						}else{
							if(new_file.mkdirs()){
								FolderSelector.this.refreshList(true);
								newfolder.cancel();
							}
							else{
								Toast.makeText(FolderSelector.this, "Make Dirs error", Toast.LENGTH_SHORT).show();
								return;
							}

						}
					}catch(Exception e){
						e.printStackTrace();
						Toast.makeText(FolderSelector.this, e.toString(), Toast.LENGTH_SHORT).show();
					}

				});
				
				newfolder.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> newfolder.cancel());
			}
			
			break;
			case R.id.folderselector_action_reset:{
				savepath=Constants.PREFERENCE_SAVE_PATH_DEFAULT;
				editor.putString(Constants.PREFERENCE_SAVE_PATH, Constants.PREFERENCE_SAVE_PATH_DEFAULT);
				editor.apply();
				Toast.makeText(this, "默认路径: "+Constants.PREFERENCE_SAVE_PATH_DEFAULT, Toast.LENGTH_SHORT).show();
				this.finish();
			}
			break;
			case android.R.id.home:{
				backToParent();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void backToParent(){
		try{
			File parent = path.getParentFile();
			Log.d("parent", parent == null?"null":parent.toString());
			if(parent == null||parent.getAbsolutePath().trim().length()<((String)((Spinner)findViewById(R.id.folderselector_spinner))
					.getSelectedItem()).trim().length()){
				finish();
			}
			else{
				path = parent;
				refreshList(true);
			}
		}catch(Exception e){e.printStackTrace();}		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToParent();
			return true;
		}
		return false;
	}
}
