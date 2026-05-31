---
name: add-camera
description: Add a new volcano camera to the app. Updates all 5 required locations: strings.xml, NavigationDrawerFragment titles array, and the three switch statements in PhotoLiveViewer (URL, timer, sharing).
disable-model-invocation: true
---

You are adding a new volcano camera to the Volcanes de Costa Rica app. This requires editing exactly 5 places. Follow each step in order.

## Step 1: Gather information

Ask the user for:
1. **Camera name** (short, for the navigation drawer, e.g., "Arenal")
2. **Feed slug** (the URL segment, e.g., `arenal` — will be used in the camera URL)
3. **Info text** (description shown below the image, in Spanish, including the refresh interval)
4. **Share message** (e.g., "Volcán Arenal")
5. **Refresh interval** — which timer to use:
   - `Timer10seg` (10 s) — very fast cameras
   - `Timer60seg` (60 s) — standard (most cameras)
   - `Timer5min` (5 min) — slow/remote cameras

If the user ran `/add-camera <name>` with a name as argument, pre-fill what you can and ask only for what's missing.

## Step 2: Determine the new position index

Read `app/src/main/java/krausoft/volcanesdecostarica/NavigationDrawerFragment.java` and count the items currently in the `titles` array inside `getData()`. The new camera's position = current count (0-indexed).

## Step 3: Add string resources

Edit `app/src/main/res/values/strings.xml`. Add these entries before `</resources>`:

```xml
<string name="{id}">CAMERA_NAME</string>
<string name="{id}_feed">FEED_SLUG</string>
<string name="{id}_info"><tt>INFO_TEXT</tt></string>
<string name="Mensaje{N}">SHARE_MESSAGE</string>
```

Where `{id}` is a short identifier (e.g., `arenal`), and `{N}` is `current_count + 1` (Mensaje strings are 1-indexed).

## Step 4: Add to the navigation drawer title list

Edit `NavigationDrawerFragment.getData()` in `NavigationDrawerFragment.java`. Add `context.getResources().getString(R.string.{id})` to the `titles` array.

```java
String[] titles = { ..., context.getResources().getString(R.string.{id}) };
```

## Step 5: Add URL case in PhotoLiveViewer.onCreate()

In `PhotoLiveViewer.java`, find the `switch (option)` block in `onCreate()` (around line 101). Add a new case at the end, before `default`:

```java
case N:
    url = url + getString(R.string.{id}_feed);
    textView.setText(getString(R.string.{id}_info));
    break;
```

Where `N` is the new position index from Step 2.

## Step 6: Add timer case in PhotoLiveViewer.startTimer()

In the `switch (option)` block inside `startTimer()` (around line 186). Add a new case:

```java
case N:
    period = Long.parseLong(getString(R.string.TIMER_CONSTANT));
    break;
```

## Step 7: Add sharing case in PhotoLiveViewer.sharePicture()

In the `switch (option)` block inside `sharePicture()` (around line 312). Add a new case:

```java
case N:
    mensaje = getString(R.string.Mensaje{N_1indexed}) +
            " [" + date + "]";
    break;
```

## Step 8: Confirm

After all edits, summarize the changes made and remind the user to:
- Build with `./gradlew assembleDebug` to verify no compilation errors
- Test on a device or emulator to confirm the camera appears and loads
