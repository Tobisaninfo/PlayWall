# PlayWall

## Development

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

## MacOS Signing

### Prerequisites

1. Create a macOS code signing certificate (Developer ID Application) in Apple Developer Portal and import it into
   Keychain.
2. Create an appstore connect API key

### Local development and release signing

1. Add the following to your `.m2/settings.xml` file:

```xml
<profiles>
    <profile>
        <id>macos-sign</id>
        <properties>
            <mac.sign.identity>[YOUR APPLICATION CERTIFICATE NAME]</mac.sign.identity>
            <mac.sign.keychain>[FULL PATH TO KEYCHAIN FILE]</mac.sign.keychain>
            <mac.notary.key>[FULL PATH TO APPSTORE CONNECT API KEY]</mac.notary.key>
            <mac.notary.key.id>[APPSTORE CONNECT KEY ID]</mac.notary.key.id>
            <mac.notary.issuer>[APPSTORE CONNECT ISSUER]</mac.notary.issuer>
        </properties>
    </profile>
</profiles>

<activeProfiles>
<activeProfile>macos-sign</activeProfile>
</activeProfiles>
```

### GitHub Actions

The following secrets must be set in the GitHub repository:

* `AC_ISSUER`: Appstore Connect API Key Issuer
* `AC_KEY`: Appstore Connect API Key (base64 encoded file)
* `AC_KEY_ID`: Appstore Connect API Key ID
* `CERT_PASSWORD`: Password for `DEVELOPER_ID_P12_BASE64`
* `DEVELOPER_ID_P12_BASE64`: Developer ID Application certificate and private key (base64 encoded file)
* `MAC_SIGN_IDENTITY`: Keychain entry name (e.g. `Developer ID Application: [Name] ([XXXXXXXXX])`)
* `MAVEN_PASSWORD`: Password for deploying to maven repository
* `MAVEN_USERNAME`: Username for deploying to maven repository
* `TEAM_ID`: Apple Developer Portal Team ID
