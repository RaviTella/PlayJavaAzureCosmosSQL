package controllers;

import play.data.validation.Constraints;

public class RecommendationFormData {

    @Constraints.Required
    private String title;
    @Constraints.Required
    private String author;
    @Constraints.Required
    private String description;
    private String reader;
    @Constraints.Required
    private String isbn;

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
