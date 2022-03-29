package com.socialmedia.service.i;

import java.util.List;

import com.socialmedia.model.Opinion;

public interface ScrapingServiceI {
    
    public Opinion getOpinionsAljazeera(String req,int nbr);

    public Opinion getOpinionsHespress(String req,int nbr) throws InterruptedException;
    
    public Opinion getOpinionsTwitter(String req,int nbr)throws InterruptedException;
    
    public Opinion getOpinionsFacebook(String req,int nbr)throws InterruptedException;
    
    public Opinion getOpinionsYoutube(String req,int nbr) throws InterruptedException;
    
    public void downloadOpinions(String req, List<Opinion> opinions);

}
