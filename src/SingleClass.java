import java.util.ArrayList;
import java.util.List;

public class SingleClass{
	String id;
	String name;
	List<Gene> list;
	
	SingleClass(String id, String name){
		this.id = id;
		this.name = name;
		this.list = new ArrayList<>();
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