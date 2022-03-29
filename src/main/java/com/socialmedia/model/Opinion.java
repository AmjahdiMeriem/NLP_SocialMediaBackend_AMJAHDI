package com.socialmedia.model;

import java.util.List;

public class Opinion{
    
private Long idOp;
    
    private String site;
    
    private List<Comment> comments;

    public Opinion() {}

    public Opinion(Long idOp, String site, List<Comment> comments) {
        super();
        this.idOp = idOp;
        this.site = site;
        this.comments = comments;
    }

    public Long getIdOp() {
        return idOp;
    }

    public void setIdOp(Long idOp) {
        this.idOp = idOp;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
