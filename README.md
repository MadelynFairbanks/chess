# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
https://sequencediagram.org/index.html?presentationMode=readOnly&shrinkToFit=true#initialData=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-Lhf8-07tAX8r7mEgh4LkyQp6hHChHbqGIskUgq2jDNlmx2TisVmsgoaqtcAgs2oYDFh5lO0NlLxE23mdo7MrotSWC9NBAFnte0gr9vGQOwdh1h2hete10B8xnq3d6GCk7yXuVOXOKl2gLlmPrW5adGq53bJVNgLQmQcVrsoH66Ambv4nMpLBzaGqfJhFA62kZx9yiqvuu+DRMynzHtKLol1vdb3CoiYimV6MAheGtX3d0sBgDYBqYQeIiQmlLxJnM8mlNqa03psYdmBH3UgG4HgLh5HdJ7vWnJuEMtbaP0xTAWTHGgP51Q7w0oiAOPYcPR2DF9ltOqbfgZ-9xm8ClubVB1NVmOPeV2rZn+9nYDuc+ZdN1wJ6WqaZYp11BrqNAr7fg+jQ9EVAA