package DAWG;

import java.io.*;
import java.util.*;

public class DAWGArrayNode implements DAWGNode {

    //The outgoing edges of this node and the node nodes they lead to.
    private char[] childEdges;
    private DAWGNode[] childNodes;

    //is this node the end of a word?
    private boolean terminus;

    DAWGArrayNode() {
        childEdges = new char[26];
        childNodes = new DAWGArrayNode[26];
        terminus = false;
    }

    DAWGArrayNode(int numChildren, boolean terminus) {
        this.terminus = terminus;
        childEdges = new char[numChildren];
        childNodes = new DAWGArrayNode[numChildren];
    }

    @Override
    public boolean hasChildren() {
        return childEdges.length >= 1 && !(childEdges[0] == 0);
    }

    @Override
    public boolean isTerminal() {
        return terminus;
    }

    @Override
    public void setTerminus(boolean terminus) {
        this.terminus = terminus;
    }

    @Override
    public void setChild(char edge, DAWGNode node) {

        for (int i = 0; i < childEdges.length; i++) {
            if (childEdges[i] == 0) {
                childEdges[i] = edge;
                childNodes[i] = node;
                return;
            } else if (childEdges[i] == edge) {
                childNodes[i] = node;
                return;
            }
        }
    }

    @Override
    public DAWGNode getChild(char edge) {

        for (int i = 0; i < childEdges.length; i++) {
            if (childEdges[i] == edge) return childNodes[i];
            if (childEdges[i] == 0) return null;
        }
        return null;

    }

    @Override
    public int numChildren() {
        int numChildren = 0;
        for (char edge : childEdges) {
            if (edge == 0) break;
            else numChildren++;
        }
        return numChildren;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) return false;
        if (!(o instanceof DAWGArrayNode)) return false;

        DAWGArrayNode that = (DAWGArrayNode) o;

        if (this.terminus != that.terminus) return false;

        if (this.numChildren() != that.numChildren()) return false;

        for (int i = 0; i < childEdges.length; i++) {

            if (childEdges[i] == 0) break;

            if (that.getChild(childEdges[i]) == null) return false;

        }

        for (int i = 0; i < childEdges.length; i++) {

            if (childEdges[i] == 0) break;

            if (!childNodes[i].equals(that.getChild(childEdges[i]))) return false;

        }

        return true;
    }

    @Override
    public Iterator<DAWGEdge> iterator() {
        return new childIterator(childEdges, childNodes);
    }

    private class childIterator implements Iterator<DAWGEdge> {

        private int current;
        char[] childEdges;
        DAWGNode[] childNodes;

        childIterator(char[] childEdges, DAWGNode[] childNodes) {
            current = 0;
            this.childEdges = childEdges;
            this.childNodes = childNodes;
        }

        @Override
        public boolean hasNext() {
            return current < childNodes.length && childNodes[current] != null;
        }

        @Override
        public DAWGEdge next() {
            DAWGEdge child = new DAWGEdge(childEdges[current], childNodes[current]);
            current++;
            return child;
        }
    }

}
