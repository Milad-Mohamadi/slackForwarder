package com.paisley.slackForwarder;

import java.util.List;

public class EventData {

    private String token;
    private String challenge;
    private Event event;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public static class Event {
        private String type;
        private String text;
        private String user;
        private String ts;
        private String channel;
        private String thread_ts;
        private List<File> files;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getTs() {
            return ts;
        }

        public void setTs(String ts) {
            this.ts = ts;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getThread_ts() {
            return thread_ts;
        }

        public void setThread_ts(String thread_ts) {
            this.thread_ts = thread_ts;
        }

        public List<File> getFiles() {
            return files;
        }

        public void setFiles(List<File> files) {
            this.files = files;
        }

        public static class File {
            private String id;
            private String url_private;
            private String filetype;
            private String name;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getUrl_private() {
                return url_private;
            }

            public void setUrl_private(String url_private) {
                this.url_private = url_private;
            }

            public String getFiletype() {
                return filetype;
            }

            public void setFiletype(String filetype) {
                this.filetype = filetype;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}