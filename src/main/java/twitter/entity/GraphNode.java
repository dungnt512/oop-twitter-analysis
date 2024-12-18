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
public class GraphNode {
    public static class SortNode implements Comparator<GraphNode> {
        public int compare(GraphNode a, GraphNode b) {
            return Double.compare(b.weight, a.weight);
        }
    }

    protected String type;
    protected String id;
    private int followersCount = 0;
    protected double weight = 0;
    protected double rank = 0;
    public GraphNode(String type, String id, double weight) {
        this.type = type;
        this.id = id;
        this.weight = weight;
    }

    protected List<GraphEdge> edges = new ArrayList<>();
    protected User user;
}
