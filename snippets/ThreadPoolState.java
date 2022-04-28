package com.earasoft.learning.hazelcast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CountDown {
    
    static class StateTrans{
        FutureState startState;
        long startTime;
        FutureState endState;
        long endTime;
        
        public StateTrans(FutureState startState, long startTime, FutureState endState, long endTime) {
            super();
            this.startState = startState;
            this.startTime = startTime;
            this.endState = endState;
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return "StateTrans [startState=" + startState + ", startTime=" + startTime + ", endState=" + endState
                    + ", endTime=" + endTime + "]";
        }
        
    }
    
    enum FutureState{
        QUEUED,
        STARTED
    }
    
    static class FutureHistory<type>{
        public FutureState currentState;
        public long currentStartTime = System.currentTimeMillis();
        
        public Future<type> future;
        
        List<StateTrans> futureStates = new ArrayList<StateTrans>();
        
        public void changeState(FutureState state) {
            long currentTime = System.currentTimeMillis();
            futureStates.add(new StateTrans(currentState, currentStartTime, state, currentTime));
            
            currentState = state;
            currentStartTime = currentTime;
        }

        public FutureHistory(FutureState currentState, Future<type> future) {
            super();
            this.currentState = currentState;
            this.future = future;
        }

        public FutureHistory(FutureState currentState) {
            this.currentState = currentState;
        }

        @Override
        public String toString() {
            return "FutureHistory [currentState=" + currentState + ", currentStartTime=" + currentStartTime
                    + ", future=" + future + ", futureStates=" + futureStates + "]";
        }

    }
    
    public static void main(String[] args) throws InterruptedException {
        int coreSize = 4;
        ExecutorService executor = Executors.newFixedThreadPool(coreSize);
        
        // futures
        List<Integer> numbers = IntStream.generate(() -> ThreadLocalRandom.current().nextInt(10000))
                .limit(20)
                .boxed()
                .collect(Collectors.toList());
        
        System.out.println(numbers);
        
        // Integer, Future<Integer> -> Integer is the id
        ConcurrentMap<Integer, FutureHistory<Integer>> priorityBlockingQueue = new ConcurrentHashMap<Integer, FutureHistory<Integer>>();
        
        for(Integer number: numbers) {
            
            priorityBlockingQueue.put(number, new FutureHistory<Integer>(FutureState.QUEUED));
            
            // inside loop
            Future<Integer> currentFuture = executor.submit(() -> {
                FutureHistory<Integer> currentFutureHistory = priorityBlockingQueue.get(number);
                currentFutureHistory.changeState(FutureState.STARTED);
                
                Thread.sleep(number);
                return number;
            });
            
        
            
            System.out.println("add future - " + number);
            FutureHistory<Integer> currentFutureHistory = priorityBlockingQueue.get(number);
            currentFutureHistory.future = currentFuture;
            
           
            
            // upper limit - make sure that map does not get very filled
            while(priorityBlockingQueue.size() >= coreSize + 2) {
                printMap(priorityBlockingQueue);
                
                iternate(priorityBlockingQueue);
                
                
               
                Thread.sleep(1000);
            }
        }
        
        System.out.println("Wait for queue to finish");
        
        // finish all futures
        while(priorityBlockingQueue.size() != 0) {
            iternate(priorityBlockingQueue);
            Thread.sleep(50);
        }
        
        System.out.println("DONE");
        executor.shutdown();
        
    }

    /**
     * @param priorityBlockingQueue
     */
    private static void printMap(ConcurrentMap<Integer, FutureHistory<Integer>> priorityBlockingQueue) {
        boolean first = true;
        for(Map.Entry<Integer, FutureHistory<Integer>> entry: priorityBlockingQueue.entrySet()) {
            if(first) {
                System.out.print(entry.getKey() + ":" + entry.getValue()+";");
            }else {
                System.out.print("\n\t" + entry.getKey() + ":" + entry.getValue()+";");
            }
            first = false;
        }
        System.out.println("");
    }

    /**
     * @param priorityBlockingQueue
     * @throws InterruptedException
     */
    private static void iternate(ConcurrentMap<Integer, FutureHistory<Integer>> priorityBlockingQueue)
            throws InterruptedException {
        for(Map.Entry<Integer, FutureHistory<Integer>> entry: priorityBlockingQueue.entrySet()) {
            Integer currentFutureKey = entry.getKey();
            FutureHistory<Integer> currentFutureHistory = entry.getValue();
            Future<Integer> currentFuture = currentFutureHistory.future;
            
            
            if(currentFuture!= null && currentFuture.isDone()) {
                try {
                    System.out.println(currentFutureKey + " - "  +currentFuture.get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                priorityBlockingQueue.remove(currentFutureKey);
            }
            
        }
    }   

}
