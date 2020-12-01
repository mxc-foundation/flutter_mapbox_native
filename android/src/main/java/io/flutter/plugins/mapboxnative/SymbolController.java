package io.flutter.plugins.mapboxnative;

import android.graphics.Color;
import android.graphics.PointF;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

/**
 * Controller of a single Symbol on the map.
 */
class SymbolController implements SymbolOptionsSink {
  private final Symbol symbol;
  private final OnSymbolTappedListener onTappedListener;
  private boolean consumeTapEvents;

  SymbolController(Symbol symbol, boolean consumeTapEvents, OnSymbolTappedListener onTappedListener) {
    this.symbol = symbol;
    this.consumeTapEvents = consumeTapEvents;
    this.onTappedListener = onTappedListener;
  }

  boolean onTap() {
    if (onTappedListener != null) {
      onTappedListener.onSymbolTapped(symbol);
    }
    return consumeTapEvents;
  }

  public Symbol getSymbol(){
    return this.symbol;
  }

  public LatLng getGeometry() {
    Point point =  symbol.getGeometry();
    return new LatLng(point.latitude(), point.longitude());
  }
  
  void remove(SymbolManager symbolManager) {
    symbolManager.delete(symbol);
  }

  @Override
  public void setIconSize(float iconSize) {
    symbol.setIconSize(iconSize);
  }

  @Override
  public void setIconImage(String iconImage) {
    symbol.setIconImage(iconImage);
  }

  @Override
  public void setGeometry(LatLng geometry) {
    symbol.setGeometry(Point.fromLngLat(geometry.getLongitude(), geometry.getLatitude()));
  }
}
