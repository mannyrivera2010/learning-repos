package com.earasoft.beam.pubmed;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.earasoft.beam.pubmed.bean.PubmedArticle;
import com.earasoft.beam.pubmed.bean.PubmedArticleSet;

/**
 * PubmedLocalJaxb
 * @author erivera
 *
 */
public class PubmedLocalJaxb {
    private static final String PUBMED_XML = "pubmed19n0186.xml.gz";

    /**
         * parseXMLJaxbContext Testing
         * @throws JAXBException
         * @throws ParserConfigurationException 
         * @throws SAXException 
         * @throws FileNotFoundException 
         */
        public static void parseXMLJaxbContext1(){
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
    //            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                // spf.setFeature("http://xml.org/sax/features/validation", false);
                
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(PUBMED_XML));
                InputSource inputSource = new InputSource(gzip);
                
                XMLReader xmlReader = spf.newSAXParser().getXMLReader();
                SAXSource source = new SAXSource(xmlReader,  inputSource);
                
                JAXBContext jc = JAXBContext.newInstance(PubmedArticleSet.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) unmarshaller.unmarshal(source);
                // System.out.print(pubmedArticleSet);
                
                List<PubmedArticle> pubmedArticles = pubmedArticleSet.getPubmedArticle();
                
                for(PubmedArticle xmlCandidateRecord: pubmedArticles) {
                    System.out.println(xmlCandidateRecord.toPrettyString());
                    System.out.println("-----------------------------------");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
             } catch (JAXBException e) {
                e.printStackTrace();
              } catch (SAXNotRecognizedException e) {
                e.printStackTrace();
            } catch (SAXNotSupportedException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public static void main(String[]args) {
            parseXMLJaxbContext1();
        }

}
