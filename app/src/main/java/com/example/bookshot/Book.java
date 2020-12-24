package com.example.bookshot;

public class Book {
    private String title;
    private String authors;
    private String bookImage;
    private String selfLink;
    private String pageCount;
    private String publishDate;
    private String description;
    //private String categories;

    public Book() {
    }


    public Book(String title, String authors, String coverImage, String pageCount, String description, String selfLink) {
        this.title = title;
        this.authors = authors;
        this.bookImage = coverImage;
        this.pageCount = pageCount;
        this.description = description;
        this.selfLink = selfLink;
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String coverImage) {
        this.bookImage = coverImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getPageCount() {
        return pageCount;
    }

    public void setPageCount(String pageCount) {
        this.pageCount = pageCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

}
