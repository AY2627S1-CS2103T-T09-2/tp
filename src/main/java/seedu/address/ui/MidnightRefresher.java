package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.logging.Logger;

import javafx.animation.PauseTransition;
import seedu.address.commons.core.LogsCenter;

/**
 * Runs a refresh action when the local date changes, so that date-dependent displays such as the
 * overdue marker on contact cards stay correct when the app is left open past midnight.
 *
 * <p>The date is checked just after each local midnight, and at least once a minute in between, so a
 * computer that sleeps through midnight or a clock that is changed is also picked up promptly.
 */
public class MidnightRefresher {

    /** How long after midnight the check runs, so a timer that fires slightly early still sees the new day. */
    static final Duration MIDNIGHT_MARGIN = Duration.ofSeconds(1);

    /** The longest wait between two date checks. */
    static final Duration MAX_CHECK_INTERVAL = Duration.ofMinutes(1);

    private static final Logger logger = LogsCenter.getLogger(MidnightRefresher.class);

    private final Clock clock;
    private final Runnable refreshAction;
    private LocalDate lastSeenDate;
    private PauseTransition timer;

    /**
     * Creates a {@code MidnightRefresher} that runs {@code refreshAction} whenever the date given by
     * {@code clock} changes. Call {@link #start()} to begin checking.
     */
    public MidnightRefresher(Clock clock, Runnable refreshAction) {
        this.clock = requireNonNull(clock);
        this.refreshAction = requireNonNull(refreshAction);
        lastSeenDate = LocalDate.now(clock);
    }

    /**
     * Returns the time from now until the start of the next day in the clock's time zone.
     * Daylight saving changes are followed, including days whose midnight is skipped.
     */
    public static Duration delayUntilNextMidnight(Clock clock) {
        requireNonNull(clock);
        ZoneId zone = clock.getZone();
        Instant now = clock.instant();
        Instant nextMidnight = LocalDate.ofInstant(now, zone).plusDays(1).atStartOfDay(zone).toInstant();
        return Duration.between(now, nextMidnight);
    }

    /**
     * Returns how long to wait before the next date check: just after the next midnight,
     * or {@link #MAX_CHECK_INTERVAL}, whichever is sooner.
     */
    Duration getNextCheckDelay() {
        Duration untilJustAfterMidnight = delayUntilNextMidnight(clock).plus(MIDNIGHT_MARGIN);
        return untilJustAfterMidnight.compareTo(MAX_CHECK_INTERVAL) < 0 ? untilJustAfterMidnight : MAX_CHECK_INTERVAL;
    }

    /**
     * Runs the refresh action if the date has changed since the last check.
     *
     * @return true if the refresh action was run.
     */
    boolean refreshIfDateChanged() {
        LocalDate today = LocalDate.now(clock);
        if (today.equals(lastSeenDate)) {
            return false;
        }

        logger.info("Date changed from " + lastSeenDate + " to " + today + "; refreshing the contact list.");
        lastSeenDate = today;
        refreshAction.run();
        return true;
    }

    /**
     * Starts checking the date on the JavaFX application thread. Calling this again has no effect.
     */
    public void start() {
        if (timer != null) {
            return;
        }

        timer = new PauseTransition();
        timer.setOnFinished(event -> {
            refreshIfDateChanged();
            scheduleNextCheck();
        });
        scheduleNextCheck();
    }

    private void scheduleNextCheck() {
        timer.setDuration(javafx.util.Duration.millis(getNextCheckDelay().toMillis()));
        timer.playFromStart();
    }
}
