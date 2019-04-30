/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 *
 * @author christopher
 */
public class StreamManager {
    private final Pane root;
    private final Button play, stop; 
    private final Slider slide;
    private MediaPlayer player;
    
    public StreamManager(Pane p) {
        this.root = p;
        play = (Button)root.lookup("#toogle");
        stop = (Button)root.lookup("#stop");
        slide = (Slider)root.lookup("#progress");
        slide.setMin(0.0);
        play.setDisable(true);
        stop.setDisable(true);
    }
    
    public void setMedia(String url) throws MalformedURLException, URISyntaxException {
        if (player != null)
            player.dispose();
        player = new MediaPlayer(new Media(new URL(url.replace("https","http")).toURI().toString()));
        ((MediaView)root.lookup("#video")).setMediaPlayer(player);
        
        configurePlayer();
        
        play.setText("Pause");
        player.play();
        
        Platform.runLater(() -> {
            MainApp.displayPane(MainApp.STREAMPANE);
        });
    }
    
    private void configurePlayer() {
        player.setOnReady(() -> {
            slide.setValue(0.0);
            slide.setMax(player.getTotalDuration().toSeconds());
        });
        
        player.currentTimeProperty().addListener((ObservableValue<? extends Duration> ov, Duration t, Duration current) -> {
            slide.setValue(current.toSeconds());
        });
        
        slide.setOnMousePressed((MouseEvent) -> {
            if(player != null)
                player.seek(Duration.seconds(slide.getValue()));
        });
        
        slide.setOnMouseClicked((MouseEvent) -> {
            if(player != null)
                player.seek(Duration.seconds(slide.getValue()));
        });
        
        play.setDisable(false);
        stop.setDisable(false);
        play.setOnAction((ActionEvent) -> {
            tooglePlay();
        });
        stop.setOnAction((ActionEvent) -> {
            stop();
        });
    }
    
    public void stop() {
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
            ((MediaView)root.lookup("#video")).setMediaPlayer(null);
        }
        play.setDisable(true);
        stop.setDisable(true);
    }
    
    public void tooglePlay() {
        if(player.getStatus() == MediaPlayer.Status.PLAYING) {
            play.setText("Play");
            player.pause();
        } else { 
            play.setText("Pause");
            player.play();
        }
    }
}
