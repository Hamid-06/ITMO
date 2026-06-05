package client;

import models.Route;
import network.CommandResponse;

public class ResponseHandler {

    public void handle(CommandResponse response) {
        if (response == null) {
            System.out.println("[!] Server is unavailable. Try again later.");
            return;
        }
        System.out.println(response.getMessage());
        if (response.getRoutes() != null && !response.getRoutes().isEmpty()) {
            for (Route r : response.getRoutes()) {
                System.out.println("  " + r);
            }
        }
    }
}