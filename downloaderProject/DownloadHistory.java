/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import downloader.CommonUtils;
import downloader.DataStructures.downloadedMedia;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javax.imageio.ImageIO;

/**
 *
 * @author christopher
 */
public class DownloadHistory {
    private final ListView<Pane> downloadHistoryList;
    private final List<Pane> items;
    private ManageSettings settingsRef;
    
    public DownloadHistory(ListView<Pane> d) {
        downloadHistoryList = d;
        items = new ArrayList<>();
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
            for(int i = d.size()-1; i > -1; i--)
                items.add(createItem(d.get(i)));
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
            CommonUtils.log(e.getMessage()+" failed add to download history",this);
        } catch (IOException e) {
            CommonUtils.log(e.getMessage()+" failed add to download history",this);
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
            ((Label)item.lookup("#mediaName")).setText(d.getName());
            ImageView thumb = (ImageView)item.lookup("#mediaThumb");
            if (d.getDownloaded().equals("Instagram")) thumb.preserveRatioProperty().setValue(true);
            try {
                if (d.getThumb().length() < 1024 * 1024 * 10) {
                    FileInputStream fis = new FileInputStream(d.getThumb());
                    Image image = new Image(fis);
                    thumb.setImage(image);
                } else {
                    BufferedImage b = ImageIO.read(d.getThumb());
                    thumb.setImage(SwingFXUtils.toFXImage(b, null));
                }
            } catch (FileNotFoundException e) {
                CommonUtils.log("Couldnt find "+d.getThumb().getName(),this);
            } catch (IOException e) {
                MainApp.createMessageDialog("Failed to load thumb: "+d.getThumb().getName()+"\n"+e.getMessage());
            }
            ((Button)item.lookup("#view")).setOnAction(event -> {
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
            ((Button)item.lookup("#openFolder")).setOnAction(event -> {
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
            if (d.exists())
                ((Label)item.lookup("#mediaSize")).setText(d.getSize());
            ((Label)item.lookup("#mediaSite")).setText(d.getDownloaded());
            ((Label)item.lookup("#timestamp")).setText(d.getDate());
        } catch (IOException e) {
            MainApp.createMessageDialog("Failed to load download history: "+e.getMessage());
        }
        return item;
    }
}
