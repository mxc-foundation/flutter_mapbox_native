#import "FlutterMapboxNativePlugin.h"
#import "MapboxPlatformViewFactory.h"
#import "MapboxController.h"

@implementation FlutterMapboxNativePlugin{
  NSMutableDictionary* _mapControllers;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {

    [registrar registerViewFactory:[[MapboxPlatformViewFactory alloc] initWithRegistrar:registrar] withId:@"plugins.flutter.io/mapbox_native"];

}

- (MapboxController*)mapFromCall:(FlutterMethodCall*)call error:(FlutterError**)error {
  id mapId = call.arguments[@"map"];
  MapboxController* controller = _mapControllers[mapId];
  if (!controller && error) {
    *error = [FlutterError errorWithCode:@"unknown_map" message:nil details:mapId];
  }
  return controller;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
