part of flutter_mapbox_native;

typedef void MapCreatedCallback(MapboxController controller);
typedef void OnFullScreenTap();
typedef void OnStyleLoadedCallback();

class FlutterMapboxNative extends StatefulWidget {

  const FlutterMapboxNative({
    Key key,
    @required this.center,
    this.mapStyle,
    this.gestureRecognizers,
    this.minimumZoomLevel,
    this.maximumZoomLevel,
    this.myLocationSwitch = true,
    this.myLocationEnabled = true,
    this.myLocationButtonEnabled = true,
    this.myLocationTrackingMode = MyLocationTrackingMode.None,
    this.myLocationRenderMode = MyLocationRenderMode.COMPASS,
    this.isFullScreen = true,
    this.fullScreenEnabled = true,
    this.onFullScreenTap,
    this.onTap,
    this.onMapCreated,
    this.onStyleLoadedCallback,
    this.markers,
    // this.clusterImage,
    this.clusters
  }) : super(key: key);

  /// Style of map to be rendered
  final String mapStyle;

  /// Center of map
  /// target constains a pair of latitude and longitude coordinates;
  /// zoom: double;
  /// animated: bool;
  final CenterPosition center;

  final double minimumZoomLevel;
  final double maximumZoomLevel;

  final bool myLocationSwitch;
  final bool myLocationEnabled;
  /// whether myLocationButton is showed or not
  /// if true the button will be showed,or it will be hided
  final bool myLocationButtonEnabled;

  /// The mode used to let the map's camera follow the device's physical location. 
  /// `myLocationEnabled` needs to be true for values other than `MyLocationTrackingMode.None` to work.
  final MyLocationTrackingMode myLocationTrackingMode;

  /// The mode to render the user location symbol
  final MyLocationRenderMode myLocationRenderMode;

  final bool isFullScreen;
  final bool fullScreenEnabled;

  final ArgumentCallback<LatLng> onTap;

  final Set<Marker> markers;

  // final String clusterImage;
  final List clusters;

  final Set<Factory<OneSequenceGestureRecognizer>> gestureRecognizers;

  final MapCreatedCallback onMapCreated;

  final OnFullScreenTap onFullScreenTap;
  final OnStyleLoadedCallback onStyleLoadedCallback;

  @override
  State createState() => _MapboxNativeState();
}

class _MapboxNativeState extends State<FlutterMapboxNative> {
  _MapboxOptions _mapboxOptions;
  MediaQueryData _mediaData;
  bool _myLocationSwitch = true;
  bool _isFullScreen= false;

  Map<MarkerId, Marker> _markers = <MarkerId, Marker>{};
  
  final Completer<MapboxController> _controller =
    Completer<MapboxController>();

  @override
  Widget build(BuildContext context) {
    final Map<String, dynamic> creationParams = <String, dynamic>{
      // 'clusterImage': widget.clusterImage,
      'clusters': jsonEncode({
        "type": "FeatureCollection",
        "features": widget.clusters
      }),
      'mapStyle': widget.mapStyle,
      'center': widget.center?.toMap(),
      'markersToAdd': serializeMarkerSet(widget.markers),
      'options': _mapboxOptions.toMap(),
    };

    return Stack(
      children: <Widget>[
        _mapboxNativePlatform.buildView(
          creationParams,
          widget.gestureRecognizers,
          onPlatformViewCreated,
        ),
        _buildActionWidgets()
      ],
    );
  }

  @override
  void initState() {
    super.initState();
    _mapboxOptions = _MapboxOptions.fromWidget(widget);
    _markers = keyByMarkerId(widget.markers);
    _myLocationSwitch = widget.myLocationSwitch;
    _isFullScreen = widget.isFullScreen;
  }

  @override
  void didUpdateWidget(FlutterMapboxNative oldWidget) {
    super.didUpdateWidget(oldWidget);
    _updateOptions();
    if(widget.markers != null){
      _updateMarkers();
    }
    if(widget.clusters != null){
      _updateClusters();
    }
  }

  @override
  void dispose() async {
    super.dispose();
    MapboxController controller = await _controller.future;
    controller.dispose();
  }

  void _updateOptions() async {
    final _MapboxOptions newOptions = _MapboxOptions.fromWidget(widget);
    final Map<String, dynamic> updates =
        _mapboxOptions.updatesMap(newOptions);
    if (updates.isEmpty) {
      return;
    }
    final MapboxController controller = await _controller.future;
    // ignore: unawaited_futures
    controller._updateMapOptions(updates);
    _mapboxOptions = newOptions;
  }

  void _updateMarkers() async {
    final MapboxController controller = await _controller.future;
    // ignore: unawaited_futures
    controller._updateMarkers(
        MarkerUpdates.from(_markers.values.toSet(), widget.markers));
    _markers = keyByMarkerId(widget.markers);
  }

  void _updateClusters() async {
    final MapboxController controller = await _controller.future;
    controller._updateClusters(widget.clusters);
  }

