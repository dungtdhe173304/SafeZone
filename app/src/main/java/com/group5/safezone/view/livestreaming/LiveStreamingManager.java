package com.group5.safezone.view.livestreaming;

import android.content.Context;
import android.content.Intent;

/**
 * Manager class for handling live streaming operations
 * Provides easy methods to start live streams as host or audience
 */
public class LiveStreamingManager {

    private static final String EXTRA_USER_ID = "userID";
    private static final String EXTRA_USER_NAME = "userName";
    private static final String EXTRA_HOST = "host";
    private static final String EXTRA_LIVE_ID = "liveID";

    /**
     * Start a live stream as a host
     * @param context Application context
     * @param userID Unique identifier for the user
     * @param userName Display name for the user
     * @param liveID Unique identifier for the live stream
     */
    public static void startLiveStreamAsHost(Context context, String userID, String userName, String liveID) {
        Intent intent = new Intent(context, LiveStreamingActivity.class);
        intent.putExtra(EXTRA_USER_ID, userID);
        intent.putExtra(EXTRA_USER_NAME, userName);
        intent.putExtra(EXTRA_HOST, true);
        intent.putExtra(EXTRA_LIVE_ID, liveID);
        context.startActivity(intent);
    }

    /**
     * Join a live stream as an audience member
     * @param context Application context
     * @param userID Unique identifier for the user
     * @param userName Display name for the user
     * @param liveID Unique identifier for the live stream to join
     */
    public static void joinLiveStreamAsAudience(Context context, String userID, String userName, String liveID) {
        Intent intent = new Intent(context, LiveStreamingActivity.class);
        intent.putExtra(EXTRA_USER_ID, userID);
        intent.putExtra(EXTRA_USER_NAME, userName);
        intent.putExtra(EXTRA_HOST, false);
        intent.putExtra(EXTRA_LIVE_ID, liveID);
        context.startActivity(intent);
    }

    /**
     * Start a live stream with default values
     * @param context Application context
     * @param userID Unique identifier for the user
     * @param userName Display name for the user
     */
    public static void startLiveStreamAsHost(Context context, String userID, String userName) {
        String liveID = "live_" + System.currentTimeMillis();
        startLiveStreamAsHost(context, userID, userName, liveID);
    }

    /**
     * Join a live stream with default values
     * @param context Application context
     * @param userID Unique identifier for the user
     * @param userName Display name for the user
     */
    public static void joinLiveStreamAsAudience(Context context, String userID, String userName) {
        String liveID = "live_" + System.currentTimeMillis();
        joinLiveStreamAsAudience(context, userID, userName, liveID);
    }

    /**
     * Share live stream invitation
     * @param context Application context
     * @param liveID Unique identifier for the live stream
     * @param streamTitle Title of the live stream
     * @param hostName Name of the stream host
     */
    public static void shareLiveStreamInvitation(Context context, String liveID, String streamTitle, String hostName) {
        ShareManager.shareLiveStreamInvitation(context, liveID, streamTitle, hostName);
    }

    /**
     * Share live stream invitation to specific platform
     * @param context Application context
     * @param liveID Unique identifier for the live stream
     * @param streamTitle Title of the live stream
     * @param hostName Name of the stream host
     * @param platform Platform to share to
     */
    public static void shareLiveStreamToPlatform(Context context, String liveID, String streamTitle, String hostName, String platform) {
        ShareManager.shareToSpecificPlatform(context, liveID, streamTitle, hostName, platform);
    }
}
