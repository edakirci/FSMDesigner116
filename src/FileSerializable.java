import java.io.IOException;

public interface FileSerializable {

    void saveToFile(String filename) throws IOException;
    void loadFromFile(String filename) throws IOException;

}
