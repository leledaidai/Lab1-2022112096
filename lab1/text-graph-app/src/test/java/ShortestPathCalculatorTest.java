import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ShortestPathCalculatorTest {

    @Test
    void testCalcShortestPath_TheToData() {
        // Arrange
        Graph<String, DefaultWeightedEdge> graph = createMockGraph();
        DijkstraShortestPath<String, DefaultWeightedEdge> mockDsp = mock(DijkstraShortestPath.class);
        when(mockDsp.getPath("the", "data")).thenReturn(createMockPath("the", "data", 1));

        ShortestPathCalculator calculator = new ShortestPathCalculator(graph);
        calculator.setDijkstraShortestPath(mockDsp);

        // Act
        String result = calculator.calcShortestPath("the", "data");

        // Assert
        assertEquals("Found 1 shortest path(s) from \"the\" to \"data\" of total weight 1.", result);
    }

    @Test
    void testCalcShortestPath_TheToAgain() {
        // Arrange
        Graph<String, DefaultWeightedEdge> graph = createMockGraph();
        DijkstraShortestPath<String, DefaultWeightedEdge> mockDsp = mock(DijkstraShortestPath.class);
        when(mockDsp.getPath("the", "again")).thenReturn(createMockPath("the", "again", 5));

        ShortestPathCalculator calculator = new ShortestPathCalculator(graph);
        calculator.setDijkstraShortestPath(mockDsp);

        // Act
        String result = calculator.calcShortestPath("the", "again");

        // Assert
        assertEquals("Found 1 shortest path(s) from \"the\" to \"again\" of total weight 5.", result);
    }

    @Test
    void testCalcShortestPath_DataToMore() {
        // Arrange
        Graph<String, DefaultWeightedEdge> graph = createMockGraph();
        DijkstraShortestPath<String, DefaultWeightedEdge> mockDsp = mock(DijkstraShortestPath.class);
        when(mockDsp.getPath("data", "more")).thenReturn(createMockPath("data", "more", 6));

        ShortestPathCalculator calculator = new ShortestPathCalculator(graph);
        calculator.setDijkstraShortestPath(mockDsp);

        // Act
        String result = calculator.calcShortestPath("data", "more");

        // Assert
        assertEquals("Found 1 shortest path(s) from \"data\" to \"more\" of total weight 6.", result);
    }

    @Test
    void testCalcShortestPath_AgainToTeam_NoPath() {
        // Arrange
        Graph<String, DefaultWeightedEdge> graph = createMockGraph();
        DijkstraShortestPath<String, DefaultWeightedEdge> mockDsp = mock(DijkstraShortestPath.class);
        when(mockDsp.getPath("again", "team")).thenReturn(null);

        ShortestPathCalculator calculator = new ShortestPathCalculator(graph);
        calculator.setDijkstraShortestPath(mockDsp);

        // Act
        String result = calculator.calcShortestPath("again", "team");

        // Assert
        assertEquals("No path from \"again\" to \"team\".", result);
    }

    private Graph<String, DefaultWeightedEdge> createMockGraph() {
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex("the");
        graph.addVertex("data");
        graph.addVertex("again");
        graph.addVertex("more");
        graph.addVertex("team");
        return graph;
    }

    private GraphPath<String, DefaultWeightedEdge> createMockPath(String source, String target, double weight) {
        GraphPath<String, DefaultWeightedEdge> mockPath = mock(GraphPath.class);
        when(mockPath.getVertexList()).thenReturn(List.of(source, target));
        when(mockPath.getWeight()).thenReturn(weight);
        return mockPath;
    }
}