/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import ChrisPackage.Reactable;
import Queryer.QueryManager;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.events.JFXDialogEvent;
import downloader.CommonUtils;
import downloader.DataStructures.Device;
import downloader.DataStructures.video;
import downloader.DownloaderItem;
import downloader.Site;
import downloader.Site.Page;
import java.awt.HeadlessException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author christopher
 */
public class mainLayoutController implements Initializable, Reactable{
    /**
     * Initializes the controller class.
     */
    
    @Override public void initialize(URL url, ResourceBundle rb) {
        MainApp.mainController = this;
    }
    
    @Override public void react() {
        getDownloadLink();
    }
    
    
    public void determineSite(String link) {
        DownloaderItem download = new DownloaderItem();
        download.setLink(link); download.setType(Site.getUrlSite(link)); download.setVideo(null);
        //add item to downloadManager for display
        MainApp.dm.addDownload(download);
    }
    
    public void determineSite(String link, video v) {
        DownloaderItem download = new DownloaderItem();
        download.setLink(link); download.setType(Site.getUrlSite(link)); download.setVideo(v);
        //add item to downloadManager for display
        MainApp.dm.addDownload(download);
    }
    
    public void determineSite(String page, Page type) {
        DownloaderItem download = new DownloaderItem();
        download.setPage(page); download.setPageType(type);
        //add item to downloadManager for display
        MainApp.dm.addDownload(download);
    }
    
    public void getDownloadLink() {
        try { 
            Toolkit tool = Toolkit.getDefaultToolkit();
            Clipboard clip = tool.getSystemClipboard();
            if (clip.getData(DataFlavor.stringFlavor) != null) {
                String clipText = (String)clip.getData(DataFlavor.stringFlavor);
                clipText = clipText.trim(); //trim off any white space that may be on the string to leave the raw link
                String[] token = clipText.split("\n"); //if multiple lines of links on clipboard
                for(String s:token) {
                    CommonUtils.log(this,s);  //this is jus a debugging output
                    if (Site.getUrlSite(s) == Site.Type.none) 
                        CommonUtils.log(this,"Couldn't determine"); //invalid link
                    else determineSite(s);
                }
            }
        } catch(UnsupportedFlavorException e) {
            CommonUtils.log(this,"Unsupported clipboard entry "+e.getMessage());
        }catch (HeadlessException | IOException e) {
            CommonUtils.log(this,e.getMessage());
            //MainApp.createMessageDialog(e.getMessage());
            //e.printStackTrace();
        }
    }
    
    public void getDownloadPage() {
        try { 
            Toolkit tool = Toolkit.getDefaultToolkit();
            Clipboard clip = tool.getSystemClipboard();
            if (clip.getData(DataFlavor.stringFlavor) != null) {
                String clipText = (String)clip.getData(DataFlavor.stringFlavor);
                clipText = clipText.trim(); //trim off any white space that may be on the string
                if (Site.getPageSite(clipText) == Site.Page.none) 
                    CommonUtils.log(this,"Was none"); //invalid link
                else
                    determineSite(clipText, Site.getPageSite(clipText));
            }
        } catch(UnsupportedFlavorException e) {
            CommonUtils.log(this,"Unsupported clipboard entry: "+e.getMessage());
        }catch (HeadlessException | IOException e) {
            CommonUtils.log(this,e.getMessage());
        }
    }
    
    public void queryString() {
    	if (MainApp.query == null)
    		MainApp.query = new QueryManager(MainApp.actionPanes[MainApp.BROWSERPANE]);
        MainApp.query.generateContent();
    }
    
    public void setVideoLocation() {
        DirectoryChooser choose = new DirectoryChooser();
        choose.setTitle("Choose a download location");
        if((MainApp.settings.preferences.getVideoFolder() != null) && (MainApp.settings.preferences.videoFolderValid()))
            choose.setInitialDirectory(MainApp.settings.preferences.getVideoFolder());
        File selected = choose.showDialog(null);
        if (selected != null) {
            MainApp.settings.setVideoText(selected.getAbsolutePath());
            MainApp.settings.preferences.setVideoFolder(selected);
            MainApp.settings.saveSettings();
        }
    }
    
