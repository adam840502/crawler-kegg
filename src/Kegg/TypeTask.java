package Kegg;

import javafx.concurrent.Task;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class TypeTask extends Task{

    private static final int SLEEP_TIME = 200;
    private static final String LIB_NAME = "kegg";

    private Organ organ;
    private String type;
    private int classNum;
    private boolean isGettingGeneAbbrev;
    private boolean isGettingNcbi;
    private int[] geneNumbers;
    private LinkedList<SingleClass> classList;
    private FileOutputStream classOutputStream;


    public TypeTask(Organ organ, String type, boolean isGettingGeneAbbrev, boolean isGettingNcbi){
        this.organ = organ;
        this.type = type;
        this.isGettingGeneAbbrev = isGettingGeneAbbrev;
        this.isGettingNcbi = isGettingNcbi;
    }

    @Override
    protected Object call() throws Exception {

        updateMessage("start");

        //create dir, file and outputStream
        File dir = new File("result");
        if(!dir.exists()){
            dir.mkdir();
        }
        File classFile = new File("result/kegg-"+ organ.getScientificName() +
                "("+organ.getTNumber()+")-"+type+".txt");
        classOutputStream = new FileOutputStream(classFile);

        writeHeader();

        //get class geneList of a type of an organ and parse to data structure
        String classListString = Jsoup.connect(Crawl.listUrl+type+"/"+organ.getOrg()).execute().body();
        classList = classListToLinkedList(classListString);

        geneNumbers = new int[classNum];

        //get link geneList of classes and genes
        String linkListString = Jsoup.connect(Crawl.linkUrl + organ.getOrg() + "/" + type).execute().body();
        getGeneNumbers(linkListString);
        readClassLinkGeneListString(linkListString);

        //write();
        updateMessage("done");

        return null;
    }

    //parsing the geneList with classes to List<SingleClass>
    private LinkedList<SingleClass> classListToLinkedList(String list) {

        LinkedList<SingleClass> classList = new LinkedList<>();

        //Scan
        Scanner sc = new Scanner(list);
        String line;
        String word = "";

        String id = "";
        String name;

        while(sc.hasNext()){
            line = sc.nextLine();
            InputStream is = new ByteArrayInputStream(line.getBytes());
            int cell;

            try {
                while((cell = is.read()) > 0){
                    switch (cell) {
                        case ':':
                            word = "";
                            break;
                        case '\t':
                            id = word;
                            word = "";
                            break;
                        default:
                            word += Character.toString((char) cell);
                            break;
                    }
                }
                name = word;

                classList.add(new SingleClass(id, name));

            } catch (IOException e) {
                try {
                    Crawl.getLogFos().write(("fail at OrganCrawler.parseClassList:\n").getBytes());
                    Crawl.getLogFos().write(("\torgan: " + organ).getBytes());
                    Crawl.getLogFos().write(("\ttype: " + this.type).getBytes());
                    Crawl.getLogFos().write(13);
                    Crawl.getLogFos().write('\n');
                    Crawl.getLogFos().write(e.getMessage().getBytes());
                    Crawl.getLogFos().write(13);
                    Crawl.getLogFos().write('\n');
                    Crawl.getLogFos().write(13);
                    Crawl.getLogFos().write('\n');
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }

        classNum = classList.size();

        sc.close();
        return classList;
    }

    private void getGeneNumbers(String linkListString){

        updateMessage("get gene numbers");

        Scanner geneNumberSc = new Scanner(linkListString);
        try {
            boolean isFirstClass = true;
            int classIndex = 0;
            int geneNumber = 0;
            String lastClassId = "";
            String line;
            while (geneNumberSc.hasNext()) {
                line = geneNumberSc.nextLine();
                InputStream is = new ByteArrayInputStream(line.getBytes());
                StringBuilder word = new StringBuilder();
                int cell;
                label:
                while ((cell = is.read()) > 0) {
                    switch (cell) {
                        case ':':
                            word = new StringBuilder();
                            break;
                        case '\t':
                            break label;
                        default:
                            word.append(Character.toString((char) cell));
                            break;
                    }
                }

                if (!lastClassId.equals(word.toString())) {
                    if (isFirstClass) {
                        isFirstClass = false;
                    } else {
                        geneNumbers[classIndex] = geneNumber;
                        ++classIndex;
                    }
                    geneNumber = 1;
                    lastClassId = word.toString();
                } else {
                    ++geneNumber;
                }
            }
            geneNumbers[classNum-1] = geneNumber;
        }catch (IOException e){
            try {
                Crawl.getLogFos().write(("fail at TypeTask.getGeneNumbers:\r\n").getBytes());
                Crawl.getLogFos().write(("\torgan: " + organ.getOrg()).getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
                Crawl.getLogFos().write(e.getMessage().getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void readClassLinkGeneListString(String linkListString){

        updateMessage("start crawling");

        try{
            String classId = "";
            String geneAbbrev;
            String geneKeggId;
            String geneNcbiId;


            Scanner sc = new Scanner(linkListString);
            String line;
            int cell;
            String word = "";

            int classIndex = -1;
            int geneIndex = 0;
            String lastClassId = "";

            //for each gene(line) in the link list
            while (sc.hasNext()) {
                line = sc.nextLine();
                InputStream is = new ByteArrayInputStream(line.getBytes());
                while ((cell = is.read()) > 0) {
                    switch (cell) {
                        case ':':
                            word = "";
                            break;
                        case '\t':
                            classId = word;
                            word = "";
                            break;
                        default:
                            word += Character.toString((char) cell);
                            break;
                    }
                }
                geneKeggId = word;

                if(isGettingGeneAbbrev){
                    geneAbbrev = getGeneAbbrev(geneKeggId);
                }else{
                    geneAbbrev = "Null";
                }
                if(isGettingNcbi){
                    geneNcbiId = getGeneNcbi(geneKeggId);
                }else{
                    geneNcbiId = "Null";
                }

                if (!lastClassId.equals(classId)) {

                    SingleClass newClass = classList.poll();
                    if(!newClass.getId().equals(classId)){
                        System.out.println("wrong while finding class:\n");
                    }

                    ++classIndex;
                    writeSingleClass(newClass, classIndex);

                    geneIndex = 0;
                    lastClassId = classId;

                } else {
                    ++geneIndex;
                }
                writeSingleGene(geneAbbrev, geneKeggId, geneNcbiId);

                //update property
                updateProgress(geneIndex+1, geneNumbers[classIndex]);

                String tempMessage = "[" + type + "]" + organ.getOrg() + " "+
                        Integer.toString(classIndex+1)+"/"+Integer.toString(classNum);
                updateMessage(tempMessage);

		System.out.println("[" + type + "]" + organ.getOrg() + " "+
                        Integer.toString(classIndex+1)+"/"+Integer.toString(classNum)+"  "+
			Integer.toString(geneIndex+1)+"/"+Integer.toString(geneNumbers[classIndex]));

                //check if task is canceled
                if(isCancelled())  return;
            }
        } catch (IOException e) {

            try {
                Crawl.getLogFos().write(("fail at TypeTask.readClassLinkGeneListString @write detail:\r\n").getBytes());
                Crawl.getLogFos().write(("\torgan: " + organ.getOrg() + "\r\n").getBytes());
                Crawl.getLogFos().write(e.getMessage().getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private String getGeneAbbrev(String geneKeggId){

        String result="";
        Document doc = null;
        try {
            Thread.sleep(SLEEP_TIME);
            doc = Jsoup.connect(Crawl.orthologyUrl+organ.getOrg()+":"+geneKeggId).get();
            Elements pres = doc.select("pre");

            for(Element pre : pres){
                List<Node> children = pre.childNodes();
                int i = 0;
                for(Node child : children){
                    if(i==2){
                        result = child.toString().split(";")[0].replaceAll(" ", "");
                    }
                    ++i;
                }
            }

        } catch (IOException e) {
            try {
                Crawl.getLogFos().write(("fail at TypeTask.getGeneAbbrev("+geneKeggId+"(:\r\n").getBytes());
                Crawl.getLogFos().write(("\torgan: " + organ.getOrg()+"\r\n").getBytes());
                Crawl.getLogFos().write(e.getMessage().getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
                Crawl.getLogFos().write(("geneKeggId: "+geneKeggId+"\r\n").getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (InterruptedException e){
            System.out.println("Interrupted at blocking call");
        }
        if(doc != null){
            doc.empty();
        }
        return result;
    }

    private String getGeneNcbi(String geneKeggId){

        String result="";
        Document doc = null;
        try {
            Thread.sleep(SLEEP_TIME);
            doc = Jsoup.connect(Crawl.ncbiUrl+organ.getOrg()+":"+geneKeggId).get();
            Elements pres = doc.select("pre");

            for(Element pre : pres){
                List<Node> children = pre.childNodes();
                int i = 0;
                for(Node child : children){
                    if(i==2){
                        if(!child.toString().equals("\n")){
                            result = child.toString().split(";")[0].replaceAll(" ", "");
                        }
                    }
                    ++i;
                }
            }

        } catch (IOException e) {
            try {
                Crawl.getLogFos().write(("fail at TypeTask.getGeneNcbi("+geneKeggId+"):\r\n").getBytes());
                Crawl.getLogFos().write(("\torgan: " + organ.getOrg()+"\r\n").getBytes());
                Crawl.getLogFos().write(("\tgeneKeggId: "+geneKeggId+"\r\n").getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
                Crawl.getLogFos().write(e.getMessage().getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
                Crawl.getLogFos().write("\r\n".getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (InterruptedException e){
            System.out.println("Interrupted at blocking call");
        }
        if(doc != null) doc.empty();
        return result;
    }

    private void writeHeader(){

        try {
            classOutputStream.write((type+" index").getBytes());
            classOutputStream.write('\t');
            classOutputStream.write((type+" name").getBytes());
            classOutputStream.write('\t');
            classOutputStream.write("gene numbers".getBytes());
            classOutputStream.write('\t');
            classOutputStream.write("gene abbrev".getBytes());
            classOutputStream.write('\t');
            classOutputStream.write((LIB_NAME+" id").getBytes());
            classOutputStream.write('\t');
            classOutputStream.write(("ncbi id").getBytes());
            classOutputStream.write("\n".getBytes());

        } catch (IOException e) {
            try {
                Crawl.getLogFos().write(("fail at TypeTask.writeHeader:\r\n").getBytes());
                Crawl.getLogFos().write(("\torg: " + organ.getOrg()+"\r\n").getBytes());
                Crawl.getLogFos().write(("\ttype: " + type+"\r\n").getBytes());
                Crawl.getLogFos().write('\r');
                Crawl.getLogFos().write('\n');
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void writeSingleClass(SingleClass singleClass, int classIndex){

        try {
            classOutputStream.write(singleClass.getId().getBytes());
            classOutputStream.write('\t');
            classOutputStream.write(singleClass.getName().getBytes());
            classOutputStream.write('\t');
            classOutputStream.write(Integer.toString(geneNumbers[classIndex]).getBytes());
            classOutputStream.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeSingleGene(String geneAbbrev, String geneKeggId, String geneNcbiId){

        try {
            classOutputStream.write("\t\t\t".getBytes());
            classOutputStream.write(geneAbbrev.getBytes());
            classOutputStream.write('\t');
            classOutputStream.write(geneKeggId.getBytes());
            classOutputStream.write('\t');
            classOutputStream.write(geneNcbiId.getBytes());
            classOutputStream.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
