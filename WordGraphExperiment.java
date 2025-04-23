import java.io.*;
import java.nio.file.*;
import java.util.*;

//javac -encoding UTF-8 WordGraphExperiment.java
//java -cp . WordGraphExperiment EasyTest.txt
//add for git test
public class WordGraphExperiment {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java WordGraphExperiment <input-text-file>");
            System.exit(1);
        }
        String filename = args[0];
        WordGraph graph = new WordGraph();
        try {
            graph.buildFromFile(filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== 功能菜单 ===");
            System.out.println("1. 展示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算两个单词之间的最短路径");
            System.out.println("5. 计算单词的 PageRank");
            System.out.println("6. 随机游走");
            System.out.println("0. 退出");
            System.out.print("请选择：");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    graph.showDirectedGraph();
                    break;
                case "2":
                    System.out.print("请输入两个单词（空格分隔）：");
                    String[] ws = scanner.nextLine().toLowerCase().split("\\s+");
                    if (ws.length == 2) {
                        System.out.println(graph.queryBridgeWords(ws[0], ws[1]));
                    } else {
                        System.out.println("输入格式错误！");
                    }
                    break;
                case "3":
                    System.out.print("请输入原始文本：");
                    String inputText = scanner.nextLine();
                    System.out.println("生成新文本：");
                    System.out.println(graph.generateNewText(inputText));
                    break;
                case "4":
                    System.out.print("请输入两个单词（空格分隔）：");
                    ws = scanner.nextLine().toLowerCase().split("\\s+");
                    if (ws.length == 2) {
                        System.out.println(graph.calcShortestPath(ws[0], ws[1]));
                    } else {
                        System.out.println("输入格式错误！");
                    }
                    break;
                case "5":
                    System.out.print("请输入单词：");
                    String w = scanner.nextLine().toLowerCase().trim();
                    Double pr = graph.calPageRank(w);
                    if (pr == null) {
                        System.out.println("单词 “" + w + "” 不在图中！");
                    } else {
                        System.out.printf("单词 “%s” 的 PageRank = %.6f\n", w, pr);
                    }
                    break;
                case "6":
                    System.out.println("随机游走结果：");
                    System.out.println(graph.randomWalk());
                    break;
                case "0":
                    System.out.println("退出程序。");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("无效选择！");
            }
        }
    }
}


class WordGraph {
    // adjacency: 出边 map，key = 源词，value = map(目标词 -> 权重)
    private Map<String, Map<String, Integer>> adj = new HashMap<>();
    private Random rand = new Random();

    // 功能 1：从文件读入并生成有向图
    public void buildFromFile(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        List<String> words = new ArrayList<>();
        for (String line : lines) {
            // 处理每一行，提取单词
            String[] ws = line.toLowerCase().split("[^a-z]+");
            for (String w : ws) {
                if (!w.isEmpty())
                    words.add(w);
            }
        }
        for (int i = 0; i + 1 < words.size(); i++) {
            String a = words.get(i), b = words.get(i + 1);
            adj.computeIfAbsent(a, k -> new HashMap<>());
            Map<String, Integer> edges = adj.get(a);
            edges.put(b, edges.getOrDefault(b, 0) + 1);
            // 确保 b 也在节点集中（可能无出边）
            adj.computeIfAbsent(b, k -> new HashMap<>());
        }
    }

    // 功能 2：展示有向图
    public void showDirectedGraph() {
        System.out.println("有向图（Adjacency List）：");
        for (String u : adj.keySet()) {
            Map<String, Integer> edges = adj.get(u);
            if (edges.isEmpty()) {
                System.out.printf("%s -> {}\n", u);
            } else {
                System.out.printf("%s -> {", u);
                List<String> parts = new ArrayList<>();
                for (Map.Entry<String, Integer> e : edges.entrySet()) {
                    parts.add(e.getKey() + ":" + e.getValue());
                }
                System.out.print(String.join(", ", parts));
                System.out.println("}");
            }
        }
    }

    // 功能 3：查询桥接词
    public String queryBridgeWords(String w1, String w2) {

        // if (!adj.containsKey(w1) || !adj.containsKey(w2)) {
        //     return String.format("No “%s” or “%s” in the graph!", w1, w2);
        // }

        if ((!adj.containsKey(w1))&&(!adj.containsKey(w2))) {
            return String.format("The word “%s” and “%s” are not in the graph!", w1, w2);
        }
        if (!adj.containsKey(w1)) {
            return String.format("The word “%s” is not in the graph!", w1);
        }
        if (!adj.containsKey(w2)) {
            return String.format("The word “%s” is not in the graph!", w2);
        }
        Set<String> bridges = new HashSet<>();
        for (String mid : adj.get(w1).keySet()) {
            if (adj.get(mid).containsKey(w2)) {
                bridges.add(mid);
            }
        }
        if (bridges.isEmpty()) {
            return String.format("No bridge words from “%s” to “%s”!", w1, w2);
        }
        return String.format("The bridge word from “%s” to “%s” is: %s.",
                w1, w2, String.join(", ", bridges));
    }

