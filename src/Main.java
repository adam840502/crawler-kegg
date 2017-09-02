import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class Main {
	
	final public static String OrganismUrl = "http://rest.kegg.jp/list/organism";
	final public static String pathwayUrl = "http://rest.kegg.jp/list/pathway/";
	final public static String moduleUrl = "http://rest.kegg.jp/list/module/";
	final public static String linkUrl = "http://rest.kegg.jp/link/";
	final public static String orthologyUrl = "http://www.genome.jp/dbget-bin/get_linkdb?-t+orthology+";
	
	
	public static void main(String[] args) {
		
		
		OrganismList ol = makeOrganismList();
		
		Scanner sc = new Scanner(System.in);
		System.out.print("enter keywords: ");
		String keyword = sc.nextLine();
		
		List<Organ> match = search(ol, keyword);
		
		System.out.print("enter target index(use \',\' to separate targets): ");
		String target = sc.nextLine();
		
		startRequests(target, match);
		sc.close();
	}
	
	static void startRequests(String target, List<Organ> match) {
		
		//parse request
		String[] targetArr = target.replace(" ", "").split(",");
		int[] targetIntArr = new int[targetArr.length];
		for(int i=0;i<targetArr.length;++i){
			targetIntArr[i] = Integer.parseInt(targetArr[i])-1;
		}
		
		//get url
		for(int targetInt : targetIntArr){
			Organ or = match.get(targetInt);
			ListCrawler lc = new ListCrawler(or);
			lc.start();
			
		}
	}

	/**
	 * title: makeOrganismList
	 * param: 
	 * return: OrganismList object of the list
	 */
	static OrganismList makeOrganismList(){
		System.out.println("Searching for organism list...");
		OrganismList ol = new OrganismList();
		try {
			Connection con = Jsoup.connect(OrganismUrl);
			String s = con.execute().body();
			ol = new OrganismList(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ol;
	}
	
	static List<Organ> search(OrganismList ol, String keyword){
		List<Organ> match = ol.search(keyword);
		if(match.isEmpty()){
			System.out.println("no matches.");
		}else{
			System.out.println("matches("+match.size()+"):");
			int i =1;
			for(Organ or : match){
				System.out.print(i+".\t");
				System.out.print(or.TNumber);
				System.out.print("  ");
				System.out.print(or.org);
				System.out.print("  ");
				System.out.print(or.scientificName);
				System.out.print("    ");
				System.out.print(or.keywords);
				System.out.println();
				++i;
			}
		}
		return match;
	}
}