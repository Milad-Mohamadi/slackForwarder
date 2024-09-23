package com.paisley.slackForwarder;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Service
public class MessageSender {

    @Value("${slack.target.token}")
    private String targetToken;

    @Value("${slack.target.channel-id}")
    private String targetChannelId;

    private final Slack targetSlack = Slack.getInstance();
    private final Map<String, String> threadMapping = new HashMap<>();

    public void sendMessageToTargetWorkspace(String message, String threadTs) throws IOException, SlackApiException {
        ChatPostMessageRequest.ChatPostMessageRequestBuilder messageBuilder = ChatPostMessageRequest.builder()
                .channel(targetChannelId)
                .text(message);

        if (threadTs != null) {
            messageBuilder.threadTs(threadTs);
        }

        ChatPostMessageResponse response = targetSlack.methods(targetToken).chatPostMessage(messageBuilder.build());

        if (!response.isOk()) {
            throw new RuntimeException("Failed to send message: " + response.getError());
        }
    }

    public String getOrCreateThreadForSourceThread(String sourceThreadTs, String message) throws IOException, SlackApiException {
        if (threadMapping.containsKey(sourceThreadTs)) {
            return threadMapping.get(sourceThreadTs);
        }

        ChatPostMessageResponse response = targetSlack.methods(targetToken).chatPostMessage(r -> r
                .channel(targetChannelId)
                .text(message));

        if (!response.isOk()) {
            throw new RuntimeException("Failed to send message: " + response.getError());
        }

        String targetThreadTs = response.getTs();
        threadMapping.put(sourceThreadTs, targetThreadTs);

        return targetThreadTs;
    }
}