package nl.teamrocket.identity.domain.model;

/**
 * Named set of permissions that can be assigned to an Account.
 *
 * Invariant: roles are platform-wide. An Account can have multiple roles.
 * EXPLICITLY NOT: this is not a per-event role (e.g. "speaker of event X").
 *                 Per-event roles live in the Schedule/Registration BC.
 */
public enum Role {
    GUEST,
    ORGANIZER,
    ADMIN,
    COMPLIANCE
}
