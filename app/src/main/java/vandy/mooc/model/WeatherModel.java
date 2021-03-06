package vandy.mooc.model;

import java.lang.ref.WeakReference;
import java.util.List;

import vandy.mooc.MVP;
import vandy.mooc.common.GenericServiceConnection;
import vandy.mooc.model.aidl.WeatherCall;
import vandy.mooc.model.aidl.WeatherData;
import vandy.mooc.model.aidl.WeatherRequest;
import vandy.mooc.model.aidl.WeatherResults;
import vandy.mooc.model.services.WeatherServiceAsync;
import vandy.mooc.model.services.WeatherServiceSync;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class plays the "Model" role in the Model-View-Presenter (MVP)
 * pattern by defining an interface for providing data that will be
 * acted upon by the "Presenter" and "View" layers in the MVP pattern.
 * It implements the MVP.ProvidedModelOps so it can be created/managed
 * by the GenericPresenter framework.
 */
public class WeatherModel
        implements MVP.ProvidedModelOps {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
            WeatherModel.class.getSimpleName();

    /**
     * A WeakReference used to access methods in the Presenter layer.
     * The WeakReference enables garbage collection.
     */
    protected WeakReference<MVP.RequiredPresenterOps> mPresenter;

    /**
     * Location we're trying to get current weather for.
     */
    private String mLocation;

    // TODO -x- define ServiceConnetions to connect to the
    // WeatherServiceSync and WeatherServiceAsync.

    WeatherCall mWeatherCall;

    WeatherRequest mWeatherRequest;

    ServiceConnection mServiceConnectionSync = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mWeatherCall = WeatherCall.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWeatherCall = null;
        }
    };

    ServiceConnection mServiceConnectionAsync = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mWeatherRequest = WeatherRequest.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWeatherRequest = null;
        }
    };

    /**
     * Hook method called when a new WeatherModel instance is created
     * to initialize the ServiceConnections and bind to the WeatherService*.
     *
     * @param presenter A reference to the Presenter layer.
     */
    @Override
    public void onCreate(MVP.RequiredPresenterOps presenter) {
        // Set the WeakReference.
        mPresenter = new WeakReference<>(presenter);

        // TODO -x?- you fill in here to initialize the WeatherService*.

        // Bind to the services.
        bindService();
    }

    /**
     * Hook method called to shutdown the Presenter layer.
     */
    @Override
    public void onDestroy(boolean isChangingConfigurations) {
        // Don't bother unbinding the service if we're simply changing
        // configurations.
        if (isChangingConfigurations)
            Log.d(TAG,
                    "Simply changing configurations, no need to destroy the Service");
        else
            unbindService();
    }

    /**
     * The implementation of the WeatherResults AIDL Interface, which
     * will be passed to the Weather Web service using the
     * WeatherRequest.getCurrentWeather() method.
     * <p/>
     * This implementation of WeatherResults.Stub plays the role of
     * Invoker in the Broker Pattern since it dispatches the upcall to
     * sendResults().
     */
    private final WeatherResults.Stub mWeatherResults =
            new WeatherResults.Stub() {
                /**
                 * This method is invoked by the WeatherServiceAsync to
                 * return the results back.
                 */
                @Override
                public void sendResults(final WeatherData weatherResults)
                        throws RemoteException {
                    // Pass the results back to the Presenter's
                    // displayResults() method.
                    // TODO -x- you fill in here.
                    mPresenter.get().displayResults(weatherResults, null);
                }

                /**
                 * This method is invoked by the WeatherServiceAsync to
                 * return error results back.
                 */
                @Override
                public void sendError(final String reason)
                        throws RemoteException {
                    // Pass the results back to the Presenter's
                    // displayResults() method.
                    // TODO -x- you fill in here.
                    mPresenter.get().displayResults(
                            null
                            , String.format("No weather data for location \"%s\" found", mLocation)
                    );
                }
            };

    /**
     * Initiate the service binding protocol.
     */
    private void bindService() {
        Log.d(TAG,
                "calling bindService()");

        // Launch the Weather Bound Services if they aren't already
        // running via a call to bindService(), which binds this
        // activity to the WeatherService* if they aren't already
        // bound.

        // TODO -x- you fill in here.
        // Bind this activity to the DownloadBoundService* Services if
        // they aren't already bound Use mBoundSync/mBoundAsync
        if (mWeatherCall == null) {
            mPresenter.get()
                    .getApplicationContext()
                    .bindService(WeatherServiceSync.makeIntent
                                    (mPresenter.get()
                                            .getActivityContext()),
                            mServiceConnectionSync,
                            Context.BIND_AUTO_CREATE);
            Log.d(TAG,
                    "Calling bindService() on DownloadBoundServiceSync");
        }
        if (mWeatherRequest == null) {
            mPresenter.get()
                    .getApplicationContext()
                    .bindService(WeatherServiceAsync.makeIntent
                                    (mPresenter.get()
                                            .getActivityContext()),
                            mServiceConnectionAsync,
                            Context.BIND_AUTO_CREATE);
            Log.d(TAG,
                    "Calling bindService() on DownloadBoundServiceAsync");
        }
    }

    /**
     * Initiate the service unbinding protocol.
     */
    private void unbindService() {
        Log.d(TAG,
                "calling unbindService()");

        // TODO -x- you fill in here to unbind from the WeatherService*.
        if (mWeatherCall != null)
            mPresenter.get()
                    .getApplicationContext()
                    .unbindService(mServiceConnectionSync);
        if (mWeatherRequest != null)
            mPresenter.get()
                    .getApplicationContext()
                    .unbindService(mServiceConnectionAsync);
    }

    /**
     * Initiate the asynchronous weather lookup.
     */
    public boolean getWeatherAsync(String location) {
        // TODO -x- you fill in here.
        if (mWeatherRequest != null) {
            try {
                mLocation = location;
                mWeatherRequest.getCurrentWeather(location, mWeatherResults);
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Initiate the synchronous weather lookup.
     */
    public WeatherData getWeatherSync(String location) {
        // TODO -x- you fill in here.
        if (mWeatherCall != null) {
            try {
                mLocation = location;
                List<WeatherData> weatherDataList
                        = mWeatherCall.getCurrentWeather(location);
                return weatherDataList != null ? weatherDataList.get(0) : null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
