package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdge> edges) {
        this.edges = edges;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
