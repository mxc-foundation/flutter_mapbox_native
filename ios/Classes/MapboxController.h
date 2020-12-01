#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <UIKit/UIKit.h>

@import Mapbox;

#import "MapboxMarkerController.h"

enum MyLocationRenderMode {
    MyLocationRenderNormal, MyLocationRenderCompass, MyLocationRenderGps
};

typedef enum MyLocationRenderMode MyLocationRenderMode;

NS_ASSUME_NONNULL_BEGIN

// Defines map UI options writable from Flutter.
@protocol MapboxOptionsSink
- (void)setMinimumZoomLevel:(float)level;
- (void)setMaximumZoomLevel:(float)level;
- (void)setMyLocationSwitch:(BOOL)visiable;
- (void)setMyLocationEnabled:(BOOL)enabled;
- (void)setMyLocationTrackingMode:(MGLUserTrackingMode)mode;
- (void)setMyLocationRenderMode:(MyLocationRenderMode)mode;
@end

@interface MapboxController : NSObject<FlutterPlatformView,MGLMapViewDelegate,UIGestureRecognizerDelegate>

- (instancetype)initWithFrame:(CGRect)frame
                   viewIdentifier:(int64_t)viewId
                        arguments:(id _Nullable)args
                        registrar:(NSObject<FlutterPluginRegistrar>*)registrar;
@end


NS_ASSUME_NONNULL_END