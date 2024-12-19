package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GraphData {
    private double sumBias;
    private int KOLsCount;
    private int nodesCount;
    private int edgesCount;
    private int followEdgeCount = 0;
    private int followingEdgeCount = 0;
    private int postEdgeCount = 0;
    private int commentEdgeCount = 0;
    private int retweetEdgeCount = 0;

    public void printParameter() {
        System.out.println("---------------------------------------");
        System.out.printf("sumBias: %,.2f\n", sumBias);
        System.out.println("KOLsCount: " + KOLsCount);
        System.out.println("nodesCount: " + nodesCount);
        System.out.println("edgesCount: " + edgesCount);
        System.out.println("followEdgeCount: " + followEdgeCount);
        System.out.println("followingEdgeCount: " + followingEdgeCount);
        System.out.println("postEdgeCount: " + postEdgeCount);
        System.out.println("commentEdgeCount: " + commentEdgeCount);
        System.out.println("retweetEdgeCount: " + retweetEdgeCount);
        System.out.println("-------------------------------------");
    }
    public void setParameter(double sumBias, int KOLsCount, int nodesCount, int edgesCount,
                             int followEdgeCount, int followingEdgeCount, int postEdgeCount, int commentEdgeCount, int retweetEdgeCount) {
        this.sumBias = sumBias;
        this.KOLsCount = KOLsCount;
        this.nodesCount = nodesCount;
        this.edgesCount = edgesCount;
        this.followEdgeCount = followEdgeCount;
        this.followingEdgeCount = followingEdgeCount;
        this.postEdgeCount = postEdgeCount;
        this.commentEdgeCount = commentEdgeCount;
        this.retweetEdgeCount = retweetEdgeCount;
    }
    private List<GraphNode> nodes = new ArrayList<>();
}
