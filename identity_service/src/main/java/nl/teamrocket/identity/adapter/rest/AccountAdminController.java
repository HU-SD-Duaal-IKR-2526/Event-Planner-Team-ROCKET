package nl.teamrocket.identity.adapter.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.teamrocket.identity.adapter.rest.dto.IdentityDtos.*;
import nl.teamrocket.identity.application.command.AssignRoleCommand;
import nl.teamrocket.identity.application.command.RevokeRoleCommand;
import nl.teamrocket.identity.application.handler.RoleManagementHandler;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.domain.exception.AccountNotFoundException;
import nl.teamrocket.identity.domain.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Primary adapter: admin endpoints for account/role management.
 * All endpoints require ADMIN role.
 *
 * Endpoints:
 *   GET  /admin/accounts/{id}            - get account info
 *   POST /admin/accounts/{id}/roles      - assign role
 *   DELETE /admin/accounts/{id}/roles/{role} - revoke role
 */
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AccountAdminController {

    private final RoleManagementHandler roleHandler;
    private final AccountRepository accountRepository;

    // ── GET /admin/accounts/{id} ───────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        var account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        return ResponseEntity.ok(new AccountResponse(
                account.getId(),
                account.getEmail().getValue(),
                account.getStatus().name(),
                account.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        ));
    }

    // ── POST /admin/accounts/{id}/roles ───────────────────────────
    @PostMapping("/{id}/roles")
    public ResponseEntity<MessageResponse> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest req) {

        roleHandler.assign(new AssignRoleCommand(id, req.role()));
        return ResponseEntity.ok(new MessageResponse("Rol toegewezen: " + req.role()));
    }

    // ── DELETE /admin/accounts/{id}/roles/{role} ──────────────────
    @DeleteMapping("/{id}/roles/{role}")
    public ResponseEntity<MessageResponse> revokeRole(
            @PathVariable UUID id,
            @PathVariable Role role) {

        roleHandler.revoke(new RevokeRoleCommand(id, role));
        return ResponseEntity.ok(new MessageResponse("Rol ingetrokken: " + role));
    }
}
