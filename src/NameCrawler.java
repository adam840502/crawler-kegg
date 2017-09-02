import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class NameCrawler extends Thread {
	
	List<SingleClass> classList;
	String type;
	String org;
	FileOutputStream fos;

	NameCrawler(List<SingleClass> classList, String type, FileOutputStream fos, String org){
		this.classList = classList;
		this.type = type;
		this.fos = fos;
		this.org = org;
	}
	
	//thread run
	@Override
	public void run() {
			try {
				String text = Jsoup.connect(Main.linkUrl + org + "/" + type).execute().body();
				
				Scanner sc = new Scanner(text);
				String line = "";
				int column = 0;
				int cell = 0;
				String word = "";
				String classId = "";
				String geneId = "";
				String geneName = "";
				while(sc.hasNext()){
					line = sc.nextLine();
					InputStream is = new ByteArrayInputStream(line.getBytes());
					while((cell = is.read()) > 0){
						if(cell == ':'){
							word = "";
						}else if(cell == 9){
							classId = word;
							word = "";
							++column;
						}else if(cell == 10){
							geneId = word;
							word = "";
							column = 0;
						}else{
							word += Character.toString((char)cell);
						}
					}
					System.out.println(classId+"\n\t"+geneId+"\n\t"+geneName);
					Document doc = Jsoup.connect(Main.orthologyUrl+org+":"+geneId).get();
					Elements pres = doc.select("pre");
					
					for(Element pre : pres){
						List<Node> cns = pre.childNodes();
						int i = 0;
						for(Node cn : cns){
							if(i==2){
								//System.out.println(cn.toString());
								geneName = cn.toString().split(";")[0].replaceAll(" ", "");
							}
							++i;
						}
					}
					for(SingleClass tempClass : classList){
						if(tempClass.id.equals(classId)){
							tempClass.addGene(geneId, geneName);
						}
					}
				}
				write();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	void write(){
		for(SingleClass tempClass : classList){
			try {
				fos.write(tempClass.id.getBytes());
				fos.write(9);
				fos.write(tempClass.name.getBytes());
				fos.write(10);
				for(Gene g : tempClass.list){
					fos.write(9);
					fos.write(9);
					fos.write(("LOC"+g.id).getBytes());
					fos.write(9);
					fos.write(g.name.getBytes());
					fos.write(10);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
