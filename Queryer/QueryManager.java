/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Queryer;

import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.historyItem;
import downloader.DataStructures.video;
import downloader.Extractors.GenericQueryExtractor;
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
import downloaderProject.MainApp;
import java.awt.Desktop;
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
public class QueryManager {
    private ExecutorService app;
    private Pane root;
    private ListView<Pane> queryPane;
    private Button searchBtn;
    private AnchorPane previewPane;
    private Label searchResultCount;
    private ScrollPane previewScroller;
    private generate generator;
    
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
            if (generator == null)
                generator = new generate(searchString.getText());
            else {
                generator.release();
                generator.setSearch(searchString.getText());
            }
            //display thumbnails of results found
            app.execute(generator);
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
        private String search;
        private GenericQuery results;
        
        public generate() {
            
        }
        
        public generate(String search) {
            setSearch(search);
        }
        
        public void setSearch(String search) {
            this.search = search;
            results = new GenericQuery();
        }
        
        public void load(GenericQuery q) {
            results = q;
            updateView();
        }
        
        public void release() {
            search = null;
            results = null;
        }
        
        //this will return the appropriate extractor
        private GenericQueryExtractor getExtractor(String site) {
            try {
                Class<?> c = Class.forName("downloader.Extractors."+site);
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
                CommonUtils.log(e.getMessage(),this);
                MainApp.createMessageDialog("Connection Error Occurred");
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtils.log("An error occured: "+e.getMessage(),this);
            } finally {
                changeButton("Search");
            }
        }
        
        private void changeButton(String msg) {
            Platform.runLater(() -> {
                searchBtn.setText(msg);
                if (msg.equals("Searching"))
                    searchBtn.setDisable(true);
                else searchBtn.setDisable(false);
            });
        }
        
        private void setOnclickAction() {
            queryPane.setOnMouseClicked((MouseEvent) -> {
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
                            CommonUtils.log(ex.getMessage(), QueryManager.this);
                            MainApp.createMessageDialog("Error");
                        }
                    }//endfor
                }
            });
        }
        
        private Vector<Image> getImages() throws FileNotFoundException{
            Vector<Image> images = new Vector<>();
            
            for(int i = 0; i < results.thumbnailCount(); i++) {
                FileInputStream fis = new FileInputStream(results.getThumbnail(i));
                Image image = new Image(fis);
                images.add(image);
            }
            return images;
        }
        
        private void setButtons(Pane pane, int which) {
            ((Button)pane.lookup("#addList")).setOnAction((ActionEvent) -> {
                //add item to downloadManager for display
                MainApp.dm.addDownload(results.getLink(which));
            });
            
            ((Button)pane.lookup("#later")).setOnAction((ActionEvent) -> {
                try {
                    DataIO.saveVideo(new video(results.getLink(which),results.getName(which),results.getThumbnail(which),results.getSize(which),results.getPreview(which)));
                    MainApp.createMessageDialog("Video saved");
                    MainApp.settings.videoUpdate();
                } catch (IOException e) {
                    MainApp.createMessageDialog("Failed to save video for later");
                }
            });
            
            ((Button)pane.lookup("#browser")).setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent t) {
                    if (Desktop.isDesktopSupported()) {
                        new Thread(() -> {
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.browse(new URI(results.getLink(which)));
                            } catch (URISyntaxException ex) {
                                CommonUtils.log("Bad Uri",this);
                            } catch (IOException ex) {
                                 MainApp.createMessageDialog("Failed to load");
                            }
                        }).start();    
                    } else
                        MainApp.createMessageDialog("Not supported");
                }
            });
        }
        
        private Pane createPane(Image image, int i) throws IOException {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("layouts/queryItem.fxml"));
            Pane pane = loader.load();
            setButtons(pane,i);
            ((ImageView)pane.lookup("#preview")).setImage(image);
            ((Label)pane.lookup("#title")).setText(results.getName(i));
            pane.getStyleClass().add("lighter");
            return pane;
        }
        
        private void updateView() {
            Platform.runLater(() -> {
                ObservableList<Pane> panes = queryPane.getItems();
                try {
                    Vector<Image>images = getImages();
                    for(int i = 0; i < images.size(); i++)
                        panes.add(createPane(images.get(i), i)); //add pane of video thumb and download button etc.
                    setOnclickAction(); //on click of a list item show thumbnails of video
                } catch (FileNotFoundException e) {
                    CommonUtils.log("Error with :("+e.getMessage()+")",this);
                    MainApp.createMessageDialog("Error in cache : 1");
                } catch (IOException e) {
                    CommonUtils.log(e.getMessage(),this);
                }
                searchResultCount.setText(panes.size()+" results found");
            });
        } //end update view   
    } //end inner class
}
