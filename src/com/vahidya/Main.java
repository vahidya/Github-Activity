package com.vahidya;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        String input;
        List<String> words;
        HttpClient httpClient= HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS) // Follow redirects
                .build();
        HttpRequest httpRequest;
        HttpResponse<String> httpResponse;
        System.out.println("Welcome to Github Activity Application!");
        System.out.println("Enter a command (type 'help' for available commands, or 'exit' to quit):");
        while (true){
            StringBuilder apiUrl=new StringBuilder("http://api.github.com/users/");
            System.out.print("Github-Activity~");
            input=scanner.nextLine().trim();
            words= convertInputToWords(input);
            switch (words.get(0).toLowerCase()){
                case "help" :
                    System.out.println("Available commands:");
                    System.out.println("  get   - get events of github user");
                    break;
                case "get" :
                    if(words.size()==2){
                        apiUrl.append(words.get(1));
                        apiUrl.append("/events");
                        System.out.println(apiUrl);
                        httpRequest=HttpRequest.newBuilder()
                                .uri(URI.create(String.valueOf(apiUrl)))
                                .header("Accept", "application/vnd.github.v3+json")
                                .GET().build();
                        try {
                            httpResponse=httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                            // Check if the response is successful
                            if (httpResponse.statusCode() == 200) {
                                System.out.println("Events for user " + words.get(1) + ":");
                                System.out.println(httpResponse.body()); // Print raw JSON response
                            } else {
                                System.out.println("Failed to fetch events. HTTP Response Code: " + httpResponse.statusCode());
                                if (httpResponse.statusCode() == 404) {
                                    System.out.println("User not found. Please check the username.");
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        System.out.println("get parameter is just github user name");
                    }
                    break;
                default:
                    System.out.println("command\'parameter is not correct");
            }
        }
    }

    private static List<String> convertInputToWords(String input) {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(input);
        // Find matches
         List<String> words = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Group 1 captures the content inside quotes
                words.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                // Group 2 captures single words
                words.add(matcher.group(2));
            }
        }
        return words;
    }
}
