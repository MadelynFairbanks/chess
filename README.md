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

## Chess Server Design (Phase 2)
[View Sequence Diagram](https://www.sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQn1igTwGcAHUJNAcxgAYA6ANkwqggFdiYAlFCpQsFFEyJUGGAFoAfDADKggG6CAXDAAKAeRkAVGAHpWhQTAAUBwWmABbFABoYpQoQDu0ACZ2Ul4EgQBKTHJQirBSXDx8AlAAEsBorgjKMAAWsfEo3Lz8gsZQKACOrCj8-hkRgjFxCSHSAKqGUIHySCAoKrmZkXXZuQVFYP5dDQrNKBK19QAiAILqKhQoYIMAQviDFtam9esoA5MzEqGDjSMqaKwICJhHwy1jMIPTszAgucACg5uCu4LHt6FTrDASUeKheKDeKABQK0EAA1uhPlB-FDgftxId6r9WjBIPC0FdMTdRujpKUstFUlUVGTItxCOcwMZceg7GYoNsSuFyRU0iFQo1EgApGTqAByMFyJAgaEMAQURhJ8GQuBUACZqNQYOoANJhDqCOkMnBxbDMNgcRDgoQiXB3AVQFQTACiABknVonXpXAAjOVBBXSS3ACmVRIpUNBqDGfyRnlVO6RrGghLBqYXaPCFNDIIjO4PGbJq2DQgZxNEu4AcSsKBBzyzVesJZjWaxdxRtZAWZheKbmatrcVscpiRkrBALUchoQWCHob50ntKmFYolRWI0tl9oOgeVGDVGq12uNrlNLHYMBdEF4+JtYn58odak0Ol0hkcSGlJjZ2zsDmcbn8LdQkva84zDYcQPIHJ8kKYpMEgtAwOqe5CRzFoVAQK9yA+HpYP6AkfnLDFBFreZFnqFY1mrREOQIqBHm3FDCLQ7EHjeYA6KTGB5GAZBXAhexgEcFwoBPa4WLbQFUSeMEIRRbsETZZEpIYxVxKadCcThdBOKI4CsMQ4dHwQqdGWZNBWS2at-AQpDGMXGBl3FSV1xlFA-WCRjbzAfdNR1C8DNM49T3NAKKDYGddzAO0H0dV13U9V8ikID80BMYApIUtBAIfO4QIipCVHDNJ8sBaDejg0qwDs-4pK4zDwsBHCYL6ZSgQHaR2wLGBXBQBIBHk7S0GMDLoSGtrpMYlEuNHcdkswaa9OkKrCrCiKgpWoz7NixyRWctcN3coDQm83zDzWwEgvQE8mDPDgXQiGAGyKTNRGi+9-UfCt3T0Chq3SzLxo8gMLwiZ7CFW4qEge-hwfKvCbLB6sIa20JnvqpHG3h1rMHRojOpU7qeL4uTAbxEayfQCbVLR6suO-atcbp-GnurUiFjTBA4f8Z6aekPGWJUGGwAAHl59jJCZ6wB2AzGilW4XwY2uWUbnbbPqXPbVylNzgfnJU3rO-zFeRq6TVu0K4FeARWesV7bQ+4IVA0bRfv+imxrxOw-usUVrL1hNrZQZ7IeHK3wQEZ7sbg8OIRD1H+eZwXniDqPcJxgWNOJAmgVrYmkH4gbKeG0akiy6m0Vp6WiRUBm7cz3Mq5rbrZMj6ifZQP3rB5tnK8T6vk47gBJCYpZQGXA1T6tVtjtvrFM4xh4mGMp+sGrHaFLWXMOgPByio3dVn4PqzNm6zXPQUIHIW33O8mKNbUGoXw7gHPZZGAl9-BBgHwQQ4AgTCSJd7SEvuQeOc4irDlAWgNOLU4LQPAbySsScs4qAAFZXxgdRdOcEG5-BztJFQ+dC6QmLh7MuQMurqEYng7Edd3K0OQdYdmYBnorBHovasI8e7ML7jfLi4swAcVoaCJIKAQCwhgEgAAZs8AB0ApGEBgMAHiPhgDegSGPDqN9azsBIc9AA6kgIEqhv6-yjJ-ewZi-7yKgJZcw-teZ8JEbIMcE5CBaKWo5TBiCqTeLASfIoRoEHT1RhvR8TltauU3LlPehsYDqj8rqEJ88gnTmCkAA)

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
