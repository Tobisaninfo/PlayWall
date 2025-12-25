# PlayWall

## Development

### Native Audio


#### MacOS

- Generate header file for native methods `javac -h . de/tobias/playwall/nativeaudio/audio/mac/AVAudioPlayerBridge.java`

## How to use custom components in SceneBuilder

- Open SceneBuilder
- Click on small gears icon next to `Library`
- Select `JAR/FXML Manager`

![scenebuilder_1.jpg](doc/scenebuilder_1.jpg)

### Add the `PlayWallClient` target folder as root folder
- Click `Add root folder wth *.class files`
- Select the following folder: `<path_to_your_working_copy/PlayWallClient/target/classes`

### Add TheCodeLabs maven repository
- Click `Manage repositories`
- Click `Add`
- Create a new repository for `https://maven.thecodelabs.de/artifactory/TheCodeLabs-release`

![scenebuilder_2.jpg](doc/scenebuilder_2.jpg)

### Add additional JARs as repositories
- Click `Search repositories`
- Search for the following libraries, select and add them:
  - `de.thecodelabs:libJfx`
  - `de.thecodelabs:libUtils`
  - `org.controlsfx:controlsfx`

![scenebuilder_3.jpg](doc/scenebuilder_3.jpg)