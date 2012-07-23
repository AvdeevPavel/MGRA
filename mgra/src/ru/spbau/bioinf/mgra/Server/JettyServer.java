package ru.spbau.bioinf.mgra.Server;

import net.sf.saxon.s9api.*;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlets.MultiPartFilter;
import ru.spbau.bioinf.mgra.Parser.TreeReader;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class JettyServer {

    private static final Logger log = Logger.getLogger(JettyServer.class);

    private static final ServletContext servletContext = new UploadServleContext();
    private static MultiPartFilter uploadFilter;

    private static final Processor processor = new Processor(false);
    private static final XsltCompiler comp = processor.newXsltCompiler();
    private static XsltTransformer xslt;

    private static final String REQUEST_START = "/2012/";      //change to /file/
    private static final String GENOME_FILE = "genome.txt";
    private static final String CFG_FILE_NAME = "mgra.cfg";

    private static final File execDir = new File("exec");
    private static final File xslDir = new File("xsl");

    public static File uploadDir = new File("upload");
    private static File exeFile;
    private static File dateDir;

    private static int port = 8080;

    private static AtomicInteger requestId;

    private static int currentDay = -1;

    static {
        try {
            xslt = comp.compile(new StreamSource(
                    new InputStreamReader(new FileInputStream(new File(xslDir, "tree.xsl")), "UTF-8"))).load();
        } catch (Throwable e) {
            log.error("Error initializing xslt", e);
        }
        exeFile = new File(execDir, "mgra.bin");
        if (!exeFile.exists()) {
            exeFile = new File(execDir, "mgra.exe");
        }
    }

    private static synchronized void updateDateDir() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day != currentDay) {
            dateDir = new File(new File(new File(uploadDir, Integer.toString(calendar.get(Calendar.YEAR))),
                        Integer.toString(calendar.get(Calendar.MONTH) + 1)),
                        Integer.toString(day));
            currentDay = day;
            if (dateDir.exists()) {
               requestId = new AtomicInteger(dateDir.list().length);
            } else {
               dateDir.mkdirs();
               requestId = new AtomicInteger(0);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            uploadDir = new File(args[0]);
        }

        if (args.length > 1)
            port = Integer.parseInt(args[1]);

        updateDateDir();

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

        Handler handler = new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                   String path = request.getPathInfo();
                   if (path.startsWith(REQUEST_START) ) { //"/file/")) {
                       log.debug("Handling request " + path);
                       File file = new File(uploadDir.getAbsolutePath(), path);
                       if (file.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
                           ServletOutputStream out = response.getOutputStream();
                           FileInputStream in = new FileInputStream(file);

                           byte[] buf = new byte[4048];
                           int count = 0;
                           while ((count = in.read(buf)) >= 0) {
                               out.write(buf, 0, count);
                           }
                           in.close();
                           out.close();
                       }
                       log.debug(path + " request processed.");
                       return;
                   }

                   final Properties properties = new Properties();
                   final File[] files = new File[1];
                   PrintWriter out = response.getWriter();
                   try {
                       uploadFilter.doFilter(request, response, new FilterChain() {
                           public void doFilter(ServletRequest wrapper, ServletResponse servletResponse) throws IOException, ServletException {
                               Enumeration uploads = wrapper.getAttributeNames();

                               while (uploads.hasMoreElements()) {
                                   String fileField = (String) uploads.nextElement();
                                   if ("genome".equals(fileField)) {
                                       files[0] = (File) wrapper.getAttribute(fileField);
                                   }
                               }

                               Enumeration parameters = wrapper.getParameterNames();
                               while (parameters.hasMoreElements()) {
                                   String name = (String) parameters.nextElement();
                                   String[] values = wrapper.getParameterValues(name);
                                   String concatValues = "";
                                   for (String value : values) {
                                       concatValues += value + " ";
                                   }
                                   properties.put(name, concatValues.trim());
                               }
                           }
                       });
                       response.setContentType("text/html");
                       response.setStatus(HttpServletResponse.SC_OK);
                       processRequest(out, properties, files[0]);
                       baseRequest.setHandled(true);
                       ((Request) request).setHandled(true);
                   } catch (Throwable e) {
                       log.error("Error processing request", e);
                       e.printStackTrace(out);
                   } finally {
                       for (int i = 0; i < files.length; i++) {
                           File file = files[i];
                           if (file != null && file.exists())
                               file.delete();
                       }
                       out.close();
                   }
               }
        };

        Server server = new Server(port);
        for (Connector connector: server.getConnectors()) {
            connector.setMaxIdleTime(2000000);
        }
        server.setHandler(handler);
        server.start();
    }

    private static void processRequest(final PrintWriter out, Properties properties, File genomeFileUpload) throws Exception {
        updateDateDir();

        File datasetDir;
        do {
            String dir = "request" + requestId.getAndIncrement();
            datasetDir = new File(dateDir, dir);
        } while (datasetDir.exists());
        datasetDir.mkdirs();

        String path = datasetDir.getCanonicalPath().replaceAll("\\\\", "/");

        int cur = path.length();
        for (int i = 0; i < 4; i++) {
            cur = path.lastIndexOf("/", cur - 1);
        }

        String treeLink = path.substring(cur + 1) + "/tree.html";

        out.println("<html><title>MGRA processing information</title><body>");
        response(out, "<pre>");

        response(out, "Create genome file...");
        createGENOME_FILE(genomeFileUpload, properties, datasetDir);

        response(out, "Generating CFG...");
        createCFG_FILE(datasetDir, properties);

        response(out, "Start MGRA algorithm...");
        String[] command = new String[]{exeFile.getAbsolutePath(), CFG_FILE_NAME};

        Process process = Runtime.getRuntime().exec(
                command,
                new String[]{}, datasetDir);

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

        response(out, "Generating results XML...");
        new TreeReader(new File(datasetDir, CFG_FILE_NAME));

        response(out, "Applying XSLT...");
        XdmNode source = getSource(processor, new File(datasetDir, "tree.xml"));
        Serializer serializer = new Serializer();
        serializer.setOutputProperty(Serializer.Property.METHOD, "html");
        serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
        serializer.setOutputFile(new File(datasetDir, "tree.html"));

        xslt.setInitialContextNode(source);
        xslt.setDestination(serializer);
        xslt.transform();

        synchronized (out) {
            out.println("Done.");
            out.println("</pre>");
            out.println("<p><a href=\"" +treeLink + "\">MGRA tree</a> Press this link if it doesn't works automatically.</p>");
            out.println("<script>document.location.href='" + treeLink + "'</script>");
            out.println("</body></html>");
            out.flush();
        }
    }

    private static void createGENOME_FILE(File genomeFileUpload, Properties properties, File datasetDir)
                throws FileNotFoundException, UnsupportedEncodingException {
        if (genomeFileUpload != null) {
            genomeFileUpload.renameTo(new File(datasetDir, GENOME_FILE));
        } else {
            PrintWriter genomeFile = createOutput(datasetDir, GENOME_FILE);

            if (getFormat(properties).equals("grimm")) {
                int genomeId = 1;
                String key = "genome" + genomeId;
                do {
                    String s = (String) properties.get(key);
                    genomeFile.println(s);
                    genomeId++;
                    genomeFile.println();
                    key = "genome" + genomeId;
                } while (properties.containsKey(key));
            } else {
                String key = "genome";
                String s = (String) properties.get(key);
                genomeFile.println(s);
            }

            genomeFile.close();
        }
    }

    private static void createCFG_FILE(File datasetDir, Properties properties)
               throws IOException {
        PrintWriter cfgFile = createOutput(datasetDir, CFG_FILE_NAME);
        cfgFile.println("[Genomes]");

        int aliasId = 1;
        String key = "alias" + aliasId;
        do {
            cfgFile.println(properties.get("name" + aliasId) + " " +  properties.get(key));
            aliasId++;
            key = "alias" + aliasId;
        } while (properties.containsKey(key));

        cfgFile.println("[Blocks]");
        cfgFile.println("format " + getFormat(properties));
        cfgFile.println("file genome.txt");

        cfgFile.println();

        cfgFile.println("[Trees]");
        cfgFile.println(properties.getProperty("trees"));
        cfgFile.println();

        cfgFile.println("[Algorithm]");
        cfgFile.println();

        cfgFile.println("stages " + properties.getProperty("stages"));
        cfgFile.println();

        boolean useTarget = "1".equals(properties.getProperty("useTarget"));

        if (useTarget) {
            cfgFile.println("target " + properties.getProperty("target"));
            cfgFile.println();
        }

        cfgFile.println("[Graphs]");
        cfgFile.println();

        cfgFile.println("filename stage");
        cfgFile.println();

        cfgFile.println("colorscheme set19");
        cfgFile.println();

        if (useTarget) {
            cfgFile.println("[Completion]");
            cfgFile.println(properties.getProperty("completion"));
            cfgFile.println();
        }

        cfgFile.close();
    }

    private static String getFormat(Properties proporties) {
        String ans = (String) proporties.get("useFormat");
        log.debug("Genome format detection: " + ans);
        return ans;
    }

    private static Thread listenOutput(InputStream inputStream, final PrintWriter out, final String type) {
        final BufferedReader input =
                new BufferedReader
                        (new InputStreamReader(inputStream));


        Thread outputThread = new Thread(new Runnable() {
            public void run() {
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        log.debug("MGRA " + type + " : " + line);
                        response(out, type + " : " + line);
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
        response(out, message);
        synchronized (out) {
            e.printStackTrace(out);
        }
    }

    private static void response(PrintWriter out, String message) {
        synchronized (out) {
            out.println(message);
            out.flush();
        }
    }

    private static PrintWriter createOutput(File datasetDir, String file) throws UnsupportedEncodingException, FileNotFoundException {
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(datasetDir, file)), "UTF-8"));
    }

    private static XdmNode getSource(Processor processor, File xmlFile) throws SaxonApiException {
        return processor.newDocumentBuilder().build(new StreamSource(
                xmlFile));
    }
}
