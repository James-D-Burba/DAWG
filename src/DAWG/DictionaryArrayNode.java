package DAWG;

import java.io.*;
import java.util.*;

public class DictionaryArrayNode implements DictionaryNode {

    //The outgoing edges of this node and the child nodes they lead to.
    private char[] childEdges;
    private DictionaryArrayNode[] childNodes;

    //is this node the end of a word?
    private boolean terminus;

    private DictionaryArrayNode() {
        childEdges = new char[26];
        childNodes = new DictionaryArrayNode[26];
        terminus = false;
    }

    private DictionaryArrayNode(int numChildren, boolean terminus) {
        this.terminus = terminus;
        childEdges = new char[numChildren];
        childNodes = new DictionaryArrayNode[numChildren];
    }

    //builds the dictionary from a list of words
    public DictionaryArrayNode(List<String> words) {
        terminus = false;
        childEdges = new char[26];
        childNodes = new DictionaryArrayNode[26];

        int counter = 1;

        HashMap<DictionaryArrayNode, Character> lastEdges = new HashMap<>();

        //words need to be added to the dictionary in alphabetical order.
        Collections.sort(words);

        ArrayList<DictionaryArrayNode> registry = new ArrayList<>();

        for (String word : words) {
            System.out.println("adding word " + counter++ + " of " + words.size() + " " + word);
            DictionaryArrayNode lastState;
            DictionaryArrayNode currentState = this;
            String currentSuffix = word;

            DictionaryArrayNode nextNode;
            while ((nextNode = currentState.getChild(currentSuffix.charAt(0))) != null) {
                currentState = nextNode;
                currentSuffix = currentSuffix.substring(1, currentSuffix.length());
            }

            lastState = currentState;
            if (lastState.hasChildren()) {
                replaceOrRegister(lastState, registry, lastEdges);
            }

            while (currentSuffix.length() > 0) {
                DictionaryArrayNode nextState = new DictionaryArrayNode();
                currentState.addChild(currentSuffix.charAt(0), nextState);
                lastEdges.put(currentState, currentSuffix.charAt(0));
                currentState = nextState;
                currentSuffix = currentSuffix.substring(1, currentSuffix.length());
            }
            currentState.terminus = true;
        }
        replaceOrRegister(this, registry, lastEdges);
    }

    private boolean hasChildren() {

        if (childEdges.length < 1) return false;

        return !(childEdges[0] == 0);
    }

