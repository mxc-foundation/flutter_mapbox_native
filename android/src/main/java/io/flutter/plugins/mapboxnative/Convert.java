package io.flutter.plugins.mapboxnative;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraPosition.Builder;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import io.flutter.view.FlutterMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Conversions between JSON-like values and GoogleMaps data types. */
class Convert {
  static Object toJson(CameraPosition position) {
    if (position == null) {
      return null;
    }
    final Map<String, Object> data = new HashMap<>();
    data.put("bearing", position.bearing);
    data.put("target", toJson(position.target));
    data.put("tilt", position.tilt);
    data.put("zoom", position.zoom);
    return data;
  }

  private static Object toJson(LatLng latLng) {
    return Arrays.asList(latLng.getLatitude(), latLng.getLongitude());
  }

  // private static BitmapDescriptor toBitmapDescriptor(Object o) {
  //   final List<?> data = toList(o);
  //   switch (toString(data.get(0))) {
  //     case "defaultMarker":
  //       if (data.size() == 1) {
  //         return BitmapDescriptorFactory.defaultMarker();
  //       } else {
  //         return BitmapDescriptorFactory.defaultMarker(toFloat(data.get(1)));
  //       }
  //     case "fromAsset":
  //       if (data.size() == 2) {
  //         return BitmapDescriptorFactory.fromAsset(
  //             FlutterMain.getLookupKeyForAsset(toString(data.get(1))));
  //       } else {
  //         return BitmapDescriptorFactory.fromAsset(
  //             FlutterMain.getLookupKeyForAsset(toString(data.get(1)), toString(data.get(2))));
  //       }
  //     case "fromAssetImage":
  //       if (data.size() == 3) {
  //         return BitmapDescriptorFactory.fromAsset(
  //             FlutterMain.getLookupKeyForAsset(toString(data.get(1))));
  //       } else {
  //         throw new IllegalArgumentException(
  //             "'fromAssetImage' Expected exactly 3 arguments, got: " + data.size());
  //       }
  //     case "fromBytes":
  //       return getBitmapFromBytes(data);
  //     default:
  //       throw new IllegalArgumentException("Cannot interpret " + o + " as BitmapDescriptor");
  //   }
  // }

  // private static BitmapDescriptor getBitmapFromBytes(List<?> data) {
  //   if (data.size() == 2) {
  //     try {
  //       Bitmap bitmap = toBitmap(data.get(1));
  //       return BitmapDescriptorFactory.fromBitmap(bitmap);
  //     } catch (Exception e) {
  //       throw new IllegalArgumentException("Unable to interpret bytes as a valid image.", e);
  //     }
  //   } else {
  //     throw new IllegalArgumentException(
  //         "fromBytes should have exactly one argument, the bytes. Got: " + data.size());
  //   }
  // }

  static boolean toBoolean(Object o) {
    return (Boolean) o;
  }

  static CameraPosition toCameraPosition(Object o) {
    final Map<?, ?> data = toMap(o);
    final CameraPosition.Builder builder = new CameraPosition.Builder();

    if(data.containsKey("bearing")){
      builder.bearing(toFloat(data.get("bearing")));
    }
    
    if(data.containsKey("target")){
      builder.target(toLatLng(data.get("target")));
    }

    if(data.containsKey("tilt")){
      builder.tilt(toFloat(data.get("tilt")));
    }

    if(data.containsKey("zoom")){
      builder.zoom(toFloat(data.get("zoom")));
    }
    
    return builder.build();
  }

  static CameraUpdate toCameraUpdate(Object o, float density) {
    final List<?> data = toList(o);
    switch (toString(data.get(0))) {
      case "newCameraPosition":
        return CameraUpdateFactory.newCameraPosition(toCameraPosition(data.get(1)));
      case "newLatLng":
        return CameraUpdateFactory.newLatLng(toLatLng(data.get(1)));
      // case "newLatLngBounds":
      //   return CameraUpdateFactory.newLatLngBounds(
      //       toLatLngBounds(data.get(1)), toPixels(data.get(2), density));
      case "newLatLngZoom":
        return CameraUpdateFactory.newLatLngZoom(toLatLng(data.get(1)), toFloat(data.get(2)));
      // case "scrollBy":
      //   return CameraUpdateFactory.scrollBy( //
      //       toFractionalPixels(data.get(1), density), //
      //       toFractionalPixels(data.get(2), density));
      // case "zoomBy":
      //   if (data.size() == 2) {
      //     return CameraUpdateFactory.zoomBy(toFloat(data.get(1)));
      //   } else {
      //     return CameraUpdateFactory.zoomBy(toFloat(data.get(1)), toPoint(data.get(2), density));
      //   }
      case "zoomIn":
        return CameraUpdateFactory.zoomIn();
      case "zoomOut":
        return CameraUpdateFactory.zoomOut();
      case "zoomTo":
        return CameraUpdateFactory.zoomTo(toFloat(data.get(1)));
      default:
        throw new IllegalArgumentException("Cannot interpret " + o + " as CameraUpdate");
    }
  }

