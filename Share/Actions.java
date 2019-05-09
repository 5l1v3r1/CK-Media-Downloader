/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Share;

import downloader.DataStructures.Device;
import downloader.DataStructures.video;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.util.Vector;
import javafx.application.Platform;
import javafx.stage.FileChooser;

/**
 *
 * @author christopher
 */
public class Actions {
    public static final int PORT = 32901;
    private boolean isPureDigit(String s) {
        if (s.length() < 1) return false;
        for(int i = 0; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i)))
                return false;
        return true;
    }
    
    private void updateProgressBar(final float progress) {
        Platform.runLater(() -> {
            MainApp.progress.setProgress(progress);
        });
    }
    
    private void displayStatus(String msg) {
        Platform.runLater(() -> {
            MainApp.log.appendText("\n"+msg);
        });
    }
    
    private String getAddress() {
        for(Device d : MainApp.devices) {
            if(MainApp.deviceBox.getSelectionModel().getSelectedItem() == null)
                return null;
            if (d.is(MainApp.deviceBox.getSelectionModel().getSelectedItem().toString()))
                return d.address();
        }
        return null;
    }
    
    private void monitor(OperationStream s) {
        Thread t = new Thread(() -> {
            String text;
            while (!(text = s.getProgress()).equals("Finished")) {
                if (text != null) {
                    if (isPureDigit(text))
                        updateProgressBar((float)Integer.parseInt(text)/100);
                    else displayStatus(text);
                }
            } displayStatus("Finished");
        });
        t.setDaemon(true); //make sure thread stops as main thread stops
        t.start();
        MainApp.settings.videoUpdate();
    }
    
    public void sendSaved(Vector<video> videos) {
        OperationStream os = new OperationStream();
        new Upload(getAddress(),os,videos).send();
        monitor(os);
    }
    
    public void receiveSaved() {
        OperationStream os = new OperationStream();
        new Download(os).receive();
        monitor(os);
    }
    
    public void sendMedia() {
        FileChooser choose = new FileChooser();
        if((MainApp.settings.preferences.getVideoFolder() != null) && (MainApp.settings.preferences.videoFolderValid()))
            choose.setInitialDirectory(MainApp.settings.preferences.getVideoFolder());
        choose.setTitle("Choose file to send");
        File selected = choose.showOpenDialog(null);
        if (selected != null) {
            if(selected.isDirectory()) 
                MainApp.createMessageDialog("Cant send a directory");
            else {
                OperationStream os = new OperationStream();
                new Upload(getAddress(),os,selected).send();
	        monitor(os);
            }
        }
    }
    
    public void receiveMedia() {
        OperationStream os = new OperationStream();
        new Download(os).receive();
        monitor(os);
    }
}
