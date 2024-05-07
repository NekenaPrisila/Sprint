package controller;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    protected void processRequested(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter out = resp.getWriter()){
            String url = req.getRequestURL().toString();
            out.println("URL :"+ url);
        } catch (Exception e) {
            PrintWriter pw = resp.getWriter();
            pw.println(e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
    throws ServletException, IOException {
        try {
            processRequested(req, resp);
        } catch (Exception e) {
            PrintWriter pw = resp.getWriter();
            pw.println(e);
        } 
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
    throws ServletException, IOException {
        try {
            processRequested(req, resp);
        } catch (Exception e) {
            PrintWriter pw = resp.getWriter();
            pw.println(e);
        } 
    }

}