  private static double toDouble(Object o) {
    return ((Number) o).doubleValue();
  }

  private static float toFloat(Object o) {
    return ((Number) o).floatValue();
  }

  private static Float toFloatWrapper(Object o) {
    return (o == null) ? null : toFloat(o);
  }

  private static int toInt(Object o) {
    return ((Number) o).intValue();
  }

  static Object cameraPositionToJson(CameraPosition position) {
    if (position == null) {
      return null;
    }
    final Map<String, Object> data = new HashMap<>();
    data.put("bearing", position.bearing);
    data.put("target", latLngToJson(position.target));
    data.put("tilt", position.tilt);
    data.put("zoom", position.zoom);
    return data;
  }

  static Object latlngBoundsToJson(LatLngBounds latLngBounds) {
    final Map<String, Object> arguments = new HashMap<>(2);
    // arguments.put("southwest", latLngToJson(latLngBounds.getSouthWest()));
    // arguments.put("northeast", latLngToJson(latLngBounds.getNortheast()));
    return arguments;
  }

  static Object markerIdToJson(String markerId) {
    if (markerId == null) {
      return null;
    }
    final Map<String, Object> data = new HashMap<>(1);
    data.put("markerId", markerId);
    return data;
  }

  // static Object polygonIdToJson(String polygonId) {
  //   if (polygonId == null) {
  //     return null;
  //   }
  //   final Map<String, Object> data = new HashMap<>(1);
  //   data.put("polygonId", polygonId);
  //   return data;
  // }

  // static Object polylineIdToJson(String polylineId) {
  //   if (polylineId == null) {
  //     return null;
  //   }
  //   final Map<String, Object> data = new HashMap<>(1);
  //   data.put("polylineId", polylineId);
  //   return data;
  // }

  // static Object circleIdToJson(String circleId) {
  //   if (circleId == null) {
  //     return null;
  //   }
  //   final Map<String, Object> data = new HashMap<>(1);
  //   data.put("circleId", circleId);
  //   return data;
  // }

  static Object latLngToJson(LatLng latLng) {
    return Arrays.asList(latLng.getLatitude(), latLng.getLongitude());
  }

  static LatLng toLatLng(Object o) {
    final List<?> data = toList(o);
    return new LatLng(toDouble(data.get(0)), toDouble(data.get(1)));
  }

  static Point toPoint(Object o) {
    Map<String, Integer> screenCoordinate = (Map<String, Integer>) o;
    return new Point(screenCoordinate.get("x"), screenCoordinate.get("y"));
  }

  static Map<String, Integer> pointToJson(Point point) {
    final Map<String, Integer> data = new HashMap<>(2);
    data.put("x", point.x);
    data.put("y", point.y);
    return data;
  }

  private static LatLngBounds toLatLngBounds(Object o) {
    if (o == null) {
      return null;
    }
    final List<?> data = toList(o);
    return new LatLngBounds.Builder().include(toLatLng(data.get(0))).include(toLatLng(data.get(1))).build();
  }

  private static List<?> toList(Object o) {
    return (List<?>) o;
  }

  private static Map<?, ?> toMap(Object o) {
    return (Map<?, ?>) o;
  }

  private static float toFractionalPixels(Object o, float density) {
    return toFloat(o) * density;
  }

  // private static int toPixels(Object o, float density) {
  //   return (int) toFractionalPixels(o, density);
  // }

  private static Bitmap toBitmap(Object o) {
    byte[] bmpData = (byte[]) o;
    Bitmap bitmap = BitmapFactory.decodeByteArray(bmpData, 0, bmpData.length);
    if (bitmap == null) {
      throw new IllegalArgumentException("Unable to decode bytes as a valid bitmap.");
    } else {
      return bitmap;
    }
  }
  static String toString(Object o) {
    return (String) o;
  }

