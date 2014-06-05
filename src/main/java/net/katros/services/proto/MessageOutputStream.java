/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto;

import com.google.protobuf.GeneratedMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.GZIPOutputStream;

/**
 * @author boris@temk.org
**/
public abstract class MessageOutputStream<T extends GeneratedMessage> {
    protected OutputStream os = null;
    
    public abstract void write(T msg)  throws IOException;
    
    public void flush() throws IOException {
        os.flush();
    }
    
    public void close() throws IOException {
        os.close();
    }
    
    private MessageOutputStream(OutputStream os) {
        this.os = os;
    }
    
    public static <T extends GeneratedMessage> MessageOutputStream<T> createBinaryStream(String path, boolean gzip) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        
        OutputStream os = new FileOutputStream(file);
        if (gzip) {
            os = new GZIPOutputStream(os);
        }
        
        return createBinaryStream(os);
    }
    
    public static <T extends GeneratedMessage> MessageOutputStream<T> createBinaryStream(OutputStream os) {
        return new MessageOutputStream<T>(os) {            
            @Override
            public void write(T msg)  throws IOException {
                msg.writeDelimitedTo(os);
            }
        };
    }

    public static <T extends GeneratedMessage> MessageOutputStream<T> createTextStream(String path, boolean gzip) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        
        OutputStream os = new FileOutputStream(file);
        if (gzip) {
            os = new GZIPOutputStream(os);
        }
        
        return createTextStream(os);
    }
    
    public static <T extends GeneratedMessage> MessageOutputStream<T> createTextStream(OutputStream os) {
        return new MessageOutputStream<T>(os) {
            
            @Override
            public void write(T msg) throws IOException {
                os.write(msg.toString().getBytes());
                os.write("--\n".getBytes());
            }
        };
    }
    
    public static <T extends GeneratedMessage> MessageOutputStream<T> createMemoryStream() {
        return createMemoryStream(new LinkedList<T>());
    }
    
    public static <T extends GeneratedMessage> MessageOutputStream<T> createMemoryStream(final Queue<T> queue) {
        return new MessageOutputStream<T>(null) {
            
            @Override
            public void write(T msg) throws IOException {
                queue.add(msg);
            }
        };
    }
    
    
}
