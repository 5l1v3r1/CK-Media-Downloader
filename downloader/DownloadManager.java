/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import downloaderProject.MainApp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

/**
 *
 * @author christopher
 */
public class DownloadManager {
    private List downloads = new ArrayList(); //list of panes
    private List<DownloaderItem> downloadItems = new ArrayList<>();//list of downloader items that create panes
    private final ExecutorService downloadThreads;
    private final ExecutorService sideJobs;
    //private final int threads = 3;
    private final int sideThreads = 5;
    
    public void release() {
        try {
            downloadThreads.shutdown();
            downloadThreads.awaitTermination(2, TimeUnit.SECONDS);
            downloadThreads.shutdownNow();
            sideJobs.shutdown();
            sideJobs.awaitTermination(2, TimeUnit.SECONDS);
            sideJobs.shutdownNow();
            downloads = null;
            downloadItems = null;
        } catch (InterruptedException ex) {
            System.out.println("Failed to stop download threads");
        }
    }
    
    public DownloadManager() {
        downloadThreads = Executors.newCachedThreadPool(); //threads for downloading are set to limit # of simultaneous downloads
        sideJobs = Executors.newFixedThreadPool(sideThreads); //fixed amount mineial tasks
    }
    
    public void changeTheme(boolean enable) {
        for(int i = 0; i < downloads.size(); i++) {
            if (((Pane)downloads.get(i)).getStylesheets() != null) ((Pane)downloads.get(i)).getStylesheets().clear();
            if(enable)
                ((Pane)downloads.get(i)).getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else ((Pane)downloads.get(i)).getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        }
    }
    
    public synchronized void addDownload(DownloaderItem d) {
        if(!isDup(d)) {
            display(d);
            sideJobs.execute(new search(d));
            System.gc();
        }
    }
    
    private boolean isDup(DownloaderItem d) {
        for(int i = 0; i < downloadItems.size(); i++) {
            if (downloadItems.get(i).getLink().equals(d.getLink()))
                return true;
        }
        return false;
    }
    
    private void display(DownloaderItem d) {
        try {
            downloadItems.add(d);
            downloads.add(d.createItem());
            ObservableList list = FXCollections.observableList(downloads);
            MainApp.downloads.setItems(list);
        } catch (IOException e) {
           MainApp.createMessageDialog("An internal error occurred: 1");
        }
    }
    
    public void startDownload(DownloaderItem who) {
        downloadThreads.execute(new manager(who));
    }
    
    public void removeDownload(DownloaderItem which) {
        int i = downloadItems.indexOf(which);
        downloadItems.get(i).setDone();
        downloadItems.get(i).release();
        downloadItems.remove(i);
        downloads.remove(i);
        ObservableList list = FXCollections.observableList(downloads);
        MainApp.downloads.setItems(list);
    }
    
    public void removeAll() {
        for(DownloaderItem d: downloadItems)
            d.setDone();
        downloadItems.clear();
        downloads.clear();
        ObservableList list = FXCollections.observableList(downloads);
        MainApp.downloads.setItems(list);
    }
    
    public void exportAll() {
        DirectoryChooser save = new DirectoryChooser();
        save.setTitle("Select a folder export links to");
        if((MainApp.preferences.getImportFolder() != null) && (MainApp.preferences.getImportFolder().exists() && (MainApp.preferences.getImportFolder().isDirectory())))
            save.setInitialDirectory(MainApp.preferences.getImportFolder());
        File selected = save.showDialog(null);
        if(selected != null) {
            try {
                Formatter writter = new Formatter(new File(selected.getAbsolutePath() + File.separator + "urls.txt"));
                for(int i = 0; i < downloadItems.size(); i++)
                    writter.format("%s\n",downloadItems.get(i).getLink());
                writter.flush();
                writter.close();
                MainApp.createMessageDialog("Links exported successfully");
            } catch (FileNotFoundException e) {
                MainApp.createMessageDialog("File not found");
            } catch (IOException e) {
                MainApp.createMessageDialog("An IOException occurred: "+e.getMessage());;
            }
        }
    }
    
    private class search implements Runnable {
        DownloaderItem d;
        
        public search(DownloaderItem d) {
            this.d = d;
        }
        
        @Override
        public void run() {
            System.out.println("Searching link");
            if(!d.searchLink()) {
                //MainApp.createMessageDialog("Error with: "+d.getLink());
                removeDownload(d);
            }
        }
    }
    
    private class manager implements Runnable{
        DownloaderItem item;
        
        public manager(DownloaderItem what) {
            this.item = what;
        }
        
        @Override
        public void run() {
            item.start();
        }
    }
}
