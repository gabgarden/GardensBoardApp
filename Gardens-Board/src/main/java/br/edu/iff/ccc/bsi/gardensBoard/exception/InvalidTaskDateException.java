package br.edu.iff.ccc.bsi.gardensBoard.exception;

public class InvalidTaskDateException extends RuntimeException {
    public InvalidTaskDateException(String message) {
        super(message);
    }
}