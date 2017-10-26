var GameCircle = {
    initialize: function (success, failure) {
        cordova.exec(success, failure, "GameCircle", "initialize", []);
    },
    getPlayerScore: function (leaderboardId, span, success, failure) {
        cordova.exec(success, failure, "GameCircle", "getPlayerScore", [leaderboardId, span]);
    },
    submitScore: function (leaderboardId, score, tag, success, failure) {
        cordova.exec(success, failure, "GameCircle", "submitScore", [leaderboardId, score, tag]);
    },
    submitScores: function (entries, success, failure) {
        cordova.exec(success, failure, "GameCircle", "submitScores", entries);
    },
    getLeaderboardScores: function (leaderboardId, scope, span, maxResults, success, failure) {
        cordova.exec(success, failure, "GameCircle", "getLeaderboardScores", [leaderboardId, scope, span, maxResults]);
    },
    showLeaderboard: function (leaderboardId, span, success, failure) {
        cordova.exec(success, failure, "GameCircle", "showLeaderboard", [leaderboardId, span]);
    },
    showLeaderboards: function (success, failure) {
        cordova.exec(success, failure, "GameCircle", "showLeaderboards", []);
    },
    getAchievements: function (success, failure) {
        cordova.exec(success, failure, "GameCircle", "getAchievements", []);
    },
    unlockAchievement: function (achievementId, success, failure) {
        cordova.exec(success, failure, "GameCircle", "unlockAchievement", [achievementId]);
    },
    showAchievements: function (success, failure) {
        cordova.exec(success, failure, "GameCircle", "showAchievements", []);
    }
}

module.exports = GameCircle;