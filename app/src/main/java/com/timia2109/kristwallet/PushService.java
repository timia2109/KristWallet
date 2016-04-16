package com.timia2109.kristwallet;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Date;

public class PushService extends IntentService {
    public PushService() {
        super("PushService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Saver saver = (Saver) intent.getSerializableExtra("saver");
        KristAPI[] apis = saver.apis;

        if (apis == null || apis.length == 0)
            stopSelf();

        try {
            long all = 0;
            String lastSnap = intent.getStringExtra("snapshot");
            int len = apis.length;
            StringBuilder kWalls = new StringBuilder();

            for (int i = 0; i < len; i++) {
                long budget = apis[i].getBalance();
                long diffToLast = budget-intent.getLongExtra("last"+i,budget);
                all += budget;
                kWalls.append( apis[i].getName() )
                        .append(": \t\t")
                        .append(budget)
                        .append( KristAPI.currency );
                if (diffToLast != 0) {
                    if (diffToLast > 0)
                        kWalls.append(" (+ ");
                    else if (diffToLast < 0)
                        kWalls.append(" (- ");
                    kWalls.append(Math.abs(diffToLast)).append(")");
                }
                kWalls.append( "\n" );
                if (lastSnap == null)
                    intent.putExtra("last"+i, budget);
            }

            Intent snapshot = new Intent(intent);
            if (intent == snapshot)
                System.out.println("ERROR ON PUSH SERVICE intent==snapshot");
            String snapshotTitle;
            String snapshotDate = "";

            if (lastSnap != null) {
                //There is a snapshot
                snapshot.removeExtra("snapshot");
                snapshotTitle = getString(R.string.removeSnapshot);
                snapshotDate = "\n"+getString(R.string.snapshotTime)+lastSnap;
            }
            else {
                //There is no Snapshot
                snapshot.putExtra("snapshot", Saver.stringifyDate(new Date(), saver, this));
                snapshotTitle = getString(R.string.snapshot);
            }

            String dateString = Saver.stringifyDate(new Date(), saver, this);

            int notificationId = 1;
            Intent viewIntent = new Intent(this, MainActivity.class);
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(this, 0, viewIntent, 0);

            NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
            kWalls.append("\n")
                    .append( getString(R.string.allWallets))
                    .append(": \t\t\t")
                    .append(all).append(KristAPI.currency)
                    .append("\n")
                    .append( getString(R.string.loadAt))
                    .append(": ").append(dateString)
                    .append(snapshotDate);
            bigStyle.bigText( kWalls.toString() );

            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                            .setBackground(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

            PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pSnapShot = PendingIntent.getService(this, 0 , snapshot, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle( getResources().getString(R.string.wallets) )
                            .setContentText(getString(R.string.allWallets)+": \t\t" + all + KristAPI.currency)
                            .setContentIntent(viewPendingIntent)
                            .setPriority(1)
                            .addAction(R.mipmap.refresh, getResources().getString(R.string.refresh) , pintent)
                            .addAction(R.mipmap.snapshot, snapshotTitle, pSnapShot)
                            .extend(wearableExtender)
                            .setStyle(bigStyle);

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            notificationManager.notify(notificationId, notificationBuilder.build());
        }
        catch (Exception ignored) {
        }

        stopSelf();
    }
}