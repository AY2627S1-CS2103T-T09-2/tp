---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# Easy-Insurance Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* This project is based on the [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `CommandInputParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `CommandInputParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `CommandInputParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T09-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section will describe noteworthy details of how Easy-Insurance features are implemented, starting with the follow-up and status features once their designs settle.

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* junior insurance agent who is building their own base of prospects and clients
* has a growing number of prospects and clients, and struggles to remember who to follow up with and when
* currently keeps prospects scattered across phone contacts, chat apps and spreadsheets
* works alone on their own laptop and prefers desktop apps
* can type fast and prefers typing and keyboard shortcuts to mouse interactions
* is new to CLI apps, but willing to learn a few short commands

**Value proposition**: Keep track of prospects and clients, what status each one has, and when to follow up next, faster than juggling phone contacts, chat apps and spreadsheets.

Easy-Insurance focuses on managing prospects and clients. It does not send messages or make calls, generate quotations or premiums, recommend policies, manage claims, track commissions, or replace the agency's official CRM. It is a single-user app, so nothing is shared with other agents.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                  | I want to …                                            | So that I can…                                                       |
|----------|------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------------------|
| `* * *`  | new insurance agent                      | add a new prospect                                      | keep track of people I may want to follow up with                    |
| `* * *`  | insurance agent                          | view my list of prospects and clients                   | see the people I am currently managing                               |
| `* * *`  | insurance agent looking for a person     | search for a contact by name                            | quickly retrieve the person's information                            |
| `* * *`  | insurance agent                          | edit a contact's details                                | keep their information up to date when it changes                    |
| `* * *`  | insurance agent                          | delete a contact                                        | remove people who no longer want to hear from me                     |
| `* * *`  | insurance agent                          | set a contact's status to prospect, client or inactive  | tell at a glance who is still worth chasing                          |
| `* * *`  | insurance agent                          | set a follow-up date for a contact                      | remember who I promised to call back                                 |
| `* * *`  | insurance agent                          | mark a follow-up as done                                | keep my list to what is still pending                                |
| `* * *`  | insurance agent unfamiliar with CLI      | see a guide to the available commands and their formats | learn to use the app without prior CLI experience                    |
| `* *`    | insurance agent                          | record when I last contacted someone                    | see who I have not spoken to in a while                              |
| `* *`    | insurance agent planning my day          | see all follow-ups due today                            | know who to call before I start                                      |
| `* *`    | insurance agent unfamiliar with CLI      | get a clear error message when I type a command wrongly | learn from the error and type it correctly next time                 |
| `* *`    | insurance agent                          | filter my list by tag                                   | focus on prospects when planning outreach                            |
| `* *`    | insurance agent with many contacts       | search by partial name or phone number                  | find someone when I only remember part of it                         |
| `* *`    | insurance agent                          | see overdue follow-ups                                  | catch the people I missed                                            |
| `* *`    | insurance agent                          | see follow-ups due this week                            | plan ahead when today is already full                                |
| `* *`    | insurance agent                          | push a follow-up to a later date                        | reschedule when a prospect asks me to call next week                 |
| `* *`    | insurance agent                          | record the policies a client has bought                 | avoid pitching what they already have                                |
| `* *`    | insurance agent                          | see renewals due in the next month                      | plan retention calls early                                           |
| `* *`    | insurance agent                          | sort contacts by how many times I met them              | identify prospects who are more engaged with me                      |
| `* *`    | insurance agent                          | be warned when I add a duplicate                        | keep my list clean                                                   |
| `* *`    | insurance agent                          | be asked to confirm before deleting                     | avoid deleting someone by accident                                   |
| `* *`    | insurance agent                          | undo my last change                                     | recover from a mistake quickly                                       |
| `* *`    | insurance agent                          | see the contacts I added most recently                  | check what I keyed in after a busy day                               |
| `* *`    | insurance agent                          | mark a contact as do-not-contact                        | respect people who said no without deleting their record             |
| `* *`    | insurance agent back from leave          | see everything that went overdue while I was away       | catch up in one go                                                   |
| `*`      | insurance agent                          | import contacts from a spreadsheet                      | bring in lead lists without retyping them                            |
| `*`      | insurance agent                          | export my contacts to a spreadsheet                     | send my manager my numbers                                           |
| `*`      | insurance agent                          | archive contacts I no longer work with                  | keep my main list short without losing their history                 |
| `*`      | insurance agent who types fast           | use short forms for common commands                     | save keystrokes on things I do many times a day                      |
| `*`      | insurance agent                          | recall a previous command                               | repeat it without retyping                                           |
| `*`      | insurance agent switching laptops        | copy my data file to a new computer                     | keep working without starting over                                   |
| `*`      | insurance agent                          | get a clear message if my data file is corrupted        | know what happened instead of losing everything silently             |
| `*`      | insurance agent                          | record a client's birthday                              | keep in touch between renewals                                       |
| `*`      | insurance agent                          | record a prospect's life stage                          | pitch products that fit them                                         |
| `*`      | insurance agent                          | record the language a client prefers                    | speak to them in a language they are comfortable with                |
| `*`      | insurance agent                          | link two contacts as family                             | pitch family plans to the right people                               |
| `*`      | insurance agent                          | see how many prospects, clients and lost leads I have   | track progress toward my target                                      |
| `*`      | insurance agent                          | set a preferred contact channel for a person            | reach them the way they like                                         |
| `*`      | insurance agent                          | copy a phone number with one command                    | paste it into WhatsApp quickly                                       |
| `*`      | insurance agent                          | see how many contacts I added this month                | check if I am prospecting enough                                     |

