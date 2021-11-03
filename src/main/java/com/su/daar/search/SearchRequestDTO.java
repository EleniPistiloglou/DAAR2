package com.su.daar.search;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.su.daar.helper.Position;

public class SearchRequestDTO  {

    private List<String> keywords;    // phrases or words to be searched in the cvContent field
    private List<Integer> expRange;    // years of experience of the candidate. Contains a min and a max value 
    private Position position;   // position sought by the candicate
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date created;

    // getters
    public List<String> getKeywords(){
        return this.keywords;
    }
    public List<Integer> getExpRsnge(){
        return expRange;
    }
    public Position getPosition(){
        return position;
    }
    public Date getDate(){
        return created;
    }
    public List<Integer> getExpRange() {
        return expRange;
    }

    // setters
    public void setExpRange(List<Integer> expRange) {
        this.expRange = expRange;
    }
    public void setPosition(Position position) {
        this.position = position;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

}