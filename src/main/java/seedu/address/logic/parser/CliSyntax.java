package seedu.address.logic.parser;

import java.util.List;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands.
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_STATUS = new Prefix("st/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");
    public static final Prefix PREFIX_FOLLOW_UP = new Prefix("f/");
    public static final Prefix PREFIX_NOTE = new Prefix("nt/");
    public static final Prefix PREFIX_LAST_CONTACT = new Prefix("d/");

    /** Every prefix used by any command, so that a command can spot one it does not take. */
    public static final List<Prefix> ALL_PREFIXES = List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
            PREFIX_STATUS, PREFIX_TAG, PREFIX_FOLLOW_UP, PREFIX_NOTE, PREFIX_LAST_CONTACT);
}
