/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Queryer;

import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.historyItem;
import downloader.DataStructures.video;
import downloader.DownloaderItem;
import downloader.Extractors.Bigbootytube;
import downloader.Extractors.GenericQueryExtractor;
import downloader.Extractors.Pornhub;
import downloader.Extractors.Redtube;
import downloader.Extractors.Ruleporn;
import downloader.Extractors.Shesfreaky;
import downloader.Extractors.SpankBang;
import downloader.Extractors.Spankwire;
import downloader.Extractors.Thumbzilla;
import downloader.Extractors.Tube8;
import downloader.Extractors.Xhamster;
import downloader.Extractors.Xvideos;
import downloader.Extractors.Youporn;
import downloader.Site;
import static downloader.Site.QueryType;
import downloaderProject.DataIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import downloaderProject.MainApp;
import java.awt.Desktop;
import java.io.InputStream;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 *
 * @author christopher
 */
public class QueryManager {
    private ExecutorService app;
    
    public void release() {
        try {
            if (app != null) {
                app.shutdown();
                app.awaitTermination(2, TimeUnit.SECONDS);
                app.shutdownNow();
            }
        } catch (InterruptedException ex) {
            
        }
    }
    
    public void generateContent(String url) {
        //if a search was done previously clear results 
        MainApp.queryPane.getItems().clear();
        clearPreviewImages();
        MainApp.searchResultCount.setText("");
        
        app = Executors.newSingleThreadExecutor();
        //display thumbnails of results found    
        app.execute(new generate(url));
        app.shutdown();
    }
    
    public void clearPreviewImages() {
        MainApp.previewPane.getChildren().clear();
        //for(int i = 0; i < MainApp.previewImages.size(); i++)
          //  MainApp.previewImages.get(i).imageProperty().set(null);
    }
    
    public void loadSearch(GenericQuery q) {
        //if a search was done previously clear results 
        MainApp.queryPane.getItems().clear();
        clearPreviewImages();
        MainApp.searchResultCount.setText("");
        new generate().load(q);
    }
    
    private class generate implements Runnable {
        String search;
        GenericQuery results = new GenericQuery();
        
        public generate() {
            
        }
        
        public generate(String search) {
            this.search = search;
        }
        
        public void load(GenericQuery q) {
            results = q;
            updateView();
        }
        
        //this will return the appropriate extractor
        private GenericQueryExtractor getExtractor(String site) {
            if(site.equals("spankbang"))
                return new SpankBang();
            else if (site.equals("pornhub"))
                return new Pornhub();
            else if (site.equals("xhamster"))
                return new Xhamster();
            else if (site.equals("xvideos"))
                return new Xvideos();
            else if (site.equals("youporn"))
                return new Youporn();
            else if (site.equals("redtube"))
                return new Redtube();
            else if (site.equals("thumbzilla"))
                return new Thumbzilla();
            else if (site.equals("shesfreaky"))
                return new Shesfreaky();
            else if (site.equals("tube8"))
                return new Tube8();
            else if(site.equals("spankwire"))
                return new Spankwire();
            else if(site.equals("bigbootytube"))
                return new Bigbootytube();
            else if (site.equals("ruleporn"))
                return new Ruleporn();
            else return null;
        }
        
        //this determines which sites are enabled for querying
        private void populateExtractors(GenericQueryExtractor[] ex) { 
            for(int i = 0; i < QueryType.length; i++)
               if (MainApp.settings.preferences.isEnabled(QueryType[i]))
                   ex[i] = getExtractor(QueryType[i]);
        }
        
        private void generateHistory(GenericQueryExtractor[] extractor) {
            historyItem history = new historyItem(search);
            for(GenericQueryExtractor e : extractor)
                if(e != null)
                    history.addSite(e.name());
            history.setSearchResult(results);
            try {
                DataIO.saveHistory(history);
            } catch (FileNotFoundException e) {
                MainApp.createMessageDialog("Failed to save history:"+e.getMessage());
            } catch (IOException e) {
                MainApp.createMessageDialog("Failed to save history:"+e.getMessage());
            }
            MainApp.settings.setHistory();
            MainApp.createNotification("Search Complete", "Finished Searching");
        }
        
        @Override public void run() {
            try {
                changeButton("Searching");
                GenericQueryExtractor[] extractor = new GenericQueryExtractor[QueryType.length];
                populateExtractors(extractor);
                for(int i = 0; i < QueryType.length; i++)
                    if (extractor[i] != null)
                        results.addQuery(extractor[i].query(search));
                updateView();
                generateHistory(extractor);
            } catch (SocketTimeoutException e) {
                MainApp.createMessageDialog("A Page took too long to respond");
            } catch(UncheckedIOException e ){
                MainApp.createMessageDialog("Error downloading a page");
            } catch(IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                MainApp.createMessageDialog("Connection Error Occurred");
            } catch (Exception e) {
                e.printStackTrace();
                MainApp.createMessageDialog("An error occured: "+e.getMessage());
            } finally {
                changeButton("Search");
            }
        }
        
