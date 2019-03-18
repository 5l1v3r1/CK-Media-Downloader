/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import downloaderProject.MainApp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Toggle;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author christopher
 */
public class QualityDialog {
    private static final int HEIGHT = 240, WIDTH = 400;
    private static ToggleGroup group;
    private String choice = null;
    
    
    public QualityDialog() {
        group = new ToggleGroup();
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old, Toggle newT) {
                 if (group.getSelectedToggle() != null)
                    choice = ((RadioButton)newT).getId();
            }
            
        });
    }
    
    private Stage createStage() {
        Stage s = new Stage();
        
        s.initModality(Modality.APPLICATION_MODAL);
        s.setMaxHeight(HEIGHT); s.setMaxWidth(WIDTH);
        s.setMinHeight(HEIGHT); s.setMinWidth(WIDTH);
        s.setResizable(false);
        
        return s;
    }
    
    public String display(Map<String,String> qualities, String mediaName) {
        Pane pane;
        try {
            Stage dialog = createStage(); dialog.setResizable(false); dialog.initStyle(StageStyle.UNDECORATED);
            pane = FXMLLoader.load(this.getClass().getResource("qualityDialog.fxml"));
            pane.getStylesheets().clear();
            //assume light theme is settings null
            pane.getStylesheets().add(MainApp.class.getResource("mainStyleSheet.css").toExternalForm());
            if (MainApp.settings == null) pane.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
            else {
                if(MainApp.settings.preferences.dark())
                    pane.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                else pane.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
            }
            Label text = (Label)pane.lookup("#mediaName"); text.setText(mediaName);
            Button ok = (Button)pane.lookup("#downloadButton");
            Button cancel = (Button)pane.lookup("#cancelButton");
            ListView<Pane> qualityList = (ListView<Pane>)pane.lookup("#qualities"); 
            qualityList.getStyleClass().clear(); qualityList.getStyleClass().add("qualityList");
            List<Pane> items = new ArrayList<>();
            List<String> q = getSortedFormats(qualities.keySet());
            for(String s: q)
                if (qualities.get(s) != null)
                    if (qualities.get(s).length() > 0)
                        items.add(createItem(s,qualities.get(s)));
            qualityList.getItems().addAll(items);
            ok.setOnAction(e -> dialog.close());
            cancel.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                        if (group.getSelectedToggle() != null)
                            group.getSelectedToggle().setSelected(false); 
                        dialog.close(); 
                    }
                });
                Scene scene = new Scene(pane);
                dialog.setScene(scene);
                dialog.showAndWait();
            } catch(IOException e) {
                e.printStackTrace();
            }
        if (group.getSelectedToggle() != null) {
            choice = ((RadioButton)group.getSelectedToggle()).getId();
            return choice;
        } else return null;
    }
    
    private Pane createItem(String quality, String link) throws IOException {
        Pane p = FXMLLoader.load(this.getClass().getResource("qualityItem.fxml"));
        if (MainApp.settings == null) p.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        else {
            if(MainApp.settings.preferences.dark())
                p.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else p.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        }
        RadioButton b = (RadioButton)p.lookup("#button");
        b.setId(quality); b.setToggleGroup(group);
        ((Label)p.lookup("#qualityName")).setText(quality);
        ((Label)p.lookup("#size")).setText(MainApp.getSizeText(CommonUtils.getContentSize(link)));
        return p;
    }
    
    private List<String> getSortedFormats(Set<String> list) {
        Iterator<String> i = list.iterator(); List<String> q = new ArrayList<>();
        while(i.hasNext())
            q.add(i.next());
        for(int j = 0; j < q.size() -1; j++) { //selection sort
            int min = j;
            for(int k = j+1; k < q.size(); k++)
                if(CommonUtils.getFormatWeight(q.get(k)) > CommonUtils.getFormatWeight(q.get(min)))
                    min = k;
            if(min != j) { //swap
                String temp = q.get(min);
                q.set(min,q.get(j)); 
                q.set(j,temp);
            }
        }
        return q;
    }
}
