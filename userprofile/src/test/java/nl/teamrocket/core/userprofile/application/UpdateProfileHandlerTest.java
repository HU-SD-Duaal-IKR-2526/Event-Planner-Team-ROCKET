package nl.teamrocket.core.userprofile.application;

import nl.teamrocket.core.userprofile.application.command.UpdateProfileCommand;
import nl.teamrocket.core.userprofile.application.handler.UpdateProfileHandler;
import nl.teamrocket.core.userprofile.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.core.userprofile.application.port.outbound.UserProfileRepository;
import nl.teamrocket.core.userprofile.domain.exception.ProfileAccessDeniedException;
import nl.teamrocket.core.userprofile.domain.exception.ProfileNotFoundException;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProfileHandler")
class UpdateProfileHandlerTest {

    @Mock UserProfileRepository repo;
    @Mock DomainEventPublisher publisher;
    @InjectMocks UpdateProfileHandler handler;

    private final UUID ownerId = UUID.randomUUID();

    private UserProfile stubProfile() {
        return UserProfile.createStub(ownerId, "test.user");
    }

    @Test
    @DisplayName("updates profile with dietary and accessibility fields, publishes event")
    void updates_all_fields_and_publishes_event() {
        var profile = stubProfile();
        when(repo.findByAccountId(ownerId)).thenReturn(Optional.of(profile));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(new UpdateProfileCommand(
                ownerId, ownerId,
                "Test User", "My bio",
                List.of("vegan", "gluten-free"), "wheelchair access",
                "en", "Europe/Amsterdam", null, true, false
        ));

        verify(repo).save(argThat(p ->
                p.getDietaryPreferences().contains("vegan") &&
                p.getAccessibilityNeeds().equals("wheelchair access")
        ));
        verify(publisher).publish(eq("profile.updated.v1"), any());
    }

    @Test
    @DisplayName("dietary preferences are included in the published ProfileUpdated event")
    void dietary_preferences_in_published_event() {
        var profile = stubProfile();
        when(repo.findByAccountId(ownerId)).thenReturn(Optional.of(profile));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(new UpdateProfileCommand(
                ownerId, ownerId, null, null,
                List.of("vegan"), "needs elevator",
                null, null, null, null, null
        ));

        // Verify the published event payload contains dietary preferences
        verify(publisher).publish(eq("profile.updated.v1"),
                argThat(event -> event.toString().contains("vegan")));
    }

    @Test
    @DisplayName("throws ProfileNotFoundException when profile does not exist")
    void throws_when_not_found() {
        when(repo.findByAccountId(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> handler.handle(new UpdateProfileCommand(
                ownerId, ownerId, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    @DisplayName("throws ProfileAccessDeniedException when stranger tries to update")
    void throws_when_stranger_updates() {
        var profile = stubProfile();
        UUID stranger = UUID.randomUUID();
        when(repo.findByAccountId(ownerId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> handler.handle(new UpdateProfileCommand(
                stranger, ownerId, "Hack", null, null, null, null, null, null, null, null)))
                .isInstanceOf(ProfileAccessDeniedException.class);
    }
}
