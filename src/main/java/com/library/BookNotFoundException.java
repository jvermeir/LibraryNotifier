package com.library;

public class BookNotFoundException extends Exception {
    BookNotFoundException(String message) {
        super(message);
    }
}
