package DAWG;

import java.io.*;
import java.util.*;

public class DictionaryTester implements Runnable {

    private static String OUTPUT_DIR = "graphs/";

    public enum Type {ARRAY}

    private Type type;

    private DictionaryNode dictionary;
    private List<String> wordList;

    private String outputFileName;
    private String inputFileName;

    public DictionaryTester(String inputFileName, String outputFileName, Type type) {

        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
        this.type = type;

    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                dictionaryTest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dictionaryTest() throws FileNotFoundException, UnsupportedEncodingException, InvalidDictionaryFormatException {

        wordList = buildWordList();

        System.out.println("Testing dictionary....");
        dictionary = buildFromList(wordList, type);
        testWantedWords();
        testUnwantedWords();
        countNodes(dictionary);

        writeToFile();
        dictionary = buildFromFile(outputFileName, type);
        testWantedWords();
        testUnwantedWords();
        countNodes(dictionary);
    }

    private DictionaryNode buildFromList(List<String> words, Type type) {

        System.out.println("Building graph from word list...");

        long startTime = System.nanoTime();

        DictionaryNode dictionary;
        switch (type) {
            case ARRAY:
                dictionary = new DictionaryArrayNode(words);
                break;
            default:
                dictionary = new DictionaryArrayNode(words);
                break;
        }

        long endTime = System.nanoTime();
        double totalTime = (double) (endTime - startTime) / 1000000000;
        System.out.println("Built graph from word list in " + totalTime + " seconds");

        return dictionary;

    }

    private void writeToFile() throws FileNotFoundException, UnsupportedEncodingException {

        System.out.println("Writing graph to " + OUTPUT_DIR + outputFileName);
        long startTime = System.nanoTime();
        File outputDirectory = new File(OUTPUT_DIR);
        outputDirectory.mkdirs();
        File outputFile = new File(OUTPUT_DIR + outputFileName);
        dictionary.writeOut(new FileOutputStream(outputFile));
        long endTime = System.nanoTime();
        double totalTime = (double) (endTime - startTime) / 1000000000;
        System.out.println("Wrote graph to file in " + totalTime + " seconds");

    }

    private DictionaryNode buildFromFile(String fileName, Type type) throws FileNotFoundException, InvalidDictionaryFormatException {

        System.out.println("Building graph from file " + OUTPUT_DIR + fileName);
        long startTime = System.nanoTime();

        InputStream inputStream = new FileInputStream(new File(OUTPUT_DIR + fileName));

        DictionaryNode dictionary;

        switch (type) {
            case ARRAY:
                dictionary = new DictionaryArrayNode(inputStream);
                break;
            default:
                dictionary = new DictionaryArrayNode(inputStream);
        }

        long endTime = System.nanoTime();
        double totalTime = (double) (endTime - startTime) / 1000000000;
        System.out.println("built graph from file in " + totalTime + " seconds");

        return dictionary;

    }

    private void countNodes(DictionaryNode dictionary) {
        System.out.println("Counting graph nodes...");
        long startTime = System.nanoTime();
        EdgeAndNodeCounter counter = dictionary.countEdgesAndNodes();
        long endTime = System.nanoTime();
        double totalTime = (double) (endTime - startTime) / 1000000000;
        System.out.println("Counted graph nodes in " + totalTime + " seconds");
        System.out.println("Graph contains " + counter.getNodeCount() + " nodes and " + counter.getEdgeCount() + " edges");
    }

    private boolean testWantedWords() {

        boolean result = true;
        int counter = 0;

        for (String s : wordList) {
            if (!dictionary.containsWord(s)) {
                counter++;
                System.out.println("Not in dictionary: " + s);
                result = false;
            }
        }

        System.out.println(counter + " words didn't make it into the dictionary.");

        return result;

    }

    private boolean testUnwantedWords() {

        boolean result = true;

        System.out.println("finding all words in graph...");
        long startTime = System.nanoTime();

        List<String> dictionaryWords = dictionary.getAllWords();

        long endTime = System.nanoTime();
        double totalTime = (double) (endTime - startTime) / 1000000000;

        System.out.println(dictionaryWords.size() + " words in dictionary");
        System.out.println("found all words in graph in " + totalTime + " seconds.");

        System.out.println("checking if graph words match list words...");

        Collections.sort(dictionaryWords);

        for (int i = 0; i < dictionaryWords.size(); i++) {
            String word1 = dictionaryWords.get(i);
            String word2 = wordList.get(i);
//            System.out.print("Comparing " + word1 + " to " + word2 + "... ");
            boolean isEqual = word1.equals(word2);
            if (!isEqual) {
                result = false;
                System.out.println(word1 + " != " + word2);
            }
//            System.out.println(isEqual);
        }

        if (result) {
            System.out.println("All words in the dictionary are valid");
        } else {
            System.out.println("The dictionary contains invalid words");
        }

        return result;

    }

    private List<String> randomWordList(int numWords, int minLength, int maxLength) {

        Random random = new Random();
        List<String> words = new ArrayList<>();

        for (int i = 0; i < numWords; i++) {
            int length = minLength + (int) (random.nextFloat() * (maxLength - minLength));
            while (true) {
                String randomWord = getRandomWord(length);
                if (!words.contains(randomWord)) {
                    words.add(randomWord);
                    break;
                }
            }
        }

        return words;

    }

    //the big dictionary should have 54336 nodes and 123453 edges
    private List<String> buildWordList() throws FileNotFoundException {
        ArrayList<String> words = new ArrayList<>();

        Scanner scanner = new Scanner(new File(inputFileName));

        while (scanner.hasNext()) {
            words.add(scanner.next());
        }
        return words;
    }

    private String getRandomWord(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder wordBuilder = new StringBuilder();
        Random rnd = new Random();
        while (wordBuilder.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * letters.length());
            wordBuilder.append(letters.charAt(index));
        }
        String word = wordBuilder.toString();
        return word;
    }

}
