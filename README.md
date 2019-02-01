
# BuildFinder
![](https://img.shields.io/github/commits-since/sidola/buildfinder/latest.svg?style=flat-square)

BuildFinder is an application that helps the user find builds that use a certain item in the game Diablo 3. It scrapes the first page of the most popular builds for each class from [DiabloFans](http://www.diablofans.com/builds) and allows the user to filter the list with a given item.

The filter function on the DiabloFans website doesn't take cubed items into account, and it's also much slower than searching locally once you've downloaded a batch of builds to search from.

![Main view](http://i.imgur.com/oJRrpIG.png)

## Download

You can find the latest working release right here on Github. Check out the [releases page](https://github.com/sidola/BuildFinder/releases)!

## Usage

To use the applicaiton, first make sure you're running the [latest version of Java](https://java.com/en/download/). Then double-click the `BuildFinder.jar` to run the application.

On first boot you will be asked to go to [diablofans](http://www.diablofans.com/builds) and setup the filters you want to use to fetch builds. Then copy that URL and paste it in to the application. After that, press the `Update builds` button and wait for the builds to be downloaded.

Once that is done, you can now select an item in the list and the application will show you if there are any builds for that item.

## Building

To build the application from source, run `mvn clean jfx:jar`.

## License

The code is available under the terms of the [MIT License](http://opensource.org/licenses/MIT).
