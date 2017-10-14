package crawler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;

public class Crawl {
	
	final public static String listUrl = "http://rest.kegg.jp/list/";
	final public static String linkUrl = "http://rest.kegg.jp/link/";
	final public static String orthologyUrl = "http://www.genome.jp/dbget-bin/get_linkdb?-t+orthology+";
    final public static String ncbiUrl = "http://www.genome.jp/dbget-bin/get_linkdb?-t+ncbi-geneid+";

	private static List<Organ> organList;
	private static FileOutputStream logFos;

	public static void init(String organ, String type) {
		
		try {
			logFos = new FileOutputStream(new File(organ+" "+type+" log.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		makeOrganList();
	}

	public static void freeOrganList(){
	    organList = null;
    }

	private static void makeOrganList(){

	    System.out.println("Searching for organism List...");

        String organListString = null;
        try {
            organListString = Jsoup.connect(listUrl+"organism/").execute().body();
        } catch (IOException e) {
            try {
                logFos.write("fail at Crawl.makeOrganList @connect to organList\r\n".getBytes());
                Crawl.logFos.write(e.getMessage().getBytes());
                Crawl.logFos.write('\r');
                Crawl.logFos.write('\n');
                Crawl.logFos.write('\r');
                Crawl.logFos.write('\n');
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

        organList = new ArrayList<Organ>();
        Scanner listSc = new Scanner(organListString);
        String line;
        while(listSc.hasNext()){
            line = listSc.nextLine();
            InputStream is = new ByteArrayInputStream(line.getBytes());

            int cell;
            int column = 0;
            String word = "";
            String TNumber = "";
            String org = "";
            String NameRaw = "";
            String keyWordsRaw = "";
            try {
                while((cell = is.read())>0){
                    if(cell == 9){
                        if(column==0){
                            TNumber = word;
                        }else if(column==1){
                            org = word;
                        }else if(column==2){
                            NameRaw = word;
                        }
                        word = "";
                        ++column;
                    }else{
                        word += Character.toString((char)cell);
                    }
                }
                keyWordsRaw = word;
            } catch (IOException e) {
                try {
                    Crawl.logFos.write(("fail at Crawl.makeOrganList @parse in line:\r\n").getBytes());
                    Crawl.logFos.write(13);
                    Crawl.logFos.write('\n');
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
            organList.add(new Organ(TNumber, org, NameRaw, keyWordsRaw));
        }
        listSc.close();
	}
	
	public static Organ search(String keyword){
		for(Organ organ : organList){
			if(organ.getOrg().matches(keyword))
			    return organ;
		}
		return null;
	}

    public static FileOutputStream getLogFos() {
        return logFos;
    }
}