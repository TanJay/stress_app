package com.tanushaj.element.models;

public class QuoteDto {

    private String author;
    private String quotation;


    public QuoteDto(String author, String quotation) {
        this.author = author;
        this.quotation = quotation;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getQuotation() {
        return quotation;
    }

    public void setQuotation(String quotation) {
        this.quotation = quotation;
    }







}
