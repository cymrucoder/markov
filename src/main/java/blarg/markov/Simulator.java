package blarg.markov;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cymrucoder
 */
public class Simulator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Simulator.class.getName());

    Map<String, Markov> markovs;
    Map<String, Integer> participantOdds;
    transient Gson gson;
    transient Random rand;
    int totalMessages;
    transient Timer timer;

    public Simulator() {
        this.timer = new Timer();
        this.rand = new Random();
        this.gson = new Gson();
        markovs = new HashMap<>();
        participantOdds = new HashMap<>();
        totalMessages = 0;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.timer = new Timer();
        this.rand = new Random();
        this.gson = new Gson();
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

    public void finishSentenceForAllParticipants(List<String> prefixes) {
        String originalSentence = "";

        for (String word : prefixes) {
            originalSentence += word + " ";
        }

        for (Map.Entry<String, Markov> entry : markovs.entrySet()) {
            String name = entry.getKey();
            String sentence = finishSentenceFor(prefixes, name);

            if (!(originalSentence.trim()).equals(sentence.trim())) {
                System.out.println(name + " says: " + sentence);
            }
        }
    }

    public void finishSentenceForAllParticipants(String prefixes) {
        List<String> prefixList = new ArrayList<>(Arrays.asList(prefixes.split(" ")));
        finishSentenceForAllParticipants(prefixList);
    }

    public String finishSentenceFor(List<String> prefixes, String name) {
        List<String> textList = new ArrayList<>();

        for (String prefix : prefixes) {
            textList.add(prefix);
        }

        while (true) {
            String nextWord = "";

            if (textList.size() == 1) {
                nextWord = generate(name, textList);
            } else {
                nextWord = generate(name, textList.subList(textList.size() - 2, textList.size()));
            }

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

    public static void main(String args[]) throws FileNotFoundException, ClassNotFoundException, IOException {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Configuration file not found at config.properties in project root directory, using default configuration.");
        }

        boolean shouldLearn = Boolean.parseBoolean(properties.getProperty("learn", "true"));
        boolean shouldSave = Boolean.parseBoolean(properties.getProperty("save", "false"));
        boolean shouldLoad = Boolean.parseBoolean(properties.getProperty("load", "false"));
        String dataToLoad = properties.getProperty("dataToLoad", "corpus\\message.json");

        if (shouldLearn && shouldLoad) {
            logger.log(Level.WARNING, "Both learn and load are configured - loaded memory will overwrite learning.");
            shouldLearn = false;
        }

        if (!shouldLearn && !shouldLoad) {
            logger.log(Level.SEVERE, "Neither learn nor load are configured - simulator has nothing to do.");
        } else {
            Simulator sim = new Simulator();

            if (shouldLearn) {
                sim.learnFromJsonFile(dataToLoad);

                if (shouldSave) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("memory\\simulator.ser"))) {
                        oos.writeObject(sim);
                    }
                }
            }

            if (shouldLoad) {
                try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(new File("memory\\simulator.ser")))) {
                    sim = (Simulator) oi.readObject();
                }
            }
            sim.run();
        }
    }

    public void run() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printNextMessage();
            }
        }, 0, 5000);
    }
}
