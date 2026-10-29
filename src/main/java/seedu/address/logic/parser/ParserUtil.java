package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LAST_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    public static final String MESSAGE_UNSUPPORTED_PREFIX = "%1$s is not used by %2$s.";
    public static final String MESSAGE_USE_NOTE_COMMAND = "Use note INDEX nt/NOTE to add a note.";
    public static final String MESSAGE_USE_CONTACTED_COMMAND =
            "Use contacted INDEX d/DD-MM-YYYY to record the last contact date.";

    /**
     * Name words that start like a prefix, such as {@code d/o} in "Siti d/o Ahmad" or "Siti D/O-Kumar",
     * in any case and not followed by another letter or digit.
     */
    private static final Pattern NAME_WORD_LIKE_PREFIX = Pattern.compile("(?i)(?:a/[lp]|d/o)(?![\\p{L}\\p{N}]).*");

    /**
     * Returns the first prefix in {@code args} that is one of {@link CliSyntax#ALL_PREFIXES} but not one of
     * {@code supportedPrefixes}, or an empty {@code Optional} if there is none.
     * A prefix counts only in lowercase at the start of a word, where words are split on spaces, tabs and
     * line breaks, so {@code data/x} and {@code NT/x} do not count. This is wider than {@link ArgumentTokenizer}, which
     * only finds a prefix after a plain space, so a prefix typed after a tab is reported too instead of being
     * saved as part of the field before it. The name words {@code a/l}, {@code a/p} and {@code d/o}
     * (in any case, alone or joined by punctuation such as {@code d/o-Kumar}) do not count either, so names
     * such as "Siti d/o Ahmad" are left alone.
     */
    public static Optional<Prefix> findUnsupportedPrefix(String args, Prefix... supportedPrefixes) {
        requireNonNull(args);
        List<Prefix> supported = List.of(supportedPrefixes);
        for (String word : args.strip().split("\\s+")) {
            if (NAME_WORD_LIKE_PREFIX.matcher(word).matches()) {
                continue;
            }
            Optional<Prefix> unsupportedPrefix = CliSyntax.ALL_PREFIXES.stream()
                    .filter(prefix -> !supported.contains(prefix) && word.startsWith(prefix.getPrefix()))
                    .findFirst();
            if (unsupportedPrefix.isPresent()) {
                return unsupportedPrefix;
            }
        }
        return Optional.empty();
    }

    /**
     * Checks that {@code args} has no prefix of another command, as found by
     * {@link #findUnsupportedPrefix(String, Prefix...)}. Without this check, such a prefix and its value
     * would be saved as part of the field before it, e.g. {@code add n/Bob nt/Met at roadshow ...} would
     * name the contact "Bob nt/Met at roadshow".
     *
     * @throws ParseException naming the prefix, {@code commandWord} and the command that uses the prefix.
     */
    public static void verifyNoUnsupportedPrefix(String args, String commandWord, Prefix... supportedPrefixes)
            throws ParseException {
        Optional<Prefix> unsupportedPrefix = findUnsupportedPrefix(args, supportedPrefixes);
        if (unsupportedPrefix.isEmpty()) {
            return;
        }
        Prefix prefix = unsupportedPrefix.get();
        String message = String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix, commandWord);
        if (prefix.equals(PREFIX_NOTE)) {
            message += " " + MESSAGE_USE_NOTE_COMMAND;
        } else if (prefix.equals(PREFIX_LAST_CONTACT)) {
            message += " " + MESSAGE_USE_CONTACTED_COMMAND;
        }
        throw new ParseException(message);
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String followUp} into a {@code FollowUp}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code followUp} is invalid.
     */
    public static FollowUp parseFollowUp(String followUp) throws ParseException {
        requireNonNull(followUp);
        String trimmedFollowUp = followUp.trim();
        if (!FollowUp.hasValidFormat(trimmedFollowUp)) {
            throw new ParseException(FollowUp.MESSAGE_CONSTRAINTS);
        }
        if (!FollowUp.isRealDate(trimmedFollowUp)) {
            throw new ParseException(String.format(FollowUp.MESSAGE_NOT_A_REAL_DATE, trimmedFollowUp));
        }
        return new FollowUp(trimmedFollowUp);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses {@code status} into a {@code Status}, ignoring case and surrounding whitespace.
     *
     * @throws ParseException if the given {@code status} is invalid.
     */
    public static Status parseStatus(String status) throws ParseException {
        requireNonNull(status);
        if (!Status.isValidStatus(status)) {
            throw new ParseException(Status.MESSAGE_CONSTRAINTS);
        }
        return Status.parse(status);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
