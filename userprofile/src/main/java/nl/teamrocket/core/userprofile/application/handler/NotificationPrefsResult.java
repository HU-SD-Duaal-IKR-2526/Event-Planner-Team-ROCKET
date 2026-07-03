package nl.teamrocket.core.userprofile.application.handler;
/** Result of GET /users/{id}/notification-prefs — consumed by Notification BC. */
public record NotificationPrefsResult(boolean pushEnabled, boolean emailEnabled, String timezone) {}
