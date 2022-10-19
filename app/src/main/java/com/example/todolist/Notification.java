package com.example.todolist;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todolist.R;

public class Notification extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channelID";

    public static final String TITLE_EXTRA = "titleExtra";
    public static final String MESSAGE_EXTRA = "messageExtra";
    public static final String ID_EXTRA = "taskid";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("title",intent.getStringExtra(TITLE_EXTRA));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,intent.getIntExtra("requestCall",0),i,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_powiadomienia)
                .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
                .setContentText(intent.getStringExtra(MESSAGE_EXTRA))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //poprawiona wersja programu
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);


       notification.setContentIntent(contentIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        notificationManagerCompat.notify(intent.getIntExtra(ID_EXTRA, 0), notification.build());
    }
}
