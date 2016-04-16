package com.timia2109.kristwallet.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;

import com.timia2109.kristwallet.KristAPI;
import com.timia2109.kristwallet.R;
import com.timia2109.kristwallet.WalletActivity;

import java.io.IOException;

/**
 * Created by Tim on 25.02.2016.
 */
public class KristSender extends Thread {
    long count;
    String target, metadata;
    KristAPI api;
    Context context;
    Runnable runnable;
    View runInView;

    public KristSender(long count, String target, KristAPI api, String metadata, Context context) {
        this.count = count;
        this.target = target;
        this.api = api;
        this.context = context;
        this.metadata = metadata;
    }

    public void putView(View v) {runInView=v;}

    public void putRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        String rText = "";
        String textHead = context.getString(R.string.sendKstFail);
        try {
            api.sendKrist(count, target, metadata);
            textHead = context.getString(R.string.sendKstSuccess);
            rText = context.getString(R.string.sendKstSuccessContent, count, KristAPI.currency, api.getName(), target);
        } catch (IOException e) {
            rText = context.getString(R.string.failWebConnectFail);
            if (runnable != null && runnable instanceof JavaScriptAPI.SendKSTResult) {
                ((JavaScriptAPI.SendKSTResult)runnable).done = false;
                ((JavaScriptAPI.SendKSTResult)runnable).receiver = null;
            }
        }
        catch (KristAPI.SendKristException e) {
            rText = context.getString(R.string.failEx, e.getMessage());
            if (runnable != null && runnable instanceof JavaScriptAPI.SendKSTResult) {
                ((JavaScriptAPI.SendKSTResult)runnable).done = false;
                ((JavaScriptAPI.SendKSTResult)runnable).receiver = null;
            }
        } finally {
            if (runnable != null) {
                try {
                    if (runInView != null)
                        runInView.post(runnable);
                    else
                        runnable.run();
                } catch (Exception ignored) {}
            }
        }

        Intent onClick = new Intent(context, WalletActivity.class);
        onClick.putExtra("api", api);
        PendingIntent onClickPI = PendingIntent.getActivity(context, 0, onClick, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(rText);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(textHead)
                        .setContentText( rText )
                        .setContentIntent(onClickPI)
                        .setSound( RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) )
                        .setVibrate(new long[]{500, 500})
                        .setStyle(bigStyle)
                        .setPriority(3);
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        notificationManager.notify(2, notificationBuilder.build());
    }
}