package gui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import Kegg.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Crawl.init();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("kegg.fxml"));
        loader.load();
        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            for(Task t: controller.tasks){
                System.out.print("\tcanceling "+t.toString()+": ");
                System.out.println(t.cancel(true));
            }
            controller.exe.shutdown();
        });

        Parent root = loader.getRoot();
        primaryStage.setTitle("Kegg Crawler");
        primaryStage.setScene(new Scene(root, 800, 640));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
