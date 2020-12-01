package io.flutter.plugins.mapboxnative;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

class SymbolBuilder implements SymbolOptionsSink {
  private final SymbolOptions symbolOptions;
  private static boolean customImage;

  SymbolBuilder() {
    this.symbolOptions = new SymbolOptions();
  }

  public SymbolOptions getSymbolOptions(){
    return this.symbolOptions;
  }

  public boolean getCustomImage() { 
    return this.customImage;
  }

  @Override
  public void setIconSize(float iconSize) {
    symbolOptions.withIconSize(iconSize);
  }

  @Override
  public void setIconImage(String iconImage) {
    symbolOptions.withIconImage(iconImage);
  }
  
  @Override
  public void setGeometry(LatLng geometry) {
    symbolOptions.withGeometry(Point.fromLngLat(geometry.getLongitude(), geometry.getLatitude()));
  }
}