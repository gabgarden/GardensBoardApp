package br.edu.iff.ccc.bsi.gardensBoard.exception;

public class InvalidUserDataException extends RuntimeException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}