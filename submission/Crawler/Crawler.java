import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;

public class Crawler {
    static void extractAndMergeLinks(List<Element> els, List<String> links, List<String> skipURL, HashMap<String, Boolean> visited) {
        try {
            for(Element ele :els) {
                String link =ele.attr("abs:href");
                if(!visited.containsKey(link) &&!links.contains(link) &&!skipURL.contains(link) &&!link.endsWith(".pdf")) {
                    links.add(ele.attr("abs:href"));
                }
            }
        }
        catch(Exception ex) {
            System.out.println("Erorr getting linked links");
        }
    }
    static String removeStopWords(String body, List<String> stopWords) {
        for(String word :stopWords) {
            body.replaceAll(word, "");
        }
        return body;
    }
    static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e) {
            System.out.println("Invalid URL");
            return false;
        }
    }
    static void savePages(List<String> pages) {
        try {
            FileWriter fwriter =new FileWriter("data.dat");
            for(String page: pages) {
                fwriter.write(page + System.lineSeparator());
            }
            fwriter.close();
        }
        catch(IOException ex) {
            System.out.println(ex);
        }
    }
    static void saveProgress(int index, List<String> links, List<String> skipURL, HashMap<String, Boolean> visited) {
        try {
            FileWriter fwriter =new FileWriter("progress.dat");
            fwriter.write(index +System.lineSeparator());
            for(String link: links) {
                fwriter.write(link + System.lineSeparator());
            }
            fwriter.close();

            fwriter =new FileWriter("skipurl.dat");
            for(String link: skipURL) {
                fwriter.write(link + System.lineSeparator());
            }
            fwriter.close();

            fwriter =new FileWriter("visited.dat");
            for(String link: visited.keySet()) {
                fwriter.write(link + System.lineSeparator());
            }
            fwriter.close();
        }
        catch(IOException ex) {
            System.out.println(ex);
        }
    }
    public static void main(String[] args) {
        // if(args.length <2) {
        //     System.out.println("Usage java main <urlseeds> <stopwords>");
        //     System.exit(-1);
        // }
        int dataSize =0;
        List<String> links =new ArrayList<String>();
        List<String> stopWords =new ArrayList<String>();
        List<String> pages =new ArrayList<String>();
        List<String> skipURL =new ArrayList<String>();
        HashMap<String, Boolean> visited =new HashMap<String, Boolean>();
        HashMap<String, Boolean> visitedHash =new HashMap<String, Boolean>();

        try {
            Scanner s =new Scanner(new File("src/stopwords_en.txt"));
            // Scanner s =new Scanner(new File(args[1]));

            while(s.hasNext()) {
                stopWords.add(s.next());
            }

            s.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(ex);
        }

        try {
            Scanner s =new Scanner(new File("src/urlseeds.dat"));
            // Scanner s =new Scanner(new File(args[0]));

            while(s.hasNext()) {
                links.add(s.next());
            }

            s.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(ex);
        }

        // try {
            for(int i =0; i <links.size(); i++) {
                savePages(pages);
                saveProgress(i, links, skipURL, visited);
                if(!visited.containsKey(links.get(i)) &&!skipURL.contains(links.get(i)) &&(links.get(i).startsWith("https") ||links.get(i).startsWith("http"))) {
                    Document doc =null;
                    try {
                        if(isValidURL(links.get(i))) {
                            doc =Jsoup.connect(links.get(i)).get();
                            // System.out.println(doc.title());
                        }
                        else {
                            continue;
                        }
                    }
                    catch(IOException ex) {
                        System.out.println("Error getting page");
                        System.out.println(links.get(i));
                        System.out.println("Message =" +ex.getMessage());

                        skipURL.add(links.get(i));
                        continue;
                    }

                    String body =doc.body().text();

                    try {
                        MessageDigest digest =MessageDigest.getInstance("SHA-256");
                        byte[] hash =digest.digest(body.getBytes(StandardCharsets.UTF_8));
                        String hex =String.format("%064x", new BigInteger(1, hash));

                        if(visitedHash.containsKey(hex)) {
                            continue;
                        }
                        else {
                            visitedHash.put(hex, true);
                        }
                    }
                    catch(NoSuchAlgorithmException ex) {
                        System.out.println("Failed to hash body");
                    }

                    String title =doc.title();
                    String subHeader ="";
                    List<Element> els =doc.select("h3");

                    for(Element el :els) {
                        subHeader += el.text() +" ";
                    }

                    String page ="";
                    if(subHeader.length() ==0) {
                        page =title +"@@@@@" +body;
                    }
                    else {
                        page =title +"@@@@@" +subHeader +"@@@@@" +body;
                    }

                    
                    pages.add(removeStopWords(page, stopWords));
                    dataSize +=page.length() *2;

                    visited.put(links.get(i), true);

                    extractAndMergeLinks(doc.select("a"), links, skipURL, visited);
                }
                System.out.println(String.valueOf(dataSize));
                System.out.println("pages.length() =" +String.valueOf(pages.size()));
                System.out.println("links.length() =" +String.valueOf(links.size()));

                if(i %10 ==0) {
                    System.out.println(String.valueOf(i) +"/" +String.valueOf(links.size()));
                }

                if((dataSize /Math.pow(10, 9)) >= 1) {
                    savePages(pages);
                    System.exit(0);
                }
            }
            savePages(pages);
        // }
        // catch(Exception ex) {
        //     System.out.println("Something went wrong");
        //     System.out.println(ex);
        // }
    }
}