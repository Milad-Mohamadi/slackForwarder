package com.paisley.slackForwarder;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.users.UsersInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserInfoService {

    private final Slack sourceSlack = Slack.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    @Value("${slack.source.token}")
    private String sourceToken;

    public String fetchSenderRealName(String userId) throws IOException, SlackApiException {

        UsersInfoResponse userInfoResponse = sourceSlack.methods(sourceToken)
                .usersInfo(r -> r.user(userId));

        if (!userInfoResponse.isOk()) {
            logger.error("Error fetching user info: {}", userInfoResponse.getError());
            throw new RuntimeException("Error fetching user info: " + userInfoResponse.getError());
        }

        return userInfoResponse.getUser().getRealName();
    }
}
