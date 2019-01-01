/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import downloader.DataStructures.downloadedMedia;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 *
 * @author christopher
 */
public class DownloadHistory {
    private final ListView downloadHistoryList;
    private final List items;
    private ManageSettings settingsRef;
    
    public DownloadHistory(ListView d) {
        downloadHistoryList = d;
        items = new ArrayList();
        refresh();
    }
    
    public void setSettings(ManageSettings s) {
        this.settingsRef = s;
    }
    
    private void refresh() {
        Vector<downloadedMedia> d = DataIO.loadDownloaded();
        items.clear();
        System.gc();
        if (d != null) 
            for(int i = 0; i < d.size(); i++)
                items.add(createItem(d.get(i)));
        d = null;
    }
    
    public void display() {
        //ObservableList list = FXCollections.observableList(items);
         Platform.runLater(new Runnable() {
           @Override public void run() {
               downloadHistoryList.getItems().clear();
               downloadHistoryList.getItems().addAll(items);
            }
        }); //ensure you are posting results with the UI thread
    }
    
    public void switchTheme(boolean enable) {
        Iterator<Pane> i = downloadHistoryList.getItems().iterator();
        while(i.hasNext()) {
            Pane j = i.next(); j.getStylesheets().clear();
            if (enable)
                j.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else j.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        }
    }
    
    public void add(downloadedMedia d) {
        try {
            DataIO.saveDownloaded(d);
            refresh();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage()+" failed add to download history");
        } catch (IOException e) {
            System.out.println(e.getMessage()+" failed add to download history");
        }
        settingsRef.updateDownloadHistory();
        display();
    }

    public void clear() {
        DataIO.clearDownloaded();
        settingsRef.updateDownloadHistory();
        refresh();
        display();
    }
    
    private Pane createItem(downloadedMedia d) {
        Pane item = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/downloadedItem.fxml"));
            item = loader.load();
            item.getStylesheets().clear();
            Label name = (Label)item.lookup("#mediaName");
            name.setText(d.getName());
            ImageView thumb = (ImageView)item.lookup("#mediaThumb");
            try {
                FileInputStream fis = new FileInputStream(d.getThumb());
                Image image = new Image(fis);
                if (fis != null) fis.close();
                thumb.setImage(image);
            } catch (FileNotFoundException e) {
                System.out.println("Couldnt find "+d.getThumb().getName());
            } catch (IOException e) {
                MainApp.createMessageDialog("Failed to load thumb: "+d.getThumb().getName()+"\n"+e.getMessage());
            }
            Button viewMedia = (Button)item.lookup("#view");
            viewMedia.setOnAction(event -> {
                if (Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            if (d.exists()) {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.open(new File(d.getLocation()));
                            } else MainApp.createMessageDialog(d.getName()+" was deleted or moved");
                        } catch (IOException ex) {
                            MainApp.createMessageDialog("error with opening "+d.getName());
                        }
                    }).start();    
                } else
                    MainApp.createMessageDialog("Not supported");
            });
            Button viewFolder = (Button)item.lookup("#openFolder");
            viewFolder.setOnAction(event -> {
                if (Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            if (d.folderExists()) {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.open(new File(d.getFolder()));
                            } else MainApp.createMessageDialog("folder for"+d.getName()+" was deleted or moved");
                        } catch (IOException ex) {
                            MainApp.createMessageDialog("error opening folder for "+d.getName());
                        }
                    }).start();    
                } else
                    MainApp.createMessageDialog("Not supported");
            });
            if (d.exists()) {
                Label size = (Label)item.lookup("#mediaSize");
                size.setText(d.getSize());
            }
            Label site = (Label)item.lookup("#mediaSite");
            site.setText(d.getDownloaded());
        } catch (IOException e) {
            MainApp.createMessageDialog("Failed to load download history: "+e.getMessage());
        }
        return item;
    }
}
