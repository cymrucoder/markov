package blarg.markov;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cymrucoder
 */
public class Markov {

    List<NGram> ngrams;
    
    public Markov() {
        ngrams = new ArrayList<>();
    }
    
    public void learn(String text) {
        ngrams = new ArrayList<>();
        
        if (text.contains(" ")) {
            String words[] = text.split(" ");
            
            if (words.length > 2) {
                for (int i = 0; i < words.length - 2; i++) {
                    List<String> prefix = new ArrayList<>();
                    prefix.add(words[i]);
                    prefix.add(words[i + 1]);
                    
                    NGram ngram = new NGram(prefix);                    
                    if (!ngrams.contains(ngram)) {
                        ngrams.add(ngram);
                    }
                    ngrams.get(ngrams.indexOf(ngram)).addSuffix(words[i + 2]);
                }
            }
        }
    }

    public String generate() {
        return "";
    }
    
    public String generate(List<String> prefix) {
        int prefixIndex = ngrams.indexOf(new NGram(prefix));
        
        if (prefixIndex > -1) {
            NGram ngram = ngrams.get(prefixIndex);
            String suffix = ngram.generateSuffix();
            return suffix;
        }
        return "";
    }

}
