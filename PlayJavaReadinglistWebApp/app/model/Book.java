package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {

	public String id;
	public String reader;
	public String isbn;
	public String title;
	public String author;
	public String description;

	public Book() {
	}

	public Book(String id, String reader, String isbn, String title, String author, String description) {
		this.id = id;
		this.reader = reader;
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.description = description;
	}
	
}
