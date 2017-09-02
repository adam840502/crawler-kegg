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
			
			List<SingleClass> pathwayList = parseClassList(pathway, "pathway");
			List<SingleClass> moduleList = parseClassList(module, "module");
			
			NameCrawler pathwayCrawler = new NameCrawler(pathwayList, "pathway", pathwayOs, or.org);
			NameCrawler moduleCrawler = new NameCrawler(moduleList, "module", moduleOs, or.org);
			pathwayCrawler.start();
			moduleCrawler.start();
			
		} catch (IOException e) {
			try {
				Main.logFos.write(("fail at ListCrawler.run():\n").getBytes());
				Main.logFos.write(("\torgan: " + or).getBytes());
				Main.logFos.write(13);
				Main.logFos.write('\n');
				Main.logFos.write(e.getMessage().getBytes());
				Main.logFos.write(13);
				Main.logFos.write('\n');
				Main.logFos.write(13);
				Main.logFos.write('\n');
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//method
	private List<SingleClass> parseClassList(String list, String type) {
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
					}else{
						word += Character.toString((char)cell);
					}
				}
				name = word;
				classList.add(new SingleClass(id, name));
				
			} catch (IOException e) {
				try {
					Main.logFos.write(("fail at ListCrawler.parseClassList:\n").getBytes());
					Main.logFos.write(("\torgan: " + or).getBytes());
					Main.logFos.write(("\ttype: " + type).getBytes());
					Main.logFos.write(13);
					Main.logFos.write('\n');
					Main.logFos.write(e.getMessage().getBytes());
					Main.logFos.write(13);
					Main.logFos.write('\n');
					Main.logFos.write(13);
					Main.logFos.write('\n');
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
		sc.close();
		return classList;
	}
}