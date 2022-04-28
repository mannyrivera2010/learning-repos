package com.earasoft.beam.pubmed.func;

import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;

import com.earasoft.beam.pubmed.bean.PubmedArticle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Convert To Json Function
 * @author erivera
 *
 */
public class ConvertToJsonDoFn extends DoFn<PubmedArticle, String> {
    private static final long serialVersionUID = 5870412892334202404L;
    private final Counter emptyLines = Metrics.counter(ConvertToJsonDoFn.class, "emptyLines");
    private final Distribution lineLenDist = Metrics.distribution(ConvertToJsonDoFn.class, "lineLenDistro");

    private static Gson builder = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    @ProcessElement
    public void processElement(@Element PubmedArticle element, OutputReceiver<String> receiver) {
        lineLenDist.update(1);
        //            if (element.trim().isEmpty()) {
        emptyLines.inc();
        //            }

        String json = builder.toJson(element);
        //            System.out.println(json);
        //          CloseableHttpClient client = HttpClientBuilder.create().build();
        //          HttpGet request = new HttpGet("http://google.com");
        //          HttpResponse response;
        //          try {
        //              response = client.execute(request);
        //              BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        //              String line = "";
        //              while ((line = rd.readLine()) != null) {
        //                System.out.println(line);
        //              }
        //          } catch (IOException e) {
        //              // TODO Auto-generated catch block
        //              e.printStackTrace();
        //          }

        receiver.output(json);
    }

}