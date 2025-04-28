package br.edu.iff.ccc.bsi.gardensBoard.exception;


public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}