    // 功能 4：根据 bridge word 生成新文本
    public String generateNewText(String text) {
        String[] tokens = text.split("\\s+");
        List<String> out = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String w = tokens[i];
            out.add(w);
            if (i + 1 < tokens.length) {
                String w1 = w.toLowerCase().replaceAll("[^a-z]", "");
                String w2 = tokens[i + 1].toLowerCase().replaceAll("[^a-z]", "");
                // 找桥接词
                if (adj.containsKey(w1) && adj.containsKey(w2)) {
                    List<String> mids = new ArrayList<>();
                    for (String mid : adj.get(w1).keySet()) {
                        if (adj.get(mid).containsKey(w2))
                            mids.add(mid);
                    }
                    if (!mids.isEmpty()) {
                        String pick = mids.get(rand.nextInt(mids.size()));
                        out.add(pick);
                    }
                }
            }
        }
        return String.join(" ", out);
    }


    // 功能 5：计算最短路径（BFS，无权图）
    public String calcShortestPath(String start, String end) {
        if (!adj.containsKey(start) && !adj.containsKey(end)) {
            return String.format("The words “%s” and “%s” are not in the graph!", start, end);
        }
        if (!adj.containsKey(start)) {
            return String.format("The word “%s” is not in the graph!", start);
        }
        if (!adj.containsKey(end)) {
            return String.format("The word “%s” is not in the graph!", end);
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, Integer> dist = new HashMap<>(); // 用于存储到每个节点的最短距离
        queue.add(start);
        prev.put(start, null);
        dist.put(start, 0); // 起点的距离为 0
        boolean found = false;

        while (!queue.isEmpty()) {
            String u = queue.poll();
            if (u.equals(end)) {
                found = true;
                break;
            }
            for (Map.Entry<String, Integer> entry : adj.get(u).entrySet()) {
                String v = entry.getKey();
                int weight = entry.getValue();
                if (!dist.containsKey(v)) {
                    dist.put(v, dist.get(u) + weight);
                    prev.put(v, u);
                    queue.add(v);
                }
            }
        }

        if (!found) {
            return String.format("No path from “%s” to “%s”!", start, end);
        }

        // 重构路径并计算权值和
        List<String> path = new ArrayList<>();
        String cur = end;
        int totalWeight = 0;
        while (cur != null) {
            path.add(cur);
            String prevNode = prev.get(cur);
            if (prevNode != null) {
                totalWeight += adj.get(prevNode).get(cur); // 累加权值
            }
            cur = prevNode;
        }
        Collections.reverse(path);
        return String.format("Path: %s\nTotal weight: %d", String.join("->", path), totalWeight);
    }



    // 功能 6：计算 PageRank，迭代 20 次
    public Double calPageRank(String word) {
        if (!adj.containsKey(word)) return null;
        final double d = 0.85;
        int N = adj.size();
        Map<String, Double> pr = new HashMap<>();
        // 初始值
        for (String u : adj.keySet()) pr.put(u, 1.0 / N);
        // 20 轮迭代
        for (int iter = 0; iter < 5; iter++) {
            Map<String, Double> next = new HashMap<>();
            for (String u : adj.keySet()) {
                next.put(u, (1 - d) / N);
            }
            for (String u : adj.keySet()) {
                Map<String, Integer> edges = adj.get(u);
                int outSum = edges.values().stream().mapToInt(Integer::intValue).sum();
                if (outSum == 0) continue;
                for (Map.Entry<String, Integer> e : edges.entrySet()) {
                    String v = e.getKey();
                    int w = e.getValue();
                    next.put(v, next.get(v) + d * pr.get(u) * (w / (double) outSum));
                }
            }
            pr = next;
        }
        return pr.get(word);
    }

    // 功能 7：随机游走，最多 20 步或无出边停
    public String randomWalk() {
        if (adj.isEmpty()) return "";
        List<String> nodes = new ArrayList<>(adj.keySet());
        String cur = nodes.get(rand.nextInt(nodes.size()));
        List<String> walk = new ArrayList<>();
        walk.add(cur);
        for (int i = 0; i < 20; i++) {
            Map<String, Integer> edges = adj.get(cur);
            if (edges.isEmpty()) break;
            // 按权重随机选一条出边
            int total = edges.values().stream().mapToInt(Integer::intValue).sum();
            int r = rand.nextInt(total) + 1;
            int cum = 0;
            for (Map.Entry<String, Integer> e : edges.entrySet()) {
                cum += e.getValue();
                if (r <= cum) {
                    cur = e.getKey();
                    walk.add(cur);
                    break;
                }
            }
        }
        return String.join(" ", walk);
    }
}
