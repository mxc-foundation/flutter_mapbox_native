#import "CustomUserLocationAnnotationView.h"

@implementation CustomUserLocationAnnotationView
 
- (instancetype)init {
    self.size = 30;
    self = [super initWithFrame:CGRectMake(0, 0, self.size, self.size)];
    
    return self;
}
 
// -update is a method inherited from MGLUserLocationAnnotationView. It updates the appearance of the user location annotation when needed. This can be called many times a second, so be careful to keep it lightweight.
- (void)update {
    // Check whether we have the user’s location yet.
    if (CLLocationCoordinate2DIsValid(self.userLocation.coordinate)) {
        [self setupLayers];
        [self updateHeading];
    }
}
 
- (void)updateHeading {
    // Show the heading arrow, if the heading of the user is available.
    if (self.userLocation.heading.trueHeading > 0) {
        _arrow.hidden = NO;
        
        // Get the difference between the map’s current direction and the user’s heading, then convert it from degrees to radians.
        CGFloat rotation = -MGLRadiansFromDegrees(self.mapView.direction - self.userLocation.heading.trueHeading);
        
        // If the difference would be perceptible, rotate the arrow.
        if (fabs(rotation) > 0.01) {
            // Disable implicit animations of this rotation, which reduces lag between changes.
            [CATransaction begin];
            [CATransaction setDisableActions:YES];
            _arrow.affineTransform = CGAffineTransformRotate(CGAffineTransformIdentity, rotation);
            [CATransaction commit];
        }
    } else {
        _arrow.hidden = YES;
    }
}
 
- (void)setupLayers {
    // This dot forms the base of the annotation.
    if (!_dot) {
        _dot = [CALayer layer];
        _dot.frame = self.bounds;
        
        // Use CALayer’s corner radius to turn this layer into a circle.
        _dot.cornerRadius = _size / 2;
        _dot.backgroundColor = super.tintColor.CGColor;
        _dot.borderWidth = 4;
        _dot.borderColor = [UIColor whiteColor].CGColor;
        
        [self.layer addSublayer:_dot];
    }
    
    // This arrow overlays the dot and is rotated with the user’s heading.
    if (!_arrow) {
        _arrow = [CAShapeLayer layer];
        _arrow.path = [self arrowPath];
        _arrow.frame = CGRectMake(0, 0, _size / 2, _size / 2);
        _arrow.position = CGPointMake(CGRectGetMidX(_dot.frame), CGRectGetMidY(_dot.frame));
        _arrow.fillColor = _dot.borderColor;
        [self.layer addSublayer:_arrow];
    }
}
 
// Calculate the vector path for an arrow, for use in a shape layer.
- (CGPathRef)arrowPath {
    CGFloat max = _size / 2;
    CGFloat pad = 3;
    
    CGPoint top =    CGPointMake(max * 0.5f, 0);
    CGPoint left =   CGPointMake(0 + pad,    max - pad);
    CGPoint right =  CGPointMake(max - pad,  max - pad);
    CGPoint center = CGPointMake(max * 0.5f, max * 0.6f);
    
    UIBezierPath *bezierPath = [UIBezierPath bezierPath];
    [bezierPath moveToPoint:top];
    [bezierPath addLineToPoint:left];
    [bezierPath addLineToPoint:center];
    [bezierPath addLineToPoint:right];
    [bezierPath addLineToPoint:top];
    [bezierPath closePath];
    
    return bezierPath.CGPath;
}
 
@end