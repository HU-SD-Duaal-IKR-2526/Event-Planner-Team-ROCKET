package nl.teamrocket.core.userprofile.adapter.rest;

import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.userprofile.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Maps domain exceptions to RFC 9457 ProblemDetail HTTP responses.
 * Lives in the adapter layer — domain model has no knowledge of HTTP.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfileNotFoundException.class)
    public ProblemDetail handleNotFound(ProfileNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "profile-not-found", ex.getMessage());
    }

    @ExceptionHandler(ProfileAccessDeniedException.class)
    public ProblemDetail handleAccessDenied(ProfileAccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "profile-access-denied", ex.getMessage());
    }

    @ExceptionHandler(InvalidDisplayNameException.class)
    public ProblemDetail handleDisplayName(InvalidDisplayNameException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-display-name", ex.getMessage());
    }

    @ExceptionHandler(InvalidBioException.class)
    public ProblemDetail handleBio(InvalidBioException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-bio", ex.getMessage());
    }

    @ExceptionHandler(AvatarUploadException.class)
    public ProblemDetail handleAvatar(AvatarUploadException ex) {
        return problem(HttpStatus.BAD_REQUEST, "avatar-upload-error", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-argument", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return problem(HttpStatus.BAD_REQUEST, "validation-failed", details);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                "An unexpected error occurred");
    }

    private ProblemDetail problem(HttpStatus status, String type, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://eventplanner.nl/errors/" + type));
        return pd;
    }
}
