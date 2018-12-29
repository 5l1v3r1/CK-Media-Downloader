/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author christopher
 */
public class historyItem implements Serializable{
    private static final long serialVersionUID = 1L;
    private String search;
    private List<String> sites;
    private GenericQuery searchRef;
    
    public historyItem() {
        sites = new ArrayList<>();
    }
    
    public historyItem(String search) {
        this();
        this.search = search;
    }
    
    public void addSite(String site) {
        sites.add(site);
    }
    
    public String Search() {
        return this.search;
    }
    
    public void Search(String search) {
        this.search = search;
    }
    
    public int siteCount() {
        return sites.size();
    }
    
    public String getSite(int i) {
        if((i > -1) && (i < sites.size()))
            return sites.get(i);
        else return null;
    }
    
    public GenericQuery getSearchResult() {
        return searchRef;
    }
    
    public void setSearchResult(GenericQuery q) {
        this.searchRef = q;
    }
    
    @Override public boolean equals(Object o) {
        if (o instanceof historyItem) {
            historyItem temp = (historyItem)o;
            return (temp.search.equals(this.search)) && (temp.sites.equals(this.sites)) && (temp.searchRef.equals(this.searchRef));
        } else return false;
    }
}
