# identity-verification-mongo-datasource# Configuring a MongoDB connector for Identity verification claims

## Prerequisites

To use this connector, you'll need to configure the identity verification components with your WSO2 Identity Server. Follow [this readme file](link) to configure the relevant components.

## Setting up the MongoDB connector

Step 1: Donwloading other resources.

- Download the MongoDB Client jar from the maven repository.
- Put the downloaded jar file into the `<IS_HOME>/repository/components/lib` folder.

Note: [You can download the latest version of the jar here.](https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver)

Step 2: Modifying files.

- Navigate to `<IS_HOME>/repository/resources/conf/templates/repository/conf/identity`.
- Open the `identity.xml.j2` file.
- Add the below segment.

```
<datasource>
    <configuration>
        <url>{{data_source.configuration.url}}</url>
        <databaseName>{{data_source.configuration.database}}</databaseName>
        <collectionName>{{data_source.configuration.collection}}</collectionName>
    </configuration>
</datasource>
```

- Then navigate to `<IS_HOME>/repository/conf` folder.
- Open the `deployment.toml` file.
- Add the below segment.

```
[data_source.configuration]
url = "mongodb+srv://admin:admin@identitydb.ow4i2ci.mongodb.net/test"
database = "identitydb"
collection = "idv_claim"
```

Step 3: Extracting the project arrtifacts.

- Clone the `identity-verification-mongo-datasource` repository.
- Open it using a code editor.
- Build the project using the command `mvn clean install` in the root directory.
- Navigate to `<IS_HOME>/repository/components/dropins` folder.
- Paste the `.jar` file to the dropins folder.

Step 4: Run the WSO2 Identity Server.
