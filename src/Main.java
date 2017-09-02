import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

public class Main {

	public static void main(String[] args) {
		
		System.out.println("Searching for organism list...");
		OrganismList ol = makeOrganismList();
		
		Scanner sc = new Scanner(System.in);
		System.out.print("enter keywords: ");
		String keyword = sc.nextLine();
		
		search(ol, keyword);
		
		
	}
	
	/**
	 * title: makeOrganismList
	 * param: 
	 * return: OrganismList object of the list
	 */
	static OrganismList makeOrganismList(){
		OrganismList ol = new OrganismList();
		try {
			Connection con = Jsoup.connect("http://rest.kegg.jp/list/organism");
			String s = con.execute().body();
			ol = new OrganismList(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ol;
	}
	
	static boolean search(OrganismList ol, String keyword){
		boolean ret = false;
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
			ret = true;
		}
		return ret;
	}
}