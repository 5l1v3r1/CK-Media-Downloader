/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import java.io.IOException;

/**
 *
 * @author christopher
 */
public interface Searchable {
    //search (similar to query except no img preview and only 1 result) 
    public abstract video search(String str) throws IOException, GenericDownloaderException; 
}