    public void setPictureLocation() {
       DirectoryChooser choose = new DirectoryChooser();
        choose.setTitle("Choose a download location");
        if((MainApp.settings.preferences.getPictureFolder() != null) && (MainApp.settings.preferences.pictureFolderValid()))
            choose.setInitialDirectory(MainApp.settings.preferences.getPictureFolder());
        File selected = choose.showDialog(null);
        if (selected != null) {
            MainApp.settings.setPictureText(selected.getAbsolutePath());
            MainApp.settings.preferences.setPictureFolder(selected);
            MainApp.settings.saveSettings();
        } 
    }
    
    public void setSharedLocation() {
       DirectoryChooser choose = new DirectoryChooser();
        choose.setTitle("Choose a download location");
        if((MainApp.settings.preferences.getSharedFolder() != null) && (MainApp.settings.preferences.sharedFolderValid()))
            choose.setInitialDirectory(MainApp.settings.preferences.getSharedFolder());
        File selected = choose.showDialog(null);
        if (selected != null) {
            MainApp.settings.setSharedText(selected.getAbsolutePath());
            MainApp.settings.preferences.setSharedFolder(selected);
            MainApp.settings.saveSettings();
        } 
    }
    
    public void importLinks() {
        try {
            //MainApp.createMessageDialog("Each link in the file should be on a new line");
            FileChooser choose = new FileChooser(); 
            Vector<String> lines = new Vector<>();
            choose.setTitle("Select a file import links from");
            if((MainApp.settings.preferences.getImportFolder() != null) && (MainApp.settings.preferences.getImportFolder().exists() && (MainApp.settings.preferences.getImportFolder().isDirectory())))
                choose.setInitialDirectory(MainApp.settings.preferences.getImportFolder());
            File selected = choose.showOpenDialog(null);
            if (selected != null) {
                try (Scanner reader = new Scanner(selected)){
                    while(reader.hasNextLine())
                        lines.add(reader.nextLine());
                    reader.close();
                } catch (FileNotFoundException e) {
                    MainApp.createMessageDialog("File doesn't exist");
                }
                for(int i = 0; i < lines.size(); i++) {
                    CommonUtils.log(this,lines.get(i).trim());
                    if (Site.getUrlSite(lines.get(i).trim()) != Site.Type.none)
                        determineSite(lines.get(i).trim());
                }
                MainApp.settings.preferences.setImportFolder(selected.getParentFile());
                MainApp.settings.saveSettings();
            }
            choose = null;
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtils.log(this,e.getMessage());
        }
    }
    
    public void showDownloads() {
        MainApp.displayPane(MainApp.DOWNLOADPANE);
    }
    
    public void showSettings() {
        MainApp.displayPane(MainApp.SETTINGSPANE);
    }
    
    public void showBrowser() {
        MainApp.displayPane(MainApp.BROWSERPANE);
    }
    
    public void showShare() {
        MainApp.displayPane(MainApp.SHAREPANE);
    }
    
    public void showDownloadHistory() {
        MainApp.displayPane(MainApp.DOWNLOADHISTORYPANE);
    }
    
    public void showAccounts() {
        MainApp.displayPane(MainApp.ACCOUNTPANE);
    }
    
    private static boolean isDup(String name) {
        for(Device d: MainApp.devices) {
            if(d.is(name))
                return true;
        }
        return false;
    }
    
    public void addDevice() {
        createInputDialog();
    }
    
    public void sendSavedVideos() {
        Vector<video> videos = DataIO.loadVideos();
        if(videos == null)
            MainApp.createMessageDialog("No saved media");
        else
            MainApp.act.sendSaved(videos);
    }
    
    public void receiveSavedVideos() {
        MainApp.act.receiveSaved();
    }
    
    public void sendMedia() {
        MainApp.act.sendMedia();
    }
    
    public void receiveMedia() {
        MainApp.act.receiveMedia();
    }
    
    public void loadSavedVideos() {
        Vector<video> videos = DataIO.loadVideos();
        if(videos == null) 
            MainApp.createMessageDialog("No saved media");
        else {
            for(int i = 0; i < videos.size(); i++)
                determineSite(videos.get(i).getLink(),videos.get(i));
        }
    }
    
