package blarg.markov;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author cymrucoder
 */
public class NGram implements Serializable {

    private static final long serialVersionUID = 1L;

    List<String> words;
    Map<String, Integer> suffixes;
    int totalSuffixes;
    transient Random rand;

    public NGram(List<String> words) {
        this.rand = new Random();
        this.words = words;
        suffixes = new HashMap<>();
        totalSuffixes = 0;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.rand = new Random();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!NGram.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        NGram nGram = (NGram) o;
        return (words.equals(nGram.words));
    }

    public void addSuffix(String word) {
        if (!suffixes.containsKey(word)) {
            suffixes.put(word, 0);
        }
        suffixes.put(word, suffixes.get(word) + 1);
        totalSuffixes++;
    }

    public String generateSuffix() {
        int counter = 0;
        int chosenSuffix = rand.nextInt(totalSuffixes);

        for (Map.Entry<String, Integer> entry : suffixes.entrySet()) {
            counter += entry.getValue();
            if (counter > chosenSuffix) {
                return entry.getKey();
            }
        }

        return "";
    }
}
