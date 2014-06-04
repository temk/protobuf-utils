package net.katros.services.proto;

import com.google.protobuf.GeneratedMessage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author boris@temk.org
**/
public class PCat {

    private static final int DEFAULT_BUF_SIZ = 64 << 10; // 64K

    private static void printUsageAndExit(String message) {
        System.err.println(message);
        System.err.println("");
        System.err.println("Usage: PCat [-zZtTbB] <root-msg> [<input-file>|-]*");
        System.exit(-1);
    }

    public static final void main(String[] args) throws Exception {
        String messageName = null;
        List<String> inputFiles = new ArrayList<>();
        String flags = "";
        for (String param : args) {
            if (param.startsWith("-") && param.length() > 1) {
                flags += param.substring(1);
                continue;
            }

            if (messageName == null) {
                messageName = param;
                continue;
            }

            inputFiles.add(param);
        }

        if (inputFiles.isEmpty()) {
            inputFiles.add("-");
        }

        boolean iz = false, oz = false, it = false, ot = true;
        for (byte b : flags.getBytes()) {
            switch (b) {
                case 'z':
                    iz = true;
                    break;

                case 'Z':
                    oz = true;
                    break;

                case 't':
                    it = true;
                    break;

                case 'T':
                    ot = true;
                    break;

                case 'b':
                    it = false;
                    break;

                case 'B':
                    ot = false;
                    break;

                case 'h':
                default:
                    printUsageAndExit("unexpected flag " + (char) b);
            }
        }
        Registry registry = Registry.getInstance();
        GeneratedMessage instance = registry.getInstanceForType(messageName);
        if (instance == null) {
            printUsageAndExit("No message type " + messageName + " found.");
        }

        OutputStream os = System.out;
        if (oz) {
            os = new GZIPOutputStream(os);
        } 
        
        MessageOutputStream mos = ot ? MessageOutputStream.createTextStream(os) : MessageOutputStream.createBinaryStream(os);

        for (String inputFile : inputFiles) {
            InputStream is = inputFile.equals("-") ? System.in : new FileInputStream(inputFile);
            if (iz) {
                is = new GZIPInputStream(is, DEFAULT_BUF_SIZ);
            } else {
                is = new BufferedInputStream(is, DEFAULT_BUF_SIZ);
            }
            MessageInputStream mis = it
                    ? MessageInputStream.createTextStream(is, messageName)
                    : MessageInputStream.createBinaryStream(is, messageName);

            while (mis.hasMoreMessages()) {
                mos.write(mis.read());
            }
        }

        mos.close();

    }
}
