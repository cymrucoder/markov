package blarg.markov;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author cymrucoder
 */
public class Simulator {
    
    Map<String, Markov> markovs;
    Map<String, Integer> participantOdds;
    Gson gson;
    Random rand;
    int totalMessages;
    Timer timer;
    
    public Simulator() {
        this.timer = new Timer();
        this.rand = new Random();
        this.gson = new Gson();
        markovs = new HashMap<>();
        participantOdds = new HashMap<>();
        totalMessages = 0;
    }
    
    public void learnFromJsonFile(String filename) throws FileNotFoundException {
        JsonObject json = gson.fromJson(new FileReader(filename), JsonObject.class); 
        
        // Load participants
        JsonArray participants = json.getAsJsonArray("participants");        
        
        for (JsonElement participant : participants) {
            JsonObject participantObject = participant.getAsJsonObject();
            JsonElement nameElement = participantObject.get("name");
            String name = nameElement.getAsString();
            addPerson(name);            
        }

        // Load messages and train Markovs
        JsonArray messages = json.getAsJsonArray("messages");
        
        System.out.println("Starting learning from " + messages.size() + " messages");
        int messagesSoFar = 0;        
        
        for (JsonElement message : messages) {
            JsonObject messageObject = message.getAsJsonObject();
            if (messageObject.has("type") && messageObject.has("sender_name") && messageObject.has("content")) {
                String type = messageObject.get("type").getAsString();
                if ("Generic".equals(type)) {
                    String name = messageObject.get("sender_name").getAsString();
                    String content = messageObject.get("content").getAsString();
                    learn(name, content);
                    participantOdds.put(name, participantOdds.get(name) + 1);
                    totalMessages++;
                }
            }
            messagesSoFar++;
            if (messagesSoFar > 0 && messagesSoFar % 500 == 0) {
                System.out.println("Completed " + messagesSoFar + " messages so far");
            }
        }
        
        System.out.println("Finished learning");
    }
    
    public void addPerson(String name) {
        markovs.put(name, new Markov());
        participantOdds.put(name, 0);
    }
    
    public void learn(String name, String text) {
        markovs.get(name).learn(text);
    }
    
    public String generate(String name, List<String> prefixes) {
        return markovs.get(name).generate(prefixes);
    }
    
//    public void finishSentenceForAllParticipants(String prefixes) {
//        String prefixArray[] = prefixes.split(" ");// I'm not checking if this is valid so break it if you want
//        
//        for (Map.Entry<String, Markov> entry : markovs.entrySet()) {
//            String name = entry.getKey();            
//            
//            String sentence = finishSentenceFor(prefixArray, name);
//
//            //String output = name + " says: ";        
//
//            //for (String word : textList) {
//            //    output += word + " ";
//            //}
//
//            if (!(prefixes).equals(sentence.trim())) {
//                System.out.println(name + " says: " + sentence);
//            }
//        }        
//    }
    
    public String finishSentenceFor(List<String> prefixes, String name) {
        List<String> textList = new ArrayList<>();
            
        for (String prefix : prefixes) {
            textList.add(prefix);
        }

        while (true) {
            String nextWord = generate(name, textList.subList(textList.size() - 2, textList.size()));            
            textList.add(nextWord);
            if (nextWord.endsWith(".") || "".equals(nextWord)) {
                break;
            }
        }        

        String output = "";        

        for (String word : textList) {
            output += word + " ";
        }
        return output;   
    }

    public String pickNextParticipant() {
        int counter = 0;
        int chosenParticipantThreshold = rand.nextInt(totalMessages);

        for (Map.Entry<String, Integer> entry : participantOdds.entrySet()) {
            counter += entry.getValue();
            if (counter > chosenParticipantThreshold) {
                return entry.getKey();
            }
        }
        return "";
    }
    
    public void printNextMessage() {
        String participant = pickNextParticipant();
        List<String> firstWords = markovs.get(participant).pickFirstWords();
        String message = finishSentenceFor(firstWords, participant);
        System.out.println(participant + " says: " + message);
    }
    
    public static void main(String args[]) throws FileNotFoundException {        
        Simulator sim = new Simulator();
        sim.learnFromJsonFile("corpus\\fbexp2018\\messages\\inbox\\chatname\\message.json");
        sim.run();        
    }
    
    public void run() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printNextMessage();
            }
        },0, 5000);
    }
}
