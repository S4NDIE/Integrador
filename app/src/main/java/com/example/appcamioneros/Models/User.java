package com.example.appcamioneros.Models;

public class User {

    private int id;
    private String first_name;
    private String last_name;
    private String document;
    private String birth_city;
    private String email;
    private String birthday;
    private String gender;
    private int verification_status;
    private String company_identifier;

    public User(int id, String first_name, String last_name, String document, String birth_city, String email, String birthday, String gender, int verification_status, String company_identifier){
        this.setFirst_name(first_name);
        this.setLast_name(last_name);
        this.setDocument(document);
        this.setBirth_city(birth_city);
        this.setEmail(email);
        this.setBirthday(birthday);
        this.setGender(gender);
        this.setVerification_status(verification_status);
        this.setCompany_identifier(company_identifier);
    }


    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getBirth_city() {
        return birth_city;
    }

    public void setBirth_city(String birth_city) {
        this.birth_city = birth_city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getVerification_status() {
        return verification_status;
    }

    public void setVerification_status(int verification_status) {
        this.verification_status = verification_status;
    }

    public String getCompany_identifier() {
        return company_identifier;
    }

    public void setCompany_identifier(String company_identifier) {
        this.company_identifier = company_identifier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
