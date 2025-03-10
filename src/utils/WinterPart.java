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
    private final ServletContext context;

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
        Path path;
        
        // Vérifier si le filePath est un chemin absolu
        if (Paths.get(filePath).isAbsolute()) {
            path = Paths.get(filePath, getSubmittedFileName());
        } else {
            String projectRoot = context.getRealPath("/");
            path = Paths.get(projectRoot, "static", filePath, getSubmittedFileName());
        }
    
        // Obtenir le répertoire parent du fichier
        Path parentDir = path.getParent();
    
        System.out.println("Chemin complet : " + path);
    
        // Créer les répertoires s'ils n'existent pas
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    
        // Écrire le contenu du fichier
        Files.write(path, getBytes());
    }    
 
}
