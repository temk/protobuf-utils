/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep;

import net.katros.services.proto.MessageInputStream;
import net.katros.services.proto.test.Geom.Shape;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 * @author boris@temk.org
**/
public class GrepProcessorTest {
    
    private MessageInputStream inputStream() throws Exception {
        return MessageInputStream.createTextStream(getClass().getClassLoader().getResourceAsStream("shapes.txt"), "Shape");
    }
    
    @Test
    public void test1() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("type == 'CIRCLE'");
        grep.setPrintStrings("id");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertArrayEquals(list.toArray(), new Long [] {1L,2L,3L,4L,5L});
    }

    @Test
    public void test2() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("type == 'SQUARE'");
        grep.setPrintStrings("$$.type");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertArrayEquals(list.toArray(), new String [] {"CIRCLE", "SQUARE", "SQUARE", "SQUARE", "SQUARE"});
    }

    @Test
    public void test3() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("type == 'POLYGON'");
        grep.setPrintStrings("#shape@Polygon.points");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertArrayEquals(list.toArray(), new Long [] {1L,2L,3L,4L});
    }
    
    @Test
    public void test4() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("type == 'POLYGON'");
        grep.setPrintStrings("#shape@Square.points");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertFalse(grep.parse());
    }
    
    @Test
    public void test5() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("type == 'POLYGON' && $$.type == 'POLYGON' && shape@Polygon.points[i] == $$.shape@Polygon.points[j]");
        grep.setPrintStrings("id");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertArrayEquals(list.toArray(), new Long [] {12L,13L,14L});
    }
    
    @Test
    public void test6() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("type == 'POLYGON' && $$.type == 'POLYGON' && shape@Polygon.points[i] == $$.shape@Polygon.points[j]");
        grep.setPrintStrings("$$.id, $.id");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertArrayEquals(list.toArray(), new Long [] {11L,12L,12L,13L,13L,14L});
    }
    
    @Test
    public void test7() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("id == 12");
        grep.setPrintStrings("shape@Polygon.points.[x,y],id");
        grep.setListOutput(list);
        grep.setFindAll(true);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertArrayEquals(list.toArray(), new Long [] {0L,0L,12L, 1L,2L, 12L});
    }
    
    @Test
    public void test8() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("shape@Polygon.points[i] == $$.shape@Polygon.points[j]");
        grep.setPrintStrings("$$.id, $.id");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertArrayEquals(list.toArray(), new Long [] {11L,12L,12L,13L,13L,14L});
    }
    
    @Test
    public void test9() throws Exception {
        List list = new ArrayList();
        
        GrepProcessor grep = new GrepProcessor();
        grep.setInputStream(inputStream());
        grep.setQueryString("id == 1");
        grep.setPrintStrings("shape@Polygon");
        grep.setListOutput(list);
        grep.setRootMessage(Shape.getDescriptor());
        
        assertTrue(grep.parse());
        grep.run();
        
        assertTrue(list.size() == 1);
        assertNull(list.get(0));
    }
    
}