        private void changeButton(String msg) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    MainApp.searchBtn.setText(msg);
                    if (msg.equals("Searching"))
                        MainApp.searchBtn.setDisable(true);
                    else MainApp.searchBtn.setDisable(false);
                }
            });
        }
        
        private void setOnclickAction() {
            MainApp.queryPane.setOnMouseClicked(new EventHandler() {
                @Override public void handle(Event t) {
                clearPreviewImages();
                int index = MainApp.queryPane.getSelectionModel().getSelectedIndex();
                Vector<File> imgs = results.getPreview(index);
                int currentHeight = 0;
                for(int j = 0; j < imgs.size(); j++) {
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(imgs.get(j));
                        Image image = new Image(fis);
                        //adjust size of imageview to fit the image and set the image
                        MainApp.previewPane.setMinWidth(image.getWidth());
                        ImageView imgView = new ImageView();
                        imgView.setLayoutY(currentHeight);
                        currentHeight += image.getHeight();
                        MainApp.previewPane.setMinHeight(currentHeight);
                        imgView.setFitHeight(image.getHeight());
                        imgView.setFitWidth(image.getWidth());
                        imgView.setImage(image);
                        MainApp.previewPane.getChildren().add(imgView);
                    } catch (FileNotFoundException ex) {
                       MainApp.createMessageDialog("Error");
                    }
                }//endfor
            }});
        }
        
        private Vector<ImageView> getImages() throws FileNotFoundException{
            Vector<ImageView> images = new Vector<>();
            
            for(int i = 0; i < results.thumbnailCount(); i++) {
                FileInputStream fis = new FileInputStream(results.getThumbnail(i));
                Image image = new Image(fis);
                ImageView view = new ImageView();
                view.setImage(image);
                //final int current = i;
                images.add(view);
            }
            return images;
        }
        
        private Button createOpenBroswerBtn(int which, double offset) {
            Button btn = new Button();
            btn.setLayoutX(offset+20);
            btn.setLayoutY(80);
            btn.setText("Open in browser");
            btn.setOnAction(new EventHandler() {
                @Override
                public void handle(Event t) {
                    if (Desktop.isDesktopSupported()) {
                        new Thread(() -> {
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.browse(new URI(results.getLink(which)));
                            } catch (URISyntaxException ex) {
                                System.out.println("Bad Uri");
                            } catch (IOException ex) {
                                 MainApp.createMessageDialog("Failed to load");
                            }
                        }).start();    
                    } else
                        MainApp.createMessageDialog("Not supported");
                }
            });
            InputStream in = System.class.getResourceAsStream("/icons/icons8-open-in-browser-40.png");
            Image img = new Image(in);
            ImageView icon = new ImageView(img);
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            
            btn.setGraphic(icon);
            
            return btn;
        }
        
        private Button createDownloadLaterBtn(int which, double offset) {
            Button btn = new Button();
            btn.setLayoutX(offset+20);
            btn.setLayoutY(45);
            btn.setText("Download Later");
            btn.setOnAction(new EventHandler() {
                @Override
                public void handle(Event t) {
                    try {
                        DataIO.saveVideo(new video(results.getLink(which),results.getName(which),results.getThumbnail(which),results.getSize(which),results.getPreview(which)));
                        MainApp.createMessageDialog("Video saved");
                        MainApp.settings.videoUpdate();
                    } catch (IOException e) {
                        MainApp.createMessageDialog("Failed to save video for later");
                    }
                }
            });
            
            InputStream in = System.class.getResourceAsStream("/icons/icons8-download-from-cloud-40.png");
            Image img = new Image(in);
            ImageView icon = new ImageView(img);
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            
            btn.setGraphic(icon);
            
            return btn;
        }
        
        private Button createDownloadBtn(int which, double offset) {
            Button btn = new Button();
            btn.setLayoutX(offset+20);
            btn.setLayoutY(10);
            btn.setText("Add to Download List");
            btn.setOnAction(new EventHandler() {
                @Override
                public void handle(Event t) {
                    DownloaderItem download = new DownloaderItem();
                    download.setLink(results.getLink(which)); 
                    download.setType(Site.getUrlSite(results.getLink(which)));
                    //add item to downloadManager for display
                    MainApp.dm.addDownload(download);
                }
            });
            
            InputStream in = System.class.getResourceAsStream("/icons/icons8-download-80.png");
            Image img = new Image(in);
            ImageView icon = new ImageView(img);
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            
            btn.setGraphic(icon);
            
            return btn;
        }
        
        private Pane createPane(ImageView image, int i) {
            Pane pane = new Pane();
            int width;
            if (image.getImage().getHeight() > 200) {
                pane.setPrefHeight(200); image.setFitHeight(200);
            } else pane.setPrefHeight(image.getImage().getHeight());
            if (image.getImage().getWidth() > 235)
                width = 235;
            else width = (int) image.getImage().getWidth();
            image.setFitWidth(width);
            pane.setPrefWidth(500); 
            pane.getChildren().add(image);
            pane.getChildren().add(createDownloadBtn(i,width));
            pane.getChildren().add(createDownloadLaterBtn(i,width));
            pane.getChildren().add(createOpenBroswerBtn(i,width));
            pane.getStyleClass().add("lighter");
            return pane;
        }
        
        private void updateView() {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    ObservableList<Pane> panes = MainApp.queryPane.getItems();
                    try {
                        Vector<ImageView>images = getImages();
                        for(int i = 0; i < images.size(); i++)
                            panes.add(createPane(images.get(i), i)); //add pane of video thumb and download button etc.
                        setOnclickAction(); //on click of a list item show thumbnails of video
                    } catch (FileNotFoundException e) {
                        MainApp.createMessageDialog("Error in cache : 1");
                    }
                    MainApp.searchResultCount.setText(panes.size()+" results found");
                }
            });
        } //end update view   
    } //end inner class
}
