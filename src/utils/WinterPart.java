package utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class WinterPart {

    private final Part part;
    private final ServletContext context;  // Stocke le contexte

    public byte[] getBytes() throws IOException {
        return part.getInputStream().readAllBytes();
    }

    public String getSubmittedFileName() {
        return part.getSubmittedFileName();
    }

    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    public long getSize() {
        return part.getSize();
    }

    public boolean isEmpty() {
        return part.getSize() == 0;
    }


    public WinterPart(Part part, ServletContext context) {
        this.part = part;
        this.context = context;
    }
        
    public void save(String filePath) throws IOException {
        String projectRoot = context.getRealPath("/");
        // Create the full path
        Path path = Paths.get(projectRoot, "static", filePath, getSubmittedFileName());

        // Get the parent directory of the file
        Path parentDir = path.getParent();

        System.out.println("chemin complet : " + parentDir);

        // Create directories if they don't exist
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Write the file content to the specified path
        Files.write(path, getBytes());
    }
 
}
