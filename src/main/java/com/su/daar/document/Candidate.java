/**********************************************************************************************************************
 * 
 *                      CV querying application using elastic search indexing
 * 
 * University project developed for the master's course Developpement d'Algorithmes pour des Applications Reticulaires 
 * in Sorbonne Universite, 2021.
 **********************************************************************************************************************/

package com.su.daar.document;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.su.daar.helper.Position;


public class Candidate {
    
    private String id;
    private String name;
    private String email;
    private String cvContent;
    private int exp; // years of experience
    private Position position;  // position sought
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date created;   // date of indexing

    public static String idGen(String name, Date d){
        return ""+name.hashCode()+d.hashCode();
    }

    //TODO
    public static boolean checkEmail(String email) {
        return true;
    }


    //CONSTRUCTORS    
    public Candidate() {
    }
    public Candidate(String id, String name, String email, Date d) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.created = d;
    }
    public Candidate(String id, String name, String email, String cvContent, int exp, Position pos, Date d) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cvContent = cvContent;
        this.exp = exp;
        this.position = pos;
        this.created = d;
    }

    //GETTERS SETTERS
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCvContent() {
        return cvContent;
    }
    public void setCvContent(String cvContent) {
        this.cvContent = cvContent;
    }
    public int getExp() {
        return exp;
    }
    public void setExp(int exp) {
        this.exp = exp;
    }
    public Position getPos() {
        return position;
    }
    public void setPos(Position pos) {
        this.position = pos;
    }
    public Date getDate() {
        return created;
    }
    public void setDate(Date d) {
        this.created = d;
    }

    @Override
    public String toString() {
        return "Candidate [ name=" + name + ", email=" + email + ", id=" + id + "]";
    }    
    
}
