/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import downloader.CommonUtils;
import downloader.DataStructures.Settings;
import downloader.DataStructures.historyItem;
import static downloader.Site.QueryType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author christopher
 */
public class ManageSettings {
    private final Pane root;
    private TextField videoFolderText = null, pictureFolderText, sharedFolderText;
    private AnchorPane querySitePane;
    private Label cacheAmount, savedVideos, deviceCount, searchCount, downloadHistory, toogleThemeText;
    public Settings preferences;
    private ListView<Pane> searchHistory;
    private final List<Pane> HISTORYLIST = new ArrayList<>();
    
    public ManageSettings(Pane p, Settings preferences) {
       this.root = p; this.preferences = preferences;
       if (preferences == null) 
            initSettings(); 
       else checkSupported();
       getRefs();
    }
    
    public void init() {
       videoFolderText.setEditable(false); videoFolderText.setText(preferences.getVideoFolder().getAbsolutePath());
       pictureFolderText.setEditable(false); pictureFolderText.setText(preferences.getPictureFolder().getAbsolutePath());
       sharedFolderText.setEditable(false); sharedFolderText.setText(preferences.getSharedFolder().getAbsolutePath());  
       
       cacheUpdate();
       videoUpdate();
       setSiteScroller();
       
       setHistory();
       updateDownloadHistory();
    }
    
    public void update(Settings s) {
        this.preferences = s;
    }
    
