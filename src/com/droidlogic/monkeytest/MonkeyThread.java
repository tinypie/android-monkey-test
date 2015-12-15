package com.droidlogic.monkeytest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MonkeyThread extends Thread {
    private static final String TAG = "monkeyThread";
    static final boolean DEBUG = false;

    static boolean isRunning;
    static double runningTime;
    String mCmd;
    Process mMonkeyProcess;
    static MonkeyThread sMonkeyThread;

    private MonkeyThread() {
        super("monkeythread");
        isRunning = false;
        runningTime = 0;
    }

    void init(String cmd) {
        mCmd = cmd;
    }

    public static MonkeyThread getInstance() {
        if (sMonkeyThread == null) {
            sMonkeyThread = new MonkeyThread();
        }

        return sMonkeyThread;
    }

    public void startMonkeyTest() {
        try {
            Log.d(TAG, "start monkey test:" + mCmd);

            try {
                File file = new File("/data/log");
                if(file.exists() && file.delete()) {
                    if (DEBUG) Log.d(TAG, "previous monkeylog file: /data/log is deleted");
                }
            } catch(Exception e){
                e.printStackTrace();
            }

            long startTime = System.currentTimeMillis();
            mMonkeyProcess = Runtime.getRuntime().exec(mCmd);

            try {mMonkeyProcess.getErrorStream().close();} catch (IOException e) {}

            BufferedWriter logOutput =
                new BufferedWriter(new FileWriter(new File("/data/log"), true));

            InputStream inStream = mMonkeyProcess.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);
            String s;
            while ((s = inBuffer.readLine()) != null) {
                if (DEBUG) {
                    logOutput.write(s);
                    logOutput.write("\n");
                }
            }

            int status = mMonkeyProcess.waitFor();
            long endTime = System.currentTimeMillis();
            runningTime = (endTime - startTime) / (1000 * 60.0);
            Log.d(TAG, "monkey test runtime:" + String.format("%.2f", runningTime)
                    + " minutes returned:" + status);
            isRunning = false;

            if (logOutput != null)
                logOutput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        isRunning = true;
        startMonkeyTest();
    }
}
