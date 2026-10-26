package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.InteractionNote;

/**
 * Jackson-friendly version of {@link InteractionNote}.
 */
class JsonAdaptedInteractionNote {

    public static final String MISSING_TEXT_MESSAGE =
            "Interaction note text is missing!";

    public static final String MISSING_CREATED_AT_MESSAGE =
            "Interaction note creation time is missing!";

    public static final String INVALID_CREATED_AT_MESSAGE =
            "Interaction note creation time is invalid.";

    private final String text;
    private final String createdAt;

    /**
     * Constructs a {@code JsonAdaptedInteractionNote}.
     */
    @JsonCreator
    public JsonAdaptedInteractionNote(
            @JsonProperty("text") String text,
            @JsonProperty("createdAt") String createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }

    /**
     * Converts a model interaction note into a JSON-friendly form.
     */
    public JsonAdaptedInteractionNote(InteractionNote source) {
        text = source.getValue();
        createdAt = source.getCreatedAt().toString();
    }

    /**
     * Converts this JSON-friendly note back into the model type.
     */
    public InteractionNote toModelType() throws IllegalValueException {
        if (text == null) {
            throw new IllegalValueException(MISSING_TEXT_MESSAGE);
        }

        if (!InteractionNote.isValidNote(text)) {
            throw new IllegalValueException(
                    InteractionNote.MESSAGE_CONSTRAINTS);
        }

        if (createdAt == null) {
            throw new IllegalValueException(MISSING_CREATED_AT_MESSAGE);
        }

        final LocalDateTime modelCreatedAt;

        try {
            modelCreatedAt = LocalDateTime.parse(createdAt);
        } catch (DateTimeParseException exception) {
            throw new IllegalValueException(
                    INVALID_CREATED_AT_MESSAGE);
        }

        return new InteractionNote(text, modelCreatedAt);
    }
}
