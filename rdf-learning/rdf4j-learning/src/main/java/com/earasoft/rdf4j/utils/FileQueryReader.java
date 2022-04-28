package com.earasoft.rdf4j.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FileQueryReader {
    public static Map<String, String> loadQueries(){
        Map<String, String> queries = new LinkedHashMap<>();

        try {
            Scanner scanner = new Scanner(new File("example_data/family.txt"));

            Queue<String> queue = new LinkedList<>();
            Stack<String> keyStack = new Stack<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Start of a new query
                if(line.startsWith("=====")) {

                    if(!keyStack.empty()){
                        commit(queries, queue, keyStack);
                    }
                    queue.clear(); // delete old contents
                    keyStack.push(line.substring(5).trim());
                } else if(line.startsWith("-----")){

                    commit(queries, queue, keyStack);

                } else if(line.startsWith("//")){
                    // comment
                }else {
                    queue.offer(line);
                    queue.offer("\n");
                }
                //
            }
            scanner.close();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return queries;
    }

    private static void commit(Map<String, String> queries, Queue<String> queue, Stack<String> keyStack) {
        String pop = keyStack.pop();
        StringBuilder queryStringBuilder = new StringBuilder();

        while(queue.peek() != null){
            queryStringBuilder.append(queue.poll());
        }

        queries.put(pop, queryStringBuilder.toString());
    }

    public static void main(String[] args) {
        Map<String, String> queries = loadQueries();

        for(Map.Entry entry: queries.entrySet()){
            System.out.println("-----"+entry.getKey()+"-----");
            System.out.println(entry.getValue());
        }
    }
}
