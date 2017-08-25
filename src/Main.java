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
		
		makeOrganismList();
		
		Scanner sc = new Scanner(System.in);
		System.out.print("enter key words: ");
		String keyWord = sc.nextLine();
		
		
		
	}
	
	static boolean makeOrganismList(){
		
		try {
			Connection con = Jsoup.connect("http://rest.kegg.jp/list/organism");
			
			String s = con.execute().body();
			OrganismList ol = new OrganismList(s);
		
			
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
}

class OrganismList {
	List<Organ> list = new ArrayList<Organ>();
	
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
			System.out.println(or.scientificName+"\t"+or.commomName);
			list.add(or);
		}
	}
}

class Organ{
	String TNumber;
	String org;
	String scientificName;
	String commomName;
	List<String> keyWords;
	
	Organ(String TNumber, String org, String NameRaw, String keyWordsRaw){
		keyWords = new ArrayList<>();
		
		this.TNumber = TNumber;
		this.org = org;
		
		int cell;
		String word = "";
		
		InputStream nameIs = new ByteArrayInputStream(NameRaw.getBytes());
		boolean hasCommomName = false;
		try {
			while((cell=nameIs.read())>0){
				if(cell == 32){
					if((cell=nameIs.read()) == 40){
						hasCommomName = true;
						this.scientificName = word;
						word = "";
					}else{
						word += " ";
						word += Character.toString((char)cell);
					}
				}else if(cell == 41){
					this.commomName = word;
				}else{
					word += Character.toString((char)cell);
				}
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
					keyWords.add(word);
					word = "";
				}else{
					word += Character.toString((char)cell);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		keyWords.add(word);
	}
}
