package net.katros.services.proto;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.TextFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.GZIPInputStream;
import static net.katros.services.proto.MessageOutputStream.createMemoryStream;

/**
 * @author boris@temk.org
**/
public abstract class MessageInputStream<T extends GeneratedMessage> {
    
    protected T next = null;    
    protected abstract T readNext() throws IOException;
    
    public boolean hasMoreMessages() {
        return next != null;
    }

    public T peek() {
        return next;
    }
    
    public T read() throws IOException {
        T result = next;
        next = readNext();
        return result;
    }
    
    public static <T extends GeneratedMessage> MessageInputStream<T> createBinaryStream(String path, boolean gzip, String msgType) throws IOException {
        InputStream is = path.equals("-")? System.in : new FileInputStream(path);
        if (gzip) {
            is = new GZIPInputStream(is);
        }
        
        return createBinaryStream(is, msgType);
    }
    
    public static <T extends GeneratedMessage> MessageInputStream<T> createBinaryStream(final InputStream is, String msgType) throws IOException {
        final Registry reg = Registry.getInstance();
        final ExtensionRegistry extReg = reg.getExtensionRegistry();
        final GeneratedMessage msg = reg.getInstanceForType(msgType);
        
        if (msg == null) {
            throw new RuntimeException("Type " + msgType + " not found.");
        }
        
        final Parser parser = msg.getParserForType();
        return new MessageInputStream<T>() {
            
            {
                next = readNext();
            }
            
            @Override
            protected T readNext()  throws IOException {
                if (is.available() > 0) {
                    return (T) parser.parseDelimitedFrom(is, extReg);
                }
                return null;
            }
        };
    }

    public static <T extends GeneratedMessage> MessageInputStream<T> createTextStream(String path, boolean gzip, String msgType) throws IOException {
        InputStream is = path.equals("-")? System.in : new FileInputStream(path);
        if (gzip) {
            is = new GZIPInputStream(is);
        }
        
        return createTextStream(is, msgType);
    }
    
    public static <T extends GeneratedMessage> MessageInputStream<T> createTextStream(final InputStream is, String msgType) throws IOException {
        final Registry reg = Registry.getInstance();
        final ExtensionRegistry extReg = reg.getExtensionRegistry();
        final GeneratedMessage msg = reg.getInstanceForType(msgType);
        final LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
        
        return new MessageInputStream<T>() {
            {
                next = readNext();
            }
            
            @Override
            protected T readNext()  throws IOException {
                StringBuilder buf = new StringBuilder();
                while(true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    
                    if (line.equals("--")) {
                        break;
                    }
                    
                    buf.append(line);
                    buf.append("\n");
                }
                
                if (buf.length() > 0) {
                    Message.Builder builder = msg.newBuilderForType();
                    TextFormat.merge(buf.toString(), extReg, builder);                
                    return (T ) builder.build();
                }
                return null;
            }
        };
    }
    
    public static <T extends GeneratedMessage> MessageInputStream<T> createMemoryStream() {
        return createMemoryStream(new LinkedList<T>());
    }
    
    
    
    public static  <T extends GeneratedMessage> MessageInputStream<T> createMemoryStream(final Queue<T> queue) {
        return new MessageInputStream<T>() {
            @Override 
            public boolean hasMoreMessages() {
                return !queue.isEmpty();
            }
            

            @Override
            public T peek() {
                return queue.peek();
            }
            
            @Override
            public T read() throws IOException {
                return queue.remove();
            }
            
            @Override
            protected T readNext() throws IOException {                
                return null; // should never called
            }
        };
    }
}
