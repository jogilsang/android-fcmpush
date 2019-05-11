package app.service.clipboarder.service;

/**
 * Created by user on 11/20/2018.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import app.service.clipboarder.activity.MainActivity;
import app.service.clipboarder.R;


//title, contents, imgurl 데이터를 가지는 json 푸시 데이터가 오면 안드로이드에서 사용가능한 데이터로 추출하여 푸시 알림을 설정하는 내용 입니다.
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    private static final int PRIORITY_MAX = 2;
    public String title = "";
    public String contents = "";
    public String imgurl = "";
    public String link = "";

    Context mContext;



    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        // TODO : if  you want, sharepreference
        // 내장 DB 호출
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean enablePush = pref.getBoolean("permission_push", true);

        // 푸시알림 설정된경우
        if (enablePush == true) {

            Log.d(TAG, "From: " + remoteMessage.getFrom());

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                if (/* Check if data needs to be processed by long running job */ true) {
                    // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                    //scheduleJob();
                } else {
                    // Handle message within 10 seconds
                    handleNow();
                }

            }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }

            // Notification 알림
            if(remoteMessage.getData().get("message") ==null) {

                sendPushNotification(remoteMessage.getData().toString());

            }
            // data 알림
            else {

                sendPushNotification(remoteMessage.getData().get("message"));
            }
        }
        // 푸시알림 설정 안된경우
        else {

            // 알림X
            // FirebaseMessaging.getInstance().unsubscribeFromTopic("notice");
        }

    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    //    private void scheduleJob() {
    //        // [START dispatch_job]
    //        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
    //        Job myJob = dispatcher.newJobBuilder()
    //                .setService(MyJobService.class)
    //                .setTag("my-job-tag")
    //                .build();
    //        dispatcher.schedule(myJob);
    //        // [END dispatch_job]
    //    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    private void sendPushNotification(String message) {

        System.out.println("received message : " + message);

        try {

            JSONObject jsonRootObject = new JSONObject(message);
            title = jsonRootObject.getString("title");
            contents = jsonRootObject.getString("contents");
            imgurl = jsonRootObject.getString("imgurl");
            link = jsonRootObject.getString("link");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        PendingIntent pendingIntent = null;

        // link 값이 없으면
        if (link.equals("")  || link.equals("empty") || link == null) {


            // 푸시알림 클릭시 액티비티 실행
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //        intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);

            pendingIntent = PendingIntent.getActivity(this, 8888 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

        }
        // link 값이 있으면면
        else {

            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(link));
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        }

        Bitmap bitmap = getBitmapFromURL(imgurl);

        String channelId = getString(R.string.firebase_sender);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 알림 builder 설정
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);

        // 일반적인 알림
        if (imgurl.equals("")  || imgurl.equals("empty") || imgurl == null) {

            Drawable thumb = getResources().getDrawable(R.drawable.app_icon);
            Bitmap bmpOrg = ((BitmapDrawable) thumb).getBitmap();

            // 일반적인 알림
            notificationBuilder
                    .setSmallIcon(R.drawable.push_icon)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setColorized(true)
                    .setLargeIcon(bmpOrg)
                    .setContentTitle(title)
                    .setContentText(contents)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri).setLights(000000255, 500, 2000)
                    .setPriority(PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(contents)
                            .setBigContentTitle(title)
                            .setSummaryText("알림 메세지")
                    );


        }
        // 이미지 알림
        else {

             //그림 날릴때 쓰는 BigPictureStyle
                    notificationBuilder
                            .setSmallIcon(R.drawable.push_icon)
                            .setColor(getResources().getColor(R.color.colorPrimary))
                            .setColorized(true)
                            .setLargeIcon(bitmap)
                            .setContentTitle(title)
                            .setContentText(contents)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri).setLights(000000255, 500, 2000)
                            .setPriority(PRIORITY_MAX)
                            .setContentIntent(pendingIntent)
                            .setStyle(new NotificationCompat.BigPictureStyle()
                                .setBigContentTitle(title)
                                .setSummaryText(contents)
                                .bigPicture(bitmap)
                                .bigLargeIcon(null));

                    notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

//               .setVisibility(Notification.VISIBILITY_PUBLIC)
//                    VISIBILITY_PUBLIC은 알림의 전체 콘텐츠를 표시합니다.
//                    VISIBILITY_SECRET은 이 알림의 어떤 부분도 화면에 표시하지 않습니다.
//                    VISIBILITY_PRIVATE은 알림 아이콘과 콘텐츠 제목 등의 기본 정보는 표시하지만 알림의 전체 콘텐츠는 숨깁니다.

        }

        // 안드로이드 공식 문서(확장 등 관련) : https://developer.android.com/training/notify-user/expanded#java
        // 안드로이드 공식 문서(구현): https://developer.android.com/guide/topics/ui/notifiers/notifications?hl=ko
        // setPriority : -2~2 , 높으면 헤드업알립
        // setVisibility : 잠금화면알림
        // 알림 예시 설명 굿 : http://itmir.tistory.com/457
        // addAction : 읽음,닫음 같은 버튼생김 ( 아이콘, 텍스트 , 인탠트)

        // addAction에 쓸 action 예시
        //        Notification.Action action = new Notification.Action.Builder(
        //                Icon.createWithResource(this, R.drawable.ic_prev),
        //                "읽음",
        //                pendingIntent).build();


        // 빅텍스트 스타일
        //        Notification.BigTextStyle style = new Notification.BigTextStyle();
        //        style.setSummaryText("and More +");
        //        style.setBigContentTitle("BigText Expanded Title");
        //        style.bigText(contents);
        //
        //        notificationBuilder.setStyle(style);
        // 빅텍스트 스타일

        //확장알림 (Expand indicator)

        // 인박스 스타일 start
        //        NotificationCompat.InboxStyle inBoxStyle =
        //                new NotificationCompat.InboxStyle();
        //
        //        // Sets a title for the Inbox in expanded layout
        //        inBoxStyle.setBigContentTitle(title);
        //        inBoxStyle.setSummaryText("(광고)");
        //
        //        // \n에 따라 나눈다
        //        String[] events = contents.split(";;");
        //
        //        // Moves events into the expanded layout
        //        for (int i=0; i < events.length; i++) {
        //
        //            inBoxStyle.addLine(events[i].trim());
        //        }
        //        // Moves the expanded layout object into the notification object.
        //        notificationBuilder.setStyle(inBoxStyle);
        //        // Issue the notification here.
        //        // 인박스 style finish


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "powerenglish:TAG");
        wakelock.acquire(5000);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }

        title = "";
        contents = "";
        imgurl = "";
        link = "";

        notificationManager.notify(8888 /* ID of notification */, notificationBuilder.build());

    }


    public Bitmap getBitmapFromURL(String strURL) {

        try {

            URL url = new URL(strURL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);

            connection.connect();

            InputStream input = connection.getInputStream();

            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            return myBitmap;

        } catch (IOException e) {

            e.printStackTrace();

            return null;

        }

    }


}
