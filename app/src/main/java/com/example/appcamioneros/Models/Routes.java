package com.example.appcamioneros.Models;

public class Routes {

    private String identifier;
    private String polylines_file;
    private String route_name;
    private int danger_level;
    private String origin;
    private String destination;
    private int distance;
    private int speed;
    private String company_identifier;


    public Routes(String identifier, String polylines_file, String route_name, int danger_level, String origin, String destination, int distance, int speed, String company_identifier) {
        this.setIdentifier(identifier);
        this.setPolylines_file(polylines_file);
        this.setRoute_name(route_name);
        this.setDanger_level(danger_level);
        this.setOrigin(origin);
        this.setDestination(destination);
        this.setDistance(distance);
        this.setCompany_identifier(company_identifier);
    }

    public  Routes(){
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPolylines_file() {
        return polylines_file;
    }

    public void setPolylines_file(String polylines_file) {
        this.polylines_file = polylines_file;
    }

    public String getRoute_name() {
        return route_name;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public int getDanger_level() {
        return danger_level;
    }

    public void setDanger_level(int danger_level) {
        this.danger_level = danger_level;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getCompany_identifier() {
        return company_identifier;
    }

    public void setCompany_identifier(String company_identifier) {
        this.company_identifier = company_identifier;
    }
}
