/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto;

import net.katros.services.proto.MessageOutputStream;
import net.katros.services.proto.test.Geom.Circle;
import net.katros.services.proto.test.Geom.Point;
import net.katros.services.proto.test.Geom.Polygon;
import net.katros.services.proto.test.Geom.Shape;
import net.katros.services.proto.test.Geom.Square;
import static net.katros.services.proto.test.Geom.TYPE.CIRCLE;
import static net.katros.services.proto.test.Geom.TYPE.POLYGON;
import static net.katros.services.proto.test.Geom.TYPE.SQUARE;
import java.io.FileOutputStream;
import org.junit.Test;

/**
 * @author boris@temk.org
**/
public class TestFileBuilder {
    
    @Test
    public void buildSimpleShapes() throws Exception {
        MessageOutputStream<Shape> out = MessageOutputStream.createTextStream(new FileOutputStream("src/test/resources/shapes.txt"));
        int id = 0;
        for (int k = 1; k <= 5; ++ k) {
            out.write(Shape.newBuilder()
                           .setType(CIRCLE)
                           .setId(++ id)
                           .setExtension(Circle.shape, 
                                         Circle.newBuilder()
                                               .setCenter(Point.newBuilder()
                                                               .setX(k)
                                                               .setY(2 * k)
                                                               .build())
                                            .setRadius(3 * k)
                                            .build())
                         .build());                        
        }
        
        for (int k = 1; k <= 5; ++ k) {
            out.write(Shape.newBuilder()
                           .setType(SQUARE)
                           .setId(++ id)
                           .setExtension(Square.shape, 
                                         Square.newBuilder()
                                               .setCorner(Point.newBuilder()
                                                               .setX(k)
                                                               .setY(2 * k)
                                                               .build())
                                            .setWidth(3 * k)
                                            .build())
                         .build());                        
        }
        
        for (int k = 1; k < 5; ++ k) {
            Polygon.Builder poly = Polygon.newBuilder();
            for (int t = 0; t < k; ++ t) {
                poly.addPoints(Point.newBuilder()
                                    .setX(t)
                                    .setY(2 * t));
            }
            
            out.write(Shape.newBuilder()
                    .setType(POLYGON)
                    .setId(++ id)
                    .setExtension(Polygon.shape, poly.build())
                    .build());
        }
                                        
    }
}