### Use cases

(For all use cases below, the **System** is Easy-Insurance and the **Actor** is the insurance agent.)

**Use case: Add a prospect and plan a follow-up**

**MSS**

1. Agent provides the new prospect's name and contact details.
2. System validates the details and saves the contact with status `PROSPECT`.
3. System shows the new prospect in the contact list.
4. Agent sets a follow-up date for the prospect.
5. System saves the pending follow-up and shows its date with the prospect.

   Use case ends.

**Extensions**

* 2a. The contact details are invalid or incomplete.
  * 2a1. System explains which details need correction without changing saved contact data.
  * 2a2. Agent corrects the details.
  * Use case resumes at step 2.
* 2b. A contact with the same name already exists, ignoring case and extra spaces.
  * 2b1. System explains that the contact already exists without changing saved contact data.
  * Use case ends.
* 4a. The follow-up date is invalid.
  * 4a1. System shows an error and does not change the saved contact.
  * Use case resumes at step 4.
* 5a. The follow-up date is in the past.
  * 5a1. System saves the date and warns the agent that it is in the past.
  * Use case ends.

**Use case: Find contacts by name**

**MSS**

1. Agent enters `find KEYWORD [MORE_KEYWORDS]`.
2. System ignores leading and trailing spaces and checks that at least one keyword was supplied.
3. System searches contact names case-insensitively. A complete word matching any supplied keyword is sufficient.
4. System shows only matching contacts and reports how many were found.

   Use case ends.

**Extensions**

* 2a. No name keyword was supplied.
  * 2a1. System shows `Please enter at least one name keyword to search for. Example: find alice tan`.
  * Use case ends without changing the contact list.
* 3a. No contact name contains a matching complete word.
  * 3a1. System shows an empty result list and reports `0 contacts listed.`
  * Use case ends.

**Use case: Complete a follow-up**

**MSS**

1. Agent views a contact with a pending follow-up.
2. System shows the scheduled follow-up date.
3. Agent requests to mark that follow-up as done.
4. System records the follow-up as completed and shows the updated contact.

   Use case ends.

**Extensions**

* 3a. The contact has no pending follow-up.
  * 3a1. System explains that there is no follow-up to complete.
  * Use case ends without changing the contact.
* 3b. The selected contact is not in the displayed list.
  * 3b1. System explains that the contact reference is invalid.
  * Use case ends without changing any contacts.

**Use case: Delete a contact**

**MSS**

1. Agent requests to list contacts, or finds contacts by name.
2. System shows the matching contacts with their index numbers.
3. Agent requests to delete the contact at a given index.
4. System removes the contact and confirms which contact was deleted.

   Use case ends.

**Extensions**

* 2a. No contacts are shown.

  Use case ends.

* 3a. The given index does not refer to a shown contact.
  * 3a1. System explains that the index is invalid and changes nothing.

    Use case resumes at step 2.

### Non-Functional Requirements

1. Easy-Insurance should run on Windows, macOS, and Linux with Java `25` or above installed.
2. With up to 1000 contacts, listing and finding contacts should complete within two seconds on a typical laptop.
3. An agent should be able to add, find, edit, and view contacts and manage follow-ups using the keyboard alone.
4. The app should work without an internet connection and should not transmit contact details to a network service.
5. An agent who knows the commands should be able to complete common contact tasks faster by typing than by using the mouse.
6. Contact data should be stored locally as human-editable JSON.
7. The app should be distributed as a single JAR of at most 100 MB, with no installer required.

### Glossary

