package example.earthquake;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

/**
 * Created by Pavel on 02/04/15.
 */
public class EarthQuakeWidget extends AppWidgetProvider {

    public void updateQuake(Context context){
        ComponentName thisWidget = new ComponentName(context,EarthQuakeWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        updateQuake(context,appWidgetManager,appWidgetIds);
    }

    public void updateQuake(Context context,AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Cursor lastEarthquake;
        ContentResolver cr = context.getContentResolver();
        lastEarthquake = cr.query(EarthQuakeContentProvider.CONTENT_URI,
                null, null, null, null);

        String magnitude = "--";
        String details = "-- None --";

        if (lastEarthquake != null) {
            try {
                if (lastEarthquake.moveToFirst()) {
                    int magColumn = lastEarthquake.getColumnIndexOrThrow(EarthQuakeContentProvider.KEY_MAGNITUDE);
                    int detailsColumn = lastEarthquake.getColumnIndexOrThrow(EarthQuakeContentProvider.KEY_DETAILS);

                    magnitude = lastEarthquake.getString(magColumn);
                    details = lastEarthquake.getString(detailsColumn);
                }
            }
            finally {
                lastEarthquake.close();
            }
        }

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.quake_widget);
            views.setTextViewText(R.id.widget_magnitude, magnitude);
            views.setTextViewText(R.id.widget_details, details);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        // Create a Pending Intent that will open the main Activity.
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent, 0);

        // Apply the On Click Listener to both Text Views.
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.quake_widget);

        views.setOnClickPendingIntent(R.id.widget_magnitude, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_details, pendingIntent);

        // Notify the App Widget Manager to update the
        appWidgetManager.updateAppWidget(appWidgetIds, views);

        // Update the Widget UI with the latest Earthquake details.
        updateQuake(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context, intent);

        if (EarthQuakeUpdateService.QUAKES_REFRESHED.equals(intent.getAction()))
            updateQuake(context);
    }
}
