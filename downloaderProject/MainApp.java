/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import Queryer.QueryManager;
import Share.Actions;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.events.JFXDialogEvent;
import downloader.DataStructures.Device;
import downloader.DataStructures.Settings;
import downloader.DataStructures.historyItem;
import downloader.DataStructures.video;
import downloader.DownloadManager;
import downloader.DownloaderItem;
import downloader.Site;
import static downloader.Site.QueryType;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

/**
 *
 * @author christopher
 */
public class MainApp extends Application {
    
    public enum OsType {Windows, Linux, Apple};
    public static OsType OS;
    public static String username;
    public static File pageCache, imageCache, saveDir, progressCache;
    public static DownloadManager dm;
    public static QueryManager query;
    public static ListView downloads, queryPane, searchHistory;
    public static Scene scene;
    public static AnchorPane previewPane;
    public static Button searchBtn;
    public static TextField videoFolderText = null, pictureFolderText, sharedFolderText;
    public static ArrayList querySites;
    private AnchorPane querySitePane;
    private static Label cacheAmount, savedVideos, deviceCount, searchCount, downloadHistory, toogleThemeText;
    public static Label searchResultCount;
    public static final int SUPPORTEDSITES = 28, PANES = 6;
    public static Settings preferences;
    public static boolean active = true;
    public static Vector<Device> devices;
    public static ComboBox deviceBox;
    public static Pane[] actionPanes = new Pane[PANES];
    public static final int DOWNLOADPANE = 0, BROWSERPANE = 1, SETTINGSPANE = 2, SHAREPANE = 3, DOWNLOADHISTORYPANE = 4, ACCOUNTPANE = 5;
    private static AnchorPane actionPaneHolder;
    public static ProgressBar progress;
    public static TextArea log;
    public static Actions act;
    private static final String TITLE = "Video Downloader Prototype build 21.3";
    private static List HISTORYLIST = new ArrayList();
    public static DownloadHistory downloadHistoryList;
    public static StackPane root;
    public static DataCollection habits;

    private void getUserName() {
        username = System.getProperty("user.name");
    }
    
    private void setOSicon(ImageView icon) {
        InputStream in;
        switch(OS) {
            case Windows:
                in = System.class.getResourceAsStream("/icons/icons8-windows-xp-48.png");
                break;
            case Linux:
                in = System.class.getResourceAsStream("/icons/icons8-linux-48.png");
                break;
            case Apple:
                in = System.class.getResourceAsStream("/icons/icons8-mac-client-30.png");
                break;
            default: 
                in = System.class.getResourceAsStream("/icons/icons8-linux-48.png");
                break;
        }
        Image image = new Image(in);
        icon.setImage(image);
    }
    
    private void determineOS() {
        String Os = System.getProperty("os.name");
        if((Os.contains("win") || Os.contains("Win")))
            OS = OsType.Windows;
        else if (Os.contains("Linux"))
            OS = OsType.Linux;
        else OS = OsType.Apple;
    }
    
