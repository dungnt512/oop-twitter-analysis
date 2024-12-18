package twitter.algorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.TypeToken;
import twitter.controller.JsonFileManager;
import twitter.entity.*;

import java.io.IOException;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class GraphBuilder {
    private final String DATA_ROOT_DIR = "data/";
    private final String X_LOGIN_DATA_ROOT_DIR = "data/x_account/";
    private final String USERS_SCRAPE_FILE = DATA_ROOT_DIR + "users.json";
    private final String USER_IDS_SCRAPE_FILE = DATA_ROOT_DIR + "userIds.json";
    private final String USER_FOLLOWERS_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowers.json";
    private final String USER_FOLLOWING_SCRAPE_FILE = DATA_ROOT_DIR + "userFollowing.json";
    private final String USER_TWEETS_SCRAPE_FILE = DATA_ROOT_DIR + "userTweets.json";
    private final String USER_RETWEETS_SCRAPE_FILE = DATA_ROOT_DIR + "userRetweets.json";
    private final String USER_COMMENTS_SCRAPE_FILE = DATA_ROOT_DIR + "userComments.json";

    private final String USERS_DATA_FILE = DATA_ROOT_DIR + "user-data.json";
    private final String GRAPH_DATA_FILE = DATA_ROOT_DIR + "graph-data.json";


    public GraphData buildGraph() {
        Set<String> KOLsList = JsonFileManager.fromJson(USER_IDS_SCRAPE_FILE, true, Set.class);
        Map<String, User> usersProfile = JsonFileManager.fromJson(USERS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        Map<String, User> usersFollowers = JsonFileManager.fromJson(USER_FOLLOWERS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        Map<String, User> usersFollowing = JsonFileManager.fromJson(USER_FOLLOWING_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        Map<String, User> usersTweets = JsonFileManager.fromJson(USER_TWEETS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        List<User> usersRetweetsTemp = JsonFileManager.fromJson(USER_RETWEETS_SCRAPE_FILE, true, new TypeToken<List<User>>() {}.getType());
        Map<String, User> usersRetweets = new HashMap<>();
        for (User user : usersRetweetsTemp) {
            usersRetweets.put(user.getUsername(), user);
        }
        Map<String, User> usersCommentsTemp = JsonFileManager.fromJson(USER_COMMENTS_SCRAPE_FILE, true, new TypeToken<Map<String, User>>() {}.getType());
        Map<String, User> usersComments = new HashMap<>();
        for (Map.Entry<String, User> entry : usersCommentsTemp.entrySet()) {
            usersComments.put(User.removeAtSign(entry.getKey()), entry.getValue());
        }

        Map<String, User> users = new HashMap<>();
        Map<String, Tweet> tweets = new HashMap<>();
        for (Map.Entry<String, User> entry : usersProfile.entrySet()) {
            User user = entry.getValue();
            if (!KOLsList.contains(entry.getKey())) {
                continue;
            }
            User temp = usersFollowers.getOrDefault(entry.getKey(), null);
            if (temp != null) {
                user.setFollowers(temp.getFollowers());
            }
            temp = usersFollowing.getOrDefault(entry.getKey(), null);
            if (temp != null) {
                user.setFollowing(temp.getFollowing());
            }
            temp = usersTweets.getOrDefault(entry.getKey(), null);
            if (temp != null) {
                user.setTweets(temp.getTweets());
                for (Tweet tweet : user.getTweets()) {
                    tweet.setTweetId(Tweet.removeSharpEnd(tweet.getTweetId()));
                    tweets.put(tweet.getTweetId(), tweet);
                }
            }
            else {
                user.setTweets(new ArrayList<>());
            }
            if (user.getFollowers() == null) user.setFollowers(new ArrayList<>());
            if (user.getFollowing() == null) user.setFollowing(new ArrayList<>());

        }

        for (Map.Entry<String, User> entry : usersProfile.entrySet()) {
            User user = entry.getValue();
            User temp = usersComments.getOrDefault(entry.getKey(), null);
            if (temp != null) {
                for (Tweet tweet : temp.getTweets()) {
                    Tweet tweetTemp = tweets.getOrDefault(Tweet.getTweetId(tweet.getTweetLink()), null);
                    if (tweetTemp != null) {
                        List<String> ids = new ArrayList<>();
                        for (String id : tweet.getComments()) {
                            ids.add(User.removeAtSign(id));
                        }
                        tweetTemp.setComments(ids);
                        tweetTemp.setComments(tweet.getComments());
                    }
                }
            }
            temp = usersRetweets.getOrDefault(entry.getKey(), null);
            if (temp != null) {
                for (Tweet tweet : temp.getTweets()) {
                    Tweet tweetTemp = tweets.getOrDefault(Tweet.getTweetId(tweet.getTweetLink()), null);
                    if (tweetTemp != null) {
                        List<String> ids = new ArrayList<>();
                        for (String id : tweet.getRetweets()) {
                            ids.add(User.removeAtSign(id));
                        }
                        tweetTemp.setRetweets(ids);
                        tweetTemp.setRetweets(tweet.getRetweets());
                    }
                }
            }
            users.put(user.getUsername(), user);
        }

        JsonFileManager.toJson(USERS_DATA_FILE, users, true);

        GraphData graphData = new GraphData();
        double sumBias = 0;
        Set<String> userIds = new HashSet<>();
        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
//           graphData.getNodes().add(new GraphUserNode(0, entry.getKey(), user));
            if (KOLsList.contains(entry.getKey())) {
//                sumBias += Math.log(user.getFollowersCount());
                sumBias += user.getFollowersCount();
            }
            userIds.add(entry.getKey());
            userIds.addAll(user.getFollowers());
            userIds.addAll(user.getFollowing());
        }
//        sumBias += tweets.size();
        for (Map.Entry<String, Tweet> entry : tweets.entrySet()) {
//            graphData.getNodes().add(new GraphTweetNode(0, entry.getKey(), entry.getValue()));
            Tweet tweet = entry.getValue();
            if (tweet.getComments() == null) tweet.setComments(new ArrayList<>());
            if (tweet.getRetweets() == null) tweet.setRetweets(new ArrayList<>());
//            userIds.add(entry.getKey());
            userIds.addAll(tweet.getComments());
            userIds.addAll(tweet.getRetweets());
        }

//        sumBias += userIds.size();
//        sumBias += KOLsList.size();
        Map<String, GraphNode> nodeMap = new HashMap<>();

        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (!KOLsList.contains(entry.getKey())) {
                continue;
            }
            GraphNode node = new GraphUserNode("kol", (double)user.getFollowersCount(), entry.getKey(), user);
            graphData.getNodes().add(node);
            nodeMap.put(entry.getKey(), node);
        }
        for (Map.Entry<String, Tweet> entry : tweets.entrySet()) {
            GraphNode node = new GraphTweetNode(0, entry.getKey(), entry.getValue());
            graphData.getNodes().add(node);
            nodeMap.put(entry.getKey(), node);
        }
        for (String userId : userIds) {
            if (nodeMap.containsKey(userId)) {
                continue;
            }
            GraphNode node = new GraphUserNode((double)0 / sumBias, userId);
            graphData.getNodes().add(node);
            nodeMap.put(userId, node);
        }

        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            String userId = entry.getKey();
            if (!KOLsList.contains(userId)) {
                continue;
            }
            GraphNode node = nodeMap.get(userId);
            for (String followerId : user.getFollowers()) {
                GraphNode follower = nodeMap.get(followerId);
                follower.getEdges().add(new GraphEdge("follow", 1.0, follower, node));
            }
            for (String followingId : user.getFollowing()) {
                GraphNode following = nodeMap.get(followingId);
                node.getEdges().add(new GraphEdge("following", 1.0, node, following));
            }
            for (Tweet tweet : user.getTweets()) {
                GraphNode tweetNode = nodeMap.get(tweet.getTweetId());
                tweetNode.getEdges().add(new GraphEdge("post", 1.0, tweetNode, node));
                for (String commentUserId : tweet.getComments()) {
                    GraphNode commentNode = nodeMap.get(commentUserId);
                    commentNode.getEdges().add(new GraphEdge("comment", 0.0, commentNode, tweetNode));
                    commentNode.getEdges().add(new GraphEdge("comment", 0.1, commentNode, node));
                }
                for (String retweetUserId : tweet.getRetweets()) {
                    GraphNode retweetNode = nodeMap.get(retweetUserId);
                    retweetNode.getEdges().add(new GraphEdge("retweet", 0.0, retweetNode, tweetNode));
                    retweetNode.getEdges().add(new GraphEdge("retweet", 0.1, retweetNode, node));
                }
            }
        }

        for (GraphNode node : graphData.getNodes()) {
            if (node.getEdges().isEmpty()) {
//                System.err.print(node.getId() + " ");
                for (String KOLId : KOLsList) {
                    node.getEdges().add(new GraphEdge("end", 1.0, node, nodeMap.get(KOLId)));
                }
            }
        }
        System.err.println();

//        graphData.setSumBias(KOLsList.size());
        graphData.setSumBias(sumBias);
//        JsonFileManager.toJson(GRAPH_DATA_FILE, graphData, true);
        return graphData;
    }


}
