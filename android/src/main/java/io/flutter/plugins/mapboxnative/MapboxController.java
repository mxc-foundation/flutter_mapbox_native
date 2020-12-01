package io.flutter.plugins.mapboxnative;

import static io.flutter.plugins.mapboxnative.FlutterMapboxNativePlugin.CREATED;
import static io.flutter.plugins.mapboxnative.FlutterMapboxNativePlugin.DESTROYED;
import static io.flutter.plugins.mapboxnative.FlutterMapboxNativePlugin.PAUSED;
import static io.flutter.plugins.mapboxnative.FlutterMapboxNativePlugin.RESUMED;
import static io.flutter.plugins.mapboxnative.FlutterMapboxNativePlugin.STARTED;
import static io.flutter.plugins.mapboxnative.FlutterMapboxNativePlugin.STOPPED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.SnapshotReadyCallback;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.plugins.annotation.Annotation;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnAnnotationClickListener;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.not;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/** Controller of a single GoogleMaps MapView instance. */
final class MapboxController
   implements Application.ActivityLifecycleCallbacks,
       DefaultLifecycleObserver,
       ActivityPluginBinding.OnSaveInstanceStateListener,
       MapboxOptionsSink,
       MethodChannel.MethodCallHandler,
       OnAnnotationClickListener,
       OnCameraTrackingChangedListener,
       OnMapReadyCallback,
       OnSymbolTappedListener,
       MapboxListener,
       PlatformView {

  private static final String TAG = "MapboxController";
  private final int id;
  private final AtomicInteger activityState;
  private final MethodChannel methodChannel;
  private final MapboxMapOptions options;
  @Nullable private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean trackCameraPosition = false;
  private boolean myLocationEnabled = false;
  private boolean myLocationButtonEnabled = false;
  private boolean zoomControlsEnabled = true;
  private boolean indoorEnabled = true;
  private boolean trafficEnabled = false;
  private boolean buildingsEnabled = true;
  private final String mapStyle;
  private Style style;
  private LocationComponent locationComponent = null;
  private int myLocationTrackingMode = 0;
  private int myLocationRenderMode = 0;
  private LocationEngine locationEngine = null;
  private LocalizationPlugin localizationPlugin;
  private SymbolManager symbolManager;
  private final Map<String, SymbolController> symbols;

  private boolean disposed = false;
  private final float density;
  private MethodChannel.Result mapReadyResult;
  private final int activityHashCode; // Do not use directly, use getActivityHashCode() instead to get correct hashCode for both v1 and v2 embedding.
  private final Lifecycle lifecycle;
  private final Context context;
  private final Application mApplication; // Do not use direclty, use getApplication() instead to get correct application object for both v1 and v2 embedding.
  private final PluginRegistry.Registrar registrar; // For v1 embedding only.
  // private final MarkersController markersController;
  private List<Object> initialMarkers;
  private FeatureCollection initialClusters;

  // private Location lastLocation;
  // private static final String SAVED_STATE_LOCATION = "saved_state_location";

  MapboxController(
    int id,
    Context context,
    AtomicInteger activityState,
    BinaryMessenger binaryMessenger,
    Application application,
    Lifecycle lifecycle,
    PluginRegistry.Registrar registrar,
    int registrarActivityHashCode,
    MapboxMapOptions options,
    String accessToken,
    String mapStyle) {
      Mapbox.getInstance(context, accessToken!=null ? accessToken : getAccessToken(context));
      this.id = id;
      this.context = context;
      this.activityState = activityState;
      this.options = options;
      this.mapView = new MapView(context, options);
      this.density = context.getResources().getDisplayMetrics().density;
      methodChannel = new MethodChannel(binaryMessenger, "plugins.flutter.io/mapbox_native_" + id);
      methodChannel.setMethodCallHandler(this);
      mApplication = application;
      this.lifecycle = lifecycle;
      this.registrar = registrar;
      this.mapStyle = mapStyle;
      this.symbols = new HashMap<>();
      this.activityHashCode = registrarActivityHashCode;
      // this.markersController = new MarkersController(methodChannel);
      //  this.polygonsController = new PolygonsController(methodChannel, density);
      //  this.polylinesController = new PolylinesController(methodChannel, density);
      //  this.circlesController = new CirclesController(methodChannel, density);
  }

private static String getAccessToken(@NonNull Context context) {
  try {
    ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
    Bundle bundle = ai.metaData;
    String token = bundle.getString("com.mapbox.token");
    if (token == null || token.isEmpty()) {
      throw new NullPointerException();
    }
    return token;
  } catch (Exception e) {
    Log.e(TAG, "Failed to find an Access Token in the Application meta-data. Maps may not load correctly. " +
      "Please refer to the installation guide at https://docs.mapbox.com/android/maps/overview/ " +
      "for troubleshooting advice." + e.getMessage());
  }
  return null;
}

  @Override
  public View getView() {
    return mapView;
  }

  void init() {
    switch (activityState.get()) {
      case STOPPED:
        mapView.onCreate(null);
        mapView.onStart();
        mapView.onResume();
        mapView.onPause();
        mapView.onStop();
        break;
      case PAUSED:
        mapView.onCreate(null);
        mapView.onStart();
        mapView.onResume();
        mapView.onPause();
        break;
      case RESUMED:
        mapView.onCreate(null);
        mapView.onStart();
        mapView.onResume();
        break;
      case STARTED:
        mapView.onCreate(null);
        mapView.onStart();
        break;
      case CREATED:
        mapView.onCreate(null);
        break;
      case DESTROYED:
        mapView.onDestroy();
        break;
      default:
        throw new IllegalArgumentException(
          "Cannot interpret " + activityState.get() + " as an activity state");
    }
    if (lifecycle != null) {
      lifecycle.addObserver(this);
    } else {
      getApplication().registerActivityLifecycleCallbacks(this);
    }

    mapView.getMapAsync(this);
  }

  private SymbolController symbol(String symbolId) {
    final SymbolController symbol = symbols.get(symbolId);
    if (symbol == null) {
      throw new IllegalArgumentException("Unknown symbol: " + symbolId);
    }
    return symbol;
  }

  private void moveCamera(CameraUpdate cameraUpdate) {
    mapboxMap.animateCamera(cameraUpdate,4500);
  }

  private void animateCamera(CameraUpdate cameraUpdate) {
    mapboxMap.animateCamera(cameraUpdate);
  }

  private CameraPosition getCameraPosition() {
    return trackCameraPosition ? mapboxMap.getCameraPosition() : null;
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    if (mapReadyResult != null) {
      mapReadyResult.success(null);
      mapReadyResult = null;
    }
    setMapboxListener(this);

    setMapStyle(mapStyle);

  }

  @Override
  public boolean onAnnotationClick(Annotation annotation) {
    if (annotation instanceof Symbol) {
      final SymbolController symbolController = symbols.get(String.valueOf(annotation.getId()));
      if (symbolController != null) {
        symbolController.onTap();
        return true;
      }
    }
    return false;
  }

  @Override
  public void setMyLocationTrackingMode(int myLocationTrackingMode) {
    if (this.myLocationTrackingMode == myLocationTrackingMode) {
      return;
    }
    this.myLocationTrackingMode = myLocationTrackingMode;
    if (mapboxMap != null && locationComponent != null) {
      updateMyLocationTrackingMode();
    }
  }

  @Override
  public void setMyLocationRenderMode(int myLocationRenderMode) {
    if (this.myLocationRenderMode == myLocationRenderMode) {
      return;
    }
    this.myLocationRenderMode = myLocationRenderMode;
    if (mapboxMap != null && locationComponent != null) {
      updateMyLocationRenderMode();
    }
  }

  @Override
  public void setMapStyle(String styleString) {
    //check if json, url or plain string:
    if (styleString == null || styleString.isEmpty()) {
      Log.e(TAG, "setStyleString - string empty or null");
    } else if (styleString.startsWith("{") || styleString.startsWith("[")) {
     mapboxMap.setStyle(new Style.Builder().fromJson(styleString), onStyleLoadedCallback);
    } else if (
      !styleString.startsWith("http://") && 
      !styleString.startsWith("https://")&& 
      !styleString.startsWith("mapbox://")) {
    // We are assuming that the style will be loaded from an asset here.
        AssetManager assetManager = registrar.context().getAssets();
        String key = registrar.lookupKeyForAsset(styleString);
        mapboxMap.setStyle(new Style.Builder().fromUri("asset://" + key), onStyleLoadedCallback);
    } else {
      mapboxMap.setStyle(new Style.Builder().fromUrl(styleString), onStyleLoadedCallback);
    }
  }

  Style.OnStyleLoaded onStyleLoadedCallback = new Style.OnStyleLoaded() {
    @Override
    public void onStyleLoaded(@NonNull Style style) {
      MapboxController.this.style = style;
      enableSymbolManager(style);
      addClusteredSource(style);

      if (myLocationEnabled) {
        enableLocationComponent(style);
      }
      // needs to be placed after SymbolManager#addClickListener,
      // is fixed with 0.6.0 of annotations plugin
      mapboxMap.addOnMapClickListener(MapboxController.this);
      // mapboxMap.addOnMapLongClickListener(MapboxController.this);
	  
	    localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);

      methodChannel.invokeMethod("map#onStyleLoaded", null);
    }
  };

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationComponent(@NonNull Style style) {
    if (hasLocationPermission()) {
      locationEngine = LocationEngineProvider.getBestLocationEngine(context);
      LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(context)
        .trackingGesturesManagement(true)
        .build();

      locationComponent = mapboxMap.getLocationComponent();
      locationComponent.activateLocationComponent(context, style, locationComponentOptions);
      locationComponent.setLocationComponentEnabled(true);
      // locationComponent.setRenderMode(RenderMode.COMPASS); // remove or keep default?
      locationComponent.setLocationEngine(locationEngine);
      locationComponent.setMaxAnimationFps(30);
      updateMyLocationTrackingMode();
      setMyLocationTrackingMode(this.myLocationTrackingMode);
      updateMyLocationRenderMode();
      setMyLocationRenderMode(this.myLocationRenderMode);
      locationComponent.addOnCameraTrackingChangedListener(this);

    } else {
      Log.e(TAG, "missing location permissions");
    }
  }

  private void enableSymbolManager(@NonNull Style style) {
    if (symbolManager == null) {
      symbolManager = new SymbolManager(mapView, mapboxMap, style);
      symbolManager.setIconAllowOverlap(true);
      symbolManager.setIconIgnorePlacement(true);
      symbolManager.setTextAllowOverlap(true);
      symbolManager.setTextIgnorePlacement(true);
      symbolManager.addClickListener(MapboxController.this::onAnnotationClick);
    }

    setInitialMarkers(initialMarkers);
  }

  private void updateMyLocationTrackingMode() {
    int[] mapboxTrackingModes = new int[] {CameraMode.NONE, CameraMode.TRACKING, CameraMode.TRACKING_COMPASS, CameraMode.TRACKING_GPS};
    locationComponent.setCameraMode(mapboxTrackingModes[this.myLocationTrackingMode]);
  }

  private void updateMyLocationRenderMode() {
    int[] mapboxRenderModes = new int[] {RenderMode.NORMAL, RenderMode.COMPASS, RenderMode.GPS};
    locationComponent.setRenderMode(mapboxRenderModes[this.myLocationRenderMode]);
  }

 @Override
 public void onMethodCall(MethodCall call, MethodChannel.Result result) {
   switch (call.method) {
     case "map#init":
       if (mapboxMap != null) {
         result.success(null);
         return;
       }
       mapReadyResult = result;
       break;
     case "myLocation#visiable":
       {
        setMyLocationEnabled(Convert.toBoolean(call.argument("visiable")));
        result.success(null);
       }
       break;
     case "camera#moveToMyLocation":
       {
        moveToMyLocation();
       }
       break;
     case "map#updateMyLocationTrackingMode": {
        int myLocationTrackingMode = call.argument("mode");
        setMyLocationTrackingMode(myLocationTrackingMode);
        result.success(null);
        break;
      }
     case "markers#update":
       {
         Object markersToAdd = call.argument("markersToAdd");
         this.initialMarkers.addAll(0,(List<Object>) markersToAdd);
         if(symbolManager != null){
          symbolManager.deleteAll();
          addInitialMarkers();
         }
         break;
       }
     case "clusters#update":
       {
        Object clusters = call.argument("clusters");
        this.initialClusters = FeatureCollection.fromJson((String) clusters);
        style.removeLayer("circle-cluster");
        style.removeLayer("unclustered-points");
        style.removeLayer("count");
        style.removeSource("clusters");
        addClusteredSource(style);
        break;
       }
     case "map#update":
       {
         Convert.interpretMapboxOptions(call.argument("options"), this);
         result.success(Convert.cameraPositionToJson(getCameraPosition()));
         break;
       }
     case "map#getVisibleRegion":
       {
         if (mapboxMap != null) {
           LatLngBounds latLngBounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
           result.success(Convert.latlngBoundsToJson(latLngBounds));
         } else {
           result.error(
               "MapboxMap uninitialized",
               "getVisibleRegion called prior to map initialization",
               null);
         }
         break;
       }
     case "map#getScreenCoordinate":
       {
         if (mapboxMap != null) {
           LatLng latLng = Convert.toLatLng(call.arguments);
          //  Point screenLocation = mapboxMap.getProjection().toScreenLocation(latLng);
          //  result.success(Convert.pointToJson(screenLocation));
         } else {
           result.error(
               "MapboxMap uninitialized",
               "getScreenCoordinate called prior to map initialization",
               null);
         }
         break;
       }
     case "map#getLatLng":
       {
         if (mapboxMap != null) {
          //  Point point = Convert.toPoint(call.arguments);
          //  LatLng latLng = mapboxMap.getProjection().fromScreenLocation(point);
          //  result.success(Convert.latLngToJson(latLng));
         } else {
           result.error(
               "MapboxMap uninitialized", "getLatLng called prior to map initialization", null);
         }
         break;
       }
     case "map#takeSnapshot":
       {
         if (mapboxMap != null) {
           final MethodChannel.Result _result = result;
           mapboxMap.snapshot(
               new SnapshotReadyCallback() {
                 @Override
                 public void onSnapshotReady(Bitmap bitmap) {
                   ByteArrayOutputStream stream = new ByteArrayOutputStream();
                   bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                   byte[] byteArray = stream.toByteArray();
                   bitmap.recycle();
                   _result.success(byteArray);
                 }
               });
         } else {
           result.error("MapboxMap uninitialized", "takeSnapshot", null);
         }
         break;
       }
     case "camera#move":
       {
         final CameraUpdate cameraUpdate =
             Convert.toCameraUpdate(call.argument("cameraUpdate"), density);
         moveCamera(cameraUpdate);
         result.success(null);
         break;
       }
     case "camera#animate":
       {
         final CameraUpdate cameraUpdate =
             Convert.toCameraUpdate(call.argument("cameraUpdate"), density);
         animateCamera(cameraUpdate);
         result.success(null);
         break;
       }
     case "map#isCompassEnabled":
       {
         result.success(mapboxMap.getUiSettings().isCompassEnabled());
         break;
       }
     case "map#getMinMaxZoomLevels":
       {
         List<Float> zoomLevels = new ArrayList<>(2);
         zoomLevels.add((float)(mapboxMap.getMinZoomLevel()));
         zoomLevels.add((float)(mapboxMap.getMaxZoomLevel()));
         result.success(zoomLevels);
         break;
       }
     case "map#isZoomGesturesEnabled":
       {
         result.success(mapboxMap.getUiSettings().isZoomGesturesEnabled());
         break;
       }
     case "map#isScrollGesturesEnabled":
       {
         result.success(mapboxMap.getUiSettings().isScrollGesturesEnabled());
         break;
       }
     case "map#isTiltGesturesEnabled":
       {
         result.success(mapboxMap.getUiSettings().isTiltGesturesEnabled());
         break;
       }
     case "map#isRotateGesturesEnabled":
       {
         result.success(mapboxMap.getUiSettings().isRotateGesturesEnabled());
         break;
       }
     case "map#getZoomLevel":
       {
         result.success(mapboxMap.getCameraPosition().zoom);
         break;
       }
     case "map#setStyle":
       {
         String mapStyle = (String) call.arguments;
         setMapStyle(mapStyle);
         break;
       }
     case "style#addImage":{
        if(style==null){
          result.error("STYLE IS NULL", "The style is null. Has onStyleLoaded() already been invoked?", null);
        }
        Logger.e("addImage","image: "+call.argument("name"));
        style.addImage(call.argument("name"), BitmapFactory.decodeByteArray(call.argument("bytes"),0,call.argument("length")), false);
        result.success(null);
        break;
      }
     default:
       result.notImplemented();
   }
 }

 private void moveToMyLocation(){
  if(myLocationEnabled){
    if(locationComponent == null){
      locationComponent = mapboxMap.getLocationComponent();
    }

    Location lastKnownLocation = locationComponent.getLastKnownLocation();
    if(lastKnownLocation != null){
      final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()),mapboxMap.getMaxZoomLevel());
      moveCamera(cameraUpdate);
    }
  }
 }

 @Override
 public boolean onMapClick(LatLng latLng) {
   final Map<String, Object> arguments = new HashMap<>(2);
   arguments.put("position", Convert.latLngToJson(latLng));
   methodChannel.invokeMethod("map#onTap", arguments);
   return true;
 }

 @Override
  public void onCameraTrackingChanged(int currentMode) {
    final Map<String, Object> arguments = new HashMap<>(2);
    arguments.put("mode", currentMode);
    methodChannel.invokeMethod("map#onCameraTrackingChanged", arguments);
  }

 @Override
  public void onCameraTrackingDismissed() {
    this.myLocationTrackingMode = 1;
    methodChannel.invokeMethod("map#onCameraTrackingDismissed", new HashMap<>());
  }

 @Override
 public void dispose() {
   if (disposed) {
     return;
   }
   disposed = true;

   if (locationComponent != null) {
    locationComponent.setLocationComponentEnabled(false);
   }

   if (symbolManager != null) {
    symbolManager.onDestroy();
   }

   methodChannel.setMethodCallHandler(null);
   setMapboxListener(null);
   destroyMapViewIfNecessary();
   getApplication().unregisterActivityLifecycleCallbacks(this);
 }

 private void setMapboxListener(@Nullable MapboxListener listener) {
  mapboxMap.addOnMapClickListener(listener);
 }

 // @Override
 // The minimum supported version of Flutter doesn't have this method on the PlatformView interface, but the maximum
 // does. This will override it when available even with the annotation commented out.
 public void onInputConnectionLocked() {
   // TODO(mklim): Remove this empty override once https://github.com/flutter/flutter/issues/40126 is fixed in stable.
 };

 // @Override
 // The minimum supported version of Flutter doesn't have this method on the PlatformView interface, but the maximum
 // does. This will override it when available even with the annotation commented out.
 public void onInputConnectionUnlocked() {
   // TODO(mklim): Remove this empty override once https://github.com/flutter/flutter/issues/40126 is fixed in stable.
 };

 // Application.ActivityLifecycleCallbacks methods
 @Override
 public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
   if (disposed || activity.hashCode() != getActivityHashCode()) {
     return;
   }
   mapView.onCreate(savedInstanceState);
 }

 @Override
 public void onActivityStarted(Activity activity) {
   if (disposed || activity.hashCode() != getActivityHashCode()) {
     return;
   }
   mapView.onStart();
 }

 @Override
 public void onActivityResumed(Activity activity) {
   if (disposed || activity.hashCode() != getActivityHashCode()) {
     return;
   }
   mapView.onResume();
 }

 @Override
 public void onActivityPaused(Activity activity) {
   if (disposed || activity.hashCode() != getActivityHashCode()) {
     return;
   }
   mapView.onPause();
 }

 @Override
 public void onActivityStopped(Activity activity) {
   if (disposed || activity.hashCode() != getActivityHashCode()) {
     return;
   }
   mapView.onStop();
 }

 @Override
 public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
   if (disposed || activity.hashCode() != getActivityHashCode()) {
     return;
   }
   mapView.onSaveInstanceState(outState);
 }

 @Override
 public void onActivityDestroyed(Activity activity) {
   if (disposed || activity.hashCode() != getActivityHashCode()) {
     return;
   }
   destroyMapViewIfNecessary();
 }

 // DefaultLifecycleObserver and OnSaveInstanceStateListener

 @Override
 public void onCreate(@NonNull LifecycleOwner owner) {
   if (disposed) {
     return;
   }
   mapView.onCreate(null);
 }

 @Override
 public void onStart(@NonNull LifecycleOwner owner) {
   if (disposed) {
     return;
   }
   mapView.onStart();
 }

 @Override
 public void onResume(@NonNull LifecycleOwner owner) {
   if (disposed) {
     return;
   }
   mapView.onResume();
 }

 @Override
 public void onPause(@NonNull LifecycleOwner owner) {
   if (disposed) {
     return;
   }
   mapView.onResume();
 }

 @Override
 public void onStop(@NonNull LifecycleOwner owner) {
   if (disposed) {
     return;
   }
   mapView.onStop();
 }

 @Override
 public void onDestroy(@NonNull LifecycleOwner owner) {
   if (disposed) {
     return;
   }
   destroyMapViewIfNecessary();
 }

 @Override
 public void onRestoreInstanceState(Bundle bundle) {
   if (disposed) {
     return;
   }
   mapView.onCreate(bundle);

  //  if (bundle != null) {
  //   lastLocation = bundle.getParcelable(SAVED_STATE_LOCATION);
  //  }
 }

 @Override
 public void onSaveInstanceState(Bundle bundle) {
   if (disposed) {
     return;
   }
   mapView.onSaveInstanceState(bundle);
  //  if (locationComponent != null) {
  //   bundle.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
  //  }
 }

  @Override
  public void onSymbolTapped(Symbol symbol) {
    final Map<String, Object> arguments = new HashMap<>(2);
    arguments.put("symbol", String.valueOf(symbol.getId()));
    methodChannel.invokeMethod("symbol#onTap", arguments);
  }
  
 // MapboxOptionsSink methods
 
 @Override
 public void setMinimumZoomLevel(double min) {
    mapboxMap.setMinZoomPreference(min);
 }

 @Override
 public void setMaximumZoomLevel(double max) {
    mapboxMap.setMaxZoomPreference(max);
 }

 @Override
 public void setCompassEnabled(boolean compassEnabled) {
   mapboxMap.getUiSettings().setCompassEnabled(compassEnabled);
 }

 @Override
 public void setTrackCameraPosition(boolean trackCameraPosition) {
   this.trackCameraPosition = trackCameraPosition;
 }

 @Override
 public void setScrollGesturesEnabled(boolean scrollGesturesEnabled) {
   mapboxMap.getUiSettings().setScrollGesturesEnabled(scrollGesturesEnabled);
 }

 @Override
 public void setMinMaxZoomPreference(Float min, Float max) {

 }

 @Override
 public void setZoomGesturesEnabled(boolean zoomGesturesEnabled) {
   mapboxMap.getUiSettings().setZoomGesturesEnabled(zoomGesturesEnabled);
 }

 @Override
 public void setMyLocationEnabled(boolean myLocationEnabled) {
   if (this.myLocationEnabled == myLocationEnabled) {
     return;
   }
   this.myLocationEnabled = myLocationEnabled;
   if (mapboxMap != null) {
     updateMyLocationSettings();
   }
 }

 @Override
 public void setMyLocationButtonEnabled(boolean myLocationButtonEnabled) {
   if (this.myLocationButtonEnabled == myLocationButtonEnabled) {
     return;
   }
   this.myLocationButtonEnabled = myLocationButtonEnabled;
   if (mapboxMap != null) {
     updateMyLocationSettings();
   }
 }

 @Override
 public void setZoomControlsEnabled(boolean zoomControlsEnabled) {
   if (this.zoomControlsEnabled == zoomControlsEnabled) {
     return;
   }
   this.zoomControlsEnabled = zoomControlsEnabled;
   if (mapboxMap != null) {
    //  mapboxMap.getUiSettings().setZoomControlsEnabled(zoomControlsEnabled);
   }
 }

 @Override
 public void setInitialMarkers(Object initialMarkers) {
   this.initialMarkers = (List<Object>) initialMarkers;
   if (mapboxMap != null) {
    addInitialMarkers();
   }
 }

 private void addInitialMarkers() {
  List<String> newSymbolIds = new ArrayList<String>();
  List<SymbolOptions> symbolOptionsList = new ArrayList<SymbolOptions>();
  if (initialMarkers != null) {
    SymbolBuilder symbolBuilder;
    for (Object o : initialMarkers) {
      symbolBuilder = new SymbolBuilder();
      Convert.interpretSymbolOptions(o, symbolBuilder);
      symbolOptionsList.add(symbolBuilder.getSymbolOptions());
    }
    if (!symbolOptionsList.isEmpty()) {
      if (!symbolOptionsList.isEmpty()) {
        List<Symbol> newSymbols = symbolManager.create(symbolOptionsList);
        String symbolId;
        for (Symbol symbol : newSymbols) {
          symbolId = String.valueOf(symbol.getId());
          newSymbolIds.add(symbolId);
          symbols.put(symbolId, new SymbolController(symbol, true, this));
        }
      }
    }
  }
 }

 @Override
 public void setInitialClusters(Object initialClusters) {
  Logger.e("clusters","initialClusters" + initialClusters);
  this.initialClusters = FeatureCollection.fromJson((String) initialClusters);
 }

 private void addClusteredSource(@NonNull Style loadedMapStyle) {
    try {
      loadedMapStyle.addSource(
        new GeoJsonSource("clusters",
        initialClusters,
        new GeoJsonOptions()
          .withCluster(true)
          .withClusterMaxZoom(14)
          .withClusterRadius(50)
        )
      );
    } catch (Exception exception) {
      Logger.e("Check Clustered data %s", exception.getMessage());
    }
    
    CircleLayer circles = new CircleLayer("circle-cluster", "clusters");
    circles.setProperties(
      circleColor(Color.parseColor("#1C1478")),
      circleRadius(18f)
    );
  
    loadedMapStyle.addLayer(circles);
    
    //Add the count labels
    SymbolLayer count = new SymbolLayer("count", "clusters");
    count.setProperties(
      textField(Expression.toString(get("point_count"))),
      textSize(12f),
      textColor(Color.WHITE),
      textIgnorePlacement(true),
      textAllowOverlap(true)
    );
    loadedMapStyle.addLayer(count);

    //Creating a marker layer for single data points
    SymbolLayer unclustered = new SymbolLayer("unclustered-points", "clusters");

    unclustered.setProperties(
      textField("1"),
      textSize(12f),
      textColor(Color.WHITE),
      textIgnorePlacement(true),
      textAllowOverlap(true)
    );
    unclustered.setFilter(not(has("point_count")));
    loadedMapStyle.addLayer(unclustered);
  }

 @SuppressLint("MissingPermission")
 private void updateMyLocationSettings() {
   if (hasLocationPermission()) {
     // The plugin doesn't add the location permission by default so that apps that don't need
     // the feature won't require the permission.
     // Gradle is doing a static check for missing permission and in some configurations will
     // fail the build if the permission is missing. The following disables the Gradle lint.
     //noinspection ResourceType

    if(this.locationComponent == null && myLocationEnabled == true){
      enableLocationComponent(mapboxMap.getStyle());
    }
    Log.v("myLocationEnabled", String.valueOf(myLocationEnabled));
    locationComponent.setLocationComponentEnabled(myLocationEnabled);
   } else {
     // TODO(amirh): Make the options update fail.
     Log.e(TAG, "Cannot enable MyLocation layer as location permissions are not granted");
   }
 }

 private boolean hasLocationPermission() {
   return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
           == PackageManager.PERMISSION_GRANTED
       || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
           == PackageManager.PERMISSION_GRANTED;
 }

 private int checkSelfPermission(String permission) {
   if (permission == null) {
     throw new IllegalArgumentException("permission is null");
   }
   return context.checkPermission(
       permission, android.os.Process.myPid(), android.os.Process.myUid());
 }

 private int getActivityHashCode() {
   if (registrar != null && registrar.activity() != null) {
     return registrar.activity().hashCode();
   } else {
     return activityHashCode;
   }
 }

 private Application getApplication() {
   if (registrar != null && registrar.activity() != null) {
     return registrar.activity().getApplication();
   } else {
     return mApplication;
   }
 }

 private void destroyMapViewIfNecessary() {
   if (mapView == null) {
     return;
   }
   mapView.onDestroy();
   mapView = null;
 }

 public void setIndoorEnabled(boolean indoorEnabled) {
   this.indoorEnabled = indoorEnabled;
 }

}

interface MapboxListener
    extends MapboxMap.OnMapClickListener {}