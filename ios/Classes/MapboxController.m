#import <Foundation/Foundation.h>

#import "MapboxController.h"
#import "JsonConversions.h"

#pragma mark - Conversion of JSON-like values sent via platform channels. Forward declarations.

static UIColor* __nullable UIColorFromHexValue(NSUInteger hexValue);
static float ToFloat(NSNumber* data);
static BOOL ToBool(NSNumber* data);
static int ToInt(NSNumber* data);
static CLLocationCoordinate2D ToLocation(NSArray* data);
static NSArray* LocationToJson(CLLocationCoordinate2D position);
static void InterpretMapOptions(NSDictionary* data, id<MapboxOptionsSink> sink);

@implementation MapboxController {
    int64_t _viewId;
    FlutterMethodChannel *_channel;
    MGLMapView *_mapView;
    MGLStyle *_style;
    MGLShapeSource *_source;
    MGLCircleStyleLayer *_targetLayer;
    MGLSymbolStyleLayer *_targetNumbertLayer;
    MGLCircleStyleLayer *_circlesLayer;
    MGLSymbolStyleLayer *_numbersLayer;
    NSObject<FlutterPluginRegistrar>* _registrar;
    MarkersController* _markersController;
    UIImage *_clusterIcon;
    UIView *_popup;
    id _markersToAdd;
    id _clusters;
}

- (instancetype)initWithFrame:(CGRect)frame 
               viewIdentifier:(int64_t)viewId 
                    arguments:(id)args 
                    registrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    if(self = [super init]){
        _viewId = viewId;

        NSString* channelName = [NSString stringWithFormat:@"plugins.flutter.io/mapbox_native_%lld", viewId];
        _channel = [FlutterMethodChannel methodChannelWithName:channelName binaryMessenger: registrar.messenger];

        // set style of map
        NSURL *styleURL = [NSURL URLWithString:args[@"mapStyle"]];
        _mapView = [[MGLMapView alloc] initWithFrame: self.view.bounds styleURL: styleURL];

        [self moveWithCenterUpdate:args[@"center"]];

        _mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

        // Enable heading tracking mode so that the arrow will appear.
        // _mapView.userTrackingMode = MGLUserTrackingModeFollowWithHeading;

        // Enable the permanent heading indicator, which will appear when the tracking mode is not `MGLUserTrackingModeFollowWithHeading`.
        _mapView.showsUserHeadingIndicator = YES;
        _mapView.allowsZooming = YES;

        InterpretMapOptions(args[@"options"], self);

        __weak __typeof__(self) weakSelf = self;
        [_channel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
        if (weakSelf) {
            [weakSelf onMethodCall:call result:result];
        }
        }];

        _mapView.delegate = weakSelf;
        _registrar = registrar;

        _markersToAdd = args[@"markersToAdd"];
        _clusters = args[@"clusters"];

        // Add a double tap gesture recognizer. This gesture is used for double
        // tapping on clusters and then zooming in so the cluster expands to its
        // children.
        UITapGestureRecognizer *doubleTap = [[UITapGestureRecognizer alloc] initWithTarget:weakSelf action:@selector(handleDoubleTapCluster:)];
        doubleTap.numberOfTapsRequired = 2;
        doubleTap.delegate = weakSelf;
        
        // It's important that this new double tap fails before the map view's
        // built-in gesture can be recognized. This is to prevent the map's gesture from
        // overriding this new gesture (and then not detecting a cluster that had been
        // tapped on).
        for (UIGestureRecognizer *recognizer in _mapView.gestureRecognizers) {
            if ([recognizer isKindOfClass:[UITapGestureRecognizer class]] &&
                ((UITapGestureRecognizer*)recognizer).numberOfTapsRequired == 2) {
                [recognizer requireGestureRecognizerToFail:doubleTap];
            }
        }
        [_mapView addGestureRecognizer:doubleTap];
        
        // Add a single tap gesture recognizer. This gesture requires the built-in
        // MGLMapView tap gestures (such as those for zoom and annotation selection)
        // to fail (this order differs from the double tap above).
        UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleMapTap:)];
        for (UIGestureRecognizer *recognizer in _mapView.gestureRecognizers) {
            if ([recognizer isKindOfClass:[UITapGestureRecognizer class]]) {
                [singleTap requireGestureRecognizerToFail:recognizer];
            }
        }
        [_mapView addGestureRecognizer:singleTap];


        // if(args[@"clusterImage"] != nil){
        //     NSString* iconPath = [registrar lookupKeyForAsset: args[@"clusterImage"]];
        //     NSString* path = [[NSBundle mainBundle] pathForResource:iconPath ofType:nil];
        //     _clusterIcon = [UIImage imageWithContentsOfFile:path];
        // }
    }
    
    return self;
}

