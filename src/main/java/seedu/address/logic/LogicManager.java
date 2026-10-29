package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CommandInputParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String FILE_OPS_DIRECTORY_ERROR_FORMAT =
            "Could not save data to file %s because %s is not a folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final CommandInputParser commandInputParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        commandInputParser = new CommandInputParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = commandInputParser.parseCommand(commandText);
        // Copy the contact list: keeping the live address book reference would retain failed edits too.
        AddressBook previousData = new AddressBook(model.getAddressBook());
        Predicate<? super Person> previousPredicate = model.getPersonListPredicate();
        Comparator<? super Person> previousComparator = model.getPersonListComparator();
        CommandResult commandResult = command.execute(model);
        if (previousData.equals(model.getAddressBook())) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException e) {
            model.setAddressBook(previousData);
            if (previousComparator == null) {
                model.updateFilteredPersonList(previousPredicate);
            } else {
                model.updateFilteredPersonList(previousPredicate, previousComparator);
            }
            logger.log(Level.WARNING, "Save failed; restored previous contact data and displayed list.", e);
            throw new CommandException(getSaveErrorMessage(e), e);
        }

        return commandResult;
    }

    /**
     * Formats permission and folder errors with the configured data file, preserving other I/O error details.
     */
    private String getSaveErrorMessage(IOException exception) {
        Path dataFile = storage.getAddressBookFilePath();
        if (exception instanceof AccessDeniedException) {
            return String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, dataFile);
        }
        if (exception instanceof NotDirectoryException directoryException) {
            return String.format(FILE_OPS_DIRECTORY_ERROR_FORMAT, dataFile, directoryException.getFile());
        }
        return String.format(FILE_OPS_ERROR_FORMAT, exception.getMessage());
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
