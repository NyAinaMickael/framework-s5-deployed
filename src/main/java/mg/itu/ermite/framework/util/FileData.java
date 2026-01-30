package mg.itu.ermite.framework.util;

public class FileData {
    private byte[] bytes;
    private String fileName;

    public FileData() {
    }

    public FileData(byte[] bytes, String fileName) {
        this.bytes = bytes;
        this.fileName = fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    public String getFileName() {
        return fileName;
    }
    public void setExtension(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension()
    {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
