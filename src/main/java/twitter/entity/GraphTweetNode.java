package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class GraphTweetNode extends GraphNode {
    public GraphTweetNode(double weight, String id) {
        super("tweet", id, weight);
    }
    public GraphTweetNode(double weight, String id, Tweet tweet) {
        super("tweet", id, weight);
        setTweet(tweet);
    }
    private Tweet tweet;
}
