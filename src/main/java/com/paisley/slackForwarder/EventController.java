package com.paisley.slackForwarder;

import com.slack.api.methods.SlackApiException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class EventController {

    private final MessageSender slackMessageSender;
    private final UserInfoService userInfo;

    public EventController(MessageSender slackMessageSender, UserInfoService userInfoService) {
        this.slackMessageSender = slackMessageSender;
        this.userInfo = userInfoService;
    }

    @PostMapping("/slack/events")
    public String handleSlackEvent(@RequestBody EventData slackEvent) throws SlackApiException, IOException {

        if (slackEvent.getChallenge() != null) {
            return slackEvent.getChallenge();
        }

        if (slackEvent.getEvent() != null) {
            EventData.Event event = slackEvent.getEvent();

            if ("message".equals(event.getType())) {
                String text = event.getText();
                String threadTs = event.getThread_ts();
                String eventTs = event.getTs();
                List<EventData.Event.File> files = event.getFiles();

                String realName = userInfo.fetchSenderRealName(event.getUser());

                Instant eventInstant = Instant.ofEpochSecond(Long.parseLong(eventTs.split("\\.")[0]));
                String eventTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(eventInstant);

                String message = String.format("*%s* _%s_\n %s", realName, eventTime, text);

                if (files != null && !files.isEmpty()) {
                    for (EventData.Event.File file : files) {
                        message = String.format("*%s* _%s_\n %s", realName, eventTime, file.getUrl_private());
                    }
                }

                if (threadTs == null) {
                    slackMessageSender.sendMessageToTargetWorkspace(message, null);
                } else {
                    String targetThreadTs = slackMessageSender.getOrCreateThreadForSourceThread(threadTs, message);
                    slackMessageSender.sendMessageToTargetWorkspace(message, targetThreadTs);
                }


                if (threadTs != null) {
                    System.out.println("Message is part of a thread: " + threadTs);
                }
            }
        }

        return "{\"status\": \"ok\"}";
    }
}

