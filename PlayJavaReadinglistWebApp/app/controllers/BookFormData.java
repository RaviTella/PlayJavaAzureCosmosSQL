package controllers;

import play.data.validation.Constraints;

public class BookFormData {
    @Constraints.Required
    private String id;
    @Constraints.Required
    private String reader;
    @Constraints.Required
    private String isbn;
    @Constraints.Required
    private String title;
    @Constraints.Required
    private String author;
    @Constraints.Required
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
