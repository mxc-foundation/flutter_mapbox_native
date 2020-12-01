#import <Flutter/Flutter.h>

@import Mapbox;
// Create a subclass of MGLUserLocationAnnotationView.
@interface CustomUserLocationAnnotationView : MGLUserLocationAnnotationView
 
@property (nonatomic) CGFloat size;
@property (nonatomic) CALayer *dot;
@property (nonatomic) CAShapeLayer *arrow;
 
@end