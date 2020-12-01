package io.flutter.plugins.mapboxnative;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Receiver of Symbol configuration options.
 */
interface SymbolOptionsSink {

  void setIconSize(float iconSize);
  void setIconImage(String iconImage);
  void setGeometry(LatLng geometry);

}
