package com.earasoft.beam.pubmed.func;

import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

import com.earasoft.beam.pubmed.bean.PubmedArticle;

/**
 * ConvertXMLToJsonPTransform
 * @author erivera
 *
 */
public class ConvertXMLToJsonPTransform extends PTransform<PCollection<PubmedArticle>, PCollection<String>> {
    private static final long serialVersionUID = -3256441732820228578L;

    @Override
    public PCollection<String> expand(PCollection<PubmedArticle> lines) {
        PCollection<String> words = lines.apply("ConvertToJsonDoFn", ParDo.of(new ConvertToJsonDoFn()));

        return words;
    }
}