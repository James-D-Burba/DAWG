package DAWG;

public class TestDriver {

    public static void main(String[] args) {
        new DictionaryTester
                ("dictionaries/smallWordList.txt",
                        "testOutput",
                        DAWG.LIST)
                .run();
    }

}
