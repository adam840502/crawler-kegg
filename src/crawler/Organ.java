package crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Organ{
    private String tNumber;
    private String org;
    private String scientificName;
    List<String> keywords;

    public Organ(){
        this.tNumber = "T";
        this.org = "unavailable";
        this.scientificName = "unavailable";
        this.keywords = new ArrayList<>();
    }

    Organ(String TNumber, String org, String NameRaw, String keyWordsRaw){
        keywords = new ArrayList<>();

        this.tNumber = TNumber;
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
            try {
                Crawl.getLogFos().write(("fail at Organ.Organ:\n").getBytes());
                Crawl.getLogFos().write(("\torg: " + org).getBytes());
                Crawl.getLogFos().write(13);
                Crawl.getLogFos().write('\n');
                Crawl.getLogFos().write(e.getMessage().getBytes());
                Crawl.getLogFos().write(13);
                Crawl.getLogFos().write('\n');
                Crawl.getLogFos().write(13);
                Crawl.getLogFos().write('\n');
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
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
            try {
                Crawl.getLogFos().write(("fail at Organ.Organ:\n").getBytes());
                Crawl.getLogFos().write(("\torg: " + org).getBytes());
                Crawl.getLogFos().write(13);
                Crawl.getLogFos().write('\n');
                Crawl.getLogFos().write(e.getMessage().getBytes());
                Crawl.getLogFos().write(13);
                Crawl.getLogFos().write('\n');
                Crawl.getLogFos().write(13);
                Crawl.getLogFos().write('\n');
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        keywords.add(word);
    }

    public String getTNumber() {
        return tNumber;
    }

    public void setTNumber(String tNumber) {
        this.tNumber = tNumber;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.tNumber = this.scientificName;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}