    private void setCacheDir() {
        if (null == OS) {
            imageCache = new File("/home/"+username+"/.downloaderCache/images");
            pageCache = new File("/home/"+username+"/.downloaderCache/pages");
            progressCache = new File("/home/"+username+"/.downloaderCache/progress");
            saveDir = new File("/home/"+username+"/.downloaderSettings");
            if (!imageCache.exists()) imageCache.mkdirs();
            if (!pageCache.exists()) pageCache.mkdirs();
            if (!saveDir.exists()) saveDir.mkdirs();
            if (!progressCache.exists()) progressCache.mkdirs();
        } else switch (OS) {
            case Windows:
                String homeFolder = System.getProperty("user.home");
                imageCache = new File(homeFolder+"\\downloaderCache\\images");
                pageCache = new File(homeFolder+"\\downloaderCache\\pages");
                progressCache = new File(homeFolder+"\\downloaderCache\\progress");
                saveDir = new File(homeFolder+"\\downloaderSettings");
                if (!imageCache.exists()) imageCache.mkdirs();
                if (!pageCache.exists()) pageCache.mkdirs();
                if (!saveDir.exists()) saveDir.mkdirs();
                if (!progressCache.exists()) progressCache.mkdirs();
                try {
                    Runtime.getRuntime().exec("attrib +h "+imageCache.getAbsolutePath());
                    Runtime.getRuntime().exec("attrib +h "+pageCache.getAbsolutePath());
                    Runtime.getRuntime().exec("attrib +h "+progressCache.getAbsolutePath());
                    Runtime.getRuntime().exec("attrib +h "+saveDir.getAbsolutePath());
                    Runtime.getRuntime().exec("attrib +h "+homeFolder+"\\downloaderCache");
                } catch (IOException e) {
                    
                }   break;
            case Linux:
                imageCache = new File("/home/"+username+"/.downloaderCache/images");
                pageCache = new File("/home/"+username+"/.downloaderCache/pages");
                progressCache = new File("/home/"+username+"/.downloaderCache/progress");
                saveDir = new File("/home/"+username+"/.downloaderSettings");
                if (!imageCache.exists()) imageCache.mkdirs();
                if (!pageCache.exists()) pageCache.mkdirs();
                if (!saveDir.exists()) saveDir.mkdirs();
                if (!progressCache.exists()) progressCache.mkdirs();
                break;
            case Apple:
                imageCache = new File("/Users/"+username+"/.downloaderCache/images");
                pageCache = new File("/Users/"+username+"/.downloaderCache/pages");
                progressCache = new File("/Users/"+username+"/.downloaderCache/progress");
                saveDir = new File("/Users/"+username+"/.downloaderSettings");
                if (!progressCache.exists()) progressCache.mkdirs();
                if (!imageCache.exists()) imageCache.mkdirs();
                if (!pageCache.exists()) pageCache.mkdirs();
                if (!saveDir.exists()) saveDir.mkdirs();
                break;
            default:
                imageCache = new File("/home/"+username+"/.downloaderCache/images");
                pageCache = new File("/home/"+username+"/.downloaderCache/pages");
                progressCache = new File("/home/"+username+"/.downloaderCache/progress");
                saveDir = new File("/home/"+username+"/.downloaderSettings");
                if (!imageCache.exists()) imageCache.mkdirs();
                if (!pageCache.exists()) pageCache.mkdirs();
                if (!saveDir.exists()) saveDir.mkdirs();
                if (!progressCache.exists()) progressCache.mkdirs();
                break;
        }
    }
    
    public static void displayPane(int tab) {
        actionPaneHolder.getChildren().clear();
        actionPaneHolder.getChildren().add(actionPanes[tab]);
    }
    
    public void setDarkTheme(boolean enable) {
        for(int i = 0; i < PANES; i++) {
            if (actionPanes[i].getStylesheets() != null) 
                    actionPanes[i].getStylesheets().clear();
            if (enable)
                actionPanes[i].getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else actionPanes[i].getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        }
        
        for(Node n:((AnchorPane)root.getChildren().get(0)).getChildren()) {
            if(n instanceof Pane) {
                if (((Pane)n).getStylesheets() != null) ((Pane)n).getStylesheets().clear();
                if (enable)
                    ((Pane)n).getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                else ((Pane)n).getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
            }
        } 
        if (queryPane != null) {
            Iterator<Pane> i = queryPane.getItems().iterator();
            while(i.hasNext()) {
                Pane j = i.next(); if (j.getStylesheets() != null) j.getStylesheets().clear();
                if (enable)
                    j.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                else j.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
            }
        }
        if (searchHistory != null) {
            Iterator<Pane> i = searchHistory.getItems().iterator();
            while(i.hasNext()) {
                Pane j = i.next(); if (j.getStylesheets() != null) j.getStylesheets().clear();
                if (enable)
                    j.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                else j.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
            }
        }
        if (downloadHistoryList != null)
            downloadHistoryList.switchTheme(enable);
        if(dm != null) 
            dm.changeTheme(enable);
        if (downloads != null) {
            if (enable)
                downloads.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else downloads.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        }
    }
    