  Future<void> onPlatformViewCreated(int id) async {
    final MapboxController controller = await MapboxController.init(
      id,
      widget.center,
      this,
    );
    _controller.complete(controller);
    if (widget.onMapCreated != null) {
      widget.onMapCreated(controller);
    }
  }

  Widget _buildActionWidgets() {
    return Positioned(
      bottom: 80,
      right: 20,
      child: Column(
        children: <Widget>[
          _buildMyLocationIcon(),
          _buildMyLocationStateChange(),
          _buildFullScreenStateChange()
        ],
      ),
    );
  }

  Widget _buildMyLocationIcon() {
    return _iconButton(
      visible: widget.myLocationEnabled && widget.myLocationButtonEnabled,
      icon: Icons.my_location,
      onPressed: () async{
        final MapboxController controller = await _controller.future;
        controller._moveCameraToMyLocation();
      }
    );
  }

  Widget _buildMyLocationStateChange() {
    return _iconButton(
      visible: widget.myLocationEnabled,
      icon: _myLocationSwitch ? Icons.location_off : Icons.location_on,
      onPressed: () async{
        _myLocationSwitch = !_myLocationSwitch;
        setState(() {});

        final MapboxController controller = await _controller.future;
        controller.isMyLocationVisible(_myLocationSwitch);
      }
    );
  }

  Widget _buildFullScreenStateChange() {
    return _iconButton(
      visible: widget.fullScreenEnabled,
      icon: widget.fullScreenEnabled && _isFullScreen ? Icons.zoom_out_map : Icons.close,
      onPressed: () => setState(() {
        if(widget.onFullScreenTap != null){
          widget.onFullScreenTap();
        }
      })
    );
  }

  Widget _iconButton({IconData icon,Function onPressed,bool visible = true}){
    return Visibility(
      visible: visible,
      child: Container(
        margin: EdgeInsets.only(bottom: 10),
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: Colors.blueAccent,
          borderRadius: BorderRadius.circular(20.0),
          boxShadow: [BoxShadow(color: Colors.grey, blurRadius: 10.0)],
        ),
        child: IconButton(
          onPressed: onPressed,
          icon: Icon(
            icon,
            color: Colors.white,
          ),
        ),
      )
    );
  }

  void onTap(LatLng position) {
    assert(position != null);
    if (widget.onTap != null) {
      widget.onTap(position);
    }
  }

  void onStyleLoadedCallback(){
    if(widget.onStyleLoadedCallback != null){
      widget.onStyleLoadedCallback();
    }
  }
}

/// Configuration options for the Mapbox user interface.
class _MapboxOptions {
  _MapboxOptions({
    this.minimumZoomLevel,
    this.maximumZoomLevel,
    this.myLocationSwitch,
    this.myLocationEnabled,
    this.myLocationButtonEnabled,
    this.myLocationTrackingMode,
    this.myLocationRenderMode,
  });

  static _MapboxOptions fromWidget(FlutterMapboxNative map) {
    return _MapboxOptions(
      minimumZoomLevel: map.minimumZoomLevel,
      maximumZoomLevel: map.maximumZoomLevel,
      myLocationSwitch: map.myLocationSwitch,
      myLocationEnabled: map.myLocationEnabled,
      myLocationButtonEnabled: map.myLocationButtonEnabled,
      myLocationTrackingMode: map.myLocationTrackingMode,
      myLocationRenderMode: map.myLocationRenderMode,
    );
  }

  final double minimumZoomLevel;
  final double maximumZoomLevel;

  final bool myLocationSwitch;
  final bool myLocationEnabled;
  final bool myLocationButtonEnabled;

  final MyLocationTrackingMode myLocationTrackingMode;
  final MyLocationRenderMode myLocationRenderMode;

  Map<String, dynamic> toMap() {
    final Map<String, dynamic> optionsMap = <String, dynamic>{};

    void addIfNonNull(String fieldName, dynamic value) {
      if (value != null) {
        optionsMap[fieldName] = value;
      }
    }

    addIfNonNull('minimumZoomLevel', minimumZoomLevel);
    addIfNonNull('maximumZoomLevel', maximumZoomLevel);
    addIfNonNull('myLocationSwitch', myLocationSwitch);
    addIfNonNull('myLocationEnabled', myLocationEnabled);
    addIfNonNull('myLocationButtonEnabled', myLocationButtonEnabled);
    addIfNonNull('myLocationTrackingMode', myLocationTrackingMode?.index);
    addIfNonNull('myLocationRenderMode', myLocationRenderMode?.index);
    return optionsMap;
  }

  Map<String, dynamic> updatesMap(_MapboxOptions newOptions) {
    final Map<String, dynamic> prevOptionsMap = toMap();

    return newOptions.toMap()
      ..removeWhere(
          (String key, dynamic value) => prevOptionsMap[key] == value);
  }
}