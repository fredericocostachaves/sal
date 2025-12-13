package com.sal.linkedin;

public class LinkedInApiException extends RuntimeException {
    public LinkedInApiException(String message) {
        super(message);
    }

    public LinkedInApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