    private void loadActionPanes() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/downloads.fxml"));
            actionPanes[DOWNLOADPANE] = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/browser.fxml"));
            actionPanes[BROWSERPANE] = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/settings.fxml"));
            actionPanes[SETTINGSPANE] = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/share.fxml"));
            actionPanes[SHAREPANE] = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/downloadHistory.fxml"));
            actionPanes[DOWNLOADHISTORYPANE] = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/accounts.fxml"));
            actionPanes[ACCOUNTPANE] = loader.load();
        } catch (IOException ex) {
            System.out.println("Action Panes failed");
        }
    }
    
    private void loadView() { //load main scene
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/mainLayout.fxml"));
            root = loader.load();
            AnchorPane main = (AnchorPane) root.getChildren().get(0);
            //main.getChildren().add(setupMenu());
            
            loadActionPanes();

            final int width = 850, height = 550;
            
            scene = new Scene(root, width, height);
            scene.getStylesheets().add(MainApp.class.getResource("mainStyleSheet.css").toExternalForm());
            root.scaleXProperty().bind(scene.widthProperty().divide(width));
            root.scaleYProperty().bind(scene.heightProperty().divide(height));
            setDarkTheme(preferences.dark());
        } catch (IOException e) {
            createMessageDialog("Failed: "+e.getMessage());
        }
    }
    
    private Pane newItem(String label, boolean enable) {
        Label site = new Label(label);
        site.setLayoutX(2);
        site.setLayoutY(5);
        toggleButton toggle = new toggleButton(enable);
        toggle.setLayoutX(195);
        toggle.setLayoutY(6);
        toggle.setOnMouseClicked(event -> {
            toggle.setBoolValue(!toggle.switchedOnProperty());
            preferences.setEnabled(label, toggle.switchedOnProperty());
            try {
                DataIO.saveSettings(preferences);
            } catch(IOException e) {
                createMessageDialog("Failed to save preferences");
            }
        });
        Pane container = new Pane();
        container.setPrefHeight(20);
        container.setPrefWidth(210);
        container.getChildren().add(site);
        container.getChildren().add(toggle);
        return container;
    }
    
    public static void clearHistory() {
        HISTORYLIST.clear();
        ObservableList list = FXCollections.observableList(HISTORYLIST);
        searchHistory.setItems(list);
    }
    
    public static void setHistory() {
        Vector<historyItem> history = DataIO.loadHistory();
        if(history != null)
            for(historyItem h:history)
                HISTORYLIST.add(addHistoryPane(h));
        ObservableList list = FXCollections.observableList(HISTORYLIST);
        searchHistory.setItems(list);
        historyUpdate();
    }
    
    private static Pane addHistoryPane(historyItem h) {
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
                historyPane.setPrefHeight(sites[i].getLayoutY()+20);
                historyPane.getChildren().add(sites[i]); 
            }
            Button viewSearch = (Button)historyPane.lookup("#view");
            viewSearch.setOnAction(new EventHandler() {
                @Override public void handle(Event e) {
                    if(query == null) query = new QueryManager();
                    query.loadSearch(h.getSearchResult());
                    displayPane(BROWSERPANE);
                }
            });
        } catch(IOException e) {
            System.out.println("Failed to load history layout");
        }
        return historyPane;
    }
    
    private void setSiteScroller() {
        Set sites = preferences.getSupportedSites();
        Iterator i = sites.iterator();
        int j = 0;
        
        while(i.hasNext()) {
            String name = (String) i.next();
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
        preferences.initDownloadFolder(OS); //set downloadFolder
        checkSupported();
        setDarkTheme(false);
    }
    
    private void moveFiles(File directory, File destination) {
        File[] files = directory.listFiles();
        
        if(files != null)
            for(File f: files)
                DataIO.moveFile(f, destination);
        directory.delete();
    }
    
    private void moveFile(File file, File destination) {
        if(file.exists()) 
            DataIO.moveFile(file, destination);
    }
    
    private void cleanUp() {
        if(OS == OsType.Windows) {
            //move files from old dirs to new locations
            if(new File("C:\\"+username+"\\downloadCache\\images").exists())
                moveFiles(new File("C:\\"+username+"\\downloadCache\\images"),imageCache);
            if(new File("C:\\"+username+"\\downloadCache\\pages").exists())
                moveFiles(new File("C:\\"+username+"\\downloadCache\\pages"),pageCache);
            if(new File("C:\\"+username+"\\downloadCache\\progress").exists())
                moveFiles(new File("C:\\"+username+"\\downloadCache\\progress"),progressCache);
            if(new File("C:\\"+username).exists())
                moveFiles(new File("C:\\"+username),saveDir);
            Vector<video> videos = DataIO.loadVideos();
            if(videos != null) {
                for(video v:videos) {
                    v.adjustThumbnail(imageCache.getAbsolutePath());
                    v.adjustPreview(imageCache.getAbsolutePath());
                }
            }
        } else if (OS == OsType.Apple) {
            moveFile(new File("/Users/"+username+"/settings.dat"),saveDir);
            moveFile(new File("/Users/"+username+"/laterVideos.dat"),saveDir);
            moveFile(new File("/Users/"+username+"/devices.dat"),saveDir);
        } else {
            moveFile(new File("/home/"+username+"/settings.dat"),saveDir);
            moveFile(new File("/home/"+username+"/laterVideos.dat"),saveDir);
            moveFile(new File("/home/"+username+"/devices.dat"),saveDir);
        }
    }
    
    public static String getSizeText(long size) {
        //ik 1024 is the right unit rather than 1000
        //but dividing by 1000 gave a more accurate result
        if (size < 1024)
            return size + " b";
        else if((size >= 1024) && (size < 1048576)) //1024b * 1024b = ?kb you do the math
            return String.format("%.2f",(double)size / 1000) + " kb"; 
        else if ((size >= 1048576) && (size < 1073741824)) //1024kb * 1024kb = ?mb you do the math
            return String.format("%.2f",(double)size / 1000 / 1000) + " mb";
        else return String.format("%.2f",(double)size / 1000 / 1000 / 1000) + " gb"; //1024mb * 1024mb = ?gb you do the math
    }
    
    public static void videoUpdate() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                int i = DataIO.getSaveVideoCount();
                savedVideos.setText(i+" saved");}
        }); //ensure you are posting results with the UI thread
    }
    
    public static void cacheUpdate() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                long size = DataIO.getCacheSize();
                String text = getSizeText(size);
                cacheAmount.setText(text+" in cache");
            }
        }); //ensure you are posting results with the UI thread
    }
    
    public static void historyUpdate() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                int i = DataIO.getHistoryCount();
                searchCount.setText(i+" searches in history");}
        }); //ensure you are posting results with the UI thread
    }
    
    public static void updateDevices() {
        devices = DataIO.loadDevices();
        if(devices == null) devices = new Vector<Device>();
        
        deviceBox.getItems().clear();
        deviceCount.setText(devices.size()+" devices");
        
        for(Device d : devices)
            deviceBox.getItems().add(d);
    }
    
    public static void downloadHistoryUpdate() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                int i = DataIO.getDownloadedCount();
                downloadHistory.setText(i+" in download history");}
        }); //ensure you are posting results with the UI thread
    }
    
    private String getlocalDeviceName() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostName();
        } catch (UnknownHostException ex) {
            return "Undetermined";
        }
    }
    
    private void setupSharePane() {
        deviceBox = (ComboBox)actionPanes[SHAREPANE].lookup("#devices");
        updateDevices();
        
        progress = (ProgressBar)actionPanes[SHAREPANE].lookup("#progressBar");
        log = (TextArea)actionPanes[SHAREPANE].lookup("#log");
        log.setEditable(false);
        Label deviceName = (Label)actionPanes[SHAREPANE].lookup("#deviceName");
        deviceName.setText("This Device Name: "+getlocalDeviceName());
        act = new Actions();
    }
    
    private void setupDownloadHistoryPane() {
        downloadHistoryList = new DownloadHistory((ListView)actionPanes[DOWNLOADHISTORYPANE].lookup("#downloaded"));
        downloadHistoryList.display();
        if (downloadHistoryList != null)
            downloadHistoryList.switchTheme(preferences.dark());
    }
    
    private toggleButton setToogle() {
        if (!preferences.dark())
            toogleThemeText.setText("Enable Dark Theme");
        else toogleThemeText.setText("Disable Dark Theme");
        toggleButton toggle = new toggleButton(preferences.dark());
        toggle.setLayoutX(420);
        toggle.setLayoutY(495);
        toggle.setOnMouseClicked(event -> {
            boolean value = toggle.switchedOnProperty();
            toggle.setBoolValue(!value);
            preferences.setDark(!value);
            setDarkTheme(!value);
            if (value)
                toogleThemeText.setText("Enable Dark Theme");
            else toogleThemeText.setText("Disable Dark Theme");
            try {
                DataIO.saveSettings(preferences);
            } catch(IOException e) {
                createMessageDialog("Failed to save preferences");
            }
        });
        return toggle;
    }

    public static void createNotification(final String title, final String msg) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                String[] tokens = msg.split(" ");
                StringBuilder m = new StringBuilder();
                for(int i = 0; i < tokens.length; i++) {
                    if (i % 6 == 0) { m.append("\n"); m.append(tokens[i]); continue;}
                    if (i == 0) m.append(tokens[i]);
                    else m.append(" "+tokens[i]);
                }
                Notifications.create().title(title).text(m.toString()).darkStyle().position(Pos.BOTTOM_RIGHT).showConfirm();
            }
        });
    }
    
    public static void createMessageDialog(String msg) {
        Platform.runLater(new Runnable() {
            Pane pane; BoxBlur blur = new BoxBlur(5,5,5);
            @Override public void run() {
                try {
                    pane = FXMLLoader.load(new MainApp().getClass().getResource("layouts/messageDialog.fxml"));
                    pane.getStylesheets().clear();
                    if(preferences.dark())
                        pane.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                    else pane.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
                    AnchorPane a = (AnchorPane)root.getChildren().get(0);
                    JFXDialog dialog = new JFXDialog(root,pane,JFXDialog.DialogTransition.CENTER);
                    Label text = (Label)pane.lookup("#messageLabel");
                    Button ok = (Button)pane.lookup("#okay");
                    text.setText(msg);
                    ok.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            dialog.close();
                        }
                    });
                    dialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
                        @Override public void handle(JFXDialogEvent event) {
                            a.setEffect(null);
                        }
                    });
                    dialog.show(); a.setEffect(blur);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Override public void start(Stage primaryStage) {
        determineOS(); //determine what OS is running
        getUserName(); //get the username
        setCacheDir(); //set up cache (create it if it doesnt exist yet)
        cleanUp(); //correct old version data
        preferences = DataIO.loadSettings();
        if (preferences == null) 
            initSettings(); 
        else checkSupported();
        
       dm = new DownloadManager(); //create the download manager
        
       loadView();

       Pane topPane = (Pane)scene.lookup("#topPane");
       setOSicon((ImageView)topPane.lookup("#OSicon"));
       actionPaneHolder = (AnchorPane)scene.lookup("#currentView");
       displayPane(DOWNLOADPANE);
       
       downloads = (ListView) actionPanes[DOWNLOADPANE].lookup("#downloadList"); //listview of downloads
       downloads.getStyleClass().add("list-view");
       queryPane = (ListView) actionPanes[BROWSERPANE].lookup("#resultPane"); //list view of thumbnails from search
       searchBtn = (Button)actionPanes[BROWSERPANE].lookup("#queryButton");
       Label userLabel = (Label) scene.lookup("#username");
       Label dets = (Label) scene.lookup("#details");
       
      dets.setText("Paste a link or import links from file to download");
      userLabel.setText(username);      
       
       ScrollPane scroller = (ScrollPane)actionPanes[SETTINGSPANE].lookup("#settingsScroller");
       AnchorPane a = (AnchorPane)scroller.getContent();
       ObservableList<Node> l = a.getChildren();
       for(int i = 0; i < l.size(); i++) {
           if (l.get(i).getId() != null) {
            if(l.get(i).getId().equals("videodownloadLoc"))
                videoFolderText = (TextField) l.get(i);
            if(l.get(i).getId().equals("picdownloadLoc"))
                pictureFolderText = (TextField) l.get(i);
            if(l.get(i).getId().equals("sharedMediaLoc"))
                sharedFolderText = (TextField) l.get(i);
            if(l.get(i).getId().equals("querySites")) {
               ScrollPane scroll = (ScrollPane)l.get(i);
               querySitePane = (AnchorPane)scroll.getContent();
            }
            if (l.get(i).getId().equals("videoCount"))
                savedVideos = (Label)l.get(i);
            if (l.get(i).getId().equals("cacheSize"))
                cacheAmount = (Label)l.get(i);
            if (l.get(i).getId().equals("deviceCount"))
                deviceCount = (Label)l.get(i);
            if (l.get(i).getId().equals("searchLabel"))
                searchCount = (Label)l.get(i);
            if (l.get(i).getId().equals("downloadLabel"))
                downloadHistory = (Label)l.get(i);
            if (l.get(i).getId().equals("searches"))
                searchHistory = (ListView)l.get(i);
           }
       }
       toogleThemeText = (Label)a.lookup("#toogleThemeLabel");
       a.getChildren().add(setToogle());
       videoFolderText.setEditable(false);
       videoFolderText.setText(preferences.getVideoFolder().getAbsolutePath());
       pictureFolderText.setEditable(false);
       pictureFolderText.setText(preferences.getPictureFolder().getAbsolutePath());
       sharedFolderText.setEditable(false);
       sharedFolderText.setText(preferences.getSharedFolder().getAbsolutePath());
       cacheUpdate();
       videoUpdate();
       setSiteScroller();
       setHistory();
       downloadHistoryUpdate();
       
       setupSharePane();
       setupDownloadHistoryPane();
       
       //get the scrollpane to get the anchor which contains the imageviews
       ScrollPane pre = (ScrollPane)actionPanes[BROWSERPANE].lookup("#scroll");
       previewPane = (AnchorPane)pre.getContent();
       searchResultCount = (Label)actionPanes[BROWSERPANE].lookup("#searchResult");
       
       primaryStage.setTitle(TITLE);
       primaryStage.setOnCloseRequest(event -> {
           if (query != null)
               query.release();
            dm.release();
            active = false;
            System.out.println("Exiting");
            //System.exit(0);
        });
       primaryStage.setScene(scene);
       try {
            if(splash != null)
                 splash.close();
       } catch (IllegalStateException e) {
           System.out.println("Splash screen error");
       }
       primaryStage.show();
       
       loadSuggestions();
    }
    
    public static void log(String mediaName, String site) {
        if (habits != null) habits.add(mediaName, site);
        try {DataIO.saveCollectedData(habits);} catch(IOException e) {System.out.println("Failed to save habits");}
    }
    
    private void loadSuggestions() {
        habits = DataIO.loadCollectedData();
        if (habits != null) {
            int pull = 1;
            if (habits.suggestions() > 15) pull = 2;
            else if (habits.suggestions() > 24) pull = 3;
            for(int i = 0; i < pull; i++) {
                video temp = habits.next(); 
                if (temp != null)
                    determineSite(temp.getLink(),temp);
                else System.out.println("null?");
            }
        } else {habits = new DataCollection(true);}
    }
    
    public void determineSite(String link, video v) {
        DownloaderItem download = new DownloaderItem();
        download.setLink(link); download.setType(Site.getUrlSite(link)); download.setVideo(v);
        //add item to downloadManager for display
        dm.addDownload(download);
    }

    /**
     * @param args the command line arguments
     */
    
    static SplashScreen splash;
    public static void main(String[] args) {
        splash = SplashScreen.getSplashScreen();
        launch(args); 
    }
    
    public static void saveSettings() {
        try {
            DataIO.saveSettings(preferences); 
        } catch (IOException e) {
            System.out.println(e.getMessage());
            createMessageDialog("Failed to save user preferences");
        }
    }
}
