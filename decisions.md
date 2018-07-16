### JavaFX framework

- *TornadoFX*

There was the initial intent to use the direct JavaFX API, but also searched for the available frameworks.
And found TornadoFX, I liked the approach there how you describe the UI (in the code, and not in FXML), also wanted to 
study Kotlin, which was the perfect match.

- *Alternatives*: Direct usage of the JavaFX API with UI design in FXML.
- *Risks*: Lots of new topics at once, which might require the steep learning curve. Although the TornadoFX 
documentation I found to be the good one with the examples, and covering various topic and being actively developed.

### Lang

- *Kotlin*

Kotlin was more like a by product of choosing the TornadoFX, but the desirable one.

- *Alternatives*: Java
- *Risks*: Being the JetBrains product IDE support should be good, although the reference of the language itself should
be enough to feel comfortable with the language.


### Build Tool

- *Gradle*

Gradle was chosen like the natural choice for the kotlin projects, and using the Kotlin scripting version of the Gradle.

- *Alternatives*: Maven
- *Risks*: So far my experience with the Gradle has mixed feelings, as it works when it works, but if something doesn't
work it then takes lots of efforts to make it work.

## Mon 16 Jul, 2018
- Kotlin testing support/frameworks is terrible. 

Spek requires the crazy Gradle setup (although to be fair Maven setup 
is way simpler, which probably suggest I should use Maven instead). Still Spek is far from the elegance and power of 
Groovy Spock.

Official testing framework kotlintest while requires simpler setup in Gradle, IDEA lacks of support to run individual 
tests, as well as the errors report in CLI is useless. And lack of explicit "setup/when/then" like in Spock is 
unfortunate.

- The latest version of Gradle (1.4.8) fails to build the project using the latest version of Kotlin (2.5.1), which is
probably the failure of Gradle rather than Kotlin, which adds more concerns of using Gradle for the future projects.

- TornadoFX so far the only chosen tool which proves to be the good fit for the task.  