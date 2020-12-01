package io.flutter.plugins.mapboxnative;

import android.app.Application;
import android.content.Context;
import android.graphics.Rect;
import androidx.lifecycle.Lifecycle;

import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.Style;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry;
import java.util.concurrent.atomic.AtomicInteger;

class MapboxBuilder implements MapboxOptionsSink {
    private final MapboxMapOptions options = new MapboxMapOptions()
        .textureMode(true)
        .attributionEnabled(true);
    private String accessToken;
    private boolean trackCameraPosition = false;
    private boolean myLocationEnabled = false;
    private boolean myLocationButtonEnabled = false;
    private double minimumZoomLevel = 1;
    private double maximumZoomLevel = 12;
    // private boolean indoorEnabled = true;
    // private boolean trafficEnabled = false;
    // private boolean buildingsEnabled = true;
    private Object initialMarkers;
    private Object initialClusters;
    private int myLocationTrackingMode = 0;
    private int myLocationRenderMode = 0;
    private String mapStyle = Style.MAPBOX_STREETS;
    // private Object initialPolygons;
    // private Object initialPolylines;
    // private Object initialCircles;
    // private Rect padding = new Rect(0, 0, 0, 0);

    MapboxController build(
        int id,
        Context context,
        AtomicInteger state,
        BinaryMessenger binaryMessenger,
        Application application,
        Lifecycle lifecycle,
        PluginRegistry.Registrar registrar,
        int activityHashCode) {
    final MapboxController controller =
        new MapboxController(
            id,
            context,
            state,
            binaryMessenger,
            application,
            lifecycle,
            registrar,
            activityHashCode,
            options,
            accessToken,
            mapStyle);
        controller.init();
        controller.setMyLocationEnabled(myLocationEnabled);
        controller.setMyLocationButtonEnabled(myLocationButtonEnabled);
        controller.setMyLocationTrackingMode(myLocationTrackingMode);
        controller.setMyLocationRenderMode(myLocationRenderMode);
        controller.setTrackCameraPosition(trackCameraPosition);
        controller.setInitialMarkers(initialMarkers);
        controller.setInitialClusters(initialClusters);
        controller.setMyLocationRenderMode(myLocationRenderMode);
        controller.setTrackCameraPosition(trackCameraPosition);
        return controller;
    }

    void setInitialCameraPosition(CameraPosition position) {
        options.camera(position);   
    }

    @Override
    public void setMapStyle(String style) {
        this.mapStyle = style;
    }

    @Override
    public void setMyLocationTrackingMode(int myLocationTrackingMode) {
        this.myLocationTrackingMode = myLocationTrackingMode;
    }

    @Override
    public void setMyLocationRenderMode(int myLocationRenderMode) {
        this.myLocationRenderMode = myLocationRenderMode;
    }

    @Override
    public void setCompassEnabled(boolean compassEnabled) {
        options.compassEnabled(compassEnabled);
    }

    @Override
    public void setMinimumZoomLevel(double min) {
        options.minZoomPreference(min);
    }

    @Override
    public void setMaximumZoomLevel(double max) {
        options.maxZoomPreference(max);
    }

    @Override
    public void setMyLocationEnabled(boolean myLocationEnabled) {
        this.myLocationEnabled = myLocationEnabled;
    }

    @Override
    public void setMyLocationButtonEnabled(boolean myLocationButtonEnabled) {
        this.myLocationButtonEnabled = myLocationButtonEnabled;
    }


    // @Override
    // public void setMapToolbarEnabled(boolean setMapToolbarEnabled) {
    //     options.mapToolbarEnabled(setMapToolbarEnabled);
    // }

    // @Override
    // public void setCameraTargetBounds(LatLngBounds bounds) {
    //     options.latLngBoundsForCameraTarget(bounds);
    // }

    // @Override
    // public void setMapType(int mapType) {
    //     options.mapType(mapType);
    // }

    @Override
    public void setMinMaxZoomPreference(Float min, Float max) {
        if (min != null) {
            options.minZoomPreference(min);
        }
        if (max != null) {
            options.maxZoomPreference(max);
        }
    }

    // @Override
    // public void setPadding(float top, float left, float bottom, float right) {
    //     this.padding = new Rect((int) left, (int) top, (int) right, (int) bottom);
    // }

    @Override
    public void setTrackCameraPosition(boolean trackCameraPosition) {
        this.trackCameraPosition = trackCameraPosition;
    }

    // @Override
    // public void setRotateGesturesEnabled(boolean rotateGesturesEnabled) {
    //     options.rotateGesturesEnabled(rotateGesturesEnabled);
    // }

    @Override
    public void setScrollGesturesEnabled(boolean scrollGesturesEnabled) {
        options.scrollGesturesEnabled(scrollGesturesEnabled);
    }

    // @Override
    // public void setTiltGesturesEnabled(boolean tiltGesturesEnabled) {
    //     options.tiltGesturesEnabled(tiltGesturesEnabled);
    // }

    @Override
        public void setZoomGesturesEnabled(boolean zoomGesturesEnabled) {
        options.zoomGesturesEnabled(zoomGesturesEnabled);
    }

    // @Override
    //     public void setLiteModeEnabled(boolean liteModeEnabled) {
    //     options.liteMode(liteModeEnabled);
    // }

    // @Override
    // public void setIndoorEnabled(boolean indoorEnabled) {
    //     this.indoorEnabled = indoorEnabled;
    // }

    // @Override
    // public void setTrafficEnabled(boolean trafficEnabled) {
    //     this.trafficEnabled = trafficEnabled;
    // }

    // @Override
    // public void setBuildingsEnabled(boolean buildingsEnabled) {
    //     this.buildingsEnabled = buildingsEnabled;
    // }

    @Override
    public void setZoomControlsEnabled(boolean zoomControlsEnabled) {
        options.zoomGesturesEnabled(zoomControlsEnabled);
    }

    @Override
    public void setInitialMarkers(Object initialMarkers) {
        this.initialMarkers = initialMarkers;
    }

    @Override
    public void setInitialClusters(Object initialClusters) {
        this.initialClusters = initialClusters;
    }

    // @Override
    // public void setInitialPolygons(Object initialPolygons) {
    //     this.initialPolygons = initialPolygons;
    // }

    // @Override
    // public void setInitialPolylines(Object initialPolylines) {
    //     this.initialPolylines = initialPolylines;
    // }

    // @Override
    // public void setInitialCircles(Object initialCircles) {
    //     this.initialCircles = initialCircles;
    // }
}
