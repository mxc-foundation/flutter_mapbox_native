part of flutter_mapbox_native;

final MapboxNativePlatform _mapboxNativePlatform =
    MapboxNativePlatform.instance;

class MapboxController {
  /// The mapId for this controller
  final int mapId;

  MapboxController._(
    CenterPosition center,
    this._mapboxNativeState, {
    @required this.mapId,
  }) : assert(_mapboxNativePlatform != null) {
    _connectStreams(mapId);
  }

  final _MapboxNativeState _mapboxNativeState;

  static Future<MapboxController> init(
    int id,
    CenterPosition center,
    _MapboxNativeState mapboxNativeState,
  ) async {
    assert(id != null);
    await _mapboxNativePlatform.init(id);
    return MapboxController._(
      center,
      mapboxNativeState,
      mapId: id,
    );
  }

  void _connectStreams(int mapId) {
     _mapboxNativePlatform
        .onTap(mapId: mapId)
        .listen((MapTapEvent e) => _mapboxNativeState.onTap(e.position));

    _mapboxNativePlatform
        .onMapStyleLoaded(mapId: mapId)
        .listen((MapStyleLoadedEvent e) => _mapboxNativeState.onStyleLoadedCallback());
  }

  Future<void> _moveCameraToMyLocation() {
    return _mapboxNativePlatform.moveCameraToMyLocation(mapId: mapId);
  }

  Future<void> isMyLocationVisible(bool visible) {
    return _mapboxNativePlatform.isMyLocationVisible(visible,mapId: mapId);
  }

  /// Updates user location tracking mode.
  ///
  /// The returned [Future] completes after the change has been made on the
  /// platform side.
  Future<void> updateMyLocationTrackingMode(
      MyLocationTrackingMode myLocationTrackingMode) async {
    return _mapboxNativePlatform.updateMyLocationTrackingMode(myLocationTrackingMode,mapId: mapId);
  }

  Future<void> _updateMapOptions(Map<String, dynamic> optionsUpdate) {
    assert(optionsUpdate != null);
    return _mapboxNativePlatform.updateMapOptions(optionsUpdate,
        mapId: mapId);
  }

  Future<void> _updateMarkers(MarkerUpdates markerUpdates) {
    assert(markerUpdates != null);
    return _mapboxNativePlatform.updateMarkers(markerUpdates,
        mapId: mapId);
  }

  Future<void> _updateClusters(List clusters) {
    assert(clusters != null);
    return _mapboxNativePlatform.updateClusters(clusters,
        mapId: mapId);
  }

  Future<void> addImage(String name,Uint8List bytes) {
    assert(name != null && bytes != null);
    return _mapboxNativePlatform.addImage(name, bytes, mapId: mapId);
  }

  void dispose() {
    _mapboxNativePlatform.dispose(mapId: mapId);
  }
}