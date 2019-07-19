/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import ChrisPackage.ClipboardListener;
import Queryer.QueryManager;
import Share.Actions;
import com.jfoenix.controls.JFXDialog;
import downloader.CommonUtils;
import downloader.DataStructures.Device;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.video;
import downloader.DownloadManager;
import downloader.Exceptions.GenericDownloaderException;
import java.awt.SplashScreen;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
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
    
    public enum OsType {Windows, Linux, Apple}; // Apple /Users/username/
    public static OsType OS;
    public static String username;
    public static File pageCache, imageCache, saveDir, progressCache;
    public static DownloadManager dm;
    public static boolean active = true;
    public static Vector<Device> devices;
    public static ComboBox<Device> deviceBox;
    public static Actions act;
    public static DownloadHistory downloadHistoryList;
    public static StackPane root;
    public static DataCollection habits;
    private static AnchorPane actionPaneHolder;
    private static QueryManager query;
    private static Scene scene;
    private static boolean dontLoad;
    private static final String VERSION = "build 35.3", TITLE = "Video Downloader "+VERSION;
    
    private static final int WIDTH = 895, HEIGHT = 550, XS = 100, PANES = 7;
    public static Pane[] actionPanes = new Pane[PANES];
    public static final byte DOWNLOADPANE = 0, BROWSERPANE = 1, SETTINGSPANE = 2, SHAREPANE = 3, DOWNLOADHISTORYPANE = 4, ACCOUNTPANE = 5, STREAMPANE = 6;
    public static int BYTE;
    
    public static ManageSettings settings;
    private ClipboardListener clippy;
    public static mainLayoutController mainController;
    private static Stage window;
    private static int load, search;

    private static void getUserName() {
        username = System.getProperty("user.name");
    }
    
    private void setOSicon(ImageView icon) {
        InputStream in;
        switch(OS) {
            case Windows: in = System.class.getResourceAsStream("/icons/icons8-windows-xp-48.png"); break;
            case Linux: in = System.class.getResourceAsStream("/icons/icons8-linux-48.png"); break;
            case Apple: in = System.class.getResourceAsStream("/icons/icons8-mac-client-30.png"); break;
            default: in = System.class.getResourceAsStream("/icons/icons8-linux-48.png"); break;
        }
        icon.setImage(new Image(in));
    }
    
    private static void determineOS() {
        String Os = System.getProperty("os.name");
        if((Os.contains("win") || Os.contains("Win")))
            OS = OsType.Windows;
        else if (Os.contains("Linux"))
            OS = OsType.Linux;
        else OS = OsType.Apple;
        BYTE = OS == OsType.Windows ? 1024 : 1000;
    }
    
    private static void setCacheDir() {
        String home = System.getProperty("user.home");
        if (null == OS) { //assume unix system
            imageCache = new File(home+File.separator+".downloaderCache/images");
            pageCache = new File(home+File.separator+".downloaderCache/pages");
            progressCache = new File(home+File.separator+".downloaderCache/progress");
            saveDir = new File(home+File.separator+".downloaderSettings");
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
                } catch (IOException e) {}   
                break;
            case Linux:
            case Apple:
            default:
                imageCache = new File(home+File.separator+".downloaderCache/images");
                pageCache = new File(home+File.separator+"/.downloaderCache/pages");
                progressCache = new File(home+File.separator+"/.downloaderCache/progress");
                saveDir = new File(home+File.separator+"/.downloaderSettings");
                if (!imageCache.exists()) imageCache.mkdirs();
                if (!pageCache.exists()) pageCache.mkdirs();
                if (!saveDir.exists()) saveDir.mkdirs();
                if (!progressCache.exists()) progressCache.mkdirs();
        }
    }
    
    private void setProfileFolder() {
        if (null == OS) { //assume unix system
            settings.preferences.setProfileFolder(new File(MainApp.saveDir.getAbsolutePath()+"/Stars"));
            if (!settings.preferences.getProfileFolder().exists()) settings.preferences.getProfileFolder().mkdirs();
        } else switch (OS) {
            case Windows:                
                settings.preferences.setProfileFolder(new File(MainApp.saveDir.getAbsolutePath()+"\\Stars"));
                if (!settings.preferences.getProfileFolder().exists()) settings.preferences.getProfileFolder().mkdirs();
                break;
            case Linux:
            case Apple:
            default:
                settings.preferences.setProfileFolder(new File(MainApp.saveDir.getAbsolutePath()+"/Stars"));
                if (!settings.preferences.getProfileFolder().exists()) settings.preferences.getProfileFolder().mkdirs();
        }       
    }
    
    public static void displayPane(int tab) {
        actionPaneHolder.getChildren().clear();
        actionPaneHolder.getChildren().add(actionPanes[tab]);
    }
    
    public static void setDarkTheme(boolean enable) {
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
        if (query != null)
            query.switchTheme(enable);
        if (downloadHistoryList != null)
            downloadHistoryList.switchTheme(enable);
        if(dm != null) 
            dm.changeTheme(enable);
    }
    
    public static void loadQuery(GenericQuery q) {
        if(query == null) query = new QueryManager(actionPanes[BROWSERPANE]);
            query.loadSearch(q);
        displayPane(BROWSERPANE);
    }
    
    public static void makeQuery() {
        if (query == null)
            query = new QueryManager(actionPanes[BROWSERPANE]);
        query.generateContent();
    }
    
    private Pane loadPane(String name) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("layouts/"+name+".fxml"));
        return loader.load();
    }
    
    private void loadActionPanes() {
        try {
            actionPanes[DOWNLOADPANE] = loadPane("downloads");
            actionPanes[BROWSERPANE] = loadPane("browser");
            actionPanes[SETTINGSPANE] = loadPane("settings");
            actionPanes[SHAREPANE] = loadPane("share");
            actionPanes[DOWNLOADHISTORYPANE] = loadPane("downloadHistory");
            actionPanes[ACCOUNTPANE] = loadPane("accounts");
            actionPanes[STREAMPANE] = loadPane("stream");
        } catch (IOException ex) {
            CommonUtils.log("Action Panes failed",this);
        }
    }
    
    private void loadView() { //load main scene
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/mainLayout.fxml"));
            root = loader.load();
            AnchorPane main = (AnchorPane) root.getChildren().get(0);
            main.setPrefHeight(HEIGHT);
            main.setPrefWidth(WIDTH);
            //main.getChildren().add(setupMenu());
            
            loadActionPanes();
            
            scene = new Scene(root, WIDTH, HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("mainStyleSheet.css").toExternalForm());
            root.scaleXProperty().bind(scene.widthProperty().divide(WIDTH));
            root.scaleYProperty().bind(scene.heightProperty().divide(HEIGHT));
        } catch (IOException e) {
            createMessageDialog("Failed: "+e.getMessage());
        }
    }

    private static void moveFiles(File directory, File destination) {
        File[] files = directory.listFiles();
        
        if(files != null)
            for(File f: files)
                DataIO.moveFile(f, destination);
        directory.delete();
    }
    
    private static void moveFile(File file, File destination) {
        if(file.exists()) 
            DataIO.moveFile(file, destination);
    }
    
    private static void cleanUp() {
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
        } else {
            String home = System.getProperty("user.home");
            moveFile(new File(home+"/settings.dat"),saveDir);
            moveFile(new File(home+"/laterVideos.dat"),saveDir);
            moveFile(new File(home+"/devices.dat"),saveDir);
        }
    }
    
    public static String getSizeText(long size) {
        //ik 1024 is the right unit rather than 1000
        //but dividing by 1000 gave a more accurate result on linux
        if (size < 0) 
            return "----";
        else if (size < BYTE)
            return size + " b";
        else if((size >= BYTE) && (size < BYTE * BYTE)) //1024b * 1024b = ?kb you do the math
            return String.format("%.2f",(double)size / BYTE) + " kb"; 
        else if ((size >= BYTE * BYTE) && (size < BYTE * BYTE * BYTE)) //1024kb * 1024kb = ?mb you do the math
            return String.format("%.2f",(double)size / BYTE / BYTE) + " mb";
        else return String.format("%.2f",(double)size / BYTE / BYTE / BYTE) + " gb"; //1024mb * 1024mb = ?gb you do the math
    }
    
    private String getlocalDeviceName() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostName();
        } catch (UnknownHostException ex) {
            CommonUtils.log(ex.getMessage(),this);
            return "Undetermined";
        }
    }
    
    public static void updateDevices() {
        devices = DataIO.loadDevices();
        if(devices == null) devices = new Vector<>();
        
        deviceBox.getItems().clear();
        settings.setDeviceCount(devices.size()+" devices");
        
        devices.forEach((d) -> {
            deviceBox.getItems().add(d);
        });
    }
    
    private void setupSharePane() {
        deviceBox = (ComboBox<Device>)actionPanes[SHAREPANE].lookup("#devices");
        updateDevices();
        
        Label deviceName = (Label)actionPanes[SHAREPANE].lookup("#deviceName");
        deviceName.setText("This Device Name: "+getlocalDeviceName());
        act = new Actions(actionPanes[SHAREPANE]);
    }
    
    private void setupDownloadHistoryPane() {
        downloadHistoryList = new DownloadHistory((ListView<Pane>)actionPanes[DOWNLOADHISTORYPANE].lookup("#downloaded"));
        downloadHistoryList.display();
        if (downloadHistoryList != null)
            downloadHistoryList.switchTheme(settings.preferences.dark());
    }

    public static void createNotification(final String title, final String msg) {
        Platform.runLater(() -> {
            String[] tokens = msg.split(" ");
            StringBuilder m = new StringBuilder();
            for(int i = 0; i < tokens.length; i++) {
                if (i % 6 == 0) { m.append("\n"); m.append(tokens[i]); continue;}
                if (i == 0) m.append(tokens[i]);
                else m.append(" "+tokens[i]);
            }
            Notifications.create().title(title).text(m.toString()).darkStyle().position(Pos.BOTTOM_RIGHT).showConfirm();
        });
    }
    
    public static void createMessageDialog(String msg) {
        Platform.runLater(new Runnable() {
            Pane pane; BoxBlur blur = new BoxBlur(5,5,5);
            @Override public void run() {
                try {
                    pane = FXMLLoader.load(new MainApp().getClass().getResource("layouts/messageDialog.fxml"));
                    pane.getStylesheets().clear();
                    if (settings == null) CommonUtils.log(msg,this); else{
                    if(settings.preferences.dark())
                        pane.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                    else pane.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
                    AnchorPane a = (AnchorPane)root.getChildren().get(0);
                    JFXDialog dialog = new JFXDialog(root,pane,JFXDialog.DialogTransition.CENTER);
                    Label text = (Label)pane.lookup("#messageLabel");
                    Button ok = (Button)pane.lookup("#okay");
                    text.setText(msg);
                    ok.setOnAction((ActionEvent) -> {
                        dialog.close();
                    });
                    dialog.setOnDialogClosed((JFXDialogEvent) -> {
                        a.setEffect(null);
                    });
                    dialog.show(); a.setEffect(blur);
                    }
                } catch(IOException e) {
                    CommonUtils.log(e.getMessage(),this);
                }
            }
        });
    }
    
    @Override public void start(Stage primaryStage) {
    	window = primaryStage;
        
        loadView();
       
        dm = new DownloadManager(); //create the download manager
        dm.setStreamer(new StreamManager(actionPanes[STREAMPANE]));
        dm.setDownloadList((ListView<Pane>) actionPanes[DOWNLOADPANE].lookup("#downloadList"));
       
        settings = new ManageSettings(actionPanes[SETTINGSPANE],DataIO.loadSettings());
        setProfileFolder();
        setDarkTheme(settings.preferences.dark());

        Pane topPane = (Pane)scene.lookup("#topPane");
        setOSicon((ImageView)topPane.lookup("#OSicon"));
        actionPaneHolder = (AnchorPane)scene.lookup("#currentView");
        displayPane(DOWNLOADPANE);
       
        ((Label)scene.lookup("#username")).setText(username);
      
        settings.init(); setupDownloadHistoryPane();
        downloadHistoryList.setSettings(settings);
       
        setupSharePane();
       
        clippy = new ClipboardListener(mainController);
        
        window.setTitle(TITLE);
        window.setOnCloseRequest(event -> {
            active = false;
            if (query != null)
                query.release();
            if (dm != null) {
                dm.release();
                dm = null;
            }
            clippy.stop();
            try {DataIO.saveCollectedData(habits);
            writeJson();} catch(IOException e) {CommonUtils.log("Failed to save habits",this);}
            habits = null;
            act = null;
            CommonUtils.log("Exiting",this);
        });
        window.setScene(scene);
        window.setMinHeight(HEIGHT);
        window.setMinWidth(WIDTH);
        window.setMaxHeight(HEIGHT+XS);
        window.setMaxWidth(WIDTH+XS);
        window.setHeight(HEIGHT);
        window.setWidth(WIDTH);
        try {
            if(splash != null)
                splash.close();
        } catch (IllegalStateException e) {
           CommonUtils.log("Splash screen error: "+e.getMessage(),this);
        }
        window.show();
        
        loadSuggestions();
        
        ExecutorService x = Executors.newSingleThreadExecutor();
        x.execute(clippy); x.shutdown();
        //startGarbageSuggester();
    }
    
    private static void startGarbageSuggester() {
        ExecutorService x = Executors.newSingleThreadExecutor();
        x.execute(() -> {
            while(active) {
                System.gc();
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        x.shutdown();
    }
    
    public static void log(String mediaName, String site) throws GenericDownloaderException {
        if (habits != null) {
            habits.add(mediaName, site);
            try {DataIO.saveCollectedData(habits);} catch(IOException e) {CommonUtils.log("Failed to save habits","MainApp");}
            writeJson();
        }
    }
    
    public static int log(video v) {
        if (!active) return -2;
        if (habits != null) {
            int code = habits.addSuggestion(v);
            try {DataIO.saveCollectedData(habits);} catch(IOException e) {CommonUtils.log("Failed to save habits","MainApp");}
            writeJson();
            return code;
        } else return -1;
    }
    
    public static void log(Vector<String> keywords) throws GenericDownloaderException {
        if (habits != null) {
            habits.add(keywords);
            keywords.forEach(s -> CommonUtils.log(s, "keyowrds"));
            try {DataIO.saveCollectedData(habits);} catch(IOException e) {CommonUtils.log("Failed to save habits","MainApp");}
            writeJson();
        }
    }
    
    public static void log(String star) throws GenericDownloaderException {
        if (habits != null) {
            habits.addStar(star);
            CommonUtils.log(star, "star");
            try {DataIO.saveCollectedData(habits);} catch(IOException e) {CommonUtils.log("Failed to save habits","MainApp");}
            writeJson();
        }
    }
    
    private static void writeJson() {
        try {
            Formatter f = new Formatter(saveDir+File.separator+"habits.json");
            f.format("%s", habits.toJson());
            f.flush(); f.close();
        } catch (FileNotFoundException e) {
            CommonUtils.log(e.getMessage(),"MainApp");
        }
    }
    
    private static void preSearch() {
        if (habits != null)
            for(int i = 0; i < search; i++)
                try {
                    habits.generateSuggestion();
                } catch (GenericDownloaderException e) {
                    CommonUtils.log(e.getMessage(),"MainApp:static");
                }
    }
    
    private static void loadSuggestions() {
        if (!dontLoad) {
            if (habits != null) {
                int pull = 1;
                if (load < 0) {
                    if (habits.suggestions() > 10 && habits.suggestions() <= 20) pull = 2;
                    else if (habits.suggestions() > 20 && habits.suggestions() <= 35) pull = 3;
                    else if (habits.suggestions() > 35 && habits.suggestions() <= 50) pull = 4;
                    else if (habits.suggestions() > 50) pull = 5;
                    dm.reserve(pull+1);
                }
                pull = load < 0 ? pull : load;
                if (!habits.hasNext())
                    CommonUtils.log("no suggestions","MainApp:static");
                else {
                    for(int i = 0; i < pull; i++) {
                        if (!habits.hasNext()) break;
                        video temp = habits.next(); 
                        if (temp != null)
                            dm.addDownload(temp.getLink(),temp);
                        else CommonUtils.log("no suggestions left","MainApp:static");
                    }
                }
                try {DataIO.saveCollectedData(habits);} catch(IOException e) {CommonUtils.log("Failed to save habits","MainApp:static");}
                writeJson();
            } else {habits = new DataCollection(true);}
        }
    }

    /**
     * @param args the command line arguments
     */
    
    static SplashScreen splash;
    public static void main(String[] args) {
        splash = SplashScreen.getSplashScreen();
        determineOS(); //determine what OS is running
        getUserName(); //get the username
        setCacheDir(); //set up cache (create it if it doesnt exist yet)
        cleanUp(); //correct old version data
        
        dontLoad = false;
        load = -1;
        search = 0;
        if (args != null)
            if (args.length > 0)
                parseArgs(args);
        
        habits = DataIO.loadCollectedData();
        habits = habits == null ? new DataCollection(true) : habits;
        
        preSearch();
        launch(args); 
    }
    
    private static void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.toLowerCase().equals("suppress"))
                dontLoad = true;
            if (arg.toLowerCase().matches("load=\\d+"))
                load = Integer.parseInt(arg.split("=")[1]);
            if (arg.toLowerCase().matches("search=\\d+"))
                search = Integer.parseInt(arg.split("=")[1]);
        }
    }
}