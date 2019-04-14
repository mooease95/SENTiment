package com.example.sentiment;

class User {

    String username;
    int numberOfThreads;
    String representationPreference;

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRepresentationPreference() {
        return representationPreference;
    }

    public void setRepresentationPreference(String representationPreference) {
        this.representationPreference = representationPreference;
    }


}
