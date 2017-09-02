import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;


public class ListCrawler extends Thread{
	
	Organ or;
	File pathwayFile;
	File moduleFile;
	
	//constructors
	ListCrawler(Organ or){
		
		this.or = or;

		File dir = new File("result");
		if(!dir.exists()){
			dir.mkdir();
		}
		pathwayFile = new File("result/kegg-"+or.scientificName+
				              "("+or.TNumber+")-pathway.txt");
		moduleFile = new File("result/kegg-"+or.scientificName+
					          "("+or.TNumber+")-module.txt");
	}

	//thread run
	@Override
	public void run() {
		try {
			String pathway = Jsoup.connect(Main.pathwayUrl + or.org).execute().body();
			String module = Jsoup.connect(Main.moduleUrl + or.org).execute().body();
			FileOutputStream pathwayOs = new FileOutputStream(pathwayFile);
			FileOutputStream moduleOs = new FileOutputStream(moduleFile);
			
			List<SingleClass> pathwayList = parseClassList(pathway);
			List<SingleClass> moduleList = parseClassList(module);
			
			NameCrawler pathwayCrawler = new NameCrawler(pathwayList, "pathway", pathwayOs, or.org);
			NameCrawler moduleCrawler = new NameCrawler(moduleList, "module", moduleOs, or.org);
			pathwayCrawler.start();
			moduleCrawler.start();
			
			pathwayOs.close();
			moduleOs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//method
	private List<SingleClass> parseClassList(String list) {
		List<SingleClass> classList = new ArrayList<>();
		Scanner sc = new Scanner(list);
		String line = "";
		String word = "";
		String id = "";
		String name = "";
		while(sc.hasNext()){
			line = sc.nextLine();
			InputStream is = new ByteArrayInputStream(line.getBytes());
			int cell = 0;
			
			try {
				while((cell = is.read()) > 0){
					if(cell == ':'){
						word = "";
					}else if(cell == 9){
						id = word;
						word = "";
					}else if(cell == 10){
						name = word;
						word = "";
						classList.add(new SingleClass(id, name));
					}else{
						word += Character.toString((char)cell);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sc.close();
		return classList;
	}
}

class SingleClass{
	String id;
	String name;
	List<Gene> list;
	
	SingleClass(String id, String name){
		this.id = id;
		this.name = name;
	}
	
	void addGene(String geneId, String geneName){
		Gene gene = new Gene(geneId, geneName);
		this.list.add(gene);
	}
}

class Gene{
	String id;
	String name;

	Gene(String id, String name){
		this.id = id;
		this.name = name;
	}
}