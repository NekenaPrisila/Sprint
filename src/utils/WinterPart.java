package utils;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WinterPart {

    private final Part part;

    public WinterPart(Part part) {
        this.part = part;
    }

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

    public void save(String directoryPath) throws IOException {
        // Récupérer le nom du fichier soumis
        String fileName = getSubmittedFileName();
        System.out.println("Nom du fichier: " + fileName); // Log du nom du fichier
    
        // Créer le chemin complet pour le fichier à enregistrer dans le répertoire spécifié
        Path path = Paths.get(directoryPath, fileName);
        System.out.println("Chemin complet du fichier: " + path.toString()); // Log du chemin complet
    
        // Obtenir le répertoire parent du fichier
        Path parentDir = path.getParent();
    
        // Créer les répertoires si ils n'existent pas
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            System.out.println("Répertoires créés: " + parentDir.toString()); // Log de la création du répertoire
        }
    
        // Écrire le contenu du fichier dans le chemin spécifié
        Files.write(path, getBytes());
        System.out.println("Fichier sauvegardé avec succès à: " + path.toString()); // Log de la sauvegarde du fichier
    }    
    
}
