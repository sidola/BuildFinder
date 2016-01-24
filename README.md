# BuildFinder

BuildFinder is an application that helps the user find builds that use a certain item in the game Diablo 3. It scrapes the first page of the most popular builds for each class from [DiabloFans](http://www.diablofans.com/builds) and allows the user to filter the list with a given item.

The filter function on the DiabloFans website doesn't take cubed items into account, and it's also much slower than searching locally once you've downloaded a batch of builds to search from.

The current fetch settings are:

- Patch 2.4
- Seasonal
- Most viewed

## Usage

To use it, simply download it and run the `BuildFinder.jar` file. On first run, you need to press the `Fetch new builds` button and wait for the builds to be downloaded. Once that is done, you simply select an item in the list and it'll show you if there are any builds for it.

![Main view](http://i.imgur.com/9f7LDet.png)

## Building

To build the application from source, run `mvn clean jfx:jar`.

## License

The code is available under the terms of the [MIT License](http://opensource.org/licenses/MIT).
