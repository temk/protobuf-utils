package net.katros.services.proto.grep;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageOrBuilder;
import net.katros.services.proto.GrepLexer;
import net.katros.services.proto.GrepParser;
import net.katros.services.proto.MessageInputStream;
import net.katros.services.proto.MessageOutputStream;
import net.katros.services.proto.Registry;
import net.katros.services.proto.grep.expr.BooleanExpression;
import net.katros.services.proto.grep.expr.Expression;
import net.katros.services.proto.grep.expr.IndexReference;
import net.katros.services.proto.grep.expr.Printer;
import net.katros.services.proto.grep.expr.SemanticException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * @author boris@temk.org
**/
public class GrepProcessor {
    private String delim = "--";
    
    private String queryStr;
    private List<String> printStr = new ArrayList<>();

    private List<MessageInputStream> input = new ArrayList<>();
    private MessageOutputStream messageOutput = null;
    private PrintStream         printOutput = null;
    private List                listOutput = null;

    private boolean flagFindAll = false;
    private Descriptor rootMessage = null;
    private ExtensionRegistry registry = Registry.getInstance().getExtensionRegistry();
    
    private boolean dryFlag = false;
    
    Set<String> indexNames = new HashSet<>();
    RootPool rootPool;
    IndexPool indexPool;

    Map<String, IndexReference> indexMap = new HashMap<>();

    private BooleanExpression query;
    private List<Printer> printers = new ArrayList<>();

    public void setQueryString(String str) {
        queryStr = str;
    }
    
    public void setPrintStrings(String... strs) {
        printStr.addAll(Arrays.asList(strs));
    }
    
    public void setFindAll(boolean b) {
        flagFindAll = b;
    }
    
    public void setInputStream(MessageInputStream... streams) {
        input.addAll(Arrays.asList(streams));
    }
    
    public void setMessageOutputStream(MessageOutputStream os) {
        messageOutput = os;
    }
    
    public void setPrintOutputStream(PrintStream ps) {
        printOutput = ps;
    }
    
    public void setListOutput(List list) {
        listOutput = list;
    }
    
    public void setDelimiter(String delim) {
        this.delim = delim;
    }

    public void setRootMessage(Descriptor descriptor) {
        rootMessage = descriptor;
    }
    
    public static void usage() {
        System.err.println("Usage: GrepProcessor [options]* <input-file>*\n" + 
        "Options: \n" + 
        "\t-q <query-expression>\tMandatory. Should apear only one time.\n" + 
        "\t-p <print-expression>\tOptional. Default is \"$\". Can apear several times.\n" + 
        "\t-d <delimiter>\tOptional. Default is \"-\"\n" + 
        "\t-o <output-file>\tOptional. Default is \"-\"\n" + 
        "\t-m <message-name>\tMandatory.\n" + 
        "\t-z\tOptional. Default false. Input assumend gzipped.\n" + 
        "\t-Z\tOptional. Default false. Output will gzipped.\n" + 
        "\t-t\tOptional. Default false. Input format assumed text.\n" + 
        "\t-T\tOptional. Default true.  Output format will text.\n" + 
        "\t-t\tOptional. Default true.  Input format assumed binary.\n" + 
        "\t-T\tOptional. Default false. Output format will binary.\n" + 
        "\t-d\tOptional. Default false. Dry run. I.e. just check syntax.\n" + 
        "\n" + 
        "flags can be grouped. File name '-' means stdin/stdout depends on context.\n" + 
        "\n" + 
        "print-expression can contain several expressions, divided by ','\n" + 
        "\n" + 
        "Special symbols:\n" + 
        "\t$\tcurrent message\n" + 
        "\t$$\tprevious message\n" + 
        "\t$..$ (n times)\t(n-1)-th previous message\n" + 
        "\n" + 
        "Example: GrepProcessor -tmqp Shape \"type == 'POLYGON'\" \"id,$$.color\" shapes.txt\n");
    }
    private GrepParser parse(String query) {
        GrepLexer lexer = new GrepLexer(new ANTLRInputStream(query));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new GrepErrorListener(query));

