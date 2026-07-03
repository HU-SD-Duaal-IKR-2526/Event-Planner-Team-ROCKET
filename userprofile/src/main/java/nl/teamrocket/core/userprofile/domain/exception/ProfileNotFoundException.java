package nl.teamrocket.core.userprofile.domain.exception;

import java.util.UUID;
public class ProfileNotFoundException extends RuntimeException { public ProfileNotFoundException(UUID accountId) { super("UserProfile not found for account: " + accountId); } }
