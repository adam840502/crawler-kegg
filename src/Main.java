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
		System.out.print("enter key words: ");
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

class OrganismList {
	List<Organ> list = new ArrayList<Organ>();
	
	OrganismList(){}
	
	OrganismList(String data){
		
		Scanner lineSc = new Scanner(data);
		String organ;
		
		while(lineSc.hasNext()){
			organ = lineSc.nextLine();
			InputStream is = new ByteArrayInputStream(organ.getBytes());
			
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Organ or = new Organ(TNumber, org, NameRaw, keyWordsRaw);
			//System.out.println(or.TNumber+"  "+or.org+"  "+or.scientificName+", "+
			//					or.commomName+", "+or.keyWords);
			list.add(or);
		}
	}

	List<Organ> search(String keyword){
		List<Organ> match = new ArrayList<>();
		for(Organ or : list){
			boolean tNumMatch = or.TNumber.matches("(.|\n)*"+keyword+"(.|\n)*");
			boolean sciMatch = or.scientificName.matches("(.|\n)*"+keyword+"(.|\n)*");
			boolean keyMatch = false;
			for(String s : or.keywords){
				if(s.matches("(.|\n)*"+keyword+"(.|\n)*")) keyMatch = true;
			}
			if(tNumMatch | sciMatch | keyMatch){
				match.add(or);
			}
		}
		return match;
	}
}

class Organ{
	String TNumber;
	String org;
	String scientificName;
	List<String> keywords;
	
	Organ(String TNumber, String org, String NameRaw, String keyWordsRaw){
		keywords = new ArrayList<>();
		
		this.TNumber = TNumber;
		this.org = org;
		
		int cell;
		String word = "";
		
		InputStream nameIs = new ByteArrayInputStream(NameRaw.getBytes());
		boolean hasParens = false;
		int parensNum = 0;
		try {
			while((cell=nameIs.read())>0){
				if(cell == 32){
					if((cell=nameIs.read()) == 40){
						hasParens = true;
						++parensNum;
						if(parensNum==1){
							this.scientificName = word;
						}else{
							keywords.add(word);
						}
						word = "";
					}else{
						word += " ";
						word += Character.toString((char)cell);
					}
				}else if(cell == 41){
					this.keywords.add(word);
					word = "";
				}else{
					word += Character.toString((char)cell);
				}
			}
			if(!hasParens){
				this.scientificName = word;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		word = "";
		InputStream keyIs = new ByteArrayInputStream(keyWordsRaw.getBytes());
		try {
			while((cell=keyIs.read())>0){
				if(cell == 59){
					keywords.add(word);
					word = "";
				}else{
					word += Character.toString((char)cell);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		keywords.add(word);
	}
}
