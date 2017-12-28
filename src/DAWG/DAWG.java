package DAWG;

import java.io.*;
import java.util.*;

public class DAWG {

    private DAWGNode root;

    //builds the dictionary from a list of words
    public DAWG(List<String> words) {

        root = new DAWGArrayNode();

        int counter = 1;

        HashMap<DAWGNode, Character> lastEdges = new HashMap<>();

        //words need to be added to the dictionary in alphabetical order.
        Collections.sort(words);

        ArrayList<DAWGNode> registry = new ArrayList<>();

        for (String word : words) {
            System.out.println("adding word " + counter++ + " of " + words.size() + " " + word);
            DAWGNode lastState;
            DAWGNode currentState = root;
            String currentSuffix = word;

            DAWGNode nextNode;
            while ((nextNode = currentState.getChild(currentSuffix.charAt(0))) != null) {
                currentState = nextNode;
                currentSuffix = currentSuffix.substring(1, currentSuffix.length());
            }

            lastState = currentState;
            if (lastState.hasChildren()) {
                replaceOrRegister(lastState, registry, lastEdges);
            }

            while (currentSuffix.length() > 0) {
                DAWGNode nextState = new DAWGArrayNode();
                currentState.setChild(currentSuffix.charAt(0), nextState);
                lastEdges.put(currentState, currentSuffix.charAt(0));
                currentState = nextState;
                currentSuffix = currentSuffix.substring(1, currentSuffix.length());
            }
            currentState.setTerminus(true);
        }

        replaceOrRegister(root, registry, lastEdges);
    }

    private void replaceOrRegister(DAWGNode node, List<DAWGNode> registry, HashMap<DAWGNode, Character> lastEdges) {

        DAWGNode child = node.getChild(lastEdges.get(node));
        if (child.hasChildren()) {
            replaceOrRegister(child, registry, lastEdges);
        }
        for (DAWGNode path : registry) {
            if (child.equals(path)) {
                //need to replace the old one instead of just adding a new one.
                node.setChild(lastEdges.get(node), path);
                return;
            }
        }
        registry.add(child);
    }

    public DAWG(InputStream input) throws InvalidDictionaryFormatException {

        Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(input)));
        scanner.useDelimiter("");
        HashMap<Integer, DAWGNode> registry = new HashMap<>();

        if (scanner.next().charAt(0) != 'A') {
            throw new InvalidDictionaryFormatException();
        }

        int rootId = buildNextInt(scanner);

        //eat this token since we know it will be a '#' anyway
        scanner.next();

        int numChildren = buildNextInt(scanner);

        root = new DAWGArrayNode(numChildren, false);

        registry.put(rootId, root);

        buildHelper(root, registry, scanner);

    }

    //this is called by DAWG(InputStream). It recursively builds the dictionary from the input
    private void buildHelper(DAWGNode currentNode, HashMap<Integer, DAWGNode> registry, Scanner scanner) {

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
                DAWGNode newChild = new DAWGArrayNode(newNodeNumChildren, newNodeTerminus);
                registry.put(newNodeID, newChild);
                currentNode.setChild(letter, newChild);
                buildHelper(newChild, registry, scanner);
            }

            //this edge points to a node that already exists
            else if (action == '>') {
                int nextNodeID = buildNextInt(scanner);
                currentNode.setChild(letter, registry.get(nextNodeID));
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

    public void writeOut(OutputStream output) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(output);
        //need to mark this as using the Array format
        writer.print('A');
        HashMap<DAWGNode, Integer> registry = new HashMap<>();
        writeHelper(root, registry, writer);
        writer.close();
    }

    private void writeHelper(DAWGNode currentNode, HashMap<DAWGNode, Integer> registry, PrintWriter writer) {

        int nodeID = registry.size();

        int numChildren = currentNode.numChildren();

        writer.print(nodeID);
        if (currentNode.isTerminal()) writer.print("*");
        else writer.print("#");
        writer.print(numChildren);
        registry.put(currentNode, nodeID);

        for (DAWGEdge child : currentNode) {
            if (!registry.containsKey(child.node)) {
                writer.print(child.edge + "\\");
                writeHelper(child.node, registry, writer);
            } else {
                writer.print(child.edge + ">" + registry.get(child.node));
            }
        }

        writer.print("/");

    }

    public boolean containsWord(String word) {

        DAWGNode currentNode = root;

        for (int i = 0; i < word.length(); i++) {

            DAWGNode nextNode;

            if ((nextNode = currentNode.getChild(word.charAt(i))) != null) currentNode = nextNode;
            else return false;

            if (i == word.length() - 1) return currentNode.isTerminal();

        }

        return false;
    }

    //returns an object which contains a count of all the edges and nodes including and beneath this node.
    public EdgeAndNodeCounter countEdgesAndNodes() {
        return countEdgesAndNodes(root, new EdgeAndNodeCounter(), new ArrayList<>());
    }

    private EdgeAndNodeCounter countEdgesAndNodes(DAWGNode currentNode, EdgeAndNodeCounter counter, ArrayList<DAWGNode> registry) {

        //count current node
        counter.addNodes(1);

        //count all the edges leading out of current node
        counter.addEdges(currentNode.numChildren());

        //can't use a simple getChildren() method on the node since different DAWGNode implementations may use different
        //data structures.
        for (DAWGEdge child : currentNode) {
            boolean foundChild = false;

            for (DAWGNode foundNode : registry) {
                if (child.node == foundNode) {
                    foundChild = true;
                    break;
                }
            }

            if (!foundChild) {
                registry.add(child.node);
                countEdgesAndNodes(child.node, counter, registry);
            }

        }

        return counter;

    }

    public List<String> getAllWords() {

        List<String> words = new ArrayList<>();
        List<Character> word = new ArrayList<>();

        return allWordGetter(root, words, word);

    }

    private List<String> allWordGetter(DAWGNode currentNode, List<String> words, List<Character> word) {

        if (currentNode.isTerminal()) {
            words.add(charListToString(word));
        }

        for (DAWGEdge child : currentNode) {
            word.add(child.edge);
            allWordGetter(child.node, words, word);
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

}
