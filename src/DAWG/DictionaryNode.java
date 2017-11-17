package DAWG;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface DictionaryNode {

    boolean containsWord(String word);

    EdgeAndNodeCounter countEdgesAndNodes();

    void writeOut(OutputStream outputStream) throws FileNotFoundException, UnsupportedEncodingException;

    List<String> getAllWords();

    @Override
    boolean equals(Object o);
}
