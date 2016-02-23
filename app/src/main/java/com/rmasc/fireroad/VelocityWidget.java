package com.rmasc.fireroad;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class VelocityWidget extends AppWidgetProvider {

    public static int speed = 0;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.velocity_widget);
        views.setProgressBar(R.id.tachoMeter,230,speed,false);
        views.setTextViewText(R.id.txtValueProgress,speed + "\n Km/h");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        SharedPreferences sharedPref = context.getSharedPreferences("DeviceBLE", Context.MODE_PRIVATE);
        speed = sharedPref.getInt("Speed", 0);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

