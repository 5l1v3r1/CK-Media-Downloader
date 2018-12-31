/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.DataStructures.video;
import java.io.IOException;

/**
 *
 * @author christopher
 */
public interface Trackable {
    public video similar(); //get a video from the related items list
    public video search(String str) throws IOException; //search (similar to query except no img preview and only 1 result) 
}
