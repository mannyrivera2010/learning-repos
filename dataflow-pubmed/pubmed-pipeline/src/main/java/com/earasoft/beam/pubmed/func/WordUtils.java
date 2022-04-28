package com.earasoft.beam.pubmed.func;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * for phone number use import com.google.i18n.phonenumbers.Phonenumber  
 *    compile 'com.googlecode.libphonenumber:libphonenumber:6.1' 
 *    
 * https://www.baeldung.com/lucene-analyzers
 *    
 * @author erivera
 *
 */
public class WordUtils {
    public static final CharArraySet ENGLISH_STOP_WORDS_SET;
    
    final static List<String> stopWords = Arrays.asList(
            "a", "all", "am", "an", "and", "any", "are", "aren't", 
            "as", "at", "be", "because", "been", "to", "from", "by", 
            "can", "can't", "do", "don't", "didn't", "did" ,
            "ourselves", "hers", "between", "yourself", "but", "again", "there", "about", "once", "during", "out", "very", "having", "with", "they", 
            "own", "an", "be", "some", "for", "do", "its", "yours", "such", "into", "of", "most", "itself", "other", "off", "is", "s", "am", "or", "who", 
            "as", "from", "him", "each", "the", "themselves", "until", "below", "are", "we", "these", "your", "his", "through", "don", "nor", "me", "were", 
            "her", "more", "himself", "this", "down", "should", "our", "their", "while", "above", "both", "up", "to", "ours", "had", "she", "all", "no", "when",
            "at", "any", "before", "them", "same", "and", "been", "have", "in", "will", "on", "does", "yourselves", "then", "that", "because", "what", "over", 
            "why", "so", "can", "did", "not", "now", "under", "he", "you", "herself", "has", "just", "where", "too", "only", "myself", "which", "those", "i", "after", 
            "few", "whom", "t", "being", "if", "theirs", "my", "against", "by", "doing", "it", "how", "further", "was", "here", "than"
    );
    
    static {
        final CharArraySet stopSet = new CharArraySet(stopWords, false);
        ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }
    
    public static void main(String[] args) throws IOException {
        String SAMPLE_TEXT = "a the mann@al.com 341-457-5774 This is baeldung.com Lucene Analyzers test, You need to use the StandardTokenizer and not the KeywordTokenizer. "
                + "The latter will simply treat the whole input as a single token, "
                + "while the former will lowercase and split the input into multiple tokens. : ( ) ";
       
        List<StringWithIndex> result = analyze(SAMPLE_TEXT, new StandardAnalyzer(ENGLISH_STOP_WORDS_SET));
        
        int count = 0;
        for(StringWithIndex i : result) {
            if(count>=1) {
                System.out.println("\t" + i);
            }else {
                System.out.println(i);
            }
            count+=1;
        }
        count = 0;
        
//       System.out.println(ngramsEnglish(5, SAMPLE_TEXT));
        for(StringWithIndex i : ngramsEnglishRange(1, 5, SAMPLE_TEXT)) {
            if(count>=1) {
                System.out.println("\t" + i);
            }else {
                System.out.println(i);
            }
            count+=1;
        }
    }
    
    public static List<StringWithIndex> ngramsEnglishRange(int n, int m, String input) throws IOException {
        List<StringWithIndex> output = new ArrayList<StringWithIndex>();
        
        for (int i = n; i <= m; i++) {
            List<StringWithIndex> result = analyze(input, new StandardAnalyzer(ENGLISH_STOP_WORDS_SET));
            output.addAll(ngrams(i, result));
        }
        
        return output;
    }
    
    public static List<StringWithIndex> ngramsEnglish(int n, String input) throws IOException {
        List<StringWithIndex> result = analyze(input, new StandardAnalyzer(ENGLISH_STOP_WORDS_SET));  // EnglishAnalyzer
        return ngrams(n, result);
    }
    
    public static List<StringWithIndex> ngrams(int n, List<StringWithIndex> result) {
        List<StringWithIndex> ngrams = new ArrayList<StringWithIndex>();
        
        for (int i = 0; i < result.size() - n + 1; i++) {
            ngrams.add(concat(result, i, i+n));
        }
            
        return ngrams;
    }

    public static StringWithIndex concat(List<StringWithIndex> result, int start, int end) {
        StringBuilder sb = new StringBuilder();
        List<Integer> indices = new ArrayList<Integer>();
        
        for (int i = start; i < end; i++) {
            sb.append((i > start ? " " : "") + result.get(i).getValue());
            indices.add(i);
        }
        
        return new StringWithIndex(sb.toString(), indices);
    }
    
    
    public static List<StringWithIndex> analyze(String text, Analyzer analyzer) throws IOException{
        List<StringWithIndex> result = new ArrayList<StringWithIndex>();
        TokenStream tokenStream = analyzer.tokenStream("FIELD_NAME", text);
        
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        
        int index = 0;
        
        while(tokenStream.incrementToken()) {
           result.add(new StringWithIndex(attr.toString(), index));
           index += 1;
        }       
        
        tokenStream.close();
        return result;
    }
    
    
}
