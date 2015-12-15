package com.droidlogic.monkeytest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DebugSettingDialogFragment extends DialogFragment {
    CharSequence[] items = { "--dbg-no-events", "--hprof", "--ignore-crashes",
        "--ignore-timeouts", "--ignore-security-exceptions", "--kill-process-after-error",
        "--monitor-native-crashes", "--wait-dbg"};
    boolean[] itemsChecked = new boolean [items.length];

    static DebugSettingDialogFragment newInstance(String title) {
        DebugSettingDialogFragment debugDialog = new DebugSettingDialogFragment();
        Bundle args = new Bundle();
        args.putString("Debug Setting", title);
        debugDialog.setArguments(args);
        return debugDialog;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        itemsChecked[2] = true;
        itemsChecked[3] = true;
        itemsChecked[4] = true;

        //String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity() );
        alertDialogBuilder.setTitle("setiings");
        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                String value = " ";
                for (int i = 0; i < items.length; i++) {
                    if (itemsChecked[i]) {
                        value += items[i];
                        value += " ";
                    }
                }

                MainActivity callingActivity = (MainActivity) getActivity();
                callingActivity.onUserSelectDebugValue(value);
            }
        });

        alertDialogBuilder.setMultiChoiceItems(items, itemsChecked,
                                                new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog,
                                 int which, boolean isChecked) {
                itemsChecked[which] = isChecked;
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }
}