- (void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([call.method isEqualToString:@"map#init"]) {
        result(nil);
    } else if ([call.method isEqualToString:@"map#update"]) {
        InterpretMapOptions(call.arguments[@"options"], self);
        result(nil);
    } else if ([call.method isEqualToString:@"camera#move"]) {
        [self moveWithCenterUpdate:call.arguments[@"cameraUpdate"]];
        result(nil);
    } else if ([call.method isEqualToString:@"camera#moveToMyLocation"]) {
        [self moveCameraToUserCenter:YES];
        result(nil);
    } else if ([call.method isEqualToString:@"myLocation#visiable"]) {
        [self setMyLocationSwitch:ToBool(call.arguments[@"visiable"])];
        result(nil);
    } else if([call.method isEqualToString:@"map#updateMyLocationTrackingMode"]){
        [self setMyLocationTrackingMode: (MGLUserTrackingMode)ToInt(call.arguments[@"mode"])];
        result(nil);
    } else if ([call.method isEqualToString:@"markers#update"]) {
        id markersToAdd = call.arguments[@"markersToAdd"];
        if ([markersToAdd isKindOfClass:[NSArray class]]) {
            [_markersController addMarkers:markersToAdd];
        }
        result(nil);
    } else if ([call.method isEqualToString:@"clusters#update"]) {
        id clustersData = call.arguments[@"clusters"];
        NSLog(@"--clustersData--:%@",clustersData);
        if ([clustersData isKindOfClass:[NSString class]]) {
            _clusters = clustersData;
            [self showClustersWithStyle:_style];
        }
        result(nil);
    } else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)moveWithCenterUpdate:(NSDictionary*)data {
    NSLog(@"showsUserLocation init : %@",data);

    [_mapView setCenterCoordinate:ToLocation(data[@"target"])
                         zoomLevel:ToFloat(data[@"zoom"])
                          animated:ToBool(data[@"animated"])];
}

- (void)mapView:(nonnull MGLMapView *)mapView
    didUpdateUserLocation:(nullable MGLUserLocation *)userLocation {

    // NSLog(@"_mapView.userLocationVisible:%d",_mapView.userLocationVisible);
    // if(_mapView.userLocationVisible){
    //     [self moveCameraToUserCenter:YES];
    // }
}

- (void)mapViewDidFinishLoadingMap:(MGLMapView *)mapView {
    if(_mapView.userTrackingMode == MGLUserTrackingModeFollow){
        [self moveCameraToUserCenter:YES];   
    }
}

- (void)moveCameraToUserCenter:(BOOL) isUserCenter{
    CLLocationCoordinate2D center;
    if(isUserCenter){
        center = _mapView.userLocation.coordinate;
    }else{
        center = _mapView.centerCoordinate;
    }
    
    // Wait for the map to load before initiating the first camera movement.
    MGLMapCamera *camera = [MGLMapCamera cameraLookingAtCenterCoordinate:center altitude:4500 pitch:0 heading:0];
    NSLog(@"center :%@",camera);
    
    // Animate the camera movement over 5 seconds.
    [_mapView setCamera:camera withDuration:5 animationTimingFunction:[CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut]];
}

-(UIView *)view{
    return _mapView;
}

- (void)mapView:(MGLMapView *)mapView didFinishLoadingStyle:(MGLStyle *)style {
    _markersController = [[MarkersController alloc] init:_channel
                                                    mapView:_mapView
                                                      style:style
                                                  registrar:_registrar];

    _style = style;

    if ([_markersToAdd isKindOfClass:[NSArray class]]) {
        [_markersController addMarkers:_markersToAdd];
    }

    if([_clusters isKindOfClass:[NSString class]]){
        [self showClustersWithStyle:style];
    }
    
}
 
