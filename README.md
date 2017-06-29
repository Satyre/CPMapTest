# ITW CPMapTest

Chaque étape est dans un commit.

## Libs

* Mapbox
* Sugar
* Espresso 

## Issue 

Probleme possible avec Sugar (je n'ai pas eu de souci mais je met au cas ou)  

### 1. Instant Run. 

Instant-Run seems to prevent Sugar ORM from finding the "table" classes, therefore it cannot create the DB tables if you run the app for the first time 

When running your app for the first time Turn off Instant run once to allow for the DB tables to be created
You can enable it after the tables have been created. 

To disable Instant-Run in Android Studio: 

``(Preferences (Mac) or Settings (PC) -> Build, Execution, Deployment -> Instant Run -> Untick "Enable Instant Run..." )``
