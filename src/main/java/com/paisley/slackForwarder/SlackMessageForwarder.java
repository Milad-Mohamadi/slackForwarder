package com.paisley.slackForwarder;


import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SlackMessageForwarder {

    @Value("${slack.source.token}")
    private String sourceToken;

    @Value("${slack.source.channel-id}")
    private String sourceChannelId;

    @Value("${slack.target.token}")
    private String targetToken;

    @Value("${slack.target.channel-id}")
    private String targetChannelId;

    private final Slack sourceSlack = Slack.getInstance();
    private final Slack targetSlack = Slack.getInstance();

    private String lastMessageTimestamp = null; // This will store the timestamp of the last forwarded message

    @Scheduled(fixedRate = 10000)  // Run every 30 seconds
    public void forwardMessages() throws IOException, SlackApiException {
        List<String> messages = getMessagesFromSourceChannel();

        for (String message : messages) {
            sendMessageToTargetChannel(message);
        }
    }

    private List<String> getMessagesFromSourceChannel() throws IOException, SlackApiException {
        ConversationsHistoryResponse historyResponse = sourceSlack.methods(sourceToken)
                .conversationsHistory(r -> r
                        .channel(sourceChannelId)
                        .oldest(Optional.ofNullable(lastMessageTimestamp).orElse("0"))
                        .limit(10)); // Adjust limit as needed

        if (!historyResponse.isOk()) {
            throw new RuntimeException("Error fetching channel history: " + historyResponse.getError());
        }

        if (!historyResponse.getMessages().isEmpty()) {
            lastMessageTimestamp = historyResponse.getMessages().get(0).getTs(); // Update with the latest timestamp
        }

        return historyResponse.getMessages().stream()
                .map(message -> message.getText())
                .toList();
    }

    private void sendMessageToTargetChannel(String message) throws IOException, SlackApiException {
        ChatPostMessageResponse response = targetSlack.methods(targetToken)
                .chatPostMessage(r -> r
                        .channel(targetChannelId)
                        .text(message));

        if (!response.isOk()) {
            throw new RuntimeException("Error posting message: " + response.getError());
        }
    }
}