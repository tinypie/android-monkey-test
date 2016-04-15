package com.droidlogic.monkeytest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileSelectActivity extends Activity implements OnItemClickListener {
    public static final String FILE = "file";
    private static final int MSG_HIDE_SHOW_DIALOG = 1;
    private static final int MSG_SHOW_WAIT_DIALOG = 2;
    private static final int MSG_NOTIFY_DATACHANGE = 3;
    private static final int WAITDIALOG_DISPALY_TIME = 500;

    private StorageManager mStorageManager;
    private LayoutInflater mInflater;
    private FileAdapter mAdapter = new FileAdapter();
    private ListView mListView;
    private ProgressDialog mPdWatingScan = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_WAIT_DIALOG:
                    mPdWatingScan = ProgressDialog.show(FileSelectActivity.this,
                            getResources().getString(R.string.scan_title),
                            getResources().getString(R.string.scan_tip));
                    break;
                case MSG_HIDE_SHOW_DIALOG:
                    removeMessages(MSG_SHOW_WAIT_DIALOG);
                    if ( mPdWatingScan != null ) {
                        mPdWatingScan.dismiss();
                        mPdWatingScan = null;
                    }
                    break;
                case MSG_NOTIFY_DATACHANGE:
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    private void startScanThread() {
        Message nmsg = mHandler.obtainMessage(MSG_SHOW_WAIT_DIALOG);
        mHandler.sendMessageDelayed(nmsg, WAITDIALOG_DISPALY_TIME);
        new Thread() {
            public void run() {
                StorageVolume[] volumes = mStorageManager.getVolumeList();
                ArrayList<File> files = new ArrayList();
                for (int i = 0; i < volumes.length; i++) {
                    if (volumes[i].isRemovable()) {
                        String path = volumes[i].getPath();
                        String state = mStorageManager.getVolumeState(path);
                        if (Environment.MEDIA_MOUNTED.equals(state)) {
                            files.add(new File(path));
                        }
                    }
                }

                files.add(new File("/cache"));

                File[] mFiles;
                mFiles = new File[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    mFiles[i] = (File) files.get(i);
                }

                mAdapter.getList(mFiles);
                mHandler.sendEmptyMessage(MSG_HIDE_SHOW_DIALOG);
            }
        } .start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        mInflater = LayoutInflater.from(this);
        setContentView(R.layout.file_select);
        mListView = (ListView) findViewById(R.id.file_list);
        mListView.setAdapter(mAdapter);
        startScanThread();
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
                              int position, long id) {
        File selectFile =(File) adapterView.getItemAtPosition(position);

        if (selectFile.isFile()) {
            Intent intent = new Intent();
            intent.putExtra(FILE, selectFile.getPath());
            setResult(0, intent);
            finish();
        }
    }

    private class FileAdapter extends BaseAdapter {
            private File[] mFiles;
            private ArrayList<File> files = new ArrayList();

            public void setCurrentList(File directory) {
                File[] tempFiles = directory.listFiles(new FileFilter());
                for (int i = 0; (tempFiles != null) && (i < tempFiles.length);
                        i++) {
                    if (tempFiles[i].isDirectory()) {
                        setCurrentList (tempFiles[i]);
                    } else {
                        files.add(tempFiles[i]);
                    }
                }
            }

            public void getList(File[] dir) {
                for (int j = 0; j < dir.length; j++) {
                    setCurrentList(dir[j]);
                }
                mFiles = new File[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    mFiles[i] = (File) files.get(i);
                }
                mHandler.sendEmptyMessage(MSG_NOTIFY_DATACHANGE);
            }

            @Override
            public int getCount() {
                return (mFiles == null) ? 0 : mFiles.length;
            }

            @Override
            public File getItem(int position) {
                File file = (mFiles == null) ? null : mFiles[position];
                return file;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.large_text, null);
                }

                TextView tv = (TextView) convertView;
                File file = mFiles[position];
                String name = file.getPath();
                tv.setText(name);
                return tv;
            }
    }

    class FileFilter implements FilenameFilter {
            public boolean accept(File directory, String file) {
                String dir = directory.getPath();
                if (new File(directory, file).isDirectory()) {
                    return true;
                } else if (file.toLowerCase().endsWith(".txt")) {
                    return true;
                } else {
                    return false;
                }
            }
    }
}
