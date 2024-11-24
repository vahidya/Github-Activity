package com.vahidya;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public static void main(String[] args) throws Exception {
        String prompt="Github-Activity~";
        Scanner scanner=new Scanner(System.in);
        String GITHUB_BASE_URL="http://api.github.com/";
        String input;
        String userName="";
        List<String> words;
        HttpRequest httpRequest;
        HttpResponse<String> httpResponse;
        System.out.println("Welcome to Github Activity Application!");
        System.out.println("Enter a command (type 'help' for available commands, or 'exit' to quit):");
        while (true){
            System.out.print(prompt);
            input=scanner.nextLine().trim();
            words= convertInputToWords(input);
            switch (words.get(0).toLowerCase()){
                case "help" :
                    System.out.println("Available commands:");
                    System.out.println("  get   - get events of github user");
                    break;
                case "change-user":
                    if(words.size()==2){
                        userName=words.get(1);
                        System.out.println("user name was changed to "+userName);
                        prompt="Github-Activity/"+userName+"~";
                    }else{
                        System.out.println("just enter username after change-user command ");
                    }
                    break;
                case "events" :
                    if(words.size()==1){
                        String apiUrl=GITHUB_BASE_URL+"users/"+userName+"/events";
                        System.out.println(apiUrl);
                        String jsonResponse=fetchGithubDAta(apiUrl);
                        // Parse and display repository information
                        if (jsonResponse != null) {
                            parseAndDisplayEvents(jsonResponse, userName);
                        } else {
                            System.out.println("No data received. Please check the username.");
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

    private static void parseAndDisplayEvents(String jsonResponse, String userName) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        System.out.println("-------------------------------------");
        for (JsonNode eventNode : rootNode) {
            String eventType = eventNode.get("type").asText();
            String repoName = eventNode.get("repo").get("name").asText();
            String createdAt = eventNode.get("created_at").asText();

            System.out.print("\nevent Type: " + eventType +" ");
            System.out.print("Repository: " + repoName +" ");
            System.out.print("Created At: " + createdAt+" ");
        }
        System.out.println("-------------------------------------");
    }

    private static void parseAndDisplayRepositories(String jsonResponse, String username) throws Exception {
        // Use Jackson to parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        if (rootNode.isArray() && rootNode.size() > 0) {
            System.out.println("\nRepositories for user: " + username);

            for (JsonNode repoNode : rootNode) {
                String repoName = repoNode.get("name").asText();
                String description = repoNode.get("description").asText(null);
                String htmlUrl = repoNode.get("html_url").asText();

                System.out.println("\nRepository: " + repoName);
                System.out.println("Description: " + (description != null ? description : "No description provided."));
                System.out.println("URL: " + htmlUrl);
            }
        } else {
            System.out.println("No repositories found for user: " + username);
        }
    }

    private static String fetchGithubDAta( String apiUrl) throws Exception  {
        HttpClient httpClient= HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS) // Follow redirects
                .build();
        HttpRequest httpRequest=HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .GET().build();
        HttpResponse<String> httpResponse=httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        // Check if the response is successful
        if (httpResponse.statusCode() == 200) {
            return httpResponse.body();
        } else {
            System.out.println("Failed to fetch events. HTTP Response Code: " + httpResponse.statusCode());
            return null;
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
