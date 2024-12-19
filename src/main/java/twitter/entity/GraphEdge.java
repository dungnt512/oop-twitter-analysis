package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GraphEdge {
    private String type;
    private double weight;
    private GraphNode nodeStart;
    private GraphNode nodeEnd;

    public GraphEdge(String type, double weight, GraphNode nodeStart, GraphNode nodeEnd) {
        this.type = type;
        this.weight = weight;
        this.nodeStart = nodeStart;
        this.nodeEnd = nodeEnd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public GraphNode getNodeStart() {
        return nodeStart;
    }

    public void setNodeStart(GraphNode nodeStart) {
        this.nodeStart = nodeStart;
    }

    public GraphNode getNodeEnd() {
        return nodeEnd;
    }

    public void setNodeEnd(GraphNode nodeEnd) {
        this.nodeEnd = nodeEnd;
    }


}
