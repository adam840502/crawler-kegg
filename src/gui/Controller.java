package gui;

import Kegg.*;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Controller implements Initializable{

    final static int THREAD_NUMBER = 4;

    @FXML Pane pane;
    @FXML HBox navBar;
    @FXML TableView<Organ> table;
    @FXML TableColumn<Organ, String> tNumber;
    @FXML TableColumn<Organ, String> org;
    @FXML TableColumn<Organ, String> scientificName;
    @FXML TableColumn<Organ, String> keywords;
    @FXML TextField searchingField;
    @FXML Button addButton;
    @FXML ListView<String> remainList;
    @FXML VBox leftDownBox;
    @FXML CheckBox pathwayBox;
    @FXML CheckBox moduleBox;
    @FXML CheckBox geneAbbrevBox;
    @FXML CheckBox ncbiBox;

    ExecutorService exe;
    List<Task> tasks;
    List<Organ> organList;
    List<Organ> searchResult;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //init table
        organList = Crawl.getOrganList();
        for(Organ or : organList){
            table.getItems().add(or);
        }
        scientificName.setMinWidth(200);
        keywords.setMinWidth(400);
        tNumber.setCellValueFactory(new PropertyValueFactory<>("tNumber"));
        org.setCellValueFactory(new PropertyValueFactory<>("org"));
        scientificName.setCellValueFactory(new PropertyValueFactory<>("scientificName"));
        keywords.setCellValueFactory(new PropertyValueFactory<>("keywords"));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //searching field listener
        searchingField.textProperty().addListener( (v, oldValue, newValue) -> {
            searchResult = Crawl.search(newValue);
            table.getItems().clear();
            for(Organ or : searchResult){
                table.getItems().add(or);
            }
        });

        pathwayBox.setSelected(true);
        //moduleBox.setSelected(true);
        geneAbbrevBox.setSelected(true);
        ncbiBox.setSelected(true);
        //searchingField.setText("Ory");

        exe = Executors.newFixedThreadPool(THREAD_NUMBER);
        tasks = new ArrayList<>();

        /*for(Node n : pane.getChildren()){
            System.out.println(n.toString());
        }*/
    }

    public void addButtonClicked(){

        List<Organ> organSelectedList = table.getSelectionModel().getSelectedItems();

        for(Organ organ : organSelectedList){
            if(pathwayBox.isSelected()){
                TypeTask pathwayTask = createTypeTask(organ, "pathway");
                this.tasks.add(pathwayTask);
            }
            if(moduleBox.isSelected()){
                TypeTask moduleTask = createTypeTask(organ,  "module");
                this.tasks.add(moduleTask);
            }
        }
    }

    public void startButtonClicked(){
        organList = null;
        searchResult = null;
        pane.getChildren().removeAll(table, navBar);
        Crawl.freeOrganList();
        for(Task task : tasks){
            exe.submit(task);
        }
    }

    private TypeTask createTypeTask(Organ organ, String type){

        TypeTask task = new TypeTask(organ, type, geneAbbrevBox.isSelected(), ncbiBox.isSelected());

        //info will reg into the remain list
        String info = "["+type+"] "+organ.getOrg()+"("+organ.getScientificName()+")";

        //create hBox for each typeTask and display in the leftDownBox
        Label label = new Label("new");
        ProgressBar geneProgressBar = new ProgressBar();
        geneProgressBar.progressProperty().bind(task.progressProperty());
        geneProgressBar.setMinWidth(250);
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.getChildren().addAll(label, geneProgressBar);

        task.messageProperty().addListener((v, o, n) -> {

            switch (n) {
                case "start":
                    leftDownBox.getChildren().add(hBox);
                    break;
                case "done":
                    leftDownBox.getChildren().remove(hBox);
                    remainList.getItems().remove(info);
                    break;
                default:
                    label.setText(n);
                    break;
            }
        });

        remainList.getItems().add(info);
        return task;
    }
}
