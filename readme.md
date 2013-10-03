RPatch
======

[![Screenshot of RPatch demo app, links to larger version](http://i.imgur.com/lBZvX8j.png)](http://i.imgur.com/szkFldu.png)

RPatch is a simple-to-use library for Android that provides a new twist on the Android [NinePatch](http://developer.android.com/reference/android/graphics/NinePatch.html).

RPatch uses the powerful and familiar single-image format employed by NinePatch, but allows you to specify repetition behavior on the middle and edge patches. RPatch will draw its contents to fill the target area as completely as possible.

##Examples

Below are two examples of RPatches that are included in the demo app. Original assets and two "final products" are included for both.

###Stamp

Original asset:

![Stamp RPatch asset](http://i.imgur.com/gQHgXK3.png)

Drawn in View:

![Stamp sample 1](http://i.imgur.com/mgoy6tG.png)
![Stamp sample 2](http://i.imgur.com/UBCzdaQ.png)

###Stripes

Original asset:

![Stripes RPatch asset](http://i.imgur.com/Q9GHhSx.png)

Drawn in View:

![Stripes sample 1](http://i.imgur.com/qOVIoKR.png)
![Stripes sample 2](http://i.imgur.com/R98a0uO.png)

##Format

RPatch assets are very similar to the Android NinePatch format. They're PNG files with a one pixel transparent border. Non-transparent pixels on this border will dictate the boundaries between patches.

The Android build system provides no mechanism for creating custom resource types, so using a special extension (like NinePatch's .9.png) isn't possible. As such, a normal .png file extension is fine.

The following image (a blown-up version of the Stripes example asset) shows how the edge and middle patches are defined. The left and right patches (yellow) may stretch/repeat vertically. The top and bottom patches (blue) may stretch/repeat horizontally. The center patch (green) may stretch/repeat horizontally and vertically. The corners (uncolored) have no special behavior.

![Stripes asset with explanatory annotations](http://i.imgur.com/lW5c1fB.png)

##Usage

RPatches must be instantiated from code due to the design of the Android resource system — the asset will behave like a normal PNG if referenced directly as a View's background or ImageView's source.

```java
RPatch patch = new RPatch(context, R.drawable.my_rpatch);
patch.setRepeatFlags(RPatch.REPEAT_OUTER_ALL);
myView.setBackground(patch);
```

###Patch Repetition Flags

The RPatch class uses the following constants to define whether a portion of the image will be stretched or repeated:

```
REPEAT_INNER_X
REPEAT_INNER_Y
REPEAT_INNER_BOTH
REPEAT_INNER_NONE

REPEAT_OUTER_TOP
REPEAT_OUTER_BOTTOM
REPEAT_OUTER_LEFT
REPEAT_OUTER_RIGHT
REPEAT_OUTER_ALL
REPEAT_OUTER_NONE
```

Just OR together the combination of flags you want to use and pass it to `setRepeatFlags()`. `REPEAT_INNER_BOTH` and `REPEAT_OUTER_ALL` are provided as aliases so you don't have to provide all the inner or outer flags.

Using `REPEAT_INNER_NONE` or `REPEAT_OUTER_NONE` is not strictly required because passing in no inner or outer flags will have the same effect, but you may find it helpful to be more explicit in your code.

###Patch Modes

RPatch has two modes: `REPEAT_MODE_DISCRETE` and `REPEAT_MODE_CUTOFF`.

In **Discrete** mode, the RPatch will be drawn so that each patch in the image is repeated a discrete number of times (e.g. 5 or 6 times, but not 5.5). This will preserve seamless edges your content might have between patches. The downside is that the image may not neatly fill the available area.

The default behavior for Discrete mode is to draw against the upper-left corner. Calling `setDrawCentered(true)` will center the drawing in the available area.

In **Cutoff** mode, the edge patches will be drawn against the edges of the available area and the center patch will be drawn in the remaining area remains within. The patch will always neatly fill the available area, but you may have visual artifacts if your asset has continuous features that cross patch boundaries.

The repeat mode flags may be passed in to `setRepeatFlags()` along with the patch flags. `REPEAT_MODE_DISCRETE` is the default, but you may provide it if you wish to be explicit in your code. 