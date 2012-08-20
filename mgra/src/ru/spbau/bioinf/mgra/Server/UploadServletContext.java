package ru.spbau.bioinf.mgra.Server;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: student
 * Date: 7/13/12
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class UploadServletContext implements ServletContext {
    public String getContextPath() {
        return null;
    }

    public ServletContext getContext(String s) {
        return null;
    }

    public int getMajorVersion() {
        return 0;
    }

    public int getMinorVersion() {
        return 0;
    }

    public String getMimeType(String s) {
        return null;
    }

    public Set getResourcePaths(String s) {
        return null;
    }

    public URL getResource(String s) throws MalformedURLException {
        return null;
    }

    public InputStream getResourceAsStream(String s) {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    public RequestDispatcher getNamedDispatcher(String s) {
        return null;
    }

    public Servlet getServlet(String s) throws ServletException {
        return null;
    }

    public Enumeration getServlets() {
        return null;
    }

    public Enumeration getServletNames() {
        return null;
    }

    public void log(String s) {
    }

    public void log(Exception e, String s) {

    }

    public void log(String s, Throwable throwable) {

    }

    public String getRealPath(String s) {
        return null;
    }

    public String getServerInfo() {
        return null;
    }

    public String getInitParameter(String s) {
        return null;
    }

    public Enumeration getInitParameterNames() {
        return null;
    }

    public Object getAttribute(String s) {
        //tempdir=(File)filterConfig.getServletContext().getAttribute("javax.servlet.context.tempdir");
        return JettyServer.uploadDir;
    }

    public Enumeration getAttributeNames() {
        return null;
    }

    public void setAttribute(String s, Object o) {
    }

    public void removeAttribute(String s) {
    }

    public String getServletContextName() {
        return null;
    }
}