    public void clearSavedVideos() {
        createConfirmDialog("Are you sure you want to clear saved videos?",5);
    }
    
    public void clearCache() {
        createConfirmDialog("Clearing cache will cause search history to be deleted. Do you wish to continue?",4);
    }
    
    public void clearHistory() {
        createConfirmDialog("Are you sure you want to clear history?",3);
    }
    
    public void clearDownloadHistory() {
        createConfirmDialog("Are you sure you want to clear download history?",2);
    }
    
    public void clearDevices() {
        createConfirmDialog("Are you sure you want to clear devices?",1);
    }
    
    public static void createInputDialog() {
        Platform.runLater(new Runnable() {
            Pane pane; BoxBlur blur = new BoxBlur(5,5,5);
            @Override public void run() {
                try {
                    pane = FXMLLoader.load(new MainApp().getClass().getResource("layouts/inputDialog.fxml"));
                    pane.getStylesheets().clear();
                    if(MainApp.settings.preferences.dark())
                        pane.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                    else pane.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
                    AnchorPane a = (AnchorPane)MainApp.root.getChildren().get(0);
                    JFXDialog dialog = new JFXDialog(MainApp.root,pane,JFXDialog.DialogTransition.CENTER);
                    TextField nick = (TextField)pane.lookup("#nickname");
                    TextField device = (TextField)pane.lookup("#hostname");
                    Button enter = (Button)pane.lookup("#enterBtn");
                    enter.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            dialog.close();
                            if((nick == null) || (nick.getText().length() < 1)) return;
                            if(isDup(nick.getText())) {MainApp.createMessageDialog("You already have a device with that name"); return;}
                            if ((device == null) || (device.getText().length() < 1)) return;
                            try {
                                DataIO.saveDevice(new Device(nick.getText(),device.getText()));
                                MainApp.updateDevices();
                            } catch (FileNotFoundException e) {
                                MainApp.createMessageDialog("Failed to save new device");
                                CommonUtils.log(this,"File not found");
                            } catch (IOException e) {
                                MainApp.createMessageDialog("Failed to save new device");
                                CommonUtils.log(this,e.getMessage());
                            }
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
    
    public static void createConfirmDialog(String msg, int action) {
        Platform.runLater(new Runnable() {
            Pane pane; BoxBlur blur = new BoxBlur(5,5,5);
            @Override public void run() {
                try {
                    pane = FXMLLoader.load(new MainApp().getClass().getResource("layouts/confirmDialog.fxml"));
                    pane.getStylesheets().clear();
                    if(MainApp.settings.preferences.dark())
                        pane.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                    else pane.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
                    AnchorPane a = (AnchorPane)MainApp.root.getChildren().get(0);
                    JFXDialog dialog = new JFXDialog(MainApp.root,pane,JFXDialog.DialogTransition.CENTER);
                    Label text = (Label)pane.lookup("#msg");
                    Button ok = (Button)pane.lookup("#yesBtn");
                    Button no = (Button)pane.lookup("#noBtn");
                    Button cancel = (Button)pane.lookup("#cancelBtn");
                    cancel.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent t) {
                            dialog.close();
                        }
                    });
                    no.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent t) {
                            dialog.close();
                        }
                    });
                    text.setText(msg);
                    ok.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            dialog.close();
                            switch(action) {
                                case 1:
                                    DataIO.clearDevices();
                                    MainApp.updateDevices();
                                    break;
                                case 2:
                                    MainApp.downloadHistoryList.clear();
                                    break;
                                case 3:
                                    DataIO.clearHistory();
                                    MainApp.settings.clearHistory();
                                    MainApp.settings.historyUpdate();
                                    break;
                                case 4:
                                    DataIO.clearCache();
                                    MainApp.settings.cacheUpdate();
                                    MainApp.settings.setHistory();
                                    break;
                                case 5:
                                    DataIO.clearVideos();
                                    MainApp.settings.videoUpdate();
                                    break;
                            }
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
}
