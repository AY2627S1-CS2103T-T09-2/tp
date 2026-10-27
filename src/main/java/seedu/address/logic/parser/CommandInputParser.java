package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.commands.DoneCommand;
import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.ContactedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class CommandInputParser {

    private static final Pattern BASIC_COMMAND_FORMAT =
            Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    private static final Logger logger =
            LogsCenter.getLogger(CommandInputParser.class);

    /**
     * Parses user input into a command for execution.
     *
     * @param userInput Full user input.
     * @return The command based on the user input.
     * @throws ParseException If the user input does not conform to the expected format.
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher =
                BASIC_COMMAND_FORMAT.matcher(userInput.trim());

        if (!matcher.matches()) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord =
                matcher.group("commandWord").toLowerCase(Locale.ROOT);

        final String arguments = matcher.group("arguments");

        logger.fine(
                "Command word: " + commandWord
                        + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD ->
                new AddCommandParser().parse(arguments);

            case EditCommand.COMMAND_WORD ->
                new EditCommandParser().parse(arguments);

            case DeleteCommand.COMMAND_WORD ->
                new DeleteCommandParser().parse(arguments);

            case DeleteNoteCommand.COMMAND_WORD ->
                new DeleteNoteCommandParser().parse(arguments);

            case DoneCommand.COMMAND_WORD ->
                new DoneCommandParser().parse(arguments);

            case ContactedCommand.COMMAND_WORD ->
                new ContactedCommandParser().parse(arguments);

            case DueCommand.COMMAND_WORD ->
                new DueCommandParser().parse(arguments);

            case ClearCommand.COMMAND_WORD ->
                new ClearCommandParser().parse(arguments);

            case FindCommand.COMMAND_WORD ->
                new FindCommandParser().parse(arguments);

            case ListCommand.COMMAND_WORD ->
                new ListCommandParser().parse(arguments);

            case NoteCommand.COMMAND_WORD ->
                new NoteCommandParser().parse(arguments);

            case HistoryCommand.COMMAND_WORD ->
                new HistoryCommandParser().parse(arguments);

            case ExitCommand.COMMAND_WORD ->
                new ExitCommandParser().parse(arguments);

            case HelpCommand.COMMAND_WORD ->
                new HelpCommandParser().parse(arguments);

            default -> {
                logger.finer(
                        "This user input caused a ParseException: "
                                + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }
}
