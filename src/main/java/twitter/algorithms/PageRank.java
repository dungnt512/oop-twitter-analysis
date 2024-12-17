package twitter.algorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import twitter.controller.JsonFileManager;
import twitter.entity.GraphData;
import twitter.entity.GraphEdge;
import twitter.entity.GraphNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PageRank {
    private final double DAMPING_FACTOR = 0.85;
    private final double EPSILON = 1e-5;
    private final String DATA_ROOT_DIR = "data/";

    private final String USERS_DATA_FILE = DATA_ROOT_DIR + "user-data.json";
    private final String PAGE_RANK_DATA_FILE = DATA_ROOT_DIR + "page-rank.json";

    public void runPageRank() {
        GraphBuilder graphBuilder = new GraphBuilder();
        GraphData graphData = graphBuilder.buildGraph();
        long sumBias = graphData.getSumBias();
        System.err.println(sumBias);

        while (true) {
            double maxDifference = 0.0;
//            for (GraphNode node : graphData.getNodes()) {
//                if (!node.getType().equals("kol")) {
//                    continue;
//                }
//                node.setRank(0);
//            }

            for (GraphNode node : graphData.getNodes()) {
                double sumWeight = 0;
                for (GraphEdge edge : node.getEdges()) {
                    sumWeight += edge.getWeight();
                }
//                if (sumWeight == 0) {
//                    System.err.print(node.getId() + " ");
//                }
                for (GraphEdge edge : node.getEdges()) {
                    GraphNode nodeEnd = edge.getNodeEnd();
                    if (nodeEnd.getType().equals("kol")) {
                        nodeEnd.setRank(nodeEnd.getRank() + node.getWeight() * edge.getWeight() / sumWeight);
                    }
                }
            }
            for (GraphNode node : graphData.getNodes()) {
                if (!node.getType().equals("kol")) {
                    continue;
                }
                node.setRank((1. - DAMPING_FACTOR) / sumBias + DAMPING_FACTOR * node.getRank());
                maxDifference = Math.max(maxDifference, node.getRank() - node.getWeight());
            }

            for (GraphNode node : graphData.getNodes()) {
                node.setWeight(node.getRank());
                node.setRank(0);
            }

//            System.err.print(maxDifference + " ");
            if (maxDifference < EPSILON) {
                break;
            }
        }

        GraphData result = new GraphData();
        for (GraphNode node : graphData.getNodes()) {
            if (node.getType().equals("kol")) {
                result.getNodes().add(new GraphNode(node.getType(), node.getId(), node.getWeight()));
            }
        }
        result.getNodes().sort(new GraphNode.SortNode());
        JsonFileManager.toJson(PAGE_RANK_DATA_FILE, result, true);
    }

    public static void main(String[] args) throws IOException {
        PageRank pageRank = new PageRank();
        pageRank.runPageRank();
    }
}
