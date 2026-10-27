package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code DeleteNoteCommand}.
 */
public class DeleteNoteCommandParser
        implements Parser<DeleteNoteCommand> {

    @Override
    public DeleteNoteCommand parse(String args)
            throws ParseException {
        requireNonNull(args);

        String trimmedArgs = args.trim();
        String[] arguments = trimmedArgs.split("\\s+");

        if (trimmedArgs.isEmpty() || arguments.length != 2) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            DeleteNoteCommand.MESSAGE_USAGE));
        }

        Index contactIndex =
                ParserUtil.parseIndex(arguments[0]);

        Index noteIndex =
                ParserUtil.parseIndex(arguments[1]);

        return new DeleteNoteCommand(
                contactIndex,
                noteIndex);
    }
}
