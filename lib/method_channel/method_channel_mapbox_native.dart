import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'package:flutter_mapbox_native/platform_interface/mapbox_native_platform.dart';
import 'package:flutter_mapbox_native/type/types.dart';
import 'package:stream_transform/stream_transform.dart';

class MethodChannelMapboxFlutter extends MapboxNativePlatform {
  // Keep a collection of id -> channel
  // Every method call passes the int mapId
  final Map<int, MethodChannel> _channels = {};

  /// Accesses the MethodChannel associated to the passed mapId.
  MethodChannel channel(int mapId) {
    return _channels[mapId];
  }

  /// Initializes the platform interface with [id].
  ///
  /// This method is called when the plugin is first initialized.
  @override
  Future<void> init(int mapId) {
    MethodChannel channel;
    if (!_channels.containsKey(mapId)) {
      channel = MethodChannel('plugins.flutter.io/mapbox_native_$mapId');
      channel.setMethodCallHandler(
          (MethodCall call) => _handleMethodCall(call, mapId));
      _channels[mapId] = channel;
    }
    return channel.invokeMethod<void>('map#init');
  }

  @override
  Future<void> updateMapOptions(
    Map<String, dynamic> optionsUpdate, {
    @required int mapId,
  }) {
    assert(optionsUpdate != null);
    return channel(mapId).invokeMethod<void>(
      'map#update',
      <String, dynamic>{
        'options': optionsUpdate,
      },
    );
  }

  @override
  Future<void> updateMarkers(
    MarkerUpdates markerUpdates, {
    @required int mapId,
  }) {
    assert(markerUpdates != null);
    return channel(mapId).invokeMethod<void>(
      'markers#update',
      markerUpdates.toJson(),
    );
  }

  @override
  Future<void> updateClusters(
    List clusterUpdates, {
    @required int mapId,
  }) {
    assert(clusterUpdates != null);
    return channel(mapId).invokeMethod<void>(
      'clusters#update',
       <String, dynamic>{
        'clusters': jsonEncode({
          "type": "FeatureCollection",
          "features": clusterUpdates
        })
      },
    );
  }

  @override
  Future<void> setMapStyle(
    String mapStyle, {
    @required int mapId,
  }) async {
    final List<dynamic> successAndError = await channel(mapId)
        .invokeMethod<List<dynamic>>('map#setStyle', mapStyle);
    final bool success = successAndError[0];
    if (!success) {
      throw Exception(successAndError[1]);
    }
  }

  @override
  Future<void> addImage(
    String name,
    Uint8List bytes, {
    @required int mapId,
  }) {
    assert(name != null && bytes != null);
    return channel(mapId).invokeMethod<void>(
      'style#addImage',
       <String, dynamic>{
        "name": name,
        "bytes": bytes,
        "length": bytes.length,
        }
    );
  }

  @override
  Future<void> moveCamera(
    CenterPosition center, {
    @required int mapId,
  }) {
    return channel(mapId).invokeMethod<void>('camera#move', <String, dynamic>{
      'cameraUpdate': center.toMap(),
    });
  }

  @override
  Future<void> moveCameraToMyLocation({@required int mapId,
  }) {
    return channel(mapId).invokeMethod<void>('camera#moveToMyLocation');
  }

  @override
  Future<void> isMyLocationVisible(bool visible,{@required int mapId,
  }) {
    return channel(mapId).invokeMethod<void>('myLocation#visiable', <String,bool>{
      'visiable': visible,
    });
  }

  @override
  Future<void> updateMyLocationTrackingMode(
    MyLocationTrackingMode myLocationTrackingMode, {
    @required int mapId,
  }) {
    return channel(mapId).invokeMethod<void>('map#updateMyLocationTrackingMode', <String,MyLocationTrackingMode>{
      'mode': myLocationTrackingMode,
    });
  }
  
  /// Dispose of the native resources.
  @override
  void dispose({int mapId}) {
  }

  // The controller we need to broadcast the different events coming
  // from handleMethodCall.
  //
  // It is a `broadcast` because multiple controllers will connect to
  // different stream views of this Controller.
  final StreamController<MapEvent> _mapEventStreamController =
      StreamController<MapEvent>.broadcast();

  // Returns a filtered view of the events in the _controller, by mapId.
  Stream<MapEvent> _events(int mapId) =>
      _mapEventStreamController.stream.where((event) => event.mapId == mapId);

  @override
  Stream<MapTapEvent> onTap({@required int mapId}) {
    return _events(mapId).whereType<MapTapEvent>();
  }

  @override
  Stream<MapStyleLoadedEvent> onMapStyleLoaded({@required int mapId}) {
    return _events(mapId).whereType<MapStyleLoadedEvent>();
  }

  Future<dynamic> _handleMethodCall(MethodCall call, int mapId) async {
    switch (call.method) {
      case 'map#onTap':
        _mapEventStreamController.add(MapTapEvent(
          mapId,
          LatLng.fromJson(call.arguments['position']),
        ));
        break;
      case 'map#onStyleLoaded':
        _mapEventStreamController.add(MapStyleLoadedEvent(
          mapId
        ));
        break;
      default:
        throw MissingPluginException();
    }
  }

  @override
  Widget buildView(
      Map<String, dynamic> creationParams,
      Set<Factory<OneSequenceGestureRecognizer>> gestureRecognizers,
      PlatformViewCreatedCallback onPlatformViewCreated) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'plugins.flutter.io/mapbox_native',
        onPlatformViewCreated: onPlatformViewCreated,
        gestureRecognizers: gestureRecognizers,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'plugins.flutter.io/mapbox_native',
        onPlatformViewCreated: onPlatformViewCreated,
        gestureRecognizers: gestureRecognizers,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
    return Text(
        '$defaultTargetPlatform is not yet supported by the maps plugin');
  }

}