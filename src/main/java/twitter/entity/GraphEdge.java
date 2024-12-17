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
    String type;
    double weight;
    GraphNode nodeStart;
    GraphNode nodeEnd;
}
