# BuildFinder

BuildFinder is an application that helps the user find builds that use a certain item in the game Diablo 3. It scrapes the first page of the most popular builds for each class from [DiabloFans](http://www.diablofans.com/builds) and allows the user to filter the list with a given item.

The filter function on the DiabloFans website doesn't take cubed items into account, and it's also much slower than searching locally once you've downloaded a batch of builds to search from.

## Usage

To use it, simply download it and run the `BuildFinder.jar` file. On first run, you need to press the `Update builds` button and wait for the builds to be downloaded. Once that is done, you simply select an item in the list and it'll show you if there are any builds for it.

### Configure what builds to get

In the `data` folder, there is a file called `config.jar`. This file defines what builds the program should download. Below are the valid settings for each option.


| *buildType*         	| *buildPatch*  	| *dateRange*  	| *filterType* 	| *pages* 	| *classes* (array) 	|
|---------------------	|---------------	|--------------	|--------------	|---------	|-------------------	|
| 0 - All             	| 0 - All       	| 1 - Hot      	| -viewcount   	| 1 to 3  	| 2 - Barbarian     	|
| 1 - Regular         	| 6 - Patch 2.4 	| 2 - New      	| -rating      	|         	| 4 - Demon hunter  	|
| 2 - Season          	| 5 - Patch 2.3 	| 3 - Week     	|              	|         	| 8 - Witch doctor  	|
| 3 - Hardcore        	|               	| 4 - Month    	|              	|         	| 16 - Monk         	|
| 4 - Hardcore season 	|               	| 5 - All time 	|              	|         	| 32 - Wizard       	|
|                     	|               	|              	|              	|         	| 64 - Crusader     	|

**Note**: Make sure you make all values are strings, meaning you put them in quotation marks. Like `"this"`, and not like `this`. Otherwise the config won't load. Also make sure all your commas are in place. Use a [JSON validator](http://jsonlint.com/) if you're unsure.

Example config:

```Json
{
  "buildType": "0",
  "buildPatch": "0",
  "dateRange": "4",
  "filterType": "-rating",
  "pages": "1",
  "classes": [
    "2",
    "4"
  ]
}
```

## Screenshot

![Main view](http://i.imgur.com/lGJUufQ.png)

## Building

To build the application from source, run `mvn clean jfx:jar`.

## License

The code is available under the terms of the [MIT License](http://opensource.org/licenses/MIT).