  static void interpretMapboxOptions(Object o, MapboxOptionsSink sink) {
    final Map<?, ?> data = toMap(o);
    final Object minimumZoomLevel = data.get("minimumZoomLevel");
    if (minimumZoomLevel != null) {
      sink.setMinimumZoomLevel(toDouble(minimumZoomLevel));
    }

    final Object maximumZoomLevel = data.get("maximumZoomLevel");
    if (maximumZoomLevel != null) {
      sink.setMaximumZoomLevel(toDouble(maximumZoomLevel));
    }

    final Object myLocationEnabled = data.get("myLocationEnabled");
    if (myLocationEnabled != null) {
      sink.setMyLocationEnabled(toBoolean(myLocationEnabled));
    }

    final Object myLocationTrackingMode = data.get("myLocationTrackingMode");
    if (myLocationTrackingMode != null) {
      sink.setMyLocationTrackingMode(toInt(myLocationTrackingMode));
    }
    
    final Object myLocationRenderMode = data.get("myLocationRenderMode");
    if (myLocationRenderMode != null) {
      sink.setMyLocationRenderMode(toInt(myLocationRenderMode));
    }

    final Object compassEnabled = data.get("compassEnabled");
    if (compassEnabled != null) {
      sink.setCompassEnabled(toBoolean(compassEnabled));
    }
    
    final Object minMaxZoomPreference = data.get("minMaxZoomPreference");
    if (minMaxZoomPreference != null) {
      final List<?> zoomPreferenceData = toList(minMaxZoomPreference);
      sink.setMinMaxZoomPreference( //
          toFloatWrapper(zoomPreferenceData.get(0)), //
          toFloatWrapper(zoomPreferenceData.get(1)));
    }

    final Object scrollGesturesEnabled = data.get("scrollGesturesEnabled");
    if (scrollGesturesEnabled != null) {
      sink.setScrollGesturesEnabled(toBoolean(scrollGesturesEnabled));
    }
    final Object trackCameraPosition = data.get("trackCameraPosition");
    if (trackCameraPosition != null) {
      sink.setTrackCameraPosition(toBoolean(trackCameraPosition));
    }
    final Object zoomGesturesEnabled = data.get("zoomGesturesEnabled");
    if (zoomGesturesEnabled != null) {
      sink.setZoomGesturesEnabled(toBoolean(zoomGesturesEnabled));
    }
    final Object zoomControlsEnabled = data.get("zoomControlsEnabled");
    if (zoomControlsEnabled != null) {
      sink.setZoomControlsEnabled(toBoolean(zoomControlsEnabled));
    }
  }

  /** Returns the dartMarkerId of the interpreted marker. */
  static String interpretSymbolOptions(Object o, SymbolOptionsSink sink) {
    final Map<?, ?> data = toMap(o);

    final Object iconSize = data.get("iconSize");
    if (iconSize != null) {
      sink.setIconSize(toFloat(iconSize));
    }else{
      sink.setIconSize(2.0f);
    }
    final Object iconImage = data.get("iconImage");
    if (iconImage != null) {
      sink.setIconImage(toString(iconImage));
    }else{
      sink.setIconImage("marker");
    }

    final Object geometry = data.get("position");
    if (geometry != null) {
      sink.setGeometry(toLatLng(geometry));
    }

    final String markerId = (String) data.get("markerId");
    if (markerId == null) {
      throw new IllegalArgumentException("markerId was null");
    } else {
      return markerId;
    }
  }

  // private static void interpretInfoWindowOptions(
  //     MarkerOptionsSink sink, Map<String, Object> infoWindow) {
  //   String title = (String) infoWindow.get("title");
  //   String snippet = (String) infoWindow.get("snippet");
  //   // snippet is nullable.
  //   if (title != null) {
  //     sink.setInfoWindowText(title, snippet);
  //   }
  //   Object infoWindowAnchor = infoWindow.get("anchor");
  //   if (infoWindowAnchor != null) {
  //     final List<?> anchorData = toList(infoWindowAnchor);
  //     sink.setInfoWindowAnchor(toFloat(anchorData.get(0)), toFloat(anchorData.get(1)));
  //   }
  // }

  private static List<LatLng> toPoints(Object o) {
    final List<?> data = toList(o);
    final List<LatLng> points = new ArrayList<>(data.size());

    for (Object ob : data) {
      final List<?> point = toList(ob);
      points.add(new LatLng(toFloat(point.get(0)), toFloat(point.get(1))));
    }
    return points;
  }

  // private static List<PatternItem> toPattern(Object o) {
  //   final List<?> data = toList(o);

  //   if (data.isEmpty()) {
  //     return null;
  //   }

  //   final List<PatternItem> pattern = new ArrayList<>(data.size());

  //   for (Object ob : data) {
  //     final List<?> patternItem = toList(ob);
  //     switch (toString(patternItem.get(0))) {
  //       case "dot":
  //         pattern.add(new Dot());
  //         break;
  //       case "dash":
  //         pattern.add(new Dash(toFloat(patternItem.get(1))));
  //         break;
  //       case "gap":
  //         pattern.add(new Gap(toFloat(patternItem.get(1))));
  //         break;
  //       default:
  //         throw new IllegalArgumentException("Cannot interpret " + pattern + " as PatternItem");
  //     }
  //   }

  //   return pattern;
  // }

//   private static Cap toCap(Object o) {
//     final List<?> data = toList(o);
//     switch (toString(data.get(0))) {
//       case "buttCap":
//         return new ButtCap();
//       case "roundCap":
//         return new RoundCap();
//       case "squareCap":
//         return new SquareCap();
//       case "customCap":
//         if (data.size() == 2) {
//           return new CustomCap(toBitmapDescriptor(data.get(1)));
//         } else {
//           return new CustomCap(toBitmapDescriptor(data.get(1)), toFloat(data.get(2)));
//         }
//       default:
//         throw new IllegalArgumentException("Cannot interpret " + o + " as Cap");
//     }
//   }
}
