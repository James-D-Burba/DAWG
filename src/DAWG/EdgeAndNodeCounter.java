package DAWG;

public class EdgeAndNodeCounter {

    private int edgeCount;
    private int nodeCount;

    public int getEdgeCount(){
        return edgeCount;
    }

    public int getNodeCount(){
        return nodeCount;
    }

    public EdgeAndNodeCounter() {
        edgeCount = 0;
        nodeCount = 0;
    }

    public void addEdges(int numEdges) {
        this.edgeCount += numEdges;
    }

    public void addNodes(int numNodes) {
        this.nodeCount += numNodes;
    }

}
