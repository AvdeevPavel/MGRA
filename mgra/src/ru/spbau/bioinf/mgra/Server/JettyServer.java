package ru.spbau.bioinf.mgra.Server;

import net.sf.saxon.s9api.*;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlets.MultiPartFilter;
import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Drawer.Drawer;
import ru.spbau.bioinf.mgra.MyException.LongUniqueName;
import ru.spbau.bioinf.mgra.Parser.Transformer;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class JettyServer {

    private static final Logger log = Logger.getLogger(JettyServer.class);

    private static final ServletContext servletContext = new UploadServleContext();
    private static MultiPartFilter uploadFilter;

    private static final Processor processor = new Processor(false);
    private static final XsltCompiler comp = processor.newXsltCompiler();
    private static XsltTransformer xslt;

    private static String REQUEST_START = "request"; //"/file/" "/2012/";
    private static final String GENOME_FILE = "genome.txt";

    private static final File execDir = new File("exec");
    private static final File xslDir = new File("xsl");

    public static File uploadDir = new File("upload");
    public static File exeFile;
    private static File dateDir;

    private static int port = 8080;

    private static AtomicInteger requestId;

    private static int currentDay = -1;

    static {
        try {
            xslt = comp.compile(new StreamSource(new InputStreamReader(new FileInputStream(new File(xslDir, "tree.xsl")), "UTF-8"))).load();
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

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        if (args.length > 2) {
            REQUEST_START = args[2];
        }

        if (args.length > 3) {
            Drawer.setThreshold(Double.parseDouble(args[2]));
        }

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
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                   String path = request.getPathInfo();
                   log.debug("Request name " + path);
                   if (path.contains(REQUEST_START) ) {
                       log.debug("Handling request " + path);
                       //path = path.substring("/file".length());
                       File file = new File(uploadDir.getAbsolutePath(), path);
                       if (file.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
                           ServletOutputStream out = response.getOutputStream();
                           FileInputStream in = new FileInputStream(file);

                           byte[] buf = new byte[4048];
                           int count;
                           while ((count = in.read(buf)) >= 0) {
                               out.write(buf, 0, count);
                           }
                           in.close();
                           out.close();
                       }
                       log.debug(path + " request processed.");
                       return;
                   } else if (path.equals("/")) {
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
                            response(out, "<font style=\"font-size:14pt;\"><p>SUMMARY: Error processing request. Read the information in the log.</p>If there are no problems with input data please contact us(maxal@cse.sc.edu or/and avdeevp@gmail.com).</font>");
                            log.error("Error processing request", e);
                        } finally {
                            for (int i = 0; i < files.length; i++) {
                                File file = files[i];
                                if (file != null && file.exists())
                                    file.delete();
                            }
                            out.close();
                        }
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

    private static void processRequest(final PrintWriter out, Properties properties, File genomeFileUpload) throws IOException, InterruptedException, SaxonApiException, LongUniqueName {
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

        out.println("<html><title>MGRA processing information</title>");
        response(out, "<body><pre>");

        responseStage(out, "Greating CFG file.");
        Config config;
        try {
            config = new Config(datasetDir.getAbsolutePath(), properties);
            config.createFile(true);
            responseInformation(out, "CFG file created");
        } catch (IOException e) {
            responseErrorServer(out, "Problem to create CFG file");
            log.error("Problem to create CFG file ", e);
            throw e;
        } catch (LongUniqueName e) {
            responseErrorUser(out, "Your name genome is not valid. Max length name = 1. Please check your input data");
            log.error("Problem with name in genome", e);
            throw new LongUniqueName();
        }

        BlocksInformation blocksInformation;
        responseStage(out, "Start create genome file.");
        try {
            blocksInformation = createGENOME_FILE(genomeFileUpload, properties, datasetDir, config);
            responseInformation(out, "Genome file created");
        } catch (IOException e) {
            responseErrorServer(out, "Problem to create genome file.");
            log.error("Problem to create genome file ", e);
            throw e;
        }

        responseStage(out, "Start MGRA algorithm");
        String[] command = new String[]{exeFile.getAbsolutePath(), config.getNameFile()};

        Process process = Runtime.getRuntime().exec(command, new String[]{}, datasetDir);

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

        responseStage(out, "Start to transform output data.");
        new Transformer(config, blocksInformation, out);

        responseStage(out, "Applying XSLT to XML for create html.");
        try {
            XdmNode source = getSource(processor, new File(datasetDir, "tree.xml"));
            Serializer serializer = new Serializer();
            serializer.setOutputProperty(Serializer.Property.METHOD, "html");
            serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
            serializer.setOutputFile(new File(datasetDir, "tree.html"));

            xslt.setInitialContextNode(source);
            xslt.setDestination(serializer);
            xslt.transform();
            responseInformation(out, "XSL transformation done");
        } catch (SaxonApiException e) {
            responseErrorServer(out, "Can not end XSL transformation, sorry.");
            log.error("Can not end XST transformation", e);
            throw e;
        }

        synchronized (out) {
            out.println("<font style=\"font-size:14pt;\">All stage done. You can see tree.</font>");
            out.println("</pre>");
            out.println("<script language = 'javascript'>\n" +
                    "var delay = 15000;\n" +
                    "setTimeout(\"document.location.href='" + treeLink + "'\", delay);\n" +
                    "</script>");
            out.println("Read log. After 15 seconds will occur automatically.");
            out.println("<p><a href=\"" +treeLink + "\">MGRA tree</a> Press this link if it doesn't works automatically or you do not want to wait.</p>");
            out.println("</body></html>");
            out.flush();
        }
    }

    private static BlocksInformation createGENOME_FILE(File genomeFileUpload, Properties properties, File datasetDir, Config config) throws IOException {
        if (genomeFileUpload != null) {
            genomeFileUpload.renameTo(new File(datasetDir, GENOME_FILE));
        } else {
            PrintWriter genomeFile = createOutput(datasetDir, GENOME_FILE);
            if (config.getInputFormat().equals("grimm")) {
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

        config.resolveFormat();
        if (config.getInputFormat().equals("infercars")) {
            return readBloksInformation(new File(datasetDir.getAbsolutePath() + "/genome.txt"), config);
        } else {
            return null;
        }
    }

    private static BlocksInformation readBloksInformation(File file, Config config) throws IOException {
        BlocksInformation output = new BlocksInformation();

        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String s;
        String nameBlock = "";
        HashMap<Character, Long> map = new HashMap<Character, Long>();

        while((s = input.readLine()) != null) {
            s = s.trim();
            if (s.isEmpty()) {
                if (nameBlock != null && !nameBlock.isEmpty())  {
                    output.putHashMap(nameBlock, map);
                    map.clear();
                    nameBlock = "";
               }
            } else if (s.startsWith(">")) {
                nameBlock = s.substring(1);
            } else if (s.startsWith("#")) {
                continue;
            } else {
                int index = s.indexOf(".");
                Character key = config.getAliasName(s.substring(0, index));
                Long left = new Long(s.substring(s.indexOf(":") + 1, s.indexOf("-")));
                Long right = new Long(s.substring(s.indexOf("-") + 1, s.indexOf(" ")));
                map.put(key, right - left);
            }
        }
        input.close();

        return output;
    }

    public static Thread listenOutput(InputStream inputStream, final PrintWriter out, final String type) {
        final BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));

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

    public static void response(PrintWriter out, String message) {
        synchronized (out) {
            out.println(message);
            out.flush();
        }
    }

    public static void responseErrorUser(PrintWriter out, String message) {
        synchronized (out) {
            out.println("<font color = \"red\"><strong>USER ERROR:</strong> " + message + "</font>");
            out.flush();
        }
    }

    public static void responseErrorServer(PrintWriter out, String message) {
        synchronized (out) {
            out.println("<font color = \"red\"><u>SERVER ERROR:</u> " + message + "</font>");
            out.flush();
        }
    }

    public static void responseStage(PrintWriter out, String message) {
        synchronized (out) {
            out.println("<strong>STAGE:</strong> " + message);
            out.flush();
        }
    }

    public static void responseInformation(PrintWriter out, String message) {
        synchronized (out) {
            out.println("<font color = \"blue\">INFORAMTION:</font>" + message);
            out.flush();
        }
    }

    private static void logError(IOException e, PrintWriter out, String message) {
        log.error(message, e);
        response(out, message);
        synchronized (out) {
            e.printStackTrace(out);
        }
    }

    private static PrintWriter createOutput(File datasetDir, String file) throws UnsupportedEncodingException, FileNotFoundException {
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(datasetDir, file)), "UTF-8"));
    }

    private static XdmNode getSource(Processor processor, File xmlFile) throws SaxonApiException {
        return processor.newDocumentBuilder().build(new StreamSource(xmlFile));
    }
}
