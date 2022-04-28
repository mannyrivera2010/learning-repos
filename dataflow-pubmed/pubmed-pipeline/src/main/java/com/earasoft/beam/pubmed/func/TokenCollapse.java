package com.earasoft.beam.pubmed.func;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class TokenCollapse {

    public static void main(String[] args) {
        Map<String, Long> index = new HashMap<String, Long>();

        String infile = "../python-src/ngram.merged_sorted2.txt.gz";

        loadFileToIndex(index, infile);

        System.out.println("index.size: " + index.size());

        String[] inputList = new String[] {
                "Based on neurobiological and development-psychological findings psychodynamic concepts for understanding compulsive disorders are supplemented and extended. A stimulus barrier impaired during development which in the "
                        + "early mother-child system normally develops into an autonomous and self-regulating ability has the consequence in compulsive disorders that the ability of drawing limits between reality and phantasy has only poor success."
                        + " Based on case examples the function of the situation triggering the compulsive symptoms and the foreseeable relationship quality are examined on the different levels of development. Then it becomes clear how complex the "
                        + "originating conditions for compulsive disorders are, and under which conditions a primary object fails as original external psycho-neurobiological regulator in the development of autonomous self-regulation notExi"
        };
        
        // simpleExample();
        // example 1
//        List<String> tokens = groupTokensAlg1(index, inputList[0]);
//        System.out.println(tokens);
        
      
        
        List<String> tokens2 = groupTokensAlg2(index, inputList[0]);
        System.out.println(tokens2);
        
    }

    /**
     * 
     */
    private static void simpleExample() {
        Map<String, Long> index = new HashMap<String, Long>();
        index.put("1 2 3", new Long(80));
        index.put("1 2", new Long(600));

        List<String> tokens = groupTokensAlg1(index, "5 6 8 1 2 5 4 6 7 1 2 3 4 5 6 7 8 9 123 1 2 3 1 2 3");

        System.out.println(tokens);
    }

        
    private static List<String> groupTokensAlg2(Map<String, Long> index, String inputString) {
        List<String> tokens = new ArrayList<String>();
        List<String> tokensBuffer = new ArrayList<String>();
        
        Map<Integer, StringWithIndex> stringByIndexRef = new HashMap<Integer, StringWithIndex>();
        List<StringWithIndex> ngramGroups = new ArrayList<StringWithIndex>();
        
        // Nested
        List<List<StringWithIndex>> nestedStringWithIndexList = new ArrayList<List<StringWithIndex>>();
        
        try {
            ngramGroups = WordUtils.ngramsEnglishRange(1, 4, inputString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        for(StringWithIndex ngramWithIndex: ngramGroups) {
            if(ngramWithIndex.getIndex().size() == 1) {
                stringByIndexRef.put(ngramWithIndex.getIndex().get(0), ngramWithIndex);
            }else {
                if(index.containsKey(ngramWithIndex.getValue())) {
                    List<StringWithIndex> tempNestedString = new ArrayList<StringWithIndex>(); // temp folder
                    
                    for(Integer currentIndex: ngramWithIndex.getIndex()) {
                        tempNestedString.add(ngramGroups.get(currentIndex));
                    }
                    
                    nestedStringWithIndexList.add(tempNestedString);
                    
                    System.out.println(ngramWithIndex + "\t" + index.get(ngramWithIndex.getValue()));
                }
            }
        }
        
        System.out.println("stringByIndexRef: " + stringByIndexRef );
        
        System.out.println("nestedStringWithIndexList: " + nestedStringWithIndexList );
        
        return tokens;
    }
    
    
    /**
     * Purpose of method is to group tokens to longest tokens
     * 
     * @param index
     * @param inputList
     * @return
     */
    private static List<String> groupTokensAlg1(Map<String, Long> index, String inputString) {
        List<String> tokens = new ArrayList<String>();
        List<String> tokensBuffer = new ArrayList<String>();
        List<StringWithIndex> singleTokens = new ArrayList<StringWithIndex>();
        
        try {
            singleTokens = WordUtils.ngramsEnglishRange(1, 1, inputString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        int stateInt = 0;
        boolean inIndex = false;
        for(StringWithIndex currentToken: singleTokens) {
            tokensBuffer.add(currentToken.getValue());
            
            if(tokensBuffer.size() >= 2) {
                String tempString = String.join(" ", tokensBuffer);
                boolean inIndexCurrent = index.containsKey(tempString);

                if(inIndex == false && inIndexCurrent == false) {
                    emptyBuffer(tokens, tokensBuffer, 1);
                    stateInt = 1;
                }else if(inIndex == true && inIndexCurrent == true) {
                    stateInt = 2;
                }else if(inIndex == true && inIndexCurrent == false) {
                    inIndex = inIndexCurrent;
                    
                    List<String> tempBuffer = new ArrayList<String>();
                    
                    emptyBuffer(tempBuffer, tokensBuffer, 1);
                    tokens.add(String.join(" ", tempBuffer));
                    stateInt = 3;
                }else if(inIndex == false && inIndexCurrent == true) {
                    inIndex = inIndexCurrent;
                    stateInt = 4;
                }
                
            } // check for tokensBuffer.size() >= 2
        } // loop
        
        if(stateInt == 4 || stateInt == 2) {
            List<String> tempBuffer = new ArrayList<String>();
            emptyBuffer(tempBuffer, tokensBuffer, 0);
            tokens.add(String.join(" ", tempBuffer));
        }else {
            emptyBuffer(tokens, tokensBuffer, 0);
        }
        return tokens;
    }

    /**
     * Empty Buffer to output list
     * @param tokens
     * @param tokensBuffer
     */
    private static void emptyBuffer(List<String> tokens, List<String> tokensBuffer, int toSize) {
        while(tokensBuffer.size() > toSize) {
            tokens.add(tokensBuffer.remove(0));
        }
    }



    /**
     * @param index
     * @param infile
     */
    private static void loadFileToIndex(Map<String, Long> index, String infile) {
        try(GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));
                Reader decoder = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(decoder);){

            String line;
            while ((line = br.readLine()) != null) {
                String trimedLine = line.trim();

                if(trimedLine.length() == 0)
                    continue; // skip empty lines

                String[] lineSplit = line.split("\t");

                if(lineSplit.length != 2)
                    continue;

                // System.out.println(lineSplit[0]);
                
                try {
                    Long popNum = Long.parseLong(lineSplit[1]); 
                    index.put(lineSplit[0], popNum);
                }catch(NumberFormatException e) {
                    index.put(lineSplit[0], new Long(0));
                }
                
            }

        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
