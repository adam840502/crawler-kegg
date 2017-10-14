package crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args){
        String org = args[0];
        String type = args[1];
        Crawl.init(org, type);
        Organ organ = Crawl.search(org);
        Crawl.freeOrganList();
        ExecutorService exe = Executors.newFixedThreadPool(1);
        Thread t = new Thread(new TypeTask(organ, type, true, true));
        exe.submit(t);
    }
}