        GrepParser parser = new GrepParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new GrepErrorListener(query));

        return parser;
    }

    private boolean eval(IndexReference[] indexes, int i) {
        if (i == indexes.length) {
            if (query.eval()) {
                print();
                return true;
            }
            
            return false;
        }

        IndexReference index = indexes[i];
        long m = index.range();
        for (int k = 0; k < m; ++k) {
            index.setValue(k);
            boolean b = eval(indexes, i + 1);
            if (b && !flagFindAll) {
                return true;
            }
        }
        return false;
    }

    private void print() {
        for (Printer p : printers) {
            p.print();
        }
    }

    public void run() throws IOException {
        while (!input.isEmpty()) {
            MessageInputStream is = input.remove(0);
            while (is.hasMoreMessages()) {
                rootPool.push(is.read());
                try {
                    if (rootPool.ready()) {
                        eval(indexPool.getIndexes(), 0);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public boolean parse() {
        validate();
        
        GrepParser queryParser = parse(queryStr);
        ParserRuleContext queryTree = queryParser.expr();
        ParseTreeWalker walker = new ParseTreeWalker();
        
        GrepDeclarationListener declListener = new GrepDeclarationListener(indexNames);
        walker.walk(declListener, queryTree);
        for (String str : printStr) {
            GrepParser printParser = parse(str);
            ParserRuleContext printTree = printParser.print();
            walker.walk(declListener, printTree);
        }

        rootPool = new RootPool(rootMessage, declListener.getDepth() + 1);
        indexPool = new IndexPool(indexNames);
         
        GrepExpressionListener queryExtractor = new GrepExpressionListener(registry, rootMessage, rootPool, indexPool);
        try {
            walker.walk(queryExtractor, queryTree);
        } catch(SemanticException ex) {
            reportError(ex, queryStr);
            return false;
        }

        query = queryExtractor.getQueryExpression();

        for (String str : printStr) {
            GrepParser printParser = parse(str);
            ParserRuleContext printTree = printParser.print();

            GrepExpressionListener printExtractor = new GrepExpressionListener(registry, rootMessage, rootPool, indexPool);
            try {
                walker.walk(printExtractor, printTree);
            } catch(SemanticException ex) {
                reportError(ex, str);
                return false;
            }

            for (Expression e: printExtractor.getPrintExpressions()) {
                if (messageOutput != null) {
                    printers.add(Printer.createPrinter(messageOutput, e));
                } else if (printOutput != null) {
                    printers.add(Printer.createPrinter(printOutput, e, delim));
                } else if (listOutput != null) {
                    printers.add(Printer.createPrinter(listOutput, e));
                }
            }
        }
        
        indexPool.build();
        return true;
    }

    private void configure(String[] args) throws IOException {
        String keys = "";
        List<String> params = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("-")) {
                keys = keys + arg.substring(1);
            } else {
                params.add(arg);
            }
        }

        boolean compressInput = false;
        boolean compressOutput = false;

        boolean binaryInput = true;
        boolean binaryOutput = true;

        String inputMessage = null;

        String outputFileName = "-";
        
        int idx = 0;
        for (char c : keys.toCharArray()) {
            switch (c) {
                case 't':
                    binaryInput = false;
                    break;

                case 'T':
                    binaryOutput = false;
                    break;

                case 'b':
                    binaryInput = true;
                    break;

                case 'B':
                    binaryOutput = true;
                    break;

                case 'z':
                    compressInput = true;
                    break;

                case 'Z':
                    compressOutput = true;
                    break;

                case 'q':
                    queryStr = params.get(idx++);
                    break;

                case 'p':
                    printStr.add(params.get(idx++));
                    break;

                case 'o':
                    outputFileName = params.get(idx++);
                    break;

                case 'a':
                    flagFindAll = true;
                    break;

                case 'm':
                    inputMessage = params.get(idx++);
                    break;
                    
                case 'd':
                    dryFlag = true;
                    break;
                    
                case 'h':
                    dryFlag = true;
                    usage();
                    return;
                    
                default:
                    throw new RuntimeException("Unexpected key " + c);
            }
        }

        MessageOrBuilder msg = Registry.getInstance().getInstanceForType(inputMessage);
        
        if (msg == null) {
            throw new RuntimeException("No such message " + inputMessage);
        }
        
        rootMessage = msg.getDescriptorForType();

        if (printStr.isEmpty()) {
            printStr.add("$");
        }
        openInputStreams(params.subList(idx, params.size()), inputMessage, compressInput, binaryInput);

        if (outputFileName.equals("-")) {
            printOutput = System.out;
        } else {
            throw new RuntimeException("use '-', other options are not implemented");
        }
    }

    private void validate() {
        if (input == null) {
            throw new IllegalStateException("input stream not set");
        }
        
        if (queryStr == null) {
            throw new IllegalStateException("query-expression not set");
        }
        
        if (printStr.size() == 0) {
            throw new IllegalStateException("no print-expression");
        }
        
        if (rootMessage == null) {
            throw new IllegalStateException("root message not set");
        }
        
        if (messageOutput == null && listOutput == null && printOutput == null) {
            throw new IllegalStateException("output not set");
        }
    }
    
    private void openInputStreams(List<String> list, String msg, boolean compress, boolean binary) throws IOException {
        if (list.isEmpty()) {
            list.add("-");
        }

        for (String name : list) {
            MessageInputStream mis = binary
                    ? MessageInputStream.createBinaryStream(name, compress, msg)
                    : MessageInputStream.createTextStream(name, compress, msg);

            input.add(mis);
        }
    }

    public static void reportError(SemanticException e, String input) {
        System.err.println("Semantic Error: " + e.getMessage());
        System.err.println("Input:" + input);
        System.err.print("Error:");
        for (int k = 0; k < e.getToken().getStartIndex(); ++k) {
            System.err.print("-");
        }
        System.err.println("^");
    }

    public static void main(String[] args) throws IOException {
        GrepProcessor grep = new GrepProcessor();
        grep.configure(args);
        if (grep.parse() && !grep.dryFlag) {
            grep.run();
        }
    }

}
