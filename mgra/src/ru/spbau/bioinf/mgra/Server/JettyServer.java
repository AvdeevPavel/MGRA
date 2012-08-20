package ru.spbau.bioinf.mgra.Server;

import net.sf.saxon.s9api.*;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlets.MultiPartFilter;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Drawer.Drawer;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

public class JettyServer {
    private static final Logger log = Logger.getLogger(JettyServer.class);

    public static final ServletContext servletContext = new UploadServletContext();
    public static MultiPartFilter uploadFilter;
    public static String REQUEST_START = "request";
    private static int port = 8080;

    public static final File xslDir = new File("xsl");
    public static final Processor processor = new Processor(false);
    public static final XsltCompiler comp = processor.newXsltCompiler();
    public static XsltTransformer[] xslt = new XsltTransformer[3];

    public static final File execDir = new File("exec");
    public static File exeFile;

    public static File uploadDir = new File("upload");
    public static File libDir = new File("/home/student/Desktop/MGRA/mgra/html/lib"); //change on server
    public static final String GENOME_FILE_NAME = "genome.txt";
    public static final String CFG_FILE_NAME = "mgra.cfg";

    static {
        try {
            xslt[0] = comp.compile(new StreamSource(new InputStreamReader(new FileInputStream(new File(xslDir, "tree.xsl")), "UTF-8"))).load();
            xslt[1] = comp.compile(new StreamSource(new InputStreamReader(new FileInputStream(new File(xslDir, "genome.xsl")), "UTF-8"))).load();
            xslt[2] = comp.compile(new StreamSource(new InputStreamReader(new FileInputStream(new File(xslDir, "transformation.xsl")), "UTF-8"))).load();
        } catch (Throwable e) {
            log.error("Error initializing xslt", e);
        }
        exeFile = new File(execDir, "mgra.bin");
        if (!exeFile.exists()) {
            exeFile = new File(execDir, "mgra.exe");
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            uploadDir = new File(args[0]);
        }

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        if (args.length > 2) {
            libDir = new File(args[2]);
        }

        if (args.length > 3) {
            REQUEST_START = args[3];
        }

        if (args.length > 4) {
            Drawer.setThreshold(Double.parseDouble(args[4]));
        }

        uploadDir.mkdirs();

        uploadFilter = new MultiPartFilter();
        FilterConfig config = new FilterConfig() {
            public String getFilterName() {
                return null;
            }

            public ServletContext getServletContext() {
                return servletContext;
            }

            public String getInitParameter(String s) {
                if ("deleteFiles".equalsIgnoreCase(s))
                    return "false";
                return Integer.toString(32 * 1024);
            }

            public Enumeration getInitParameterNames() {
                return null;
            }
        };

        uploadFilter.init(config);

        Handler handler = new MyHandler();

        Server server = new Server(port);
        for (Connector connector: server.getConnectors()) {
            connector.setMaxIdleTime(2000000);
        }
        server.setHandler(handler);
        server.start();
    }

    public static void runMgraTool(Config config, PrintWriter out) throws InterruptedException, IOException {
        String[] command = new String[]{exeFile.getAbsolutePath(), config.getNameFile()};
        Process process = Runtime.getRuntime().exec(command, new String[]{}, new File(config.getPathParentFile()));

        Thread outputThread = listenOutput(process.getInputStream(), out, "output");
        Thread errorThread = listenOutput(process.getErrorStream(), out, "output");

        do {
            try {
                int value = process.waitFor();
                log.debug("MGRA process return value : " + value);
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);

        outputThread.join();
        errorThread.join();
    }


    public static Thread listenOutput(InputStream inputStream, final PrintWriter out, final String type) {
        final BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));

        Thread outputThread = new Thread(new Runnable() {
            public void run() {
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        log.debug("MGRA " + type + " : " + line);
                        MyHandler.response(out, type + " : " + line);
                    }
                } catch (IOException e) {
                    logError(e, out, "Error reading MGRA " + type);
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        logError(e, out, "Error closing MGRA " + type);
                    }
                }
            }
        });
        outputThread.start();
        return outputThread;
    }

    private static void logError(IOException e, PrintWriter out, String message) {
        log.error(message, e);
        MyHandler.response(out, message);
        synchronized (out) {
            e.printStackTrace(out);
        }
    }
}
