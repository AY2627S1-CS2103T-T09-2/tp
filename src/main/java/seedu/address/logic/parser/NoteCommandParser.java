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

        String trimmedArgs = args.trim();
        String notePrefix = PREFIX_NOTE.getPrefix();

        int prefixPosition = findNotePrefixPosition(trimmedArgs, notePrefix);

        if (prefixPosition < 0) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            NoteCommand.MESSAGE_USAGE));
        }

        String preamble =
                trimmedArgs.substring(0, prefixPosition).trim();

        if (preamble.isEmpty()
                || preamble.matches(".*\\s+.*")) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            NoteCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(preamble);

        String noteText =
                trimmedArgs.substring(
                        prefixPosition + notePrefix.length());

        if (!InteractionNote.isValidNote(noteText)) {
            throw new ParseException(
                    InteractionNote.MESSAGE_CONSTRAINTS);
        }

        return new NoteCommand(
                index,
                new InteractionNote(noteText));
    }

    /**
     * Returns the position of the first note prefix that is separated from
     * the contact index by whitespace.
     */
    private static int findNotePrefixPosition(String args, String notePrefix) {
        int searchFrom = 0;

        while (searchFrom < args.length()) {
            int position = args.indexOf(notePrefix, searchFrom);

            if (position < 0) {
                return -1;
            }

            if (position > 0
                    && Character.isWhitespace(args.charAt(position - 1))) {
                return position;
            }

            searchFrom = position + notePrefix.length();
        }

        return -1;
    }
}
