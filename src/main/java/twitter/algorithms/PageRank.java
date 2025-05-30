package twitter.algorithms;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;
import twitter.controller.JsonFileManager;
import twitter.entity.*;
import twitter.navigators.TwitterQuery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PageRank {
    private final double DAMPING_FACTOR = 0.85;
    private final double EPSILON = 1e-6;
    private final String DATA_ROOT_DIR = "data/";
    private final long BASE = 100000000L;

    private final String USERS_DATA_FILE = DATA_ROOT_DIR + "user-data.json";
    private final String PAGE_RANK_DATA_FILE = DATA_ROOT_DIR + "page-rank.json";
    private ProgressPrinter progressPrinter;
    private DoubleProperty progress;
    private StringProperty message;


    public void runPageRank() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.setMessage(message);
        graphBuilder.setProgress(progress);
        GraphData graphData = graphBuilder.buildGraph();
        double sumBias = graphData.getSumBias();
//        System.err.println(sumBias);

        Map<GraphNode, Double> followerCountMap = new HashMap<>();
        for (GraphNode node : graphData.getNodes()) {
            if (node.getType().equals("kol")) {
                double followersCount = node.getWeight();
                followerCountMap.put(node, followersCount);
            }
        }

        int counter = 0;
        double minDifference = Double.MAX_VALUE;
        long startTime = System.nanoTime();
        if (progress != null) {
            progress.set(-1.0);
            message.set("PageRank: " + counter + " iterations. Running time: " + new TimePrinter(System.nanoTime() - startTime).getApproximateTime() + ".");
        }
        while (true) {
            double maxDifference = 0.0;
//            for (GraphNode node : graphData.getNodes()) {
//                if (!node.getType().equals("kol")) {
//                    continue;
//                }
//                node.setRank(0);
//            }

            for (GraphNode node : graphData.getNodes()) {
//                if (!node.getType().equals("kol")) {
//                    continue;
//                }
                double sumWeight = 0;
                for (GraphEdge edge : node.getEdges()) {
                    sumWeight += edge.getWeight();
                }
//                if (Math.abs(sumWeight) < EPSILON) {
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
//                if (((GraphUserNode)node).getUser().getFollowingCount() > 0) {
//                    System.err.print(((GraphUserNode)node).getUser().getFollowingCount() + " ");
//                }
//                node.setRank((1. - DAMPING_FACTOR) * ((GraphUserNode)node).getUser().getFollowingCount() + DAMPING_FACTOR * node.getRank());
                node.setRank((1. - DAMPING_FACTOR) * followerCountMap.get(node) + DAMPING_FACTOR * node.getRank());
                maxDifference = Math.max(maxDifference, Math.abs(node.getRank() - node.getWeight()));
            }

            for (GraphNode node : graphData.getNodes()) {
                node.setWeight(node.getRank());
                node.setRank(0);
            }

            minDifference = Math.max(Math.min(minDifference, maxDifference), EPSILON);
            counter++;
            if (message != null) {
                message.set("PageRank: " + counter + " iterations. Running time: " + new TimePrinter(System.nanoTime() - startTime).getApproximateTime() + ".");
                System.out.println(message.getValue());
            }

//            if (minDifference < EPSILON * 70 && counter == 0) {
//                progressPrinter = new ProgressPrinter("Running PageRank...", (long)(BASE * (minDifference - EPSILON)));
////                progressPrinter = new ProgressPrinter("Running PageRank...", 200);
//                initDifference = minDifference;
//                counter++;
//            }
//            if (counter > 0) {
//                printProgress(Math.min((long) (BASE * (initDifference - minDifference)), progressPrinter.getTotal() - 1), false);
//            }
//            printProgress(Math.min(counter, progressPrinter.getTotal() - 1), false);
//            System.err.print(maxDifference + " ");
            if (maxDifference < EPSILON) {
                break;
            }
        }

        GraphData result = new GraphData();
        for (GraphNode node : graphData.getNodes()) {
            if (node.getType().equals("kol")) {
                node.setWeight(node.getWeight() / sumBias);
                GraphNode graphNode = new GraphNode(node.getType(), node.getId(), node.getWeight());
                graphNode.setType(TwitterQuery.TWITTER_HOME_PAGE + node.getId());
                graphNode.setFollowersCount(followerCountMap.get(node).intValue());
                result.getNodes().add(graphNode);
            }
        }
        result.getNodes().sort(new GraphNode.SortNode());
        JsonFileManager.toJson(PAGE_RANK_DATA_FILE, result, true);
//        printProgress(progressPrinter.getTotal(), true);
        if (progress != null) {
            message.set("PageRank algorithm completed! Running time: " + new TimePrinter(System.nanoTime() - startTime).getApproximateTime() + ".");
            progress.set(1.0);
            System.out.println(message.getValue());
        }
    }

    public static void main(String[] args) throws IOException {
//        GraphData graphData = JsonFileManager.fromJson("data/page-rank.json", true, GraphData.class);
//        if (graphData == null) {
//            System.err.println("...");
//        }
        PageRank pageRank = new PageRank();
        pageRank.runPageRank();
    }

    public void setProgress(DoubleProperty progress) {
        this.progress = progress;
    }

    public void setMessage(StringProperty message) {
        this.message = message;
    }
}