- (void) showClustersWithStyle:(MGLStyle *)style{
    //first step: obtain data fo the geojson and declare clusters variable
    NSData *jsonData = [_clusters dataUsingEncoding:NSUTF8StringEncoding];
    MGLShape *shapeData = [MGLShape shapeWithData:jsonData encoding:NSUTF8StringEncoding error:nil];

    if (_source != nil) {
        [_source setShape:shapeData];
    }else{
        _source = [[MGLShapeSource alloc] initWithIdentifier:@"cluster-circle" shape:shapeData options:@{
            MGLShapeSourceOptionClustered: @(YES),
            MGLShapeSourceOptionClusterRadius: @(60)
        }];
        [style addSource:_source];
    }    
    
    // Use a template image so that we can tint it with the `iconColor` runtime styling property.
    // [style setImage:[_clusterIcon imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate] forName:@"cluster-icon"];
    
    // Show clustered features as circles. The `point_count` attribute is built into
    // clustering-enabled source features.
    if (_circlesLayer == nil) {
        _circlesLayer = [[MGLCircleStyleLayer alloc] initWithIdentifier:@"cluster-circle" source:_source];
        _circlesLayer.circleRadius = [NSExpression expressionForConstantValue:@(16)];
        _circlesLayer.circleOpacity = [NSExpression expressionForConstantValue:@0.9];
        _circlesLayer.circleColor = [NSExpression expressionForConstantValue:[UIColorFromHexValue(0x1c1478) colorWithAlphaComponent:0.9]];
        _circlesLayer.predicate = [NSPredicate predicateWithFormat:@"cluster == YES"];
        [style addLayer:_circlesLayer];
    }

    if (_targetLayer == nil) {
        _targetLayer = [[MGLCircleStyleLayer alloc] initWithIdentifier:@"cluster-target" source:_source];
        _targetLayer.circleRadius = [NSExpression expressionForConstantValue:@(16)];
        _targetLayer.circleOpacity = [NSExpression expressionForConstantValue:@0.9];
        _targetLayer.circleColor = [NSExpression expressionForConstantValue:[UIColorFromHexValue(0x1c1478) colorWithAlphaComponent:0.9]];
        _targetLayer.predicate = [NSPredicate predicateWithFormat:@"cluster != YES"];
        [style addLayer:_targetLayer];
    }

    if(_targetNumbertLayer == nil){
        _targetNumbertLayer = [[MGLSymbolStyleLayer alloc] initWithIdentifier:@"unClusteredTargetNumbers" source:_source];
        _targetNumbertLayer.textColor = [NSExpression expressionForConstantValue:[UIColor whiteColor]];
        _targetNumbertLayer.textFontSize = [NSExpression expressionForConstantValue:@(12)];
        _targetNumbertLayer.iconAllowsOverlap = [NSExpression expressionForConstantValue:@(YES)];
        _targetNumbertLayer.text = [NSExpression expressionWithFormat:@"CAST(1, 'NSString')"];
        _targetNumbertLayer.predicate = [NSPredicate predicateWithFormat:@"cluster != YES"];
        [style addLayer:_targetNumbertLayer];
    }
    
    // Label cluster circles with a layer of text indicating feature count. The value for
    // `point_count` is an integer. In order to use that value for the
    // `MGLSymbolStyleLayer.text` property, cast it as a string.
    if (_numbersLayer == nil) {
        _numbersLayer = [[MGLSymbolStyleLayer alloc] initWithIdentifier:@"clusteredTargetNumbers" source:_source];
        _numbersLayer.textColor = [NSExpression expressionForConstantValue:[UIColor whiteColor]];
        _numbersLayer.textFontSize = [NSExpression expressionForConstantValue:@(12)];
        _numbersLayer.iconAllowsOverlap = [NSExpression expressionForConstantValue:@(YES)];
        _numbersLayer.text = [NSExpression expressionWithFormat:@"CAST(point_count, 'NSString')"];
        _numbersLayer.predicate = [NSPredicate predicateWithFormat:@"cluster == YES"];
        [style addLayer:_numbersLayer];
    }
}

- (MGLPointFeatureCluster *)firstClusterWithGestureRecognizer:(UIGestureRecognizer *)gestureRecognizer {
    CGPoint point = [gestureRecognizer locationInView:gestureRecognizer.view];
    CGFloat width = 16;//_clusterIcon.size.width;
    CGRect rect = CGRectMake(point.x - width / 2, point.y - width / 2, width, width);
    
    // This example shows how to check if a feature is a cluster by
    // checking for that the feature is a `MGLPointFeatureCluster`. Alternatively, you could
    // also check for conformance with `MGLCluster` instead.
    NSArray<id<MGLFeature>> *features = [_mapView visibleFeaturesInRect:rect inStyleLayersWithIdentifiers:[NSSet setWithObjects:@"cluster-circle", @"cluster-target", nil]];
    
    NSPredicate *clusterPredicate = [NSPredicate predicateWithBlock:^BOOL(id  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
    return [evaluatedObject isKindOfClass:[MGLPointFeatureCluster class]];
    }];
    
    NSArray *clusters = [features filteredArrayUsingPredicate:clusterPredicate];
    
    // Pick the first cluster, ideally selecting the one nearest nearest one to
    // the touch point.
    return (MGLPointFeatureCluster *)clusters.firstObject;
}

- (IBAction)handleDoubleTapCluster:(UITapGestureRecognizer *)sender {
    
    if (![_source isKindOfClass:[MGLShapeSource class]]) {
        return;
    }
    
    if (sender.state != UIGestureRecognizerStateEnded) {
        return;
    }
    
    MGLPointFeatureCluster *cluster = [self firstClusterWithGestureRecognizer:sender];
    
    if (!cluster) {
        return;
    }
    
    double zoom = [(MGLShapeSource *)_source zoomLevelForExpandingCluster:cluster];
 
    if(zoom > 0){
        [_mapView setCenterCoordinate:cluster.coordinate
                        zoomLevel:zoom
                         animated:YES];
    }
}

