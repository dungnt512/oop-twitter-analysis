package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GraphUserNode extends GraphNode {
    public GraphUserNode(double weight, String id) {
        super("user", id, weight);
    }
    public GraphUserNode(String type, double weight, String id) {
        super(type, id, weight);
    }
    public GraphUserNode(String type, double weight, String id, User user) {
        super(type, id, weight);
        this.user = user;
    }
    public GraphUserNode(double weight, String id, User user) {
        super("user", id, weight);
        this.user = user;
    }
}