* **Contact**: A person recorded in Easy-Insurance, together with their contact details, status, and any follow-up.
* **Status**: Where a contact is in the agent's pipeline: `PROSPECT`, `CLIENT` or `INACTIVE`. New contacts are prospects unless told otherwise.
* **Prospect**: A contact the agent may sell a policy to but who is not yet a client.
* **Client**: A contact who has bought a policy from the agent.
* **Inactive**: A contact the agent is no longer actively pursuing but keeps in the app.
* **Follow-up**: The date on which the agent intends to next contact a prospect or client. It may be in the past, for example when recording a follow-up that already happened.
* **Pending follow-up**: A follow-up that has not been marked as done.
* **Duplicate contact**: A contact whose name matches an existing contact's name after ignoring letter case and extra spaces. Easy-Insurance refuses to add a duplicate.
* **Index**: The number shown next to a contact in the currently displayed list. Commands such as `edit`, `delete` and `done` refer to contacts by index.
* **Name keyword**: A non-empty, complete word used by `find` to match contact names, ignoring letter case. Partial-name matching is outside the initial version.
--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download `EasyInsurance.jar` and copy it into an empty folder.

   1. Open a terminal in that folder and run `java -jar EasyInsurance.jar`.<br>
      Expected: The window titled Easy-Insurance opens with a set of sample contacts, each showing a status badge.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app the same way.<br>
      Expected: The most recent window size and location are retained.

### Deleting a contact

1. Deleting a contact while all contacts are being shown

   1. Prerequisites: List all contacts using the `list` command, with multiple contacts in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is removed from the list. The result shows `Deleted contact: ` followed by that contact's name.

   1. Test case: `delete 0`<br>
      Expected: No contact is deleted. The result shows the invalid-command-format error with the delete usage.

   1. Test case: `delete 99` (where 99 is larger than the list size)<br>
      Expected: No contact is deleted. The result shows `The contact index provided is invalid.`

   1. Other incorrect delete commands to try: `delete`, `delete x`, `delete 1 2`<br>
      Expected: No contact is deleted. An error explains the problem.

1. Deleting a contact from a filtered list

   1. Prerequisites: Run `find` with a keyword that matches at least one contact.

   1. Test case: `delete 1`<br>
      Expected: The first contact in the filtered list is deleted, not the first contact overall.

### Adding a contact with a follow-up date

1. Adding a contact with a future follow-up date

   1. Test case: `add n/Sarah Lim p/92345678 e/sarah@example.com a/10 Tampines Street 1 f/25-12-2099`<br>
      Expected: The contact is added and the card shows `Follow-up: 25-12-2099`. No note is shown.

1. Adding a contact with a past follow-up date

   1. Test case: `add n/Ben Ong p/93456789 e/ben@example.com a/5 Bishan Road f/01-01-2020`<br>
      Expected: The contact is added. The result message ends with a note that `01-01-2020` is in the past.

1. Adding a contact with an impossible or badly formatted date

   1. Test case: `add n/Ann Lee p/94567890 e/ann@example.com a/1 Clementi Ave f/31-02-2026`<br>
      Expected: No contact is added. Error: `31-02-2026 is not a real date.`
   1. Test case: `... f/2026-12-25`<br>
      Expected: No contact is added. Error: `Follow-up date must be in DD-MM-YYYY format, e.g. 25-12-2026.`

### Completing a follow-up

1. Completing a pending follow-up from a filtered list

   1. Prerequisites: Add a contact named `Followup Test` with a scheduled follow-up, for example:<br>
      `add n/Followup Test p/91234567 e/followuptest@example.com a/21 Clementi Ave 3 st/CLIENT f/25-12-2099 t/referral`<br>
      Run `find Followup` and check that only this contact is shown.

   1. Test case: `done 1`<br>
      Expected: The result shows `Marked follow-up for Followup Test as completed.`
      The card shows `Follow-up: None`. The contact's name, phone, email, address, status and tags remain unchanged.

   1. Test case: Run `done 1` again.<br>
      Expected: The result shows `Followup Test does not have a pending follow-up.` No contact details change.

   1. Test case: `done 2` while only the test contact is displayed.<br>
      Expected: The result shows `The contact index provided is invalid.` No contact details change.

   1. Other incorrect commands to try: `done`, `done 0`, `done x`, `done 1 extra`<br>
      Expected: The result shows the invalid-command-format error with `done INDEX`. No contact details change.

1. Saving a completed follow-up

   1. Prerequisites: Complete the test contact's follow-up as described above.

   1. Test case: Close the app, relaunch it, and run `find Followup`.<br>
      Expected: The contact still shows `Follow-up: None`, and all its other details remain unchanged.

### Saving data

1. Dealing with a missing data file

   1. Close the app. Delete `data/addressbook.json` from the folder containing the JAR.

   1. Relaunch the app.<br>
      Expected: The app starts with the sample contacts and recreates the data file on the next change.

1. Dealing with a corrupted data file

   1. Close the app. Open `data/addressbook.json` and change one contact's `"status"` value to `"lead"`.

   1. Relaunch the app.<br>
      Expected: The app starts with an empty contact list, because the file could not be read. Restoring the original value and relaunching brings the contacts back.

1. Dealing with an older data file

   1. Close the app. Remove the `"status"` and `"followUp"` fields from one contact in `data/addressbook.json`.

   1. Relaunch the app.<br>
      Expected: The contact loads with status `PROSPECT` and no follow-up. Nothing else is lost.
