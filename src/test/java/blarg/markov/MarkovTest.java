package blarg.markov;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cymrucoder
 */
public class MarkovTest {

    Markov mk;
    
    public MarkovTest() {
        this.mk = new Markov();
    }    

    @Test
    public void testSomeMethod() {
        mk.learn("hello my name is bob I have a cat my cat has a name the name is rob");
        List<String> testPrefixes = new ArrayList<>();
        testPrefixes.add("hello");
        testPrefixes.add("my");
        String actualNextWord = mk.generate(testPrefixes);
        
        assertEquals("name", actualNextWord);
    }

}