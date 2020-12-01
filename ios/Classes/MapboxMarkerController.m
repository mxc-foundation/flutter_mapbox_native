#import "MapboxMarkerController.h"
#import "JsonConversions.h"

static UIImage* ExtractIcon(NSObject<FlutterPluginRegistrar>* registrar, NSArray* icon);

@implementation MapboxMarkerController {
  MGLPointAnnotation* _marker;
  MGLMapView* _mapView;
  MGLStyle* _style;
  BOOL _consumeTapEvents;
}
- (instancetype)initMarkerWithPosition:(CLLocationCoordinate2D)position
                              markerId:(NSString*)markerId
                               mapView:(MGLMapView*)mapView
                                 style:(MGLStyle *)style {
  self = [super init];
  if (self) {
    _marker = [[MGLPointAnnotation alloc] init];

    _mapView = mapView;
    _style = style;
    _markerId = markerId;
    _consumeTapEvents = NO;
  }
  return self;
}

#pragma mark - MapboxMarkerOptionsSink methods
- (void)setPosition:(CLLocationCoordinate2D)position {
  _marker.coordinate = position;
}
- (void)setIcon:(UIImage*)icon {
  MGLShapeSource *shapeSource = [[MGLShapeSource alloc] initWithIdentifier:@"marker-source" shape:_marker options:nil];
  MGLSymbolStyleLayer *shapeLayer = [[MGLSymbolStyleLayer alloc] initWithIdentifier:@"marker-style" source:shapeSource];
  
  [_style setImage:icon forName:@"marker-icon"];
  shapeLayer.iconImageName = [NSExpression expressionForConstantValue:@"marker-icon"];

  [_style addSource:shapeSource];
  [_style addLayer:shapeLayer];
}
- (void)addAnnotation{
  [_mapView addAnnotation:_marker];
}

@end

static double ToDouble(NSNumber* data) { return [MapboxJsonConversions toDouble:data]; }

static float ToFloat(NSNumber* data) { return [MapboxJsonConversions toFloat:data]; }

static CLLocationCoordinate2D ToLocation(NSArray* data) {
  return [MapboxJsonConversions toLocation:data];
}

static int ToInt(NSNumber* data) { return [MapboxJsonConversions toInt:data]; }

static BOOL ToBool(NSNumber* data) { return [MapboxJsonConversions toBool:data]; }

// static CGPoint ToPoint(NSArray* data) { return [MapboxJsonConversions toPoint:data]; }

// static NSArray* PositionToJson(CLLocationCoordinate2D data) {
//   return [MapboxJsonConversions positionToJson:data];
// }

static UIImage* scaleImage(UIImage* image, NSNumber* scaleParam) {
  double scale = 1.0;
  if ([scaleParam isKindOfClass:[NSNumber class]]) {
    scale = scaleParam.doubleValue;
  }
  if (fabs(scale - 1) > 1e-3) {
    return [UIImage imageWithCGImage:[image CGImage]
                               scale:(image.scale * scale)
                         orientation:(image.imageOrientation)];
  }
  return image;
}

static UIImage* ExtractIcon(NSObject<FlutterPluginRegistrar>* registrar, NSArray* iconData) {
  UIImage* image;
  if ([iconData.firstObject isEqualToString:@"defaultMarker"] || [iconData.firstObject isEqualToString:@"fromAsset"]) {
    if (iconData.count == 2) {
      image = [UIImage imageNamed:[registrar lookupKeyForAsset:iconData[1]]];
    } else {
      image = [UIImage imageNamed:[registrar lookupKeyForAsset:iconData[1]
                                                   fromPackage:iconData[2]]];
    }
  } else if ([iconData.firstObject isEqualToString:@"fromAssetImage"]) {
    if (iconData.count == 3) {
      image = [UIImage imageNamed:[registrar lookupKeyForAsset:iconData[1]]];
      NSNumber* scaleParam = iconData[2];
      image = scaleImage(image, scaleParam);
    } else {
      NSString* error =
          [NSString stringWithFormat:@"'fromAssetImage' should have exactly 3 arguments. Got: %lu",
                                     (unsigned long)iconData.count];
      NSException* exception = [NSException exceptionWithName:@"InvalidBitmapDescriptor"
                                                       reason:error
                                                     userInfo:nil];
      @throw exception;
    }
  } else if ([iconData[0] isEqualToString:@"fromBytes"]) {
    if (iconData.count == 2) {
      @try {
        FlutterStandardTypedData* byteData = iconData[1];
        CGFloat screenScale = [[UIScreen mainScreen] scale];
        image = [UIImage imageWithData:[byteData data] scale:screenScale];
      } @catch (NSException* exception) {
        @throw [NSException exceptionWithName:@"InvalidByteDescriptor"
                                       reason:@"Unable to interpret bytes as a valid image."
                                     userInfo:nil];
      }
    } else {
      NSString* error = [NSString
          stringWithFormat:@"fromBytes should have exactly one argument, the bytes. Got: %lu",
                           (unsigned long)iconData.count];
      NSException* exception = [NSException exceptionWithName:@"InvalidByteDescriptor"
                                                       reason:error
                                                     userInfo:nil];
      @throw exception;
    }
  }

  return image;
}

static void InterpretMarkerOptions(NSDictionary* data, id<MapboxMarkerOptionsSink> sink,
                                   NSObject<FlutterPluginRegistrar>* registrar) {
  NSArray* position = data[@"position"];
  if (position) {
    [sink setPosition:ToLocation(position)];
  }
  NSString* icon = data[@"iconImage"];
  if (icon) {
      NSString* iconPath = [registrar lookupKeyForAsset: icon];
      NSString* path = [[NSBundle mainBundle] pathForResource:iconPath ofType:nil];
      UIImage* imageFromAsset = [UIImage imageWithContentsOfFile:path];

      if(imageFromAsset){
        [sink setIcon:imageFromAsset];
      }
  }else{
    [sink addAnnotation];
  }
}

@implementation MarkersController {
  NSMutableDictionary* _markerIdToController;
  FlutterMethodChannel* _methodChannel;
  NSObject<FlutterPluginRegistrar>* _registrar;
  MGLMapView* _mapView;
  MGLStyle* _style;
}
- (instancetype)init:(FlutterMethodChannel*)methodChannel
             mapView:(MGLMapView*)mapView
               style:(MGLStyle *)style
           registrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  self = [super init];
  if (self) {
    _methodChannel = methodChannel;
    _mapView = mapView;
    _style = style;
    _markerIdToController = [NSMutableDictionary dictionaryWithCapacity:1];
    _registrar = registrar;
  }
  return self;
}
- (void)addMarkers:(NSArray*)markersToAdd {
  for (NSDictionary* marker in markersToAdd) {
    CLLocationCoordinate2D position = [MarkersController getPosition:marker];
    NSString* markerId = [MarkersController getMarkerId:marker];
    MapboxMarkerController* controller =
        [[MapboxMarkerController alloc] initMarkerWithPosition:position
                                                            markerId:markerId
                                                             mapView:_mapView
                                                               style:_style];
    InterpretMarkerOptions(marker, controller, _registrar);
    _markerIdToController[markerId] = controller;
  }
}

+ (CLLocationCoordinate2D)getPosition:(NSDictionary*)marker {
  NSArray* position = marker[@"position"];
  return ToLocation(position);
}
+ (NSString*)getMarkerId:(NSDictionary*)marker {
  return marker[@"markerId"];
}
@end
