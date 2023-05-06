import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Main class and start point of the program */

public final class YoutubeSubscription {
    public static void main(final String[] args) {
        final var alice = new YoutubeUser("Alice");
        final var bob = new YoutubeUser("Bob");

        final var discoveryChannel = new YoutubeChannel("Discovery");
        final var pewdiepieChannel = new YoutubeChannel("Pewdiepie");
        final var innopolisChannel = new YoutubeChannel("Innopolis");

        alice.subscribe(discoveryChannel);
        bob.subscribe(pewdiepieChannel);
        alice.subscribe(innopolisChannel);
        bob.subscribe(innopolisChannel);

        discoveryChannel.publish(new Video("Animals in Australia"));
        pewdiepieChannel.publish(new Shorts("Minecraft"));
        innopolisChannel.publish(new LiveStream("Report from rainforest"));

        alice.unsubscribe(innopolisChannel);

        discoveryChannel.publish(new Video("Scuba diving in Great Barrier Reef"));
        pewdiepieChannel.publish(new Shorts("Fortnite montage"));
        innopolisChannel.publish(new LiveStream("Dorms survival tips"));
    }
}

/** Content trait that may be published by YouTube channels */

interface Content {

    /** Content's description */

    @NotNull
    String description();

    /** Message to print for user */

    @NotNull
    String notificationMessage();
}

record Video(@NotNull String description) implements Content {
    @Override
    public @NotNull String notificationMessage() {
        return String.format("published new Video: %s", description);
    }
}

record Shorts(@NotNull String description) implements Content {
    @Override
    public @NotNull String notificationMessage() {
        return String.format("published new Shorts: %s", description);
    }
}

record LiveStream(@NotNull String description) implements Content {
    @Override
    public @NotNull String notificationMessage() {
        return String.format("is going live: %s", description);
    }
}

/**
 * I've decided to move observing management to the 'server'.
 * Remote API that manages clients and channels looks more realistic,
 * than channels independently managing their subscribers.
 * Actually, the only difference is that I've moved subscribers to this class,
 * everything stays the same as in original observer's implementation.
 *
 * Class itself implemented as a Singleton, because it has to be used
 * inside YouTube's Channel classes to apply registration (analog of PUT HTTP request)
 */

final class YoutubeServer {
    @Nullable
    private static YoutubeServer INSTANCE;

    @NotNull
    private Map<YoutubeChannel, List<YoutubeUser>> channelsToSubscribers = new HashMap<>();

    private YoutubeServer() {}

    @NotNull
    static synchronized YoutubeServer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new YoutubeServer();
        return INSTANCE;
    }

    /** Adds new channel to the system */

    public void registerChannel(final @NotNull YoutubeChannel channel) {
        channelsToSubscribers.put(channel, new ArrayList<>());
    }

    /** Connects new user to the channel's updates */

    public void onUserSubscribed(final @NotNull YoutubeUser user, final @NotNull YoutubeChannel channel) {
        channelsToSubscribers.get(channel).add(user);
    }

    /** Disconnects the user from the channel's updates */

    public void onUserUnsubscribed(final @NotNull YoutubeUser user, final @NotNull YoutubeChannel channel) {
        channelsToSubscribers.get(channel).remove(user);
    }

    /** Notifies all subscribers about new channel's content */

    public void notifyUsers(final @NotNull YoutubeChannel channel, final @NotNull Content content) {
        channelsToSubscribers.get(channel).forEach(subscriber -> {
            System.out.printf("%s: Channel %s %s\n", subscriber.name(), channel.name, content.notificationMessage());
        });
    }
}

/**
 * YouTube Channel that post's some content
 * P.S. After Message-Passing and Channels in Kotlin, Rust and Go,
 * the word `Channel` seems triggering, so it is `YoutubeChannel`
 */

final class YoutubeChannel {
    @NotNull
    public final String name;

    public YoutubeChannel(final @NotNull String name) {
        this.name = name;
        YoutubeServer.getInstance().registerChannel(this);
    }

    public void publish(final @NotNull Content content) {
        YoutubeServer.getInstance().notifyUsers(this, content);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final YoutubeChannel channel = (YoutubeChannel) o;
        return Objects.equals(name, channel.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }
}

/** Entity for the YouTube's user */

record YoutubeUser(@NotNull String name) {

    /**
     * Seems more logical if user subscribes
     * to the channel, not channel to the user
     */

    public void subscribe(final @NotNull YoutubeChannel channel) {
        YoutubeServer.getInstance().onUserSubscribed(this, channel);
    }

    public void unsubscribe(final @NotNull YoutubeChannel channel) {
        YoutubeServer.getInstance().onUserUnsubscribed(this, channel);
    }
};
