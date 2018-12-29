package blarg.markov;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cymrucoder
 */
public class Simulator {
    
    Map<String, Markov> markovs;
    
    public Simulator() {
        markovs = new HashMap<>();
    }
    
    public void addPerson(String name) {
        markovs.put(name, new Markov());
    }
    
    public void learn(String name, String text) {
        markovs.get(name).learn(text);
    }
    
    public static void main(String args[]) throws FileNotFoundException {
        Simulator sim = new Simulator();
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(new FileReader("corpus\\fbexp\\messages\\inbox\\chatname\\message.json"), JsonObject.class);
        
        // Load participants
        JsonArray participants = json.getAsJsonArray("participants");        
        
        for (JsonElement participant : participants) {
            JsonObject participantObject = participant.getAsJsonObject();
            JsonElement nameElement = participantObject.get("name");
            String name = nameElement.getAsString();
            sim.addPerson(name);            
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
                    sim.learn(name, content);
                }
            }
        }
    }
}
