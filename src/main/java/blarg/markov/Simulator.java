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

/**
 *
 * @author cymrucoder
 */
public class Simulator {
    
    Map<String, Markov> markovs;
    Gson gson;
    
    public Simulator() {
        this.gson = new Gson();
        markovs = new HashMap<>();
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
        
        for (JsonElement message : messages) {
            JsonObject messageObject = message.getAsJsonObject();
            if (messageObject.has("type") && messageObject.has("sender_name") && messageObject.has("content")) {
                String type = messageObject.get("type").getAsString();
                if ("Generic".equals(type)) {
                    String name = messageObject.get("sender_name").getAsString();
                    String content = messageObject.get("content").getAsString();
                    learn(name, content);
                }
            }
        }
        
        System.out.println("Finished learning");
    }
    
    public void addPerson(String name) {
        markovs.put(name, new Markov());
    }
    
    public void learn(String name, String text) {
        markovs.get(name).learn(text);
    }
    
    public String generate(String name, List<String> prefixes) {
        return markovs.get(name).generate(prefixes);
    }
    
    public void finishSentenceForAllParticipants(String prefixes) {
        String prefixArray[] = prefixes.split(" ");// I'm not checking if this is valid so break it if you want
        
        for (Map.Entry<String, Markov> entry : markovs.entrySet()) {
            String name = entry.getKey();            
            
            List<String> textList = new ArrayList<>();
            
            for (String prefix : prefixArray) {
                textList.add(prefix);
            }

            while (true) {
                String nextWord = generate(name, textList.subList(textList.size() - 2, textList.size()));            
                textList.add(nextWord);
                if (nextWord.endsWith(".") || "".equals(nextWord)) {
                    break;
                }
            }        

            String output = name + " says: ";        

            for (String word : textList) {
                output += word + " ";
            }

            if (!(name + " says: " + prefixes).equals(output.trim())) {
                System.out.println(output);
            }
        }        
    }
    
    public static void main(String args[]) throws FileNotFoundException {
        Simulator sim = new Simulator();
        sim.learnFromJsonFile("corpus\\fbexp\\messages\\inbox\\chatname\\message.json");
    }
}
