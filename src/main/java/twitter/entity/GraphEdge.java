package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GraphEdge {
    private String type;
    private double weight;
    private GraphNode nodeStart;
    private GraphNode nodeEnd;
}
