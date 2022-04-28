package com.earasoft.rdf4j.utils;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;

public class ModelUtils {

    public static Difference getDiff(Model original, Model changed) {
        Model additions =  new LinkedHashModelFactory().createEmptyModel();
        Model deletions = new LinkedHashModelFactory().createEmptyModel();

        original.forEach(statement -> {
            if (!changed.contains(statement.getSubject(), statement.getPredicate(), statement.getObject())) {
                deletions.add(statement);
            }
        });

        changed.forEach(statement -> {
            if(!original.contains(statement.getSubject(), statement.getPredicate(), statement.getObject())) {
                additions.add(statement);
            }
        });

        return new Difference.Builder()
                .additions(additions)
                .deletions(deletions)
                .build();
    }

}
