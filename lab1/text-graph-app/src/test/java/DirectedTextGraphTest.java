import com.example.graph.DirectedTextGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DirectedTextGraphTest {
    private DirectedTextGraph dtg;

    @BeforeEach
    void setUp() {
        dtg = new DirectedTextGraph();
        File inputFile = new File("src/main/resources/Easy Test.txt"); // 确保文件路径和内容正确
        dtg.buildFromFile(inputFile);
    }

    @Test
    void testBridgeWords_theToAnalyzed() {
        String result = dtg.queryBridgeWords("the", "analyzed");
        assertTrue(result.contains("scientist"),
                "Expected bridge word 'scientist' between 'the' and 'analyzed', but got: " + result);
        assertEquals("The bridge words from \"the\" to \"analyzed\" are: scientist.", result);
        System.out.println("测试1通过");
    }

    @Test
    void testBridgeWords_teamToBut() {
        String result = dtg.queryBridgeWords("team", "but");
        assertTrue(result.contains("No bridge words"),
                "Expected no bridge words between 'team' and 'but', but got: " + result);
        assertEquals("No bridge words from \"team\" to \"but\"!", result);
        System.out.println("测试2通过");
    }

    @Test
    void testBridgeWords_appleToBanana() {
        String result = dtg.queryBridgeWords("apple", "banana");
        assertTrue(result.contains("No") && result.contains("in the graph"),
                "Expected message about words not being in the graph, but got: " + result);
        assertEquals("No \"apple\" or \"banana\" in the graph!", result);
        System.out.println("测试3通过");
    }

    @Test
    void testBridgeWords_theToThe() {
        String result = dtg.queryBridgeWords("the", "the");
        assertTrue(result.contains("No bridge words"),
                "Expected no bridge words from 'the' to 'the', but got: " + result);
        assertEquals("No bridge words from \"the\" to \"the\"!", result);
        System.out.println("测试4通过");
    }
}
