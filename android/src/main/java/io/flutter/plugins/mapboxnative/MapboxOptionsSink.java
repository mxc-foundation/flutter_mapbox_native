package io.flutter.plugins.mapboxnative;

import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

/** Receiver of GoogleMap configuration options. */
interface MapboxOptionsSink {
    void setMapStyle(String style);
    void setMinimumZoomLevel(double min);
    void setMaximumZoomLevel(double max);
    void setMyLocationEnabled(boolean myLocationEnabled);
    void setMyLocationButtonEnabled(boolean myLocationButtonEnabled);
    void setInitialMarkers(Object initialMarkers);
    void setInitialClusters(Object initialClusters);
    void setMyLocationTrackingMode(int myLocationTrackingMode);
    void setMyLocationRenderMode(int myLocationRenderMode);

    void setCompassEnabled(boolean compassEnabled);
    void setMinMaxZoomPreference(Float min, Float max);;
    void setScrollGesturesEnabled(boolean scrollGesturesEnabled);
    void setTrackCameraPosition(boolean trackCameraPosition);
    void setZoomGesturesEnabled(boolean zoomGesturesEnabled);
    void setZoomControlsEnabled(boolean zoomControlsEnabled);
}
