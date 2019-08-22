package com.example.appcamioneros.Models;

public class MessagesError {
    private String message;
    private String field;
    private String validate;

    public MessagesError() {
    }

    public MessagesError(String message, String field, String validate) {
        this.message = message;
        this.field = field;
        this.validate = validate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValidate() {
        return validate;
    }

    public void setValidate(String validate) {
        this.validate = validate;
    }
}
