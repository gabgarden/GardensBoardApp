package br.edu.iff.ccc.bsi.gardensBoard.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserIdNullException extends RuntimeException {
    public UserIdNullException() {
        super("User ID cannot be null");
    }

    public UserIdNullException(String message) {
        super(message);
    }

}