    //This constructor builds the dictionary from a an InputStream.
    //The input should be in the form that the writeOut method outputs.
    public DictionaryArrayNode(InputStream input) throws InvalidDictionaryFormatException {

        Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(input)));
        scanner.useDelimiter("");
        HashMap<Integer, DictionaryArrayNode> hash = new HashMap<>();

        if (scanner.next().charAt(0) != 'A') {
            throw new InvalidDictionaryFormatException();
        }

        hash.put(buildNextInt(scanner), this);

        //eat this token since we know it will be a '#' anyway
        scanner.next();

        int numChildren = buildNextInt(scanner);
        childNodes = new DictionaryArrayNode[numChildren];
        childEdges = new char[numChildren];
        terminus = false;

        buildHelper(hash, scanner);

    }

    //this is called by DictionaryArrayNode(InputStream). It recursively builds the dictionary from the input
    private void buildHelper(HashMap<Integer, DictionaryArrayNode> registry, Scanner scanner) {

        while (true) {

            char action = scanner.next().charAt(0);
            char letter;

            //if action isn't '/', that means it's a letter. '/' is the only action not preceded by a letter.
            if (action != '/') {
                letter = action;
                action = scanner.next().charAt(0);
            } else {
                //if the action is '/', that means we're done adding children to this node. So we go up a level.
                return;
            }

            //this edge points to a new node
            if (action == '\\') {
                int newNodeID = buildNextInt(scanner);
                boolean newNodeTerminus = scanner.next().charAt(0) == '*';
                int newNodeNumChildren = buildNextInt(scanner);
                DictionaryArrayNode newChild = new DictionaryArrayNode(newNodeNumChildren, newNodeTerminus);
                registry.put(newNodeID, newChild);
                addChild(letter, newChild);
                newChild.buildHelper(registry, scanner);
            }

            //this edge points to a node that already exists
            else if (action == '>') {
                int nextNodeID = buildNextInt(scanner);
                addChild(letter, registry.get(nextNodeID));
            }
        }
    }

    //this just gets the next integer in the scanner. Since the delimiter is "" it only reads one digit at a time.
    private int buildNextInt(Scanner scanner) {
        int num = 0;
        while (scanner.hasNextInt()) {
            num = num * 10 + scanner.nextInt();
        }
        return num;
    }

    private void replaceOrRegister(DictionaryArrayNode node, List<DictionaryArrayNode> registry, HashMap<DictionaryArrayNode, Character> lastEdges) {
        DictionaryArrayNode child = node.getChild(lastEdges.get(node));
        if (child.hasChildren()) {
            replaceOrRegister(child, registry, lastEdges);
        }
        for (DictionaryArrayNode path : registry) {
            if (child.equals(path)) {
                //need to replace the old one instead of just adding a new one.
                node.replaceChild(lastEdges.get(node), path);
                return;
            }
        }
        registry.add(child);
    }

    private void addChild(char edge, DictionaryArrayNode node) {
        for (int i = 0; i < childEdges.length; i++) {
            if (childEdges[i] == 0) {
                childEdges[i] = edge;
                childNodes[i] = node;
                return;
            }
        }
    }

    private void replaceChild(char edge, DictionaryArrayNode node) {

        for (int i = 0; i < childEdges.length; i++) {
            if (childEdges[i] == edge) {
                childNodes[i] = node;
                return;
            }
        }

    }

    public boolean containsWord(String word) {

        DictionaryArrayNode currentNode = this;

        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);

            DictionaryArrayNode nextNode;
            if ((nextNode = currentNode.getChild(letter)) != null) {
                currentNode = nextNode;
            } else return false;

            if (i == word.length() - 1) {
                return currentNode.terminus;
            }

        }

        return false;

    }

    public DictionaryArrayNode getChild(char edge) {

        for (int i = 0; i < childEdges.length; i++) {
            if (childEdges[i] == edge) return childNodes[i];
            if (childEdges[i] == 0) return null;
        }
        return null;

    }


    //returns an object which contains a count of all the edges and nodes including and beneath this node.
    @Override
    public EdgeAndNodeCounter countEdgesAndNodes() {
        return countEdgesAndNodes(new EdgeAndNodeCounter(), new ArrayList<>());
    }

    private EdgeAndNodeCounter countEdgesAndNodes(EdgeAndNodeCounter counter, ArrayList<DictionaryArrayNode> registry) {

        //count this node
        counter.addNodes(1);

        //count all the edges leading out of this node
        counter.addEdges(numChildren());

        for (int i = 0; i < childEdges.length; i++) {

            if (childEdges[i] == 0) break;

            boolean found = false;

            for (DictionaryArrayNode foundNode : registry) {
                if (childNodes[i] == foundNode) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                registry.add(childNodes[i]);
                childNodes[i].countEdgesAndNodes(counter, registry);
            }

        }

        return counter;

    }

    @Override
    public void writeOut(OutputStream output) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(output);
        //need to mark this as using the Array format
        writer.print('A');
        HashMap<DictionaryArrayNode, Integer> registry = new HashMap<>();
        writeHelper(registry, writer);
        writer.close();
    }

    private void writeHelper(HashMap<DictionaryArrayNode, Integer> registry, PrintWriter writer) {

        int nodeID = registry.size();

        int numChildren = numChildren();

        writer.print(nodeID);
        if (this.terminus) writer.print("*");
        else writer.print("#");
        writer.print(numChildren);
        registry.put(this, nodeID);

        for (int i = 0; i < numChildren; i++) {
            char edge = childEdges[i];
            DictionaryArrayNode node = childNodes[i];
            if (!registry.containsKey(node)) {
                writer.print(edge + "\\");
                node.writeHelper(registry, writer);
            } else {
                writer.print(edge + ">" + registry.get(node));
            }
        }

        writer.print("/");

    }

    private int numChildren() {
        int numChildren = 0;
        for (char edge : childEdges) {
            if (edge == 0) break;
            else numChildren++;
        }
        return numChildren;
    }

    @Override
    public List<String> getAllWords() {

        List<String> words = new ArrayList<>();
        List<Character> word = new ArrayList<>();

        return allWordGetter(words, word);

    }

    private List<String> allWordGetter(List<String> words, List<Character> word) {

        if (this.terminus) {
            words.add(charListToString(word));
        }

        int numChildren = numChildren();

        for (int i = 0; i < numChildren; i++) {
            word.add(childEdges[i]);
            childNodes[i].allWordGetter(words, word);
        }

        if (word.size() > 0)
            word.remove(word.size() - 1);

        return words;

    }

    private String charListToString(List<Character> charList) {

        StringBuilder word = new StringBuilder();

        for (Character letter : charList) {
            word.append(letter);
        }

        return word.toString();

    }

    @Override
    public boolean equals(Object o) {

        if (o == null) return false;
        if (!(o instanceof DictionaryArrayNode)) return false;

        DictionaryArrayNode that = (DictionaryArrayNode) o;

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


}
