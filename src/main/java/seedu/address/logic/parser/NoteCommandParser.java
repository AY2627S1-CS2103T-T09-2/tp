package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

/**
 * Parses input arguments and creates a new {@code NoteCommand}.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NOTE);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NOTE);

        String preamble = argMultimap.getPreamble().trim();

        if (preamble.isEmpty()
                || preamble.matches(".*\\s+.*")
                || argMultimap.getValue(PREFIX_NOTE).isEmpty()) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            NoteCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(preamble);

        String noteText =
                argMultimap.getValue(PREFIX_NOTE).orElse("").trim();

        if (!InteractionNote.isValidNote(noteText)) {
            throw new ParseException(
                    InteractionNote.MESSAGE_CONSTRAINTS);
        }

        return new NoteCommand(
                index,
                new InteractionNote(noteText));
    }
}
