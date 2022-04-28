package my.plugin.words.api;

import java.io.InputStream;

public interface WordCounter {
    int countWord(String word, String text);
    int countWord(String word, InputStream file);
}
