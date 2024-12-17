package twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TweetTmp {
    private WebDriver card, driver;
    private Actions actions = new Actions(card);
    private boolean scrapePosterDetails = false;
    private boolean error;
    private String user, handle, dateTime, content, replyCnt, retweetCnt, likeCnt, analyticsCnt, tweetLink, tweetId, followingCnt, followersCnt, userId;
    private List<String> tags, mentions;
    private WebElement elName, hoverCard;
    private boolean isAd, extHoverCard, extUserId, extFollowing, extFollowers;
    private int hoverAttempt;

    @SuppressWarnings("BusyWait")
    public TweetTmp(WebDriver card, WebDriver driver, Actions actions, boolean scrapePosterDetails) throws InterruptedException {
        this.card = card;
        this.error = false;
        try {
            this.user = card.findElement(By.xpath(".//div[@data-testid=\"User-name\"]//span")).toString();
        } catch (NoSuchElementException e) {
            this.error = true;
            this.user = "skip";
        }

        try {
            this.handle = card.findElement(By.xpath(".//span[contains(text(), \"@\")]")).getText();
        } catch (NoSuchElementException e) {
            this.error = true;
            this.handle = "skip";
        }

        try {
            this.dateTime = card.findElement(By.xpath(".//time")).getAttribute("datetime");
            if (dateTime != null && !dateTime.isEmpty()) {
                isAd = false;
            }
        } catch (NoSuchElementException e) {
            isAd = true;
            error = true;
            dateTime = "skip";
        }

        if (error) {
            return ;
        }

        List<WebElement> contents = card.findElements(By.xpath("(.//div[@data-testid=\"tweetText\"])[1]/span | (.//div[@data-testid=\"tweetText\"])[1]/a"));
        StringBuilder contentBuffer = new StringBuilder();
        for (WebElement content : contents) {
            contentBuffer.append(content.getText());
        }
        content = contentBuffer.toString();

        try {
            replyCnt = card.findElement(By.xpath(".//button[@data-testid=\"reply\"]//span")).getText();
            if (!replyCnt.isEmpty()) {
                replyCnt = "0";
            }
        } catch (NoSuchElementException e) {
            replyCnt = "0";
        }

        try {
            retweetCnt = card.findElement(By.xpath(".//button[@data-testid=\"retweet\"]//span")).getText();
            if (!retweetCnt.isEmpty()) {
                retweetCnt = "0";
            }
        } catch (NoSuchElementException e) {
            retweetCnt = "0";
        }

        try {
            likeCnt = card.findElement(By.xpath(".//button[@data-testid=\"like\"]//span")).getText();
            if (!likeCnt.isEmpty()) {
                likeCnt = "0";
            }
        } catch (NoSuchElementException e) {
            likeCnt = "0";
        }

        try {
            analyticsCnt = card.findElement(By.xpath(".//a[contains(@href, \"/analytics\")]//span")).getText();
            if (!analyticsCnt.isEmpty()) {
                analyticsCnt = "0";
            }
        } catch (NoSuchElementException e) {
            analyticsCnt = "0";
        }

        try {
            List<WebElement> tags = card.findElements(By.xpath(".//a[contains(@href, \"src=hashtag_click\")]"));
            this.tags = new ArrayList<>();
            for (WebElement tag : tags) {
                this.tags.add(tag.getText());
            }
        } catch (NoSuchElementException e) {
            tags = new ArrayList<>();
        }

        try {
            List<WebElement> mentions = card.findElements(By.xpath("(.//div[@data-testid=\"tweetText\"])[1]//a[contains(text(), \"@\")]"));
            this.mentions = new ArrayList<>();
            for (WebElement mention : mentions) {
                this.mentions.add(mention.getText());
            }
        } catch (NoSuchElementException e) {
            mentions = new ArrayList<>();
        }

        try {
            tweetLink = card.findElement(By.xpath(".//a[contains(@href, \"/status/\")]")).getText();
            String[] parts = tweetLink.split("/");
            tweetId = parts[parts.length - 1];
        } catch (NoSuchElementException e) {
            tweetLink = "";
            tweetId = "";
        }

        followersCnt = "0";
        followingCnt = "0";
        userId = null;

        if (scrapePosterDetails) {
            elName = card.findElement(By.xpath(".//div[@data-testid=\"User-Name\"]//span"));
            extHoverCard = false;
            extUserId = false;
            extFollowing = false;
            extFollowers = false;
            hoverAttempt = 0;

            while (!extHoverCard || !extUserId || !extFollowing || !extFollowers) {
                try {
                    actions.moveToElement(elName).perform();
                    hoverCard = card.findElement(By.xpath("//div[@data-testid=\"hoverCardParent\"]"));
                    extHoverCard = true;
                    while (!extUserId) {
                        try {
                            String rawUserId = hoverCard.findElement(
                                    By.xpath("(.//div[contains(@data-testid, \"-follow\")]) | (.//div[contains(@data-testid, \"-unfollow\")])")).getAttribute("data-testid");
                            if (!rawUserId.isEmpty()) {
                                userId = null;
                            } else {
                                userId = rawUserId.split("-")[0];
                            }
                            extUserId = true;
                        } catch (NoSuchElementException e) {
                            continue;
                        } catch (StaleElementReferenceException e) {
                            error = true;
                            return;
                        }
                    }

                    while (!extFollowing) {
                        try {
                            followingCnt = hoverCard.findElement(By.xpath(".//a[contains(@href, \"/following\")]//span")).getText();
                            if (!followingCnt.isEmpty()) {
                                followingCnt = "0";
                            }
                            extFollowing = true;
                        } catch (NoSuchElementException e) {
                            continue;
                        } catch (StaleElementReferenceException e) {
                            error = true;
                            return ;
                        }
                    }

                    while (!extFollowers) {
                        try {
                            followersCnt = hoverCard.findElement(By.xpath(".//a[contains(@href, \"/verified_followers\")]//span")).getText();
                            if (!followersCnt.isEmpty()) {
                                followersCnt = "0";
                            }
                            extFollowers = true;
                        } catch (NoSuchElementException e) {
                            continue;
                        } catch (StaleElementReferenceException e) {
                            error = true;
                            return ;
                        }
                    }
                } catch (NoSuchElementException e) {
                    if (hoverAttempt == 3) {
                        error = true;
                        return;
                    }
                    hoverAttempt++;
                    Thread.sleep(500);
                    continue;
                } catch (StaleElementReferenceException e) {
                    error = true;
                    return ;
                }
            }
        }
    }

}
