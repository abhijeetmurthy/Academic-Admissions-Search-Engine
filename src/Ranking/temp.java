// package Ranking;

import java.util.List;
import java.util.Set;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.CacheRequest;
import java.net.URL;
import java.nio.file.Paths;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Ranking {
    private static HashMap<String, HashMap<String, Boolean>> createIndex(HashMap<String, HashMap<String, Boolean>> index, String docID, String doc) {
        String[] words =doc.split(" ");

        for(String word :words) {
            // index tells which documents have a word
            if(index.containsKey(word)) {
                index.get(word).put(docID, true);
            }
            else {
                index.put(word, new HashMap<String, Boolean>());
                index.get(word).put(docID, true);
            }
        }

        return index;
    }
    private static HashMap<String, Double> calculateDocsIDF(int nDocs, HashMap<String, HashMap<String, Boolean>> index) {
        // HashMap<String, HashMap<String, Float>> token =new HashMap<>();
        HashMap<String, Double> token =new HashMap<>();
        for(String word :index.keySet()) {
            if(!token.containsKey(word)) {
                token.put(word, Math.log10(nDocs / (double)index.get(word).keySet().size()));
            }
        }
        return token;
    }
    private static HashMap<String, Double> calculateQueryTFIDF(List<String> query, HashMap<String, Double> token) {
        HashMap<String, Double> qTFIDF =new HashMap<String, Double>();

        for(String q :query) {
            if(qTFIDF.containsKey(q)) {
                qTFIDF.put(q, (double)qTFIDF.get(q) +1);
            }
            else {
                qTFIDF.put(q, (double)1);
            }
        }
        for(String q :qTFIDF.keySet()) {
            if(token.containsKey(q)) {
                qTFIDF.put(q, ((double)qTFIDF.get(q) /query.size()) *token.get(q));
            }
            else {
                qTFIDF.put(q, (double)0);
            }
        }

        return qTFIDF;
    }
    //private static List<HashMap<String, String>> calculateRank(String query, HashMap<String, String> docs, HashMap<String, HashMap<String, Boolean>> index, HashMap<String, Double> token) {
    private static List<HashMap<String, String>> calculateRank(String query, HashMap<String, String> docs, HashMap<String, HashMap<String, Boolean>> index, HashMap<String, Double> token) {
        List<HashMap<String, String>> output =new ArrayList<HashMap<String, String>>();
        List<String> tQuery =Arrays.asList(query.split(" "));
        List<String> contain =new ArrayList<>();

        for(String word :tQuery) {
            if(index.containsKey(word)) {
                contain.addAll(new ArrayList<String>(index.get(word).keySet()));
            }
        }
        Set<String> uniqueDocs =new HashSet<String>(contain);
        HashMap<String, Double> qTFIDF =calculateQueryTFIDF(tQuery, token);

        // System.out.println("contain ==" +String.valueOf(contain.size()));
        // System.out.println("unique ==" +String.valueOf(uniqueDocs.size()));

        for(String docID :uniqueDocs) {
            String doc =docs.get(docID);
            double dTFIDF =0.0;
            double numerator =0.0;
            double dDenum =0.0;
            double qDenum =0.0;
            String[] sDoc =doc.split(" ");

            HashMap<String, Boolean> done =new HashMap<>();
            for(String word :sDoc) {
                if(!done.containsKey(word)) {
                    done.put(word, true);

                    double temp =doc.length() -doc.replace(word, "").length();
                    temp /=(double)sDoc.length;
                    temp *=token.get(word);
                    dDenum +=Math.pow(temp, 2);
                }
            }
            for(String q :qTFIDF.keySet()) {
                dTFIDF =doc.length() -doc.replace(q, "").length();
                
                if(dTFIDF !=0) {

                    dTFIDF /=(double)doc.split(" ").length;

                    dTFIDF *=token.get(q);

                    numerator +=qTFIDF.get(q) *dTFIDF;

                    // dDenum +=(double) Math.pow(dTFIDF, 2);
                    qDenum +=(double) Math.pow(qTFIDF.get(q), 2);
                }
            }
            double result =(numerator /(Math.sqrt(dDenum) *Math.sqrt(qDenum)));

            System.out.print(result);
            System.out.println();

            HashMap<String, String> temp =new HashMap<>();
            temp.put("docID", docID);
            temp.put("body", doc);
            temp.put("score", String.valueOf(result));

            output.add(temp);

            // System.out.println("here");
            // System.out.println(String.valueOf(numerator));
            // System.out.println(String.valueOf(dDenum));
            // System.out.println(String.valueOf(qDenum));
            // System.out.println(String.valueOf(result));
            // System.out.println("there");
            // System.out.println(doc);
            // System.exit(0);
        }

        return output;
    }

    public static void main(String[] args) {
        HashMap<String, String> docs =new HashMap<>();     // docID ->body
        HashMap<String, HashMap<String, Boolean>> index =new HashMap<>();   // word ->docID*, true
        HashMap<String, Double> token =new HashMap<>();     // word ->IDF, double
        String query ="orbach hours";
        
        int counter =0;

        try(BufferedReader br =Files.newBufferedReader(Paths.get("./dd.dat"), StandardCharsets.UTF_8)) {
            for(String line =null; (line =br.readLine()) !=null;) {
                counter++;
                String[] elements = line.split("@@@@@");
                
                if(elements.length ==3) {
                    line =elements[1] +" " +elements[2];
                }
                else if(elements.length ==4) {
                    line =elements[1] +" " +elements[2] +" " +elements[3];
                }
                docs.put(elements[0], line);
                index =createIndex(index, elements[0], line);
            }
        }
        catch(IOException ex) {
            System.out.println("reading dd.dat error");
            System.out.println(ex);
        }
        token =calculateDocsIDF(docs.size(), index);
        // System.out.println(docs.size());
        // System.out.println(index.size());
        // System.out.println(counter);

        List<HashMap<String, String>> result =calculateRank(query, docs, index, token);
        System.out.println("here");
    }
}
