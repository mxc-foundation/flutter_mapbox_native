#import "MapboxPlatformViewFactory.h"
#import "MapboxController.h"

@implementation MapboxPlatformViewFactory{
    NSObject<FlutterPluginRegistrar>* _registrar;
}

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar{
    self = [super init];
    if (self) {
        _registrar = registrar;
    }
    return self;
}

-(NSObject<FlutterMessageCodec> *)createArgsCodec{
    return [FlutterStandardMessageCodec sharedInstance];
}

-(NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args{
    
    MapboxController *mapboxController = [[MapboxController alloc] initWithFrame:frame 
                                                                   viewIdentifier:viewId 
                                                                    arguments:args 
                                                                    registrar:_registrar];
    
    return mapboxController;
    
}

@end