    public void updateDownloadHistory() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                int i = DataIO.getDownloadedCount();
                downloadHistory.setText(i+" in download history");}
        }); //ensure you are posting results with the UI thread
    }
    
    public void setVideoText(String text) {
        videoFolderText.setText(text);
    }
    
    public void setPictureText(String text) {
        pictureFolderText.setText(text);
    }
    
    public void setSharedText(String text) {
        sharedFolderText.setText(text);
    }
    
     public void cacheUpdate() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                long size = DataIO.getCacheSize();
                String text = MainApp.getSizeText(size);
                cacheAmount.setText(text+" in cache");
            }
        }); //ensure you are posting results with the UI thread
    }
    
    public void videoUpdate() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                int i = DataIO.getSaveVideoCount();
                savedVideos.setText(i+" saved");}
        }); //ensure you are posting results with the UI thread
    }

    public void historyUpdate() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                int i = DataIO.getHistoryCount();
                searchCount.setText(i+" searches in history");}
        }); //ensure you are posting results with the UI thread
    }
    
    public void setDeviceCount(String text) {
        deviceCount.setText(text);
    }
    
    public void historySwitch(boolean enable) {
        if (searchHistory != null) {
            Iterator<Pane> i = searchHistory.getItems().iterator();
            while(i.hasNext()) {
                Pane j = i.next(); if (j.getStylesheets() != null) j.getStylesheets().clear();
                if (enable)
                    j.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                else j.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
            }
        }
    }
    
    public void clearHistory() {
        HISTORYLIST.clear();
        ObservableList<Pane> list = FXCollections.observableList(HISTORYLIST);
        searchHistory.setItems(list);
    }
    
    private Pane addHistoryPane(historyItem h) {
        Pane historyPane = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/historyItemLayout.fxml"));
            historyPane = loader.load(); historyPane.getStylesheets().clear();
            Label search = (Label)historyPane.lookup("#searchString");
            search.setText("   "+h.Search());
            Label[] sites = new Label[h.siteCount()-1];
            Label site = (Label)historyPane.lookup("#site");
            site.setText(h.getSite(0));
            for(int i = 0; i < sites.length; i++) {
                sites[i] = new Label(h.getSite(i+1));
                sites[i].setLayoutX(site.getLayoutX());
                sites[i].setLayoutY(site.getLayoutY() + (20 * (i+1)));
                sites[i].setPrefHeight(site.getPrefHeight());
                sites[i].setPrefWidth(site.getPrefWidth());
                historyPane.setPrefHeight(sites[i].getLayoutY()+30);
                historyPane.getChildren().add(sites[i]); 
            }
            Button viewSearch = (Button)historyPane.lookup("#view");
            viewSearch.setOnAction(new EventHandler() {
                @Override public void handle(Event e) {
                    MainApp.loadQuery(h.getSearchResult());
                }
            });
        } catch(IOException e) {
            CommonUtils.log("Failed to load history layout",this);
        }
        return historyPane;
    }
    
    public void setHistory() {
        clearHistory();
        Vector<historyItem> history = DataIO.loadHistory();
        if(history != null)
            history.forEach((h) -> {
                HISTORYLIST.add(addHistoryPane(h));
            });
        ObservableList<Pane> list = FXCollections.observableList(HISTORYLIST);
        searchHistory.setItems(list);
        historyUpdate();
    }
    
    private Pane newItem(String label, boolean enable) {
        Label site = new Label(label);
        site.setLayoutX(5);
        site.setLayoutY(5);
        toggleButton toggle = new toggleButton(enable);
        toggle.setLayoutX(200);
        toggle.setLayoutY(6);
        toggle.setOnMouseClicked(event -> {
            toggle.setBoolValue(!toggle.switchedOnProperty());
            preferences.setEnabled(label, toggle.switchedOnProperty());
            try {
                DataIO.saveSettings(preferences);
            } catch(IOException e) {
                MainApp.createMessageDialog("Failed to save preferences: "+e.getMessage());
            }
        });
        Pane container = new Pane();
        container.setPrefHeight(20);
        container.setPrefWidth(210);
        container.getChildren().add(site);
        container.getChildren().add(toggle);
        return container;
    }
    
    private toggleButton setToogle() {
        if (!preferences.dark())
            toogleThemeText.setText("Enable Dark Theme");
        else toogleThemeText.setText("Disable Dark Theme");
        toggleButton toggle = new toggleButton(preferences.dark());
        toggle.setLayoutX(420);
        toggle.setLayoutY(480);
        toggle.setOnMouseClicked(event -> {
            boolean value = toggle.switchedOnProperty();
            toggle.setBoolValue(!value);
            preferences.setDark(!value);
            MainApp.setDarkTheme(!value);
            historySwitch(!value);
            if (value)
                toogleThemeText.setText("Enable Dark Theme");
            else toogleThemeText.setText("Disable Dark Theme");
            try {
                DataIO.saveSettings(preferences);
            } catch(IOException e) {
                MainApp.createMessageDialog("Failed to save preferences");
            }
        });
        return toggle;
    }
    
    private void setSiteScroller() {
        Iterator<String> i = preferences.getSupportedSites().iterator();
        int j = 0;
        
        while(i.hasNext()) {
            String name = i.next();
            Pane item = newItem(name,preferences.isEnabled(name));
            item.setLayoutY(j++ * 20); 
            querySitePane.getChildren().add(item);
        }
        querySitePane.setMinHeight((j*20) + 5);
    }
    
    private void checkSupported() { //if new site support added after release add it to settings
        for(int i = 0; i < QueryType.length; i++)
            if (!preferences.isSupported(QueryType[i]))
                preferences.addSupport(QueryType[i]);
        saveSettings();
    }
    
    private void initSettings() { //at first run set default settings
        preferences = new Settings();
        preferences.initDownloadFolder(MainApp.OS); //set downloadFolder
        checkSupported();
        MainApp.setDarkTheme(false);
    }
    
    private void getRefs() {
       ScrollPane scroller = (ScrollPane)root.lookup("#settingsScroller");
       AnchorPane a = (AnchorPane)scroller.getContent();
       videoFolderText = (TextField)a.lookup("#videodownloadLoc");
       pictureFolderText = (TextField)a.lookup("#picdownloadLoc");
       sharedFolderText = (TextField) a.lookup("#sharedMediaLoc");
       querySitePane = (AnchorPane)((ScrollPane)a.lookup("#querySites")).getContent();
       savedVideos = (Label)a.lookup("#videoCount");
       cacheAmount = (Label)a.lookup("#cacheSize");
       deviceCount = (Label)a.lookup("#deviceCount");
       searchCount = (Label)a.lookup("#searchLabel");
       downloadHistory = (Label)a.lookup("#downloadLabel");
       
       searchHistory = (ListView<Pane>)a.lookup("#searches");
       toogleThemeText = (Label)a.lookup("#toogleThemeLabel");
       a.getChildren().add(setToogle());
       
    }
    
    public void saveSettings() {
        try {
            DataIO.saveSettings(preferences); 
        } catch (IOException e) {
            CommonUtils.log(e.getMessage(),this);
            MainApp.createMessageDialog("Failed to save user preferences");
        }
    }
}
