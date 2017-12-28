package DAWG;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DAWGListNode implements DAWGNode {

    private List<DAWGEdge> children;

    private boolean terminus;

    DAWGListNode() {
        this.children = new ArrayList<DAWGEdge>();
        this.terminus = false;
    }

    DAWGListNode(boolean terminus) {
        this.children = new ArrayList<DAWGEdge>();
        this.terminus = terminus;
    }

    @Override
    public DAWGNode getChild(char edge) {

        for (DAWGEdge child : children) {
            if (child.edge == edge) {
                return child.node;
            }
        }

        return null;
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
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
    public int numChildren() {
        return children.size();
    }

    @Override
    public void setChild(char edge, DAWGNode child) {
        for (DAWGEdge childEdge : children) {
            if (childEdge.edge == edge) {
                childEdge.node = child;
                return;
            }
        }
        children.add(new DAWGEdge(edge, child));
    }

    @Override
    public Iterator<DAWGEdge> iterator() {
        return children.iterator();
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) return false;
        if (!(o instanceof DAWGNode)) return false;

        DAWGNode that = (DAWGNode) o;

        if (this.terminus != that.isTerminal()) return false;

        if (this.numChildren() != that.numChildren()) return false;

        for (DAWGEdge child : children) {
            if (that.getChild(child.edge) == null) return false;
        }

        for (DAWGEdge child : children) {
            if (!child.node.equals(that.getChild(child.edge))) return false;
        }

        return true;
    }

}
