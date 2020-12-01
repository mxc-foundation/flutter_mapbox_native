#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
@import Mapbox;

NS_ASSUME_NONNULL_BEGIN

//  Defines marker UI options writable from Flutter.
@protocol MapboxMarkerOptionsSink
- (void)setPosition:(CLLocationCoordinate2D)position;
- (void)setIcon:(UIImage*)icon;
- (void)addAnnotation;
@end

// Defines marker controllable by Flutter.
@interface MapboxMarkerController : NSObject <MapboxMarkerOptionsSink>
@property(atomic, readonly) NSString* markerId;
- (instancetype)initMarkerWithPosition:(CLLocationCoordinate2D)position
                              markerId:(NSString*)markerId
                               mapView:(MGLMapView*)mapView
                                 style:(MGLStyle *)style;
@end

@interface MarkersController : NSObject
- (instancetype)init:(FlutterMethodChannel*)methodChannel
             mapView:(MGLMapView*)mapView
               style:(MGLStyle *)style
           registrar:(NSObject<FlutterPluginRegistrar>*)registrar;
- (void)addMarkers:(NSArray*)markersToAdd;
@end

NS_ASSUME_NONNULL_END