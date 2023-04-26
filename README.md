# Configuring MongoDB connector for Identity verification claims

## Prerequisites

To use this connector,

- you'll need to configure the `identity verification` components with your WSO2 Identity Server.
- Follow [this readme file](link) to configure the relevant components.

## Setting up the MongoDB connector

**Step 1**: Downloading MongoDB to your machine.

- [Using this link](https://www.mongodb.com/try/download/shell) you can use MongoDB locally on your machine with MongoDB compass.
- Or [using this link](https://www.mongodb.com/cloud/atlas/register?utm_source=google&utm_campaign=search_gs_pl_evergreen_atlas_core_prosp-brand_gic-null_apac-lk_ps-all_desktop_eng_lead&utm_term=mongodb%20atlas&utm_medium=cpc_paid_search&utm_ad=e&utm_ad_campaign_id=12212624368&adgroup=115749715343&cq_cmp=12212624368&gad=1&gclid=CjwKCAjw6IiiBhAOEiwALNqncRx1qPZ0o8icx1wj1uVg0oTUPEALFgmThjJcWsAHnRjUyhJQi0-wHxoCREsQAvD_BwE) you can use the MongoDB cloud using MongoDB atlas.

**Step 2**: Setting up MongoDB.

- Install MongoDB compass and then first click on the `New connection +` button. Then click on the `Databases + ` icon and create a new database call `identitydb`. Then go to the database you created and click on `+` to create a new collection call `idv_claim`.
- Or you can follow the [documentation](https://www.mongodb.com/docs/atlas/) to set up Mongo Atlas as above.

**Step 3**: Downloading other resources.

- Download the MongoDB Client jar from the maven repository.
- Navigate to `<IS_HOME>/repository/components/lib`.
- Put the downloaded jar file into the folder.

Note: You can download the latest version of the jar [here.](https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver)

**Step 4**: Modifying the necessary files.

- Navigate to `<IS_HOME>/repository/resources/conf/templates/repository/conf/identity`.
- Open the `identity.xml.j2` file.
- Add the below segment in the bottom of the file before the `</Server>` tag.

```
<datasource>
    <configuration>
        <url>{{data_source.configuration.url}}</url>
        <databaseName>{{data_source.configuration.database}}</databaseName>
        <collectionName>{{data_source.configuration.collection}}</collectionName>
    </configuration>
</datasource>
```

- Next navigate to `<IS_HOME>/repository/conf`.
- Open the `deployment.toml` file.
- Add the below segment.

```
[data_source.configuration]
url = "mongodb://localhost:27017"
database = "identitydb"
collection = "idv_claim"
```

Note: `url` is the MongoDB connection URL. You can get that by either conecting MongoDB locally or using MongoDB cloud.

**Step 5**: Extracting the project arrtifacts.

- Clone the `identity-verification-mongo-datasource` repository.
- Open it using a code editor.
- Build the project using the command `mvn clean install` in the root directory.
- Navigate to `<IS_HOME>/repository/components/dropins`.
- Paste the `.jar` file to the dropins folder.

**Step 6**: Run the WSO2 Identity Server.
