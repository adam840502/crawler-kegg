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
	
	int geneNumber[];
	List<SingleClass> classList;
	String type;
	String org;
	FileOutputStream fos;

	NameCrawler(List<SingleClass> classList, String type, FileOutputStream fos, String org){
		this.classList = classList;
		this.type = type;
		this.fos = fos;
		this.org = org;
		geneNumber = new int[classList.size()];
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
				String tempClassId = "";
				Boolean isFirstClass = true;
				
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
						}else{
							word += Character.toString((char)cell);
						}
					}
					geneId = word;
					
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
					
					
					for(int i=0; i<classList.size(); ++i){
						if(classList.get(i).id.equals(classId)){
							classList.get(i).addGene(geneId, geneName);
							++geneNumber[i];
							/*if(!classId.equals(tempClassId)){
								if(isFirstClass) isFirstClass = false;
								else writeOneClass(classList.get(i-1), geneNumber[i-1]);
							}
							System.out.println(classId+"  "+tempClassId);
							tempClassId = classList.get(i).id;
							*/
							
							//print
							System.out.print("["+type+"] "+classList.get(i).name + "  ");
							for(int j=0; j<geneNumber[i]/100; ++j){
								System.out.print("*");
							}
							for(int j=0; j<geneNumber[i]%100; ++j){
								System.out.print(".");
							}
							System.out.println();
							
							break;
						}
					}
					sleep(500);
				}
				write();
				
			} catch (IOException | InterruptedException e) {
				try {
					Main.logFos.write(("fail at NameCrawler.run():\n").getBytes());
					Main.logFos.write(("\torg: " + org).getBytes());
					Main.logFos.write(("\ttype: " + type).getBytes());
					Main.logFos.write(13);
					Main.logFos.write('\n');
					Main.logFos.write(e.toString().getBytes());
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
	
	void write(){
		int i = 0;
		for(SingleClass tempClass : classList){
			try {
				fos.write(tempClass.id.getBytes());
				fos.write(9);
				fos.write(tempClass.name.getBytes());
				fos.write(9);
				fos.write(Integer.toString(geneNumber[i]).getBytes());
				fos.write(10);
				for(Gene g : tempClass.list){
					fos.write(9);
					fos.write(9);
					fos.write(9);
					fos.write(g.name.getBytes());
					fos.write(9);
					fos.write(("LOC"+g.id).getBytes());
					fos.write(10);
				}
			} catch (IOException e) {
				try {
					Main.logFos.write(("fail at ListCrawler.parseClassList:\n").getBytes());
					Main.logFos.write(("\torg: " + org).getBytes());
					Main.logFos.write(("\ttype: " + type).getBytes());
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
		++i;	
		}
	}
	
	void writeOneClass(SingleClass singleClass, int geneNumber){
		
			try {
				fos.write(singleClass.id.getBytes());
				fos.write(9);
				fos.write(singleClass.name.getBytes());
				fos.write(9);
				fos.write(Integer.toString(geneNumber).getBytes());
				fos.write(10);
				for(Gene g : singleClass.list){
					fos.write(9);
					fos.write(9);
					fos.write(9);
					fos.write(g.name.getBytes());
					fos.write(9);
					fos.write(("LOC"+g.id).getBytes());
					fos.write(10);
				}
			} catch (IOException e) {
				try {
					Main.logFos.write(("fail at ListCrawler.parseClassList:\n").getBytes());
					Main.logFos.write(("\torg: " + org).getBytes());
					Main.logFos.write(("\ttype: " + type).getBytes());
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
}
