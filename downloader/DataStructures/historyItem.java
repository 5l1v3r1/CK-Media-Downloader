/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author christopher
 */
public class historyItem implements Externalizable{
    private static final long serialVersionUID = 1L, VERSION = 2;
    private String search, date;
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
    
    public String getDate() {
        return date;
    }
    
    public GenericQuery getSearchResult() {
        return searchRef;
    }
    
    public void setSearchResult(GenericQuery q) {
        this.searchRef = q;
    }
    
    public void setDate(String s) {
        date = s;
    }
    
    @Override public boolean equals(Object o) {
        if (o instanceof historyItem) {
            historyItem temp = (historyItem)o;
            return (temp.search.equals(this.search)) && (temp.sites.equals(this.sites)) && (temp.searchRef.equals(this.searchRef) && temp.date.equals(this.date));
        } else return false;
    }

    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(VERSION);
        out.writeObject(search);
        out.writeObject(date);
        out.writeObject(sites);
        out.writeObject(searchRef);
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readObject(); //skip version for now
        search = (String)in.readObject();
        date = (String)in.readObject();
        sites = (List<String>)in.readObject();
        searchRef = (GenericQuery)in.readObject();
    }
}
