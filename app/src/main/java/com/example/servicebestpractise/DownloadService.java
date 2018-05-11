package com.example.servicebestpractise;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {

     private DownloadTask downloadTask;

     private String downloadUrl;

     private DownloadListener listener = new DownloadListener() {
         @Override
         public void onProgress(int progress) {
             getNotificationManger().notify(1, getNotification("Downloading...", progress));//触发下载通知
         }

         @Override
         public void onSuccess() {
             downloadTask = null;
             stopForeground(true);
             getNotificationManger().notify(1, getNotification("download success", -1));
             Toast.makeText(DownloadService.this,"download success", Toast.LENGTH_SHORT).show();
         }

         @Override
         public void onFailed() {

             downloadTask = null;
             stopForeground(true);
             getNotificationManger().notify(1, getNotification("download failed", -1));
             Toast.makeText(DownloadService.this, "download failed", Toast.LENGTH_SHORT).show();
         }

         @Override
         public void onPaused() {
             downloadTask = null;
             Toast.makeText(DownloadService.this,"paused", Toast.LENGTH_SHORT).show();

         }

         @Override
         public void onCanceled() {

             downloadTask = null;
             stopForeground(true);
             Toast.makeText(DownloadService.this, "download canceled", Toast.LENGTH_SHORT).show();
         }
     };

     private DownloadBinder mBinder = new DownloadBinder();

    public DownloadService() {
    }

    private NotificationManager getNotificationManger(){
         return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    //创建下载成功通知
    private Notification getNotification(String title, int progress){
         Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivities(this, 0, new Intent[]{intent}, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress >= 0){
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder{
        public void startDownload(String url){
            if (downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);//开启下载，将Url地址传入execute方法
                startForeground(1, getNotification("downloading...", 0));
                Toast.makeText(DownloadService.this, "downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload(){
            if (downloadTask != null){
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload(){
            if (downloadTask != null){
                downloadTask.cancelDownload();
            }
            if (downloadUrl != null){
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + fileName);
                if (file.exists()){
                    file.delete();
                }//取消下载后将已下载文件删除
                getNotificationManger().cancel(1);
                stopForeground(true);
                Toast.makeText(DownloadService.this, "canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
