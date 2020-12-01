import 'dart:async';
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_mapbox_native/flutter_mapbox_native.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  BitmapDescriptor _markerIcon;
  List _clusters;
  List<Marker> _markers;
  MapboxController _controller;

  @override
  void initState() {
    super.initState();
    _clusters = [{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[0,0]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[17.18408550000001,50.9352832]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-74.0242508,4.6780785]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-73.3783338,5.5966581]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-70.7209186,-33.4677846]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-70.5087339,-33.4834135]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[2.478581200000008,44.1902619]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[15.86524030000001,45.7283808]}}];

    _markers = [
      Marker(
        iconImage: 'assets/images/gateways.png',
        markerId: MarkerId('center-1'),
        position: LatLng(0,0)
      ),
      Marker(
        iconImage: 'assets/images/gateways.png',
        markerId: MarkerId('center-2'),
        position: LatLng(0,0.02),
      )
    ];
  }

  @override
  Widget build(BuildContext context) {
    _createMarkerImageFromAsset(context);

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: FlutterMapboxNative(
            mapStyle: 'mapbox://styles/mxcdatadash/ck9qr005y5xec1is8yu6i51kw',
            center: CenterPosition(
              target: LatLng(0,0),
              zoom: 12,
              animated: false,
            ),
            minimumZoomLevel: 1,
            maximumZoomLevel: 12,
            myLocationEnabled: false,
            myLocationButtonEnabled: true,
            onMapCreated: (controller){
            //   _controller = controller;
            //   setState(() {});
              Future.delayed(Duration(seconds: 3),(){
                _clusters.addAll([{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[0.0001,0]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[0.0002,0]}},{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[0.0003,0]}}]);

                // _markers.add( Marker(
                //   iconImage: 'assets/images/gateways.png',
                //   markerId: MarkerId('center-3'),
                //   position: LatLng(0.002,0.05)
                // ));
                setState(() {});
              });
            },
            onTap: (poistion){
              print('-----poistion----:$poistion');
            },
            // onStyleLoadedCallback: (){
            //   // addImageFromAsset('assets/images/gateways.png');
            // },
            // markers: _markers.toSet(),
            // // clusterImage: 'assets/images/gateways.png',
            clusters: _clusters,
          )
        ),
      ),
    );
  }

  Future<void> addImageFromAsset(String assetName) async {
    final ByteData bytes = await rootBundle.load(assetName);
    final Uint8List list = bytes.buffer.asUint8List();
    return _controller.addImage(assetName, list);
  }

  Future<void> _createMarkerImageFromAsset(BuildContext context) async {
    if (_markerIcon == null) {
      // final ImageConfiguration imageConfiguration =
      //     createLocalImageConfiguration(context, size: Size.square(12));
      // BitmapDescriptor.fromAssetImage(
      //         imageConfiguration, 'assets/images/gateways.png')
      //     .then(_updateBitmap);

    }
  }

  void _updateBitmap(BitmapDescriptor bitmap) {
    setState(() {
      _markerIcon = bitmap;
    });
  }
}
