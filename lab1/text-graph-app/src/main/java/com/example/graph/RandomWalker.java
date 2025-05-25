package com.example.graph;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

public class RandomWalker {
    /**
     * 从随机起点出发，随机沿出边游走，直到遇到重复边或无出边
     */
    public static String randomWalk(Graph<String, DefaultWeightedEdge> graph) {
        if (graph.vertexSet().isEmpty()) return "";
        Random rnd = new Random();
        List<String> vs = new ArrayList<>(graph.vertexSet());
        String curr = vs.get(rnd.nextInt(vs.size()));
        Set<DefaultWeightedEdge> seen = new HashSet<>();
        StringBuilder sb = new StringBuilder(curr);
        while (true) {
            var edges = graph.outgoingEdgesOf(curr);
            if (edges.isEmpty()) break;
            var e = new ArrayList<>(edges).get(rnd.nextInt(edges.size()));
            if (seen.contains(e)) break;
            seen.add(e);
            curr = graph.getEdgeTarget(e);
            sb.append(" → ").append(curr);
        }
        return sb.toString();
    }
}
