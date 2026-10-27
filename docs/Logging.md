# Logging guide

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

## Follow-up, list and Help diagnostics

| Level | Event | Context |
| --- | --- | --- |
| `INFO` | `done` clears a pending follow-up in the model | One-based displayed index. This does not confirm a successful disk save. |
| `INFO` | `list` applies filters | Status (or `ALL`), whether a tag filter is present, and result count. Zero matches are a normal result. |
| `FINE` | `done` is rejected | Invalid syntax or index, displayed list size for an out-of-range index, or no pending follow-up. |
| `FINE` | `list` arguments are rejected | Unsupported preamble, duplicate prefix, invalid status, or invalid tag. |
| `FINE` | Help window diagnostics | Selected font families, changes between table and stacked layouts, adjusted dimensions, window shown, and successful URL copy. |
| `WARNING` | User Guide URL cannot be copied | The clipboard rejected the write. |

These feature logs record reasons and safe metadata. They do not include contact names, phone numbers,
email addresses, addresses, raw arguments or tag values. Existing shared command and feedback logs
are unchanged; this is not application-wide redaction.

To inspect the detailed diagnostics locally:

1. Temporarily change `LOG_LEVEL` in `src/main/java/seedu/address/commons/core/LogsCenter.java`
   from `Level.INFO` to `Level.FINE`.
2. Run `./gradlew run`, try valid and invalid `done` and `list` commands, open Help,
   resize across the table/stacked layout boundary, and copy the User Guide URL.
3. Inspect the console or `easyinsurance.log.0` in the project directory. A successful `done`
   log describes the model update; persistence errors are still reported by the existing logic layer.
4. Restore `LOG_LEVEL` to `Level.INFO` before committing. Do not commit local log files.
