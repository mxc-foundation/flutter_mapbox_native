package io.flutter.plugins.mapboxnative;

import android.util.Log;
import android.app.Application;
import android.content.Context;
import androidx.lifecycle.Lifecycle;

import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.camera.CameraPosition;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapboxFactory extends PlatformViewFactory {

 private final AtomicInteger mActivityState;
 private final BinaryMessenger binaryMessenger;
 private final Application application;
 private final int activityHashCode;
 private final Lifecycle lifecycle;
 private final PluginRegistry.Registrar registrar; // V1 embedding only.

 MapboxFactory(
     AtomicInteger state,
     BinaryMessenger binaryMessenger,
     Application application,
     Lifecycle lifecycle,
     PluginRegistry.Registrar registrar,
     int activityHashCode) {
   super(StandardMessageCodec.INSTANCE);
   mActivityState = state;
   this.binaryMessenger = binaryMessenger;
   this.application = application;
   this.activityHashCode = activityHashCode;
   this.lifecycle = lifecycle;
   this.registrar = registrar;
 }

 @SuppressWarnings("unchecked")
 @Override
 public PlatformView create(Context context, int id, Object args) {
   Map<String, Object> params = (Map<String, Object>) args;
   final MapboxBuilder builder = new MapboxBuilder();

   Convert.interpretMapboxOptions(params.get("options"), builder);
   if (params.containsKey("mapStyle")) {
      final Object styleString = params.get("mapStyle");
      builder.setMapStyle(Convert.toString(styleString));
   }
   if (params.containsKey("center")) {
     CameraPosition position = Convert.toCameraPosition(params.get("center"));
     builder.setInitialCameraPosition(position);
   }
   if (params.containsKey("markersToAdd")) {
     builder.setInitialMarkers(params.get("markersToAdd"));
   }
   if (params.containsKey("clusters")) {
     builder.setInitialClusters(params.get("clusters"));
   }
   // if (params.containsKey("polygonsToAdd")) {
   //   builder.setInitialPolygons(params.get("polygonsToAdd"));
   // }
   // if (params.containsKey("polylinesToAdd")) {
   //   builder.setInitialPolylines(params.get("polylinesToAdd"));
   // }
   // if (params.containsKey("circlesToAdd")) {
   //   builder.setInitialCircles(params.get("circlesToAdd"));
   // }
   return builder.build(
       id,
       context,
       mActivityState,
       binaryMessenger,
       application,
       lifecycle,
       registrar,
       activityHashCode);
 }
}
