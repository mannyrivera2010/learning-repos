package com.earasoft.rdf4j.sparql;

public class Queries {

    private static String getQuery(String g1, String g2) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ?s ?p ?o WHERE { VALUES ?g1 {<");
        sb.append(g1);
        sb.append(">} ");
        sb.append(" VALUES ?g2 {<");
        sb.append(g2);
        sb.append(">} GRAPH ?g1 { ?s ?p ?o .}");
        sb.append(" FILTER NOT EXISTS { GRAPH ?g2");
        sb.append(" { ?s ?p ?o } } }");
        return sb.toString();
    }
}
