package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book after confirmation.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRM_KEYWORD = "confirm";

    public static final String MESSAGE_CONFIRM_CLEAR =
            "This deletes all %d contacts. Type 'clear confirm' to proceed.";
    public static final String MESSAGE_SUCCESS =
            "All contacts have been cleared from Easy-Insurance.";

    private final boolean isConfirmed;

    /**
     * Creates a {@code ClearCommand}.
     *
     * @param isConfirmed Whether the user has confirmed the clear operation.
     */
    public ClearCommand(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        if (!isConfirmed) {
            int contactCount = model.getAddressBook().getPersonList().size();
            return new CommandResult(String.format(MESSAGE_CONFIRM_CLEAR, contactCount));
        }

        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