- (void)callbackMapTap:(CLLocationCoordinate2D) coordinate{
    [_channel invokeMethod:@"map#onTap" arguments:@{@"position" : LocationToJson(coordinate)}];
}

- (IBAction)handleMapTap:(UITapGestureRecognizer *)tap {

    CGPoint tapPoint = [tap locationInView:_mapView];
    CLLocationCoordinate2D tapCoordinate = [_mapView convertPoint:tapPoint toCoordinateFromView:nil];
    [self callbackMapTap:tapCoordinate];

}

#pragma mark - UIGestureRecognizerDelegate
 
- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer {
    // This will only get called for the custom double tap gesture,
    // that should always be recognized simultaneously.
    return YES;
}
 
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
   // This will only get called for the custom double tap gesture.
   return [self firstClusterWithGestureRecognizer:gestureRecognizer] != nil;
}

#pragma mark - MapboxOptionsSink methods

- (void)setMinimumZoomLevel:(float)level{
  _mapView.minimumZoomLevel = level;
}

- (void)setMaximumZoomLevel:(float)level{
  _mapView.maximumZoomLevel = level;
}

- (void)setMyLocationSwitch:(BOOL)visiable{
    if(_mapView.isUserLocationVisible != visiable){
        _mapView.showsUserLocation = visiable;
    }
}

- (void)setMyLocationEnabled:(BOOL)enabled{
    _mapView.showsUserLocation = enabled;
}

- (void)setMyLocationTrackingMode:(MGLUserTrackingMode) mode {
    _mapView.userTrackingMode = mode;
}

- (void)setMyLocationRenderMode: (MyLocationRenderMode) mode {

    switch(mode) {
        case MyLocationRenderNormal:
            _mapView.showsUserHeadingIndicator = NO;
            break;
        case MyLocationRenderCompass:
            _mapView.showsUserHeadingIndicator = YES;
            break;
        default:
            break;
    }
}

@end

#pragma mark - Implementations of JSON conversion functions.

static UIColor * __nullable UIColorFromHexValue(NSUInteger hexValue) {
    
    CGFloat red = (hexValue & 0xFF0000) >> 16;
    CGFloat green = (hexValue & 0x00FF00) >> 8;
    CGFloat blue = hexValue & 0x0000FF;
 
    return [UIColor colorWithRed:red/255.0 green:green/255.0 blue:blue/255.0 alpha:1.0];
}

static float ToFloat(NSNumber* data) { return [MapboxJsonConversions toFloat:data]; }
static BOOL ToBool(NSNumber* data) { return [MapboxJsonConversions toBool:data]; }
static int ToInt(NSNumber* data) { return [MapboxJsonConversions toInt:data]; }

static CLLocationCoordinate2D ToLocation(NSArray* data) {
  return [MapboxJsonConversions toLocation:data];
}

static NSArray* LocationToJson(CLLocationCoordinate2D position) {
  return @[ @(position.latitude), @(position.longitude) ];
}

static void InterpretMapOptions(NSDictionary* data, id<MapboxOptionsSink> sink) {
    NSNumber* minimumZoomLevel = data[@"minimumZoomLevel"];
    if (minimumZoomLevel != nil) {
        [sink setMinimumZoomLevel:ToFloat(minimumZoomLevel)];
    }
    NSNumber* maximumZoomLevel = data[@"maximumZoomLevel"];
    if (maximumZoomLevel != nil) {
        [sink setMaximumZoomLevel:ToFloat(maximumZoomLevel)];
    }
    NSNumber* myLocationEnabled = data[@"myLocationEnabled"];
    if (myLocationEnabled != nil) {
        [sink setMyLocationEnabled:ToBool(myLocationEnabled)];
    }
    NSNumber* myLocationSwitch = data[@"myLocationSwitch"];
    if (myLocationSwitch != nil) {
        [sink setMyLocationSwitch:ToBool(myLocationSwitch)];
    }
    NSNumber* myLocationTrackingMode = data[@"myLocationTrackingMode"];
    if (myLocationTrackingMode != nil) {
        [sink setMyLocationTrackingMode: (MGLUserTrackingMode)ToInt(myLocationTrackingMode)];
    }
    NSNumber* myLocationRenderMode = data[@"myLocationRenderMode"];
    if (myLocationRenderMode != nil) {
        [sink setMyLocationRenderMode: (MyLocationRenderMode)ToInt(myLocationRenderMode)];
    }

}
