package com.earasoft.beam.pubmed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Top;
import org.apache.beam.sdk.transforms.DoFn.Element;
import org.apache.beam.sdk.transforms.DoFn.OutputReceiver;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.http.client.ClientProtocolException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.earasoft.beam.pubmed.bean.PubmedArticle;
import com.earasoft.beam.pubmed.bean.medlineCitation.Author;
import com.earasoft.beam.pubmed.func.ConvertXMLToJsonPTransform;
import com.earasoft.beam.pubmed.func.StringWithIndex;
import com.earasoft.beam.pubmed.func.WordUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.beam.sdk.io.xml.XmlIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;

/**
 * Pubmed Beam Driver
 * 
 * @author erivera
 *
 */
public class PubmedDriver {
    private static final Logger LOG = LoggerFactory.getLogger(PubmedDriver.class);

    /**
     * runWordCount
     * @param options
     */
    static void runPipeline(String[] args) {
        PipelineOptionsFactory.register(PubmedDriverOptions.class);

        PubmedDriverOptions options = PipelineOptionsFactory
                .fromArgs(args)
                .withValidation()
                .as(PubmedDriverOptions.class);

        Pipeline p = Pipeline.create(options);

        // Start Pipeline
        PCollection<PubmedArticle> pubmedArticlePCollection = p.apply("readXMLs",
                XmlIO.<PubmedArticle>read()
                .from(options.getInputFile())
                .withCompression(Compression.GZIP)
                .withRootElement("PubmedArticleSet")
                .withRecordElement("PubmedArticle")
                .withRecordClass(PubmedArticle.class));

        boolean getAuthors = false;
        getAuthors(options, pubmedArticlePCollection, getAuthors);


        Counter counter = Metrics.counter(PubmedDriver.class, "getAbstractTextError");

        /*
         * ngrams
         */
        boolean titleWordCount = true;
        if(titleWordCount) {
            // Title Word Count
            pubmedArticlePCollection
            .apply("Expand AbstractTextList", ParDo.of(new DoFn<PubmedArticle, String>(){
                private static final long serialVersionUID = 4045742062651672919L;
                
                @ProcessElement
                public void processElement(@Element PubmedArticle pubmedArticle, OutputReceiver<String> out) {
                    try {
                        List<String> abstractTextList = pubmedArticle.getMedlineCitation().getArticle().getAbstractClass().getAbstractText();
                        
                        for(String abstractText: abstractTextList) {
                            out.output(abstractText.toLowerCase().trim());
                        }
                    }catch(Exception e){
                        counter.inc(1);
                    }
                }
            } ))
            
            .apply("Expand to Ngrams", ParDo.of(new DoFn<String, String>(){
                private static final long serialVersionUID = 4045742062651672919L;
                
                @ProcessElement
                public void processElement(@Element String abstractText, OutputReceiver<String> out) {
                    try {
                        for(StringWithIndex ngram: WordUtils.ngramsEnglishRange(1, 5, abstractText)) {
                            out.output(ngram.getValue());
                        }
                       
                    }catch(Exception e){
                        counter.inc(1);
                    }
                }
            } ))

            .apply("Count Per Element", Count.<String>perElement())

            .apply("Filter Value", ParDo.of(new DoFn<KV<String, Long>, KV<String, Long>>(){
                private static final long serialVersionUID = -6960979417150272090L;

                @ProcessElement
                public void processElement(@Element KV<String, Long> input, OutputReceiver<KV<String, Long>> out) {
                    if(input.getValue() >= 50) {
                        out.output(input);
                    }

                }
            } ))

            //            .apply("s", Top.of(100000, new KV.OrderByValue<String, Long>()))

            .apply("FormatResults", MapElements
                    .into(TypeDescriptors.strings())
                    .via((KV<String, Long> wordCount) -> wordCount.getKey() + "\t" + wordCount.getValue()))

            .apply("ToFile2", 
                    TextIO.write()
                    .withCompression(Compression.GZIP)
                    //.withoutSharding()
                    .withNumShards(5)
                    .withSuffix("-ngram.txt")
                    .to(options.getOutput()));
        }

        // Save whole file
        boolean saveWholeFile = false;
        saveAllToFile(options, pubmedArticlePCollection, saveWholeFile);

        // END of pipeline
        PipelineResult pipelineResult = p.run();

        // State jobState = pipelineResult.waitUntilFinish();
        // LOG.info("jobState:" + jobState);
        // Boolean isDataflowRunner =  DataflowRunner.class.isAssignableFrom( options.getRunner() );
    }

    /**
     * @param options
     * @param pubmedArticlePCollection
     * @param saveWholeFile
     */
    private static void saveAllToFile(PubmedDriverOptions options, PCollection<PubmedArticle> pubmedArticlePCollection,
            boolean saveWholeFile) {
        if(saveWholeFile) {
            pubmedArticlePCollection
            .apply("JsonPTransform", new ConvertXMLToJsonPTransform())

            .apply("ToFile3", 
                    TextIO.write()
                    .withCompression(Compression.GZIP)
                    //                    .withoutSharding()
                    //.withNumShards(20)
                    .withSuffix(".json")
                    .to(options.getOutput()));
        }
    }

    /**
     * @param options
     * @param pubmedArticlePCollection
     * @param getAuthors
     */
    private static void getAuthors(PubmedDriverOptions options, PCollection<PubmedArticle> pubmedArticlePCollection,
            boolean getAuthors) {
        if(getAuthors) {
            pubmedArticlePCollection
            // get authors and expand
            .apply(ParDo.of(new DoFn<PubmedArticle, KV<String, List<Author>>>(){
                private static final long serialVersionUID = 4045742062651672919L;

                @ProcessElement
                public void processElement(@Element PubmedArticle pubmedArticle, OutputReceiver<KV<String, List<Author>>> out) {
                    String pmid = "NA";
                    try {
                        pmid = pubmedArticle.getMedlineCitation().getPMID();

                        List<Author> authors = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().getAuthors();

                        out.output(KV.of(pmid, authors));

                    }catch(Exception e) {
                        out.output(KV.of(pmid, new ArrayList<Author>()));
                    }
                }
            }))

            .apply(ParDo.of(new DoFn<KV<String, List<Author>>, String>(){
                private static final long serialVersionUID = 1L;
                // private static final Gson builder = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

                @ProcessElement
                public void processElement(@Element KV<String, List<Author>> kv, OutputReceiver<String> out) {
                    Gson builder = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(kv.getKey());
                    stringBuilder.append("\t");

                    List<Author> author = kv.getValue();

                    stringBuilder.append(builder.toJson(author));

                    out.output(stringBuilder.toString());
                }
            }))

            .apply("ToFile1", 
                    TextIO.write()
                    .withCompression(Compression.GZIP)
                    // .withoutSharding()
                    .withNumShards(1)
                    .withSuffix(".pmidAuthor.txt")
                    .to(options.getOutput()));

        }
    }

    /**
     * Main
     * @param args
     * @throws JAXBException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static void main(String[] args) throws JAXBException, ClientProtocolException, IOException {
        runPipeline(args);
    }

}
