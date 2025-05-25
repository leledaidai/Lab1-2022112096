package com.example.graph;

import com.example.utils.FileUtils;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.model.mxICell;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DirectedTextGraph {
    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph;
    private static final int highlightThreshold = 100; // 可根据需要调整阈值

    public DirectedTextGraph() {
        graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public Graph<String, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    public void buildFromFile(File file) {
        String raw = FileUtils.readFileToString(file);
        // 规范化：小写，非字母替换为空格
        String norm = raw.toLowerCase().replaceAll("[^a-z\\s]+", " ").replaceAll("\\s+", " ").trim();
        String[] words = norm.split(" ");
        for (int i = 0; i < words.length - 1; i++) {
            String a = words[i], b = words[i + 1];
            graph.addVertex(a);
            graph.addVertex(b);
            DefaultWeightedEdge e = graph.getEdge(a, b);
            if (e == null) {
                e = graph.addEdge(a, b);
                graph.setEdgeWeight(e, 1);
            } else {
                graph.setEdgeWeight(e, graph.getEdgeWeight(e) + 1);
            }
        }
    }

    /** 功能2：展示并保存有向图 */
    public void showAndSaveGraph(String outputImage) {
        JGraphXAdapter<String, DefaultWeightedEdge> adapter = new JGraphXAdapter<>(graph);

        // **把权值写到每条边上**
        for (DefaultWeightedEdge e : graph.edgeSet()) {
            mxICell cell = (mxICell) adapter.getEdgeToCellMap().get(e);
            adapter.getModel().setValue(cell, (int) graph.getEdgeWeight(e));
        }

        mxCircleLayout layout = new mxCircleLayout(adapter);
        layout.execute(adapter.getDefaultParent());

        JFrame frame = new JFrame("Directed Graph");
        frame.getContentPane().add(new mxGraphComponent(adapter));
        frame.pack();
        frame.setVisible(true);

        BufferedImage img = mxCellRenderer.createBufferedImage(
                adapter, null, 2, java.awt.Color.WHITE, true, null);
        try {
            ImageIO.write(img, "PNG", new File(outputImage));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /** 功能3：查询桥接词 */
    public String queryBridgeWords(String w1, String w2) {
        w1 = w1.toLowerCase();
        w2 = w2.toLowerCase();
        if (!graph.containsVertex(w1) || !graph.containsVertex(w2)) {
            return String.format("No \"%s\" or \"%s\" in the graph!", w1, w2);
        }
        List<String> bridges = new ArrayList<>();
        for (String mid : graph.vertexSet()) {
            if (graph.containsEdge(w1, mid) && graph.containsEdge(mid, w2)) {
                bridges.add(mid);
            }
        }
        if (bridges.isEmpty()) {
            return String.format("No bridge words from \"%s\" to \"%s\"!", w1, w2);
        }
        return String.format("The bridge words from \"%s\" to \"%s\" are: %s.",
                w1, w2, String.join(", ", bridges));
    }

    /** 功能4：生成新文本 */
    public String generateNewText(String inputText) {
        String norm = inputText.toLowerCase().replaceAll("[^a-z\\s]+", " ").replaceAll("\\s+", " ").trim();
        String[] ws = norm.split(" ");
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < ws.length; i++) {
            sb.append(ws[i]);
            if (i < ws.length - 1) {
                List<String> br = new ArrayList<>();
                for (String mid : graph.vertexSet()) {
                    if (graph.containsEdge(ws[i], mid) && graph.containsEdge(mid, ws[i+1])) {
                        br.add(mid);
                    }
                }
                if (!br.isEmpty()) {
                    sb.append(" ").append(br.get(rnd.nextInt(br.size())));
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    public String calcShortestPath(String w1, String w2) {
        w1 = w1.toLowerCase();
        if (w2 != null) w2 = w2.toLowerCase();
        if (!graph.containsVertex(w1)) {
            return (w2 == null || w2.isEmpty())
                    ? String.format("No \"%s\" in the graph!", w1)
                    : String.format("No \"%s\" or \"%s\" in the graph!", w1, w2);
        }

        DijkstraShortestPath<String, DefaultWeightedEdge> dsp =
                new DijkstraShortestPath<>(graph);

        // 当 w2 为空串或 null 时，遍历所有目标
        if (w2 == null || w2.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String v : graph.vertexSet()) {
                if (v.equals(w1)) continue;
                GraphPath<String, DefaultWeightedEdge> path = dsp.getPath(w1, v);
                if (path != null) {
                    sb.append(v)
                            .append(": ")
                            .append(path.getVertexList())
                            .append(" (len=").append((int) path.getWeight()).append(")\n");
                } else {
                    sb.append(v).append(": unreachable\n");
                }
            }
            return sb.toString();
        }

        // 两参非空时，节点数量小于阈值才高亮路径
        if (graph.vertexSet().size() <= highlightThreshold) {
            return calcShortestPathVisual(w1, w2, dsp);
        } else {
            System.out.println("enter");
            GraphPath<String, DefaultWeightedEdge> path = dsp.getPath(w1, w2);
            System.out.println("still alive, path is null? " + (path == null));
            if (path != null) {
                System.out.println("Vertex list: " + path.getVertexList());
                return String.format("Shortest path from \"%s\" to \"%s\": %s (len=%.0f)",
                        w1, w2, path.getVertexList(), path.getWeight());
            } else {
                return String.format("No path from \"%s\" to \"%s\".", w1, w2);
            }
        }
    }

    /**
     * 功能5 辅助：可视化并高亮 w1→w2 的所有最短路径
     * 返回一条摘要信息，并在 Swing 窗口中展示高亮路径，同时导出图片
     */
    private String calcShortestPathVisual(String w1, String w2,
                                          DijkstraShortestPath<String, DefaultWeightedEdge> dsp) {
        // 1. 找到单条最短路径长度
        GraphPath<String, DefaultWeightedEdge> best = dsp.getPath(w1, w2);
        if (best == null) {
            JOptionPane.showMessageDialog(null,
                    String.format("No path from \"%s\" to \"%s\".", w1, w2));
            return "";
        }
        double minWeight = best.getWeight();

        // 2. 枚举所有简单路径并筛选出权重 == minWeight 的
        AllDirectedPaths<String, DefaultWeightedEdge> allAlg =
                new AllDirectedPaths<>(graph);
        List<GraphPath<String, DefaultWeightedEdge>> shortest = allAlg
                .getAllPaths(w1, w2, true, graph.vertexSet().size())
                .stream()
                .filter(p -> p.getWeight() == minWeight)
                .collect(Collectors.toList());

        // 3. 适配到 JGraphX
        JGraphXAdapter<String, DefaultWeightedEdge> adapter =
                new JGraphXAdapter<>(graph);

        // 4. 给所有边贴权值标签
        graph.edgeSet().forEach(e -> {
            mxICell cell = (mxICell) adapter.getEdgeToCellMap().get(e);
            adapter.getModel().setValue(cell, (int) graph.getEdgeWeight(e));
        });

        // 5. 为每条最短路径用不同样式高亮
        String[] styles = {
                "strokeColor=red;strokeWidth=4",
                "strokeColor=blue;strokeWidth=4",
                "strokeColor=green;strokeWidth=4",
                "strokeColor=orange;strokeWidth=4"
        };
        for (int i = 0; i < shortest.size(); i++) {
            GraphPath<String, DefaultWeightedEdge> path = shortest.get(i);
            String style = styles[i % styles.length];
            for (DefaultWeightedEdge e : path.getEdgeList()) {
                mxICell cell = (mxICell) adapter.getEdgeToCellMap().get(e);
                adapter.getModel().setStyle(cell, style);
            }
        }

        // 6. 布局并展示
        mxCircleLayout layout = new mxCircleLayout(adapter);
        layout.execute(adapter.getDefaultParent());

        JFrame frame = new JFrame(
                String.format("Shortest paths from \"%s\" to \"%s\" (len=%.0f)",
                        w1, w2, minWeight));
        frame.getContentPane().add(new mxGraphComponent(adapter));
        frame.pack();
        frame.setVisible(true);

        // 7. 导出为图片
        try {
            BufferedImage img = mxCellRenderer.createBufferedImage(
                    adapter, null, 2, java.awt.Color.WHITE, true, null);
            ImageIO.write(img, "PNG", new File("shortest_paths.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return String.format(
                "Found %d shortest path(s) from \"%s\" to \"%s\" of total weight %.0f.",
                shortest.size(), w1, w2, minWeight);
    }

    /**
     * 功能5：单参重载 —— 只给起点，列出它到所有其他节点的最短路径
     */
    public String calcShortestPath(String w1) {
        return calcShortestPath(w1, "");
    }
}
