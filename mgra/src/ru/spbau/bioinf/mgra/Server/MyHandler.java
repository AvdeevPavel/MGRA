package ru.spbau.bioinf.mgra.Server;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import ru.spbau.bioinf.mgra.DataFile.BlocksInformation;
import ru.spbau.bioinf.mgra.DataFile.Config;
import ru.spbau.bioinf.mgra.Parser.Transformer;
import ru.spbau.bioinf.mgra.MyException.LongUniqueName;
import ru.spbau.bioinf.mgra.Tree.TreeReader;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class MyHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(JettyServer.class);
    private static File dateDir;
    private static AtomicInteger requestId;
    private static int currentDay = -1;

    private static synchronized void updateDateDir() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day != currentDay) {
            dateDir = new File(new File(new File(JettyServer.uploadDir, Integer.toString(calendar.get(Calendar.YEAR))),
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

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getPathInfo();
        log.debug("Request name " + path);

        if (path.equals("/")) {
            final Properties properties = new Properties();
            final File[] files = new File[1];
            PrintWriter out = response.getWriter();
            try {
                JettyServer.uploadFilter.doFilter(request, response, new FilterChain() {
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
        } else if (path.contains("showtree.html")) {
            log.debug("Handling request " + path);
            updateDateDir();

            File datasetDir;
            do {
                String dir = JettyServer.REQUEST_START + requestId.getAndIncrement();
                datasetDir = new File(dateDir, dir);
            } while (datasetDir.exists());
            datasetDir.mkdirs();

            String pathDir = datasetDir.getCanonicalPath().replaceAll("\\\\", "/");

            String[] data = null;
            if (request.getParameterValues("trees")[0] != null) {
                data = request.getParameterValues("trees")[0].split(";");
            }

            try {
                TreeReader.createShowTree(data, pathDir);
                applyXslt("showtree.xml", "showtree.html", pathDir, 4);
                writeInRequest(new File(pathDir, "showtree.html"), response);
            } catch (Exception e) {
                log.error("Problem with create file showtree.html " + e);
            }
            log.debug(path + " request processed.");
        } else if (path.contains("tree.html")) {
            log.debug("Handling request " + path);
            writeInRequest(new File(JettyServer.uploadDir.getAbsolutePath(), path), response);
            log.debug(path + " request processed.");
        } else if (path.contains("_gen.html") || path.contains("_trs.html")) {
            String nameFile = path.substring(path.lastIndexOf("/") + 1);
            String pathDirectory = path.substring(0, path.lastIndexOf("/"));
            int width = Integer.valueOf(request.getParameterValues("width")[0]);
            try {
                String requestDirectory = JettyServer.uploadDir.getAbsolutePath() + pathDirectory;
                Config config = new Config(requestDirectory, JettyServer.CFG_FILE_NAME, width);
                if (nameFile.substring(nameFile.indexOf("_") + 1, nameFile.indexOf(".")).equals("gen")) {
                    Transformer.createGenome(nameFile.substring(0, nameFile.indexOf("_")), config);
                    applyXslt(nameFile.substring(0, nameFile.indexOf(".") + 1) + "xml", nameFile, requestDirectory, 1);
                } else if (nameFile.substring(nameFile.indexOf("_") + 1, nameFile.indexOf(".")).equals("trs")) {
                    Transformer.createTransformationToXml(nameFile.substring(0, nameFile.indexOf("_")), config);
                    applyXslt(nameFile.substring(0, nameFile.indexOf(".") + 1) + "xml", nameFile, requestDirectory, 2);
                }
                writeInRequest(new File(JettyServer.uploadDir.getAbsolutePath(), path), response);
                log.debug(nameFile + " created. " + path + " request processed.");
            } catch (Exception e) {
                log.error("Problem with create file " + nameFile + " " + e);
            }
        } else if (path.contains(".html")) {
            log.debug("Handling request " + path);
            int idRear = Integer.valueOf(path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")));
            String nameTransf = path.substring(path.lastIndexOf('/', path.lastIndexOf('/') - 1) + 1, path.lastIndexOf('_'));
            String pathDirectory = path.substring(0, path.lastIndexOf("/", path.lastIndexOf('/') - 1));
            int width = Integer.valueOf(request.getParameterValues("width")[0]);
            String requestDirectory = JettyServer.uploadDir.getAbsolutePath() + pathDirectory;
            Config config = new Config(requestDirectory, JettyServer.CFG_FILE_NAME, width);
            String answer = Transformer.createTransformationToPng(nameTransf, config, idRear);
            if (answer != null) {
                ServletOutputStream out = response.getOutputStream();
                out.write(answer.getBytes(), 0, answer.getBytes().length);
                out.close();
            }
            log.debug(path + " request processed.");
        } else if (path.contains("download")) {
            log.debug("Download request " + path);
            String nameFile = path.substring(path.indexOf("download") + "download".length() + 1);
            String pathDirectory = path.substring(0, path.indexOf("download"));
            response.setHeader("Content-Disposition", "attachment;filename=\"" + nameFile + "\"");
            writeInRequest(new File(JettyServer.uploadDir, pathDirectory + "/" + nameFile), response);
            log.debug(path + " download request processed.");
        } else if (path.contains("lib")) {
            log.debug("Handling request " + path);
            writeInRequest(new File(JettyServer.libDir, path.substring(path.lastIndexOf('/'))), response);
            log.debug(path + " request processed.");
        } else if (path.contains(JettyServer.REQUEST_START)) {
            log.debug("Handling request " + path);
            writeInRequest(new File(JettyServer.uploadDir.getAbsolutePath(), path), response);
            log.debug(path + " request processed.");
        }
    }

    private static void writeInRequest(File file, HttpServletResponse response) throws IOException {
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

    private static void processRequest(final PrintWriter out, Properties properties, File genomeFileUpload) throws Exception {
        updateDateDir();

        File datasetDir;
        do {
            String dir = JettyServer.REQUEST_START + requestId.getAndIncrement();
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
            config = new Config(datasetDir.getAbsolutePath(), properties, JettyServer.CFG_FILE_NAME);
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

        responseStage(out, "Start create genome file.");
        try {
            BlocksInformation.writeGenomeFile(genomeFileUpload, properties, datasetDir, config, JettyServer.GENOME_FILE_NAME);
            responseInformation(out, "Genome file created");
        } catch (IOException e) {
            responseErrorServer(out, "Problem to create genome file.");
            log.error("Problem to create genome file ", e);
            throw e;
        }

        responseStage(out, "Start MGRA algorithm");
        try {
            JettyServer.runMgraTool(config, out);
        } catch (Exception e) {
            responseErrorServer(out, "Problem with MGRA tool, sorry.");
            log.error("Problem with MGRA tool", e);
            throw e;
        }

        responseStage(out, "Start to transform output data.");
        int i;
        if (config.isUseTarget() && config.getTarget() != null) {
            TreeReader.createTarget(config, out);
            i = 3;
        } else {
            TreeReader.createFullPage(config, out);
            i = 0;
        }

        responseStage(out, "Applying XSLT to XML for create html.");
        try {
            applyXslt("tree.xml", "tree.html", datasetDir.getAbsolutePath(), i);
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
                    "var delay = 10000;\n" +
                    "setTimeout(\"document.location.href='" + treeLink + "'\", delay);\n" +
                    "</script>");
            out.println("Read log. After 10 seconds will occur automatically.");
            out.println("<p><a href=\"" +treeLink + "\">MGRA tree</a> Press this link if it doesn't works automatically or you do not want to wait.</p>");
            out.println("</body></html>");
            out.flush();
        }
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

    private static void applyXslt(String fileXml, String fileHtml, String datasetDir, int i) throws SaxonApiException {
        XdmNode source = getSource(JettyServer.processor, new File(datasetDir, fileXml));
        Serializer serializer = new Serializer();
        serializer.setOutputProperty(Serializer.Property.METHOD, "html");
        serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
        serializer.setOutputFile(new File(datasetDir, fileHtml));

        JettyServer.xslt[i].setInitialContextNode(source);
        JettyServer.xslt[i].setDestination(serializer);
        JettyServer.xslt[i].transform();
    }

    private static XdmNode getSource(Processor processor, File xmlFile) throws SaxonApiException {
        return processor.newDocumentBuilder().build(new StreamSource(xmlFile));
    }
}
