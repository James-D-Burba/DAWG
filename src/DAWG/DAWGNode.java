package DAWG;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

public interface DAWGNode extends Iterable<DAWGEdge> {

    DAWGNode getChild(char edge);

    boolean hasChildren();

    boolean isTerminal();

    void setTerminus(boolean terminus);

    void setChild(char edge, DAWGNode child);

    int numChildren();

    @Override
    boolean equals(Object o);
}
