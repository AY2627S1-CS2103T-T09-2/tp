package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ContactedCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.commands.DoneCommand;
import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.LastContact;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class CommandInputParserTest {

    private final CommandInputParser parser = new CommandInputParser();

    @Test
    public void parseCommand_add() throws Exception {
        // EP: the recognized add command delegates its field arguments to the add parser.
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clearWithoutArgs_returnsClearCommand() throws ParseException {
        // EP: the recognized clear command with no arguments, including trailing whitespace.
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + "   ") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_clearWithExtraArgs_throwsParseException() {
        // EP: the recognized clear command with an unsupported numeric or textual argument.
        String expectedMessage = "The clear command does not accept additional parameters.";

        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand(ClearCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand(ClearCommand.COMMAND_WORD + " extra"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        // EP: the recognized delete command with one valid displayed index.
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_deleteNote() throws ParseException {
        // EP: a recognized deletenote command with two valid indexes is dispatched to its parser.
        DeleteNoteCommand expectedCommand =
                new DeleteNoteCommand(
                        INDEX_FIRST_PERSON,
                        Index.fromOneBased(2));

        assertEquals(
                expectedCommand,
                parser.parseCommand(
                        DeleteNoteCommand.COMMAND_WORD + " 1 2"));
    }

    @Test
    public void parseCommand_edit() throws Exception {
        // EP: the recognized edit command with a valid index and field updates.
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " "
                + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exitWithoutArgs_returnsExitCommand() throws ParseException {
        // EP: the recognized exit command with no arguments, including trailing whitespace.
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + "   ") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_exitWithExtraArgs_throwsParseException() {
        // EP: the recognized exit command with forbidden arguments.
        String expectedMessage = "The exit command does not accept additional parameters.";

        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand(ExitCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand(ExitCommand.COMMAND_WORD + " extra"));
    }

    @Test
    public void parseCommand_doneWithValidIndex_returnsDoneCommand() throws ParseException {
        // EP: the recognized done command with one valid index; surrounding whitespace is ignored.
        DoneCommand expectedCommand = new DoneCommand(INDEX_FIRST_PERSON);

        assertEquals(expectedCommand, parser.parseCommand(DoneCommand.COMMAND_WORD + " 1"));
        assertEquals(expectedCommand, parser.parseCommand(DoneCommand.COMMAND_WORD + "   1   "));
    }

    @Test
    public void parseCommand_due() throws ParseException {
        // EP: recognized due command with omitted duration.
        assertEquals(new DueCommand(0), parser.parseCommand(DueCommand.COMMAND_WORD));

        // EP: recognized due command with an explicit duration.
        assertEquals(new DueCommand(7), parser.parseCommand(DueCommand.COMMAND_WORD + " 7"));
    }

    @Test
    public void parseCommand_doneWithInvalidArgs_throwsParseException() {
        String invalidIndexMessage = ParserUtil.MESSAGE_INVALID_INDEX;
        String[] invalidIndexCommands = {
            "done 0", // EP: zero index; boundary value immediately below the minimum.
            "done -1", // EP: negative index.
            "done abc" // EP: nonnumeric index.
        };
        for (String commandText : invalidIndexCommands) {
            assertThrows(ParseException.class, invalidIndexMessage, () -> parser.parseCommand(commandText));
        }

        String usageMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DoneCommand.MESSAGE_USAGE);
        // EP: missing argument.
        assertThrows(ParseException.class, usageMessage, () -> parser.parseCommand("done"));

        // EP: extra arguments.
        assertThrows(ParseException.class, usageMessage, () -> parser.parseCommand("done 1 extra"));
    }

    @Test
    public void parseCommand_find() throws Exception {
        // EP: the recognized find command with multiple name keywords.
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " "
                        + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_helpWithoutArgs_returnsHelpCommand() throws ParseException {
        // EP: the recognized help command with no arguments, including trailing whitespace.
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + "  ") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_helpWithExtraArgs_throwsParseException() {
        // EP: the recognized help command with forbidden arguments.
        String expectedMessage = "The help command does not accept additional parameters.";

        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand(HelpCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand(HelpCommand.COMMAND_WORD + " extra"));
    }

    @Test
    public void parseCommand_listWithoutArgs_returnsListCommand() throws ParseException {
        // EP: the recognized list command with no filters, including trailing whitespace.
        assertEquals(new ListCommand(), parser.parseCommand("list"));
        assertEquals(new ListCommand(), parser.parseCommand("list  "));
    }

    @Test
    public void parseCommand_listWithFilters_returnsFilteredListCommand() throws ParseException {
        // EP: a recognized list command with only a status filter.
        assertEquals(new ListCommand(Optional.of(Status.CLIENT), Optional.empty()),
                parser.parseCommand("list st/CLIENT"));

        // EP: a recognized list command with only a tag filter.
        assertEquals(new ListCommand(Optional.empty(), Optional.of(new Tag("referral"))),
                parser.parseCommand("list t/REFERRAL"));

        // EP: both optional filters supplied; either prefix order has the same meaning.
        ListCommand expectedCommand =
                new ListCommand(Optional.of(Status.CLIENT), Optional.of(new Tag("referral")));
        assertEquals(expectedCommand, parser.parseCommand("list st/CLIENT t/referral"));
        assertEquals(expectedCommand, parser.parseCommand("list t/REFERRAL st/client"));
    }

    @Test
    public void parseCommand_listWithExtraArgs_throwsParseException() {
        // EP: the recognized list command with unsupported bare arguments.
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("list 3"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("list extra"));
    }

    @Test
    public void parseCommand_note() throws ParseException {
        // EP: a recognized note command with an index and note text is dispatched to its parser.
        NoteCommand expectedCommand = new NoteCommand(
                INDEX_FIRST_PERSON,
                new InteractionNote("Discussed hospital coverage"));

        assertEquals(
                expectedCommand,
                parser.parseCommand(
                        NoteCommand.COMMAND_WORD
                                + " 1 nt/Discussed hospital coverage"));
    }

    @Test
    public void parseCommand_history() throws ParseException {
        // EP: a recognized history command with a valid index is dispatched to its parser.
        HistoryCommand expectedCommand =
                new HistoryCommand(INDEX_FIRST_PERSON);

        assertEquals(
                expectedCommand,
                parser.parseCommand(
                        HistoryCommand.COMMAND_WORD + " 1"));
    }

    @Test
    public void parseCommand_contacted() throws ParseException {
        ContactedCommand expectedCommand =
                new ContactedCommand(
                        INDEX_FIRST_PERSON,
                        new LastContact("27-10-2026"));
        assertEquals(
                expectedCommand,
                parser.parseCommand(
                        ContactedCommand.COMMAND_WORD
                                + " 1 d/27-10-2026"));
    }

    @Test
    public void parseCommand_commandWordCaseInsensitive_returnsCommand() throws ParseException {
        // EP: recognized command words in uppercase or mixed case use the same dispatch as lowercase.
        assertTrue(parser.parseCommand("LIST") instanceof ListCommand);
        assertTrue(parser.parseCommand("HeLp") instanceof HelpCommand);
        assertTrue(parser.parseCommand("eXiT") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_caseInsensitiveCommandWord_preservesArguments() throws Exception {
        // EP: an uppercase command word must not change the case of its contact-data arguments.
        Person person = new PersonBuilder().build();
        String addCommand = PersonUtil.getAddCommand(person);
        String upperCaseCommandWord = addCommand.replaceFirst("^add", "ADD");

        AddCommand command = (AddCommand) parser.parseCommand(upperCaseCommandWord);

        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        // EP: empty command text; no command word can be extracted.
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
                        -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        // EP: a nonempty command word that is not supported.
        String expectedMessage = "Unknown command. Type help to see all commands.";

        assertEquals(expectedMessage, MESSAGE_UNKNOWN_COMMAND);
        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand("unknownCommand"));
    }
}
