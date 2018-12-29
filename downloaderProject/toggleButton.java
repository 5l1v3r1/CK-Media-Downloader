/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author christopher
 */

public class toggleButton extends Parent{
    private boolean switchedOn;

    final private TranslateTransition translateAnimation = new TranslateTransition(Duration.seconds(0.25));
    final private FillTransition fillAnimation = new FillTransition(Duration.seconds(0.25));

    final private ParallelTransition animation = new ParallelTransition(translateAnimation, fillAnimation);
    
    public toggleButton() {
        this(false);
    }
    
    public toggleButton(boolean state) {
        Rectangle background = new Rectangle(25, 12.5);
        background.setArcWidth(12.5);
        background.setArcHeight(12.5);
        background.setFill(Color.WHITE);
        background.setStroke(Color.LIGHTGRAY);

        Circle trigger = new Circle(6.25);
        trigger.setCenterX(6.25);
        trigger.setCenterY(6.25);
        trigger.setFill(Color.WHITE);
        trigger.setStroke(Color.LIGHTGRAY);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(2);
        trigger.setEffect(shadow);

        translateAnimation.setNode(trigger);
        fillAnimation.setShape(background);

        getChildren().addAll(background, trigger);
        setBoolValue(state);

        /*switchedOn.addListener((obs, oldState, newState) -> {
            boolean isOn = switchedOn.getValue(); //newState.booleanValue();
            
        });*/
    }
    
    public void setBoolValue(boolean state) {
        switchedOn = state;
        translateAnimation.setToX(state ? 25 - 12.5 : 0);
        fillAnimation.setFromValue(state ? Color.WHITE : Color.LIGHTBLUE);
        fillAnimation.setToValue(state ? Color.LIGHTBLUE : Color.WHITE);

        animation.play();
    }
        
    public boolean switchedOnProperty() {
       return switchedOn;
    }
}
