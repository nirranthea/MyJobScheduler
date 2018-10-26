package dicoding.made.codelabs9myjobscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class GetCurrentWeatherJobService extends JobService {

    public static final String TAG = GetCurrentWeatherJobService.class.getSimpleName();
    final String APP_ID = "21d835b157bdce462b0f305ae830b7f1";
    final String CITY = "Bandung";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("nirrant", "onStartJob() Executed");
        getCurrentWeather(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("nirrant", "onStopJob() Executed");

        return true;
    }

    private void getCurrentWeather(final JobParameters job) {
        Log.d("nirrant", "Running");
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&appid=" + APP_ID;
        Log.e("nirrant", "getCurrentWeather: " + url);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d("nirrant", result);
                try {
                    JSONObject responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");
                    double tempInCelcius = tempInKelvin - 273;
                    String temperature = new DecimalFormat("##.##").format(tempInCelcius);
                    String title = "Current Weather";
                    String message = currentWeather + ", " + description + " with " + temperature + " celcius";
                    int notifId = 100;
                    showNotification(getApplicationContext(), title, message, notifId);
                    jobFinished(job, false);
                    Log.d("check","success Response");
                } catch (Exception e){
                    jobFinished(job, true);
                    e.printStackTrace();
                    Log.d("check","exception Response");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                jobFinished(job, true);
                Log.d("check","failure Response");
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId) {
        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"ChannelId")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.replay_30_black)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.black))
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setSound(alarmSound);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel("ChannelId", "NOTIFICATION_CHANNEL_NAME",importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            builder.setChannelId("ChannelId");
            notificationManagerCompat.createNotificationChannel(notificationChannel);
        }
        notificationManagerCompat.notify(notifId, builder.build());
    }
}
