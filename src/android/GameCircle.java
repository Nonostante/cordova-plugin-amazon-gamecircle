package io.nonostante.games.cordova;

import android.util.Log;

import java.util.*;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazon.ags.api.*;
import com.amazon.ags.api.achievements.Achievement;
import com.amazon.ags.api.achievements.GetAchievementsResponse;
import com.amazon.ags.api.achievements.UpdateProgressResponse;
import com.amazon.ags.api.leaderboards.GetPlayerScoreResponse;
import com.amazon.ags.api.leaderboards.GetScoresResponse;
import com.amazon.ags.api.leaderboards.LeaderboardsClient;
import com.amazon.ags.api.leaderboards.Score;
import com.amazon.ags.api.leaderboards.SubmitScoreResponse;
import com.amazon.ags.api.overlay.PopUpLocation;
import com.amazon.ags.api.player.Player;
import com.amazon.ags.constants.LeaderboardFilter;

public class GameCircle extends CordovaPlugin {
    private static final String LOGTAG = "Amazon GameCircle";

    private AmazonGamesClient agsClient;
    private AmazonGamesCallback callback = new AmazonGamesCallback() {
        @Override
        public void onServiceNotReady(AmazonGamesStatus status) {
            //unable to use service
        }
        @Override
        public void onServiceReady(AmazonGamesClient amazonGamesClient) {
            agsClient = amazonGamesClient;
            agsClient.setPopUpLocation(PopUpLocation.TOP_CENTER);
            //ready to use GameCircle
        }
    };
    private EnumSet<AmazonGamesFeature> myGameFeatures = EnumSet.of(
            AmazonGamesFeature.Achievements, AmazonGamesFeature.Leaderboards);

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        if (agsClient != null) {
            agsClient.release();
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if(this.initialized) {
            AmazonGamesClient.initialize(this.cordova.getActivity(), callback, myGameFeatures);
        }
    }

    private boolean initialized = false;

    private boolean checkInitialized(final CallbackContext callbackContext) {
        if(this.agsClient == null){
            callbackContext.error("NotInitialized");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        Log.d(LOGTAG, "Action: " + action);
        if(action.equals("initialize")){
            this.initialized = true;
            AmazonGamesClient.initialize(this.cordova.getActivity(), callback, myGameFeatures);
            callbackContext.success();
        } else if(action.equals("getAchievements")) {
            if(!checkInitialized(callbackContext)) return true;
            agsClient.getAchievementsClient().getAchievements().setCallback(new AGResponseCallback<GetAchievementsResponse>() {
                @Override
                public void onComplete(GetAchievementsResponse getAchievementsResponse) {
                    if (getAchievementsResponse.isError()) {
                        callbackContext.error(getAchievementsResponse.getError().toString());
                    } else {
                        JSONArray items = new JSONArray();
                        try {
                            for (Achievement achievement : getAchievementsResponse.getAchievementsList()) {
                                items.put(new JSONObject()
                                        .put("id", achievement.getId())
                                        .put("title", achievement.getTitle())
                                        .put("description", achievement.getDescription())
                                        .put("points", achievement.getPointValue())
                                        .put("unlocked", achievement.isUnlocked())
                                        .put("hidden", achievement.isHidden())
                                );
                            }
                        } catch (JSONException e) {
                        }
                        callbackContext.success(items);
                    }
                }
            });
        } else if(action.equals("unlockAchievement")){
            if(!checkInitialized(callbackContext)) return true;

            String achievementId = data.getString(0);
            agsClient.getAchievementsClient().updateProgress(achievementId, 100.0f).setCallback(new AGResponseCallback<UpdateProgressResponse>() {
                @Override
                public void onComplete(UpdateProgressResponse result) {
                    if (result.isError()) {
                        callbackContext.error(result.getError().toString());
                    } else {
                        callbackContext.success();
                    }
                }
            });
            return true;
        } else if(action.equals("showAchievements")) {
            if(!checkInitialized(callbackContext)) return true;

            agsClient.getAchievementsClient().showAchievementsOverlay();
            callbackContext.success();
        } else if(action.equals("getPlayerScore")) {
            if(!checkInitialized(callbackContext)) return true;

            String leaderboardId = data.getString(0);
            int span = data.optInt(1, 0);
            agsClient.getLeaderboardsClient().getLocalPlayerScore(leaderboardId, LeaderboardFilter.fromOrdinal(span)).setCallback(new AGResponseCallback<GetPlayerScoreResponse>() {
                @Override
                public void onComplete(GetPlayerScoreResponse getPlayerScoreResponse) {
                    if(getPlayerScoreResponse.isError()){
                        callbackContext.error(getPlayerScoreResponse.getError().toString());
                    } else {
                        try {
                            callbackContext.success(new JSONObject()
                                    .put("score", getPlayerScoreResponse.getScoreValue())
                                    .put("rank", getPlayerScoreResponse.getRank())
                            );
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                }
            });
        } else if(action.equals("getLeaderboardScores")) {
            if(!checkInitialized(callbackContext)) return true;

            String leaderboardId = data.getString(0);
            int span = data.optInt(2, 0);
            agsClient.getLeaderboardsClient().getScores(leaderboardId, LeaderboardFilter.fromOrdinal(span)).setCallback(new AGResponseCallback<GetScoresResponse>() {
                @Override
                public void onComplete(GetScoresResponse getScoresResponse) {
                    if(getScoresResponse.isError()){
                        callbackContext.error(getScoresResponse.getError().toString());
                    } else {
                        JSONArray items = new JSONArray();
                        try {
                            for (Score score : getScoresResponse.getScores()) {
                                Player player = score.getPlayer();
                                items.put(new JSONObject()
                                        .put("playerId", player.getPlayerId())
                                        .put("playerName", player.getAlias())
                                        .put("playerImage", player.getAvatarUrl())
                                        .put("score", score.getScoreValue())
                                        .put("rank", score.getRank())
                                );
                            }
                        } catch (JSONException e) {
                        }
                        callbackContext.success(items);
                    }
                }
            });
        } else if(action.equals("submitScore")) {
            if(!checkInitialized(callbackContext)) return true;

            String leaderboardId = data.getString(0);
            long value = data.getLong(1);
            agsClient.getLeaderboardsClient().submitScore(leaderboardId, value).setCallback(new AGResponseCallback<SubmitScoreResponse>() {
                @Override
                public void onComplete(SubmitScoreResponse submitScoreResponse) {
                    if(submitScoreResponse.isError()){
                        callbackContext.error(submitScoreResponse.getError().toString());
                    } else {
                        callbackContext.success();
                    }
                }
            });
        } else if(action.equals("submitScores")) {
            if(!checkInitialized(callbackContext)) return true;

            LeaderboardsClient client = agsClient.getLeaderboardsClient();
            for (int a = 0, l = data.length(); a < l; a++) {
                try {
                    JSONObject entry = data.getJSONObject(a);
                    String leaderboardId = entry.getString("leaderboardId");
                    long score = entry.getLong("score");
                    client.submitScore(leaderboardId, score);
                }catch (Exception e) {
                }
            }
            callbackContext.success();
        } else if(action.equals("showLeaderboard")) {
            if(!checkInitialized(callbackContext)) return true;

            String leaderboardId = data.getString(0);
            agsClient.getLeaderboardsClient().showLeaderboardOverlay(leaderboardId);
            callbackContext.success();
        } else if(action.equals("showLeaderboards")) {
            if(!checkInitialized(callbackContext)) return true;

            agsClient.getLeaderboardsClient().showLeaderboardsOverlay();
            callbackContext.success();
        } else {
            return false;
        }
        return true;
    }
}