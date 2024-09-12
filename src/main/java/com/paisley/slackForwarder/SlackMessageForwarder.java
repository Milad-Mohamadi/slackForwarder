package com.paisley.slackForwarder;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SlackMessageForwarder {

    private static final Logger logger = LoggerFactory.getLogger(SlackMessageForwarder.class);

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

    private String lastMessageTimestamp = null;

    // To store mapping between original source thread timestamp and target channel thread timestamp
    private final Map<String, String> threadMapping = new HashMap<>();

    // DateTime formatter for displaying timestamps in readable format
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Scheduled(fixedRate = 10000)
    public void forwardMessages() {
        try {
            List<Message> messages = getMessagesFromSourceChannel();

            for (Message message : messages) {
                // If the message is part of a thread, fetch and forward the thread replies
                if (message.getThreadTs() != null) {
                    forwardThreadMessages(message);
                } else {
                    // Forward as a new message
                    String formattedMessage = formatMessageWithDetails(message);
                    String targetTs = sendMessageToTargetChannel(formattedMessage, null);
                    threadMapping.put(message.getTs(), targetTs);  // Map source message timestamp to target message timestamp
                }
            }
        } catch (IOException | SlackApiException e) {
            logger.error("Error occurred while forwarding messages", e);
        }
    }

    // Fetch messages from the source channel
    private List<Message> getMessagesFromSourceChannel() throws IOException, SlackApiException {
        logger.info("Fetching messages from source channel: {}", sourceChannelId);

        ConversationsHistoryResponse historyResponse = sourceSlack.methods(sourceToken)
                .conversationsHistory(r -> r
                        .channel(sourceChannelId)
                        .oldest(Optional.ofNullable(lastMessageTimestamp).orElse("0"))
                        .limit(10));

        if (!historyResponse.isOk()) {
            logger.error("Error fetching channel history: {}", historyResponse.getError());
            throw new RuntimeException("Error fetching channel history: " + historyResponse.getError());
        }

        if (!historyResponse.getMessages().isEmpty()) {
            lastMessageTimestamp = historyResponse.getMessages().get(0).getTs();
        }

        return historyResponse.getMessages();
    }

    // Forward thread messages (including the original message and all its replies)
    private void forwardThreadMessages(Message message) throws IOException, SlackApiException {
        logger.info("Forwarding thread messages for message ID: {}", message.getTs());

        // Forward the parent message
        String parentMessage = formatMessageWithDetails(message);
        String targetParentTs = sendMessageToTargetChannel(parentMessage, null);
        threadMapping.put(message.getTs(), targetParentTs);  // Map source thread ts to target thread ts

        // Forward the replies in the same thread
        List<Message> replies = getRepliesForMessage(message.getTs());
        for (Message reply : replies) {
            String formattedReply = formatMessageWithDetails(reply);
            sendMessageToTargetChannel(formattedReply, targetParentTs);
        }
    }

    // Fetch replies for a message (if it's part of a thread)
    private List<Message> getRepliesForMessage(String threadTs) throws IOException, SlackApiException {
        ConversationsRepliesResponse repliesResponse = sourceSlack.methods(sourceToken)
                .conversationsReplies(r -> r
                        .channel(sourceChannelId)
                        .ts(threadTs)
                        .limit(10));

        if (!repliesResponse.isOk()) {
            logger.error("Error fetching replies: {}", repliesResponse.getError());
            throw new RuntimeException("Error fetching replies: " + repliesResponse.getError());
        }

        return repliesResponse.getMessages();
    }

    // Get the sender's real name from their user ID
    private String getSenderName(String userId) throws IOException, SlackApiException {
        UsersInfoResponse userInfoResponse = sourceSlack.methods(sourceToken)
                .usersInfo(r -> r.user(userId));

        if (!userInfoResponse.isOk()) {
            logger.error("Error fetching user info: {}", userInfoResponse.getError());
            throw new RuntimeException("Error fetching user info: " + userInfoResponse.getError());
        }

        return userInfoResponse.getUser().getRealName();
    }

    // Format the message with the sender, message text, and timestamp
    private String formatMessageWithDetails(Message message) throws IOException, SlackApiException {
        String senderName = message.getUser() != null ? getSenderName(message.getUser()) : "Unknown User";
        String messageText = message.getText() != null ? message.getText() : "[No Text]";
        String timestamp = convertTimestampToReadableFormat(message.getTs());

        return String.format("*%s* _%s_\n %s", senderName, timestamp, messageText);  // Emojis will display as :emoji:
    }

    // Convert Slack timestamp to human-readable format
    private String convertTimestampToReadableFormat(String ts) {
        long epochTime = (long) Double.parseDouble(ts);  // Slack sends ts as a string, converting to epoch time
        return formatter.format(Instant.ofEpochSecond(epochTime));
    }

    // Send the formatted message to the target channel, and optionally in a thread
    private String sendMessageToTargetChannel(String message, String threadTs) throws IOException, SlackApiException {
        logger.info("Forwarding message to target channel: {}", targetChannelId);

        ChatPostMessageResponse response = targetSlack.methods(targetToken)
                .chatPostMessage(r -> r
                        .channel(targetChannelId)
                        .text(message)
                        .threadTs(threadTs));  // Forward in a thread if threadTs is provided

        if (!response.isOk()) {
            logger.error("Error posting message to target channel: {}", response.getError());
            throw new RuntimeException("Error posting message: " + response.getError());
        }

        logger.info("Message successfully posted to target channel.");
        return response.getTs();  // Return the timestamp of the posted message
    }
}