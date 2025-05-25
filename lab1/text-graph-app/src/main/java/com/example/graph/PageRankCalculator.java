package com.example.graph;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultWeightedEdge;

public class PageRankCalculator {
    /**
     * 计算整个图的 PageRank，返回 PageRank 对象
     */
    public static PageRank<String, DefaultWeightedEdge> calculate(
            Graph<String, DefaultWeightedEdge> graph, double dampingFactor) {
        return new PageRank<>(graph, dampingFactor);
    }

    /**
     * 直接返回指定顶点的 PageRank 值（如果不存在则返回 0.0）
     */
    public static double getPageRankFor(String vertex,
                                        Graph<String, DefaultWeightedEdge> graph,
                                        double dampingFactor) {
        PageRank<String, DefaultWeightedEdge> pr = calculate(graph, dampingFactor);
        if (graph.containsVertex(vertex)) {
            return pr.getVertexScore(vertex);
        } else {
            return 0.0;
        }
    }
}
