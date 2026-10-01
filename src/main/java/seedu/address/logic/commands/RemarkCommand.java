package seedu.address.logic.commands;

import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static java.util.Objects.requireNonNull;

import java.util.List;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/** Adds or replaces a person's remark. */
public class RemarkCommand extends Command {
    public static final String COMMAND_WORD = "remark";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a remark to the person identified by INDEX.\n"
            + "Parameters: INDEX (must be a positive integer) r/REMARK";
    public static final String MESSAGE_SUCCESS = "Added remark to Person: %1$s";
    private final Index index;
    private final Remark remark;

    public RemarkCommand(Index index, Remark remark) {
        this.index = requireNonNull(index);
        this.remark = requireNonNull(remark);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> persons = model.getFilteredPersonList();
        if (index.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person old = persons.get(index.getZeroBased());
        Person updated = new Person(old.getName(), old.getPhone(), old.getEmail(), old.getAddress(), remark,
                old.getTags());
        model.setPerson(old, updated);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(updated)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof RemarkCommand command
                && index.equals(command.index) && remark.equals(command.remark));
    }
}
