# FaroByBonita
## Inspiration

People with dementia often struggle to recognize familiar faces and feel safe in their own home. Caregivers already carry a demanding routine, and making continuous reassuring reminders adds to that load. When someone with dementia feels lost or confused, it creates real fear and anxiety.

We found inspiration in how caregivers handle this today. Many use whiteboards, printed notes, and simple scripts to give calm, consistent answers to repeated questions. One caregiver wrote: "I started giving the same short, calm response each time, almost like a script. We also keep a small whiteboard in the kitchen with the day and any plans written out, which cuts down on some of it." Faro takes that same idea and puts it on a screen that is always visible, always updated, and never requires the person with dementia to do anything at all.

## What it does

Faro is two apps working together. The dependent app runs on a tablet in the home of the person with dementia. The caregiver app runs on the caregiver's phone and handles all setup and customization.

**Dependent app (tablet)**

The tablet runs in kiosk mode and cannot be exited without caregiver access. It has two states:

* Idle mode: displays the current date and a message set by the caregiver. When it is time for medication, the screen shows a reminder like "Time for Aspirin" for one minute around the scheduled time.
* Arrival mode: when a family member arrives home, their name fades in and their photo appears on screen for about one minute. No loud alerts, no sudden sounds. Just a familiar face and a name.

**Caregiver app (phone)**

The caregiver app gives full control over what the person with dementia sees, without them ever needing to interact with the tablet:

* Set a live display message that updates on the tablet in real time, like "Roast for dinner tonight!"
* Schedule medicine reminders with a name, time, and frequency
* Manage multiple dependents from one account
* Toggle dark mode and location settings from the edit page

## How we built it

Both apps are built natively in Android using Java. We used Firebase Realtime Database to sync data between the caregiver phone and the dependent tablet instantly. Caregiver account data and dependent profiles are stored in Firestore. Firebase Auth handles login and registration.

The caregiver app uses a fragment-based navigation structure with a bottom nav bar. The home fragment writes the display message to Realtime Database in real time, the reminder fragment reads and writes reminder entries per device, and the edit page controls app-wide settings. The profile tab opens as a small bottom sheet overlay rather than a full page.

## Challenges we ran into

* Firebase Auth silent failures caused by email enumeration protection on a shared Firebase project, which required the project owner to resolve since settings were locked to the original creator.
* Database inconsistency between teammates, where one used Realtime Database and another used Firestore. We aligned on using Realtime Database for all live device state and Firestore for caregiver account data only.
* Git merge conflicts from three separate branches with overlapping layout files.
* Designing for a sensitive audience where every UI decision had to account for the fact that excess stimulus can be distressing. We kept animations subtle, avoided sudden changes, and kept the interface as minimal as possible.

## Accomplishments that we are proud of

* Real-time sync between the caregiver phone and the dependent tablet that works instantly with no refresh needed.
* A kiosk-mode tablet experience that a person with dementia can passively benefit from without ever touching the screen.
* A caregiver app flow that goes from registration to a fully functional home page in a few steps.
* Designing around a real problem that does not have a great existing solution, backed by stories from actual caregivers.

## What we learned

* How to structure two Android apps that communicate through a shared Firebase project.
* How to use Firebase Realtime Database listeners for live UI updates across devices.
* How to design for users who are not the ones directly operating the app, where the real user is in the background setting things up for someone else.
* How to collaborate across multiple Git branches and resolve conflicts without losing each contributor's work.
* How important it is to think about the emotional context of your user, not just the technical functionality.
