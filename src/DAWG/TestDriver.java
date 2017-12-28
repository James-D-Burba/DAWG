package DAWG;

public class TestDriver {

    public static void main(String[] args) {
        new DictionaryTester
                ("dictionaries/bigWordList.txt",
                        "testOutput",
                        DAWG.ARRAY)
                .run();
    }

}
