package my.plugin.words.impl;

import my.plugin.words.api.WordCounter;
import org.osgi.service.component.annotations.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(service = WordCounter.class, immediate = true)
public class WordCounterImpl implements WordCounter {

    @Override
    public int countWord(String word, String text) {
        int i = 0;
        Pattern p = Pattern.compile(word);
        Matcher m = p.matcher(text);
        while (m.find()) {
            i++;
        }
        return i;
    }

    @Override
    public int countWord(String word, InputStream file) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));
        StringBuilder out = new StringBuilder();
        String line;
        while (true) {
            try {
                if (!((line = reader.readLine()) != null)) break;
                out.append(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int i = 0;
        Pattern p = Pattern.compile(word);
        Matcher m = p.matcher(out.toString());
        while (m.find()) {
            i++;
        }
        return i;
    }
}
