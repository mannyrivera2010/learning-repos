package com.earasoft.beam.pubmed;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.RuntimeValueProvider;

/**
 * Options supported by {@link PubmedDriver}.
 *
 * <p>
 * Concept #4: Defining your own configuration options. Here, you can add your
 * own arguments to be processed by the command-line parser, and specify default
 * values for them. You can then access the options values in your pipeline
 * code.
 *
 * <p>
 * Inherits standard configuration options.
 */
public interface PubmedDriverOptions extends PipelineOptions {

    // @Required
    @Description("Path of the file to read from")
    @Default.String("gs://pubmed-input/baseline_xml_gz/*.xml.gz") // pubmed19n0001.xml.gz
    //@Default.String("pubmed*.xml.gz") // pubmed19n0001.xml.gz
    ValueProvider <String> getInputFile();
    void setInputFile(ValueProvider <String> value);
  
    /** Set this required option to specify where to write the output. */
    @Required
    @Description("Path of the file to write to")
    @Default.String("gs://pubmed-output/pubmed")
//    @Default.String("out")
    ValueProvider<String> getOutput();
    void setOutput(ValueProvider<String> value);
    
}