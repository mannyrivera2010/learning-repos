package com.earasoft.rdf4j.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.List;

public class AppCassandra {
    public static void main(String[] args) {

        try (CqlSession session = CqlSession.builder().build()) {                                  // (1)
            ResultSet rs = session.execute("select release_version from system.local");              // (2)
            Row row = rs.one();
            System.out.println(row.getString("release_version"));                                    // (3)
        }

        CassandraConnector connector = null;
        try{
            connector = new CassandraConnector();
            connector.connect("127.0.0.1", 9042, "datacenter1");
            CqlSession session = connector.getSession();

            KeyspaceRepository1 keyspaceRepository = new KeyspaceRepository1(session);

//            keyspaceRepository.createKeyspace("testKeyspace", 1);
            keyspaceRepository.useKeyspace("testKeyspace");
//
            VideoRepository videoRepository = new VideoRepository(session);

//            videoRepository.createTable();

            Video video = new Video("Video Title 1", Instant.now());

            for(int i = 1; i<=100;i++){
                videoRepository.insertVideo(video, "testKeyspace");
            }

//        videoRepository.insertVideo(new CassandraConnector.VideoRepository.Video("Video Title 2",
//                Instant.now().minus(1, ChronoUnit.DAYS)));

            List<Video> videos = videoRepository.selectAll("key");

            videos.forEach(x -> System.out.println(x.toString()));
        }finally {
            if(connector!=null){
                connector.close();
            }
        }




    }
}
