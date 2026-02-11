# Overview
Yuan is similar to a simplified version of IntelliJ IDEA, supporting both Java and Python languages. It has currently only been tested on the Windows operating system. 

It supports basic functionalities such as editing, compiling, running, packaging, installing to repositories, and adding custom plugins for standard Maven projects. 

It also supports Python editing, running, and other related features.

The detailed operation process of the yuan-platform can be found at yuan/yuan.docx or yuan/yuan-platform/target/artifacts/yuan/yuan.pdf

---
# Yuan Open Source Repository

This repository is the open-source part of the yuan IDEs codebase.
It also serves as the basis for [yuan development](https://github.com/YuanIdea/yuan).

These instructions will help you build and run open source parts of yuan.

___
## Getting the Source Code

This section will guide you through getting the project sources and help avoid common issues in git config and other steps before opening it in the IDE.

#### Prerequisites
- [Git](https://git-scm.com/) installed
- ~100MB free disk space

#### Clone Main Repository

Yuan open source repository is available from the [GitHub repository](https://github.com/YuanIdea/yuan), which can be cloned or downloaded as a zip file (based on a branch) into `<IDEA_HOME>`. 
The **master** (_default_) branch contains the source code which will be used to create the next major version of all yuan IDEs. 

Alternatively, follow the steps below in a terminal:

   ```
   git clone https://github.com/YuanIdea/yuan.git
   cd yuan
   ```
---
## Building yuan IDEA
These instructions will help you build yuan IDEA from source code, which is the basis for yuan-platform development.

### Build Configuration Steps
1.**JDK Setup**

 - Download and install [jdk-11](https://www.oracle.com/java/technologies/downloads/archive/). 
 - Add JAVA_HOME to the environment variables.

2.**Run the yuan IDEA Application from Source**

Using the latest yuan.exe, click '**File | Open**', select the yuan-platform directory.

 - click '**Help | Install dependency libraries**'

 - To run the yuan IDEA that was built from source, choose '**Run | Run**' from the main menu. This will use the preconfigured run configuration `IDEA`.

---
# Other
#### License

This project is licensed under the Apache License 2.0.
This project does not open-source the following patented algorithms. The patent clause in the license does not apply to the following patented technologies:
Granted Patent Authorization Announcement No.: CN117404285B
Patent Application No. (Pending): CN202510893336.3

#### Important Notes
The main program code is licensed under Apache-2.0

The included Docking Frames library is licensed under LGPL-2.1

Other dependencies are licensed under BSD-3-Clause or Apache-2.0