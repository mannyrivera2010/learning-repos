package com.earasoft.rdf4j;

import org.eclipse.rdf4j.model.Model;

public class Difference {
    private Model additions;
    private Model deletions;

    private Difference(Builder builder) {
        this.additions = builder.additions;
        this.deletions = builder.deletions;
    }

    public Model getAdditions() {
        return additions;
    }

    public Model getDeletions() {
        return deletions;
    }

    public static class Builder {
        private Model additions;
        private Model deletions;

        public Builder additions(Model additions) {
            this.additions = additions;
            return this;
        }

        public Builder deletions(Model deletions) {
            this.deletions = deletions;
            return this;
        }

        public Difference build() {
            return new Difference(this);
        }
    }
}
