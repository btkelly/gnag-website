# Gnag Website

This file will be packaged with your application when using `activator dist`.

## Setup

You will need:

- Java 8
- [Homebrew](http://brew.sh/)

Ensure your `$JAVA_HOME` environment variable points to your Java 8 installation path:

```sh
$ echo $JAVA_HOME
/Library/Java/JavaVirtualMachines/jdk1.8.0_xx.jdk/Contents/Home
```

Download [activator](https://github.com/typesafehub/activator) with Homebrew:

```sh
$ brew update
$ brew install typesafe-activator
```

## Development

Start a development server:

```sh
$ activator ui
```

> **NOTE:** when you first run this command, `activator` will download a lot of dependencies needed to run the application. Once complete the activator UI should open in your web browser.

In the web UI select **Open existing app** and navigate to where you cloned this repo, activator should take control here and begin to start services required by the Gnag application.

Once the initial load is complete you should be able to select the **Run** tab on the left side bar. Click "Run" on the top of this page and the application should compile and run allowing you to access the site at "localhost:9000".

You are now ready to go and can make changes without restarting activator, it should pick up on changes you make and recompile the Gnag application on the fly.

## Helpful Information

### Controllers

- **HomeController.java:** contains the controller function for the index.html

- **GitHubAuthController.java:** handles all communication with the GitHub api and rendering of results

### Play Components

- **Module.java:** use Guice to bind all the components needed by your application.

- **Filers.java:** set global filters that will run on all web requests.

- **application.conf:** main application config file, check her for most Play Framework settings.

- **routes:** routing file used to route web urls to their corresponding controller actions.

- **build.sbt:** SBT file for the application, define versions,