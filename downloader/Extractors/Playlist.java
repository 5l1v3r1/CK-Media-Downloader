/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.Exceptions.GenericDownloaderException;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author christopher
 */
public interface Playlist {
    public boolean isPlaylist();
    public Vector<String> getItems() throws IOException, GenericDownloaderException;
}
