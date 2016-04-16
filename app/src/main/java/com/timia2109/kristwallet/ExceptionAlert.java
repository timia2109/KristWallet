package com.timia2109.kristwallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;

/**
 * Created by Tim on 11.03.2016.
 */
public class ExceptionAlert implements Dialog.OnClickListener, Runnable {

    AlertDialog.Builder builder;
    String message;
    Activity ac;

    public ExceptionAlert(Exception e, Handler h, Activity ac) {
        this.ac = ac;
        //TODO: parse Exception
        if (e instanceof KristAPI.APIBadResult) {

        }
        prepareBuilder();
        h.post(this);
    }

    public ExceptionAlert(String msg, Handler h, Activity ac) {
        this.ac = ac;
        message = msg;
        prepareBuilder();
        h.post(this);
    }

    private void prepareBuilder() {
        builder = new AlertDialog.Builder(ac);
        builder.setTitle(R.string.fail);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
    }

    @Override
    public void run() {
        if (builder != null && message != null)
            builder.show();
        else
            System.out.println("Error builder OR message are null");
    }
}
