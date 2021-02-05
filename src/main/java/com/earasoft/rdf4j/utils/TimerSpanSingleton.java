package com.earasoft.rdf4j.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class TimerSpanSingleton {

    private static TimerSpanSingleton INSTANCE;
    private TimerSpan timerSpan;

    private TimerSpanSingleton() {
    }

    public static TimerSpanSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TimerSpanSingleton();
        }
        return INSTANCE;
    }

    public TimerSpan getCurrentTimerSpan(){
        return timerSpan;
    }

    public TimerSpan createTimerSpan (String timerEvent) {
        this.timerSpan = new TimerSpan(timerEvent);
        return this.timerSpan;
    }

    /**
     * TimerSpan
     */
    public static class TimerSpan implements AutoCloseable {

        DateFormat df = new SimpleDateFormat("HH:mm:ss");

        public static class TimerEvent{
            final String title;
            final long currentTimeMillis;
            public TimerEvent(String title, long currentTimeMillis) {
                this.title = title;
                this.currentTimeMillis = currentTimeMillis;
            }
        }

        List<TimerEvent> timerEvents = new ArrayList<TimerEvent>();

        public TimerSpan(String timerEvent){
            timerEvents.add(new TimerEvent(timerEvent, System.currentTimeMillis()));
        }

        public void event(String event){
            TimerEvent e = new TimerEvent(event, System.currentTimeMillis());
            timerEvents.add(e);
        }

        @Override
        public void close() {
            timerEvents.add(new TimerEvent("end", System.currentTimeMillis()));

            boolean first = true;
            long firstTime = 0;
            for(TimerEvent e : timerEvents){
                if(first) {
                    firstTime = e.currentTimeMillis;
                }
                first = false;

                Date currentDate = new Date(e.currentTimeMillis);
                print(firstTime, e, currentDate);
            }

        }

        private void print(long firstTime, TimerEvent e, Date currentDate) {
            System.out.println(e.title + "\t" +
                    (e.currentTimeMillis - firstTime) + "\t" +
                    df.format(currentDate) + "\t" +
                    e.currentTimeMillis);
        }
    }
}
