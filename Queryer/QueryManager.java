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
import downloader.Extractors.GenericExtractor;
import downloader.Extractors.GenericQueryExtractor;
import downloader.Extractors.Pornhub;
import downloader.Extractors.Redtube;
import downloader.Extractors.Ruleporn;
import downloader.Extractors.Shesfreaky;
import downloader.Extractors.Spankbang;
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
import java.util.Iterator;
import java.util.Vector;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import downloaderProject.MainApp;
import java.awt.Desktop;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jsoup.UncheckedIOException;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
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
public class QueryManager {
    private ExecutorService app;
    private Pane root;
    private ListView<Pane> queryPane;
    private Button searchBtn;
    private AnchorPane previewPane;
    private Label searchResultCount;
    private ScrollPane previewScroller;
    
    //private final int scrollerWidth = 287;
    
    public QueryManager(Pane root) {
    	this.root = root;
    	getRefs();
    }
    
    private void getRefs() {
    	queryPane = (ListView<Pane>)root.lookup("#resultPane"); //list view of thumbnails from search
        searchBtn = (Button)root.lookup("#queryButton");
    	
    	//get the scrollpane to get the anchor which contains the imageviews
        previewScroller = (ScrollPane)root.lookup("#scroll");
        previewPane = (AnchorPane)previewScroller.getContent();
        searchResultCount = (Label)root.lookup("#searchResult");
    }
    
    public void switchTheme(boolean enable) {
    	if (queryPane != null) {
            Iterator<Pane> i = queryPane.getItems().iterator();
            while(i.hasNext()) {
                Pane j = i.next(); if (j.getStylesheets() != null) j.getStylesheets().clear();
                if (enable)
                    j.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
                else j.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
            }
        }
    }
    
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
    
    public void generateContent() {
        TextField searchString = (TextField)root.lookup("#queryBox");
        if (searchString.getText().length() > 0) {
            //if a search was done previously clear results 
            queryPane.getItems().clear();
            clearPreviewImages();
            searchResultCount.setText("");

            app = Executors.newSingleThreadExecutor();
            //display thumbnails of results found    
            app.execute(new generate(searchString.getText()));
            app.shutdown();
        } else MainApp.createMessageDialog("Probably should enter something to search first");
    }
    
    public void clearPreviewImages() {
        previewPane.getChildren().clear();
        //for(int i = 0; i < MainApp.previewImages.size(); i++)
          //  MainApp.previewImages.get(i).imageProperty().set(null);
    }
    
    public void loadSearch(GenericQuery q) {
        //if a search was done previously clear results 
        queryPane.getItems().clear();
        clearPreviewImages();
        searchResultCount.setText("");
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
            try {
                Class<?> c = Class.forName("downloader.Extractors."+site.toString().substring(0,1).toUpperCase()+site.toString().substring(1));
                Constructor<?> cons = c.getConstructor();
                return (GenericQueryExtractor)cons.newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                return null;
            }
            //spankbang, pornhub, xhamster, xvideos, youporn, redtube, thumbzilla, shesfreaky, tube8,
            //spankwire, bigbootytube, ruleporn
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
                    history.addSite(e.getClass().getSimpleName());
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
                    searchBtn.setText(msg);
                    if (msg.equals("Searching"))
                        searchBtn.setDisable(true);
                    else searchBtn.setDisable(false);
                }
            });
        }
        
        private void setOnclickAction() {
            queryPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					clearPreviewImages();
	                int index = queryPane.getSelectionModel().getSelectedIndex();
	                Vector<File> imgs = results.getPreview(index);
	                int currentHeight = 0;
	                if (imgs != null) {
		                for(int j = 0; j < imgs.size(); j++) {
		                    FileInputStream fis;
		                    try {
		                        fis = new FileInputStream(imgs.get(j));
		                        Image image = new Image(fis);
		                        //adjust size of imageview to fit the image and set the image
		                        double width = 285;
		                        if (image.getWidth() < 285)
		                        	width = image.getWidth();
		                        previewPane.setPrefWidth(width);
		                        ImageView imgView = new ImageView();
		                        imgView.setFitWidth(width);
		                        imgView.setImage(image);
		                        imgView.setLayoutY(currentHeight);
		                        currentHeight += image.getHeight();
		                        previewPane.setMinHeight(currentHeight);
		                        previewPane.getChildren().add(imgView);
		                    } catch (FileNotFoundException ex) {
		                       MainApp.createMessageDialog("Error");
		                    }
		                }//endfor
	                }
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
            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
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
            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
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
            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
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
            pane.setPrefWidth(450); 
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
                    ObservableList<Pane> panes = queryPane.getItems();
                    try {
                        Vector<ImageView>images = getImages();
                        for(int i = 0; i < images.size(); i++)
                            panes.add(createPane(images.get(i), i)); //add pane of video thumb and download button etc.
                        setOnclickAction(); //on click of a list item show thumbnails of video
                    } catch (FileNotFoundException e) {
                        MainApp.createMessageDialog("Error in cache : 1");
                    }
                    searchResultCount.setText(panes.size()+" results found");
                }
            });
        } //end update view   
    } //end inner class
}
