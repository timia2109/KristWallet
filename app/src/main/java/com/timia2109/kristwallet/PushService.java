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
        intent.putExtra("dummy",true);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        try {
            KristAPI k = new KristAPI(MainActivity.bUrl, "0");
            String kWalls = "";
            String[] apis = intent.getStringExtra("apis").split(";");
            long all = 0;
            int len = apis.length-1;

            for (int i = 0; i <= len; i++) {
                long budget = k.getBalance(apis[i]);
                all += budget;
                kWalls += apis[i]+": \t\t"+budget+" KST";
                if (i != len)
                    kWalls += "\n";
            }

            Date d = new Date();

            int notificationId = 001;
            Intent viewIntent = new Intent(this, MainActivity.class);
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(this, 0, viewIntent, 0);

            NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
            bigStyle.bigText(kWalls + "\n\n" + "All Wallets: \t\t" + all + " KST"+"\nLoad at: "+d.toString());

            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                            .setHintHideIcon(true)
                            .setBackground(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Krist Wallets")
                            .setContentText("All Wallets: \t\t" + all + " KST")
                            .setContentIntent(viewPendingIntent)
                            .setPriority(1)
                            .addAction(R.drawable.ic_drawer,"Refresh ", pintent)
                            .extend(wearableExtender)
                            .setStyle(bigStyle);

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            notificationManager.notify(notificationId, notificationBuilder.build());
        }
        catch (Exception e) {
        }

        stopSelf();
    }
}