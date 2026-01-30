package mg.itu.ermite.framework.util;

/**
 * Classe représentant un fichier uploadé par le client.
 * 
 * FileData encapsule les données binaires d'un fichier uploadé ainsi que
 * les métadonnées associées (nom du fichier, extension).
 * 
 * Utilisation avec le framework :
 * - Les fichiers uploadés sont automatiquement convertis en objets FileData
 * - Les paramètres de type Map<String, List<FileData>> reçoivent les fichiers
 * - Le binding est effectué automatiquement par EndPointDetails
 * 
 * Exemple d'utilisation dans un contrôleur :
 * <pre>
 * @Controller
 * public class FileController {
 *     @UrlMapping(url = "/upload")
 *     @PostMapping
 *     public ModelView uploadFile(Map<String, List<FileData>> files) {
 *         if (files.containsKey("document")) {
 *             FileData file = files.get("document").get(0);
 *             
 *             // Traiter le fichier
 *             byte[] content = file.getBytes();
 *             String name = file.getFileName();
 *             String ext = file.getExtension();
 *         }
 *     }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see EndPointDetails
 */
public class FileData {
    
    /** Les données binaires du fichier */
    private byte[] bytes;
    
    /** Le nom complet du fichier (y compris extension) */
    private String fileName;

    /**
     * Constructeur par défaut.
     */
    public FileData() {
    }

    /**
     * Constructeur avec données et nom.
     * 
     * @param bytes les données binaires du fichier
     * @param fileName le nom du fichier
     */
    public FileData(byte[] bytes, String fileName) {
        this.bytes = bytes;
        this.fileName = fileName;
    }

    /**
     * Récupère les données binaires du fichier.
     * 
     * @return le contenu du fichier en bytes
     */
    public byte[] getBytes() {
        return bytes;
    }
    
    /**
     * Définit les données binaires du fichier.
     * 
     * @param bytes les données binaires
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    
    /**
     * Récupère le nom complet du fichier.
     * 
     * @return le nom du fichier avec extension (ex: "photo.jpg")
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Définit le nom du fichier.
     * 
     * @param fileName le nom du fichier
     */
    public void setExtension(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Extrait et retourne l'extension du fichier.
     * 
     * L'extension est la partie du nom après le dernier point.
     * 
     * @return l'extension du fichier (ex: "jpg") ou vide si pas d'extension
     */
    public String getExtension()
    {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
