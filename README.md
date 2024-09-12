# Slack Message Forwarder

This is a Spring Boot application that forwards messages from one Slack channel in one workspace to another channel in a different workspace. The application fetches new messages from a source Slack channel and sends them to a target Slack channel every 30 seconds, ensuring no duplicate messages are sent.

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Slack App Configuration](#slack-app-configuration)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Using Docker](#using-docker)
- [How It Works](#how-it-works)
- [Error Handling](#error-handling)
- [Development](#development)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Features
- Fetches new messages from a source Slack channel.
- Forwards the messages to a target Slack channel.
- Ensures no duplicate messages are sent by tracking the timestamp of the last forwarded message.
- Runs on a scheduled task every 30 seconds.
- Can be run using Docker for easy deployment.

## Requirements
- Java 22
- Maven 3.6+
- Docker (optional, for running the application inside a container)
- Slack App with OAuth tokens and the required permissions (see below)

## Slack App Configuration

To use the Slack API, you must create a Slack App in both the source and target workspaces. Here's how to configure the app:

1. Go to the [Slack API Dashboard](https://api.slack.com/apps) and create a new Slack App.
2. For both workspaces (source and target), install the app and give it the following permissions (scopes):
    - `channels:read`
    - `channels:history`
    - `chat:write`
    - `groups:read`
    - `groups:history` (for private channels, if needed)
3. After installing the app in both workspaces, get the **OAuth Tokens**:
    - One token for the **source workspace**.
    - One token for the **target workspace**.
4. Note the **channel ID** of the source channel (where messages are read) and the target channel (where messages will be sent).

You can find the channel IDs by right-clicking on a channel in Slack, then selecting "Copy link." The last part of the link is the channel ID.

## Installation

### 1. Clone the Repository
Clone the repository to your local machine:

```bash
git clone https://github.com/your-repo/slack-forwarder.git
cd slack-forwarder