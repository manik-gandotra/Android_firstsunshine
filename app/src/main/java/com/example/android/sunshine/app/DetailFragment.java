package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int loaderID=0;
    private Uri mUri;
    static final String DETAIL_URI="URI";
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };
    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView dayView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView wind;
        public final TextView pressure;
        public final TextView humidity;
        public final TextView dateView;

        public ViewHolder(View view){
            iconView=(ImageView)view.findViewById(R.id.detail_image);
            dateView=(TextView)view.findViewById(R.id.detail_date);
            descriptionView=(TextView)view.findViewById(R.id.detail_forecast);
            highTempView=(TextView)view.findViewById(R.id.detail_high_temp);
            lowTempView=(TextView)view.findViewById(R.id.detail_low_temp);
            wind=(TextView)view.findViewById(R.id.detail_wind);
            pressure=(TextView)view.findViewById(R.id.detail_pressure);
            humidity=(TextView)view.findViewById(R.id.detail_humidity);
            dayView=(TextView)view.findViewById(R.id.detail_day);
        }
    }

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_HUMIDITY=9;
    static final int COL_PRESSURE=10;
    static final int COL_WIND_SPEED=11;
    static final int COL_DEGREES=12;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;
    private String mForecast;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments=getArguments();
        if(arguments!=null)
            mUri=arguments.getParcelable(DETAIL_URI);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mForecastStr = intent.getDataString();
        }
        return rootView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(loaderID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private ShareActionProvider mShareActionProvider;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mForecast != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }
    private Uri uri;
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
       if(mUri!=null) {
           return new CursorLoader(getActivity(),mUri, FORECAST_COLUMNS, null, null, null);
       }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst())
        {
            return;
        }
        String day=Utility.getDayName(getActivity(),data.getLong(COL_WEATHER_DATE));
        String date=Utility.getFormattedMonthDay(getActivity(),data.getLong(COL_WEATHER_DATE));
        String desc=data.getString(COL_WEATHER_DESC);
        float hum=data.getFloat(COL_HUMIDITY);
        ViewHolder viewHolder=new ViewHolder(getView());
        viewHolder.descriptionView.setText(desc);
        viewHolder.humidity.setText(String.format(getActivity().getString(R.string.format_humidity),hum));
        double press=data.getDouble(COL_PRESSURE);
        viewHolder.pressure.setText(String.format(getActivity().getString(R.string.format_pressure),press));
        viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));
        viewHolder.wind.setText(Utility.getFormattedWind(getActivity(),data.getFloat(COL_WIND_SPEED),data.getFloat(COL_DEGREES)));
        boolean isMetric=Utility.isMetric(getActivity());
        String high=Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_MAX_TEMP),isMetric);
        String low=Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_MIN_TEMP),isMetric);
        viewHolder.highTempView.setText(high);
        viewHolder.lowTempView.setText(low);
        viewHolder.dayView.setText(day);
        viewHolder.dateView.setText(date);
        if(mShareActionProvider!=null)
        mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(loaderID, null, this);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecast + FORECAST_SHARE_HASHTAG);

        return shareIntent;
    }
}
