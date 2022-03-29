package com.socialmedia.model;


public class Comment {
    
    private Long idCo;
    
    private String comment;
    
    private String date;
    
    private String polarity;
    
    public Comment() {
        
    }

    public Comment(Long idCo, String comment, String date,String polarity) {
        super();
        this.idCo = idCo;
        this.comment = comment;
        this.date = date;
        this.polarity=polarity;
    }

    public Long getIdCo() {
        return idCo;
    }

    public void setIdCo(Long idCo) {
        this.idCo = idCo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

	public String getPolarity() {
		return polarity;
	}

	public void setPolarity(String polarity) {
		this.polarity = polarity;
	}
}
