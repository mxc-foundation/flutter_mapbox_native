import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_mapbox_native/method_channel/method_channel_mapbox_native.dart';
import 'package:flutter_mapbox_native/type/types.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class MapboxNativePlatform extends PlatformInterface{
  /// Constructs a MapboxNativePlatform.
  MapboxNativePlatform() : super(token: _token);

  static final Object _token = Object();

  static MapboxNativePlatform _instance = MethodChannelMapboxFlutter();
  static MapboxNativePlatform get instance => _instance;

  static set instance(MapboxNativePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> init(int mapId) {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future<void> updateMapOptions(
    Map<String, dynamic> optionsUpdate, {
    @required int mapId,
  }) {
    throw UnimplementedError('updateMapOptions() has not been implemented.');
  }

  Future<void> setMapStyle(
    String mapStyle, {
    @required int mapId,
  }) {
    throw UnimplementedError('setMapStyle() has not been implemented.');
  }

  Future<void> addImage(
    String name,
    Uint8List bytes, {
    @required int mapId,
  }) {
    throw UnimplementedError('addImage() has not been implemented.');
  }

  /// Changes the map camera position.
  Future<void> moveCamera(
    CenterPosition center, {
    @required int mapId,
  }) {
    throw UnimplementedError('moveCamera() has not been implemented.');
  }

  Future<void> moveCameraToMyLocation({
    @required int mapId,
  }) {
    throw UnimplementedError('moveCameraToMyLocation() has not been implemented.');
  }

  Future<void> isMyLocationVisible(
    bool visible, {
    @required int mapId,
  }) {
    throw UnimplementedError('isMyLocationVisible() has not been implemented.');
  }

  Future<void> updateMyLocationTrackingMode(
    MyLocationTrackingMode myLocationTrackingMode,{
    @required int mapId,
  }) async {
    throw UnimplementedError(
        'updateMyLocationTrackingMode() has not been implemented.');
  }

  Future<void> updateMarkers(
    MarkerUpdates markerUpdates, {
    @required int mapId,
  }) {
    throw UnimplementedError('updateMarkers() has not been implemented.');
  }

  Future<void> updateClusters(
    List clusterUpdates, {
    @required int mapId,
  }) {
    throw UnimplementedError('updateClusters() has not been implemented.');
  }

  /// A Map has been tapped at a certain [LatLng].
  Stream<MapTapEvent> onTap({@required int mapId}) {
    throw UnimplementedError('onTap() has not been implemented.');
  }

  Stream<MapStyleLoadedEvent> onMapStyleLoaded({@required int mapId}) {
    throw UnimplementedError('onMapStyleLoaded() has not been implemented.');
  }

  /// Dispose of whatever resources the `mapId` is holding on to.
  void dispose({@required int mapId}) {
    throw UnimplementedError('dispose() has not been implemented.');
  }

  /// Returns a widget displaying the map view
  Widget buildView(
      Map<String, dynamic> creationParams,
      Set<Factory<OneSequenceGestureRecognizer>> gestureRecognizers,
      PlatformViewCreatedCallback onPlatformViewCreated) {
    throw UnimplementedError('buildView() has not been implemented.');
  }

}