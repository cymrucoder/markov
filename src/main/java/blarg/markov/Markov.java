package blarg.markov;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author cymrucoder
 */
public class Markov implements Serializable {

    private static final long serialVersionUID = 1L;

    List<NGram> oneGrams;
    List<NGram> twoGrams;
    List<NGram> threeGrams;

    Map<List<String>, Integer> prefixes;
    int totalPrefixes;

    transient Random rand;

    public Markov() {
        oneGrams = new ArrayList<>();
        twoGrams = new ArrayList<>();
        threeGrams = new ArrayList<>();
        prefixes = new HashMap<>();
        rand = new Random();
        totalPrefixes = 0;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.rand = new Random();
    }

    public void learn(String text) {
        if (text.contains(" ")) {
            String words[] = text.split(" ");

            List<String> firstWords = new ArrayList<>();
            for (String word : words) {
                if (firstWords.size() > 2) {
                    break;
                }
                firstWords.add(word);
            }
            if (!prefixes.containsKey(firstWords)) {
                prefixes.put(firstWords, 0);
            }
            prefixes.put(firstWords, prefixes.get(firstWords) + 1);
            totalPrefixes++;

            if (words.length > 1) {
                for (int i = 0; i < words.length - 2; i++) {
                    List<String> onePrefix = new ArrayList<>();
                    onePrefix.add(words[i]);
                    NGram oneGram = new NGram(onePrefix);
                    if (!oneGrams.contains(oneGram)) {
                        oneGrams.add(oneGram);
                    }
                    oneGrams.get(oneGrams.indexOf(oneGram)).addSuffix(words[i + 1]);

                    if (words.length - 1 > i + 1) {
                        List<String> twoPrefix = new ArrayList<>();
                        twoPrefix.add(words[i]);
                        twoPrefix.add(words[i + 1]);
                        NGram twoGram = new NGram(twoPrefix);
                        if (!twoGrams.contains(twoGram)) {
                            twoGrams.add(twoGram);
                        }
                        twoGrams.get(twoGrams.indexOf(twoGram)).addSuffix(words[i + 2]);

                        if (words.length - 1 > i + 2) {
                            List<String> threePrefix = new ArrayList<>();
                            threePrefix.add(words[i]);
                            threePrefix.add(words[i + 1]);
                            threePrefix.add(words[i + 2]);
                            NGram threeGram = new NGram(threePrefix);
                            if (!threeGrams.contains(threeGram)) {
                                threeGrams.add(threeGram);
                            }
                            threeGrams.get(threeGrams.indexOf(threeGram)).addSuffix(words[i + 3]);
                        }
                    }
                }
            }
        }
    }

    public String generate() {
        return "";
    }

    public String generate(List<String> prefix) {

        int prefixIndex = -1;
        NGram ngram = null;

        if (prefix.size() == 3) {
            prefixIndex = threeGrams.indexOf(new NGram(prefix));
            if (prefixIndex > -1) {
                ngram = threeGrams.get(prefixIndex);
            }
        }

        if (prefix.size() == 2 || (prefixIndex < 0 && prefix.size() == 2)) {
            prefixIndex = twoGrams.indexOf(new NGram(prefix.subList(prefix.size() - 2, prefix.size())));
            if (prefixIndex > -1) {
                ngram = twoGrams.get(prefixIndex);
            }
        }

        if (prefix.size() == 1 || prefixIndex < 0) {
            prefixIndex = oneGrams.indexOf(new NGram(prefix.subList(prefix.size() - 1, prefix.size())));
            if (prefixIndex > -1) {
                ngram = oneGrams.get(prefixIndex);
            }
        }

        if (ngram != null) {
            return ngram.generateSuffix();
        }
        return "";
    }

    public List<String> pickFirstWords() {
        int counter = 0;
        int chosenPrefixThreshold = rand.nextInt(totalPrefixes);

        for (Map.Entry<List<String>, Integer> entry : prefixes.entrySet()) {
            counter += entry.getValue();
            if (counter > chosenPrefixThreshold) {
                return entry.getKey();
            }
        }

        return new ArrayList<>();
    }
}
