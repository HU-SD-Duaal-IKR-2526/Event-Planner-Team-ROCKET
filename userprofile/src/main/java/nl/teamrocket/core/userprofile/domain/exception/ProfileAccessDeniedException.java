package nl.teamrocket.core.userprofile.domain.exception;

import java.util.UUID;
public class ProfileAccessDeniedException extends RuntimeException { public ProfileAccessDeniedException(UUID ownerId, UUID requesterId) { super("Access denied: account " + requesterId + " cannot modify profile of " + ownerId); } }
