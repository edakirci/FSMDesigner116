![Java](https://img.shields.io/badge/Java-17-orange)
![CLI](https://img.shields.io/badge/Interface-CommandLine-blue)
![FSM](https://img.shields.io/badge/Concept-FiniteStateMachine-green)
![Git](https://img.shields.io/badge/VersionControl-Git-darkgreen)

# FSM Designer

This project is a **Finite State Machine (FSM) designer and simulator** developed for the **SE116 course (Spring 2025)**.

The application allows users to design, configure, and execute deterministic finite state machines through a command-line interface. Users can define symbols, states, transitions, and execute input strings to determine whether they are accepted by the FSM.

The program processes commands entered by the user, builds an FSM dynamically, and simulates its execution on input strings.

---

# Features

- Create and manage **FSM symbols**
- Define **states, initial state, and final states**
- Add and modify **state transitions**
- Execute FSM with input strings
- Print FSM configuration
- Load FSM definitions from files
- Compile FSM definitions into binary files
- Log commands and program responses to files
- Clear FSM configuration

---

# FSM Components

A deterministic finite state machine consists of the following elements:

- **Symbols** → Input characters processed by the FSM
- **States** → Named states representing machine configurations
- **Initial State** → The starting state of the machine
- **Final States** → Accepting states
- **Transitions** → State changes based on input symbols

During execution, the FSM reads the input string from **left to right** and updates its state according to defined transitions. The input is accepted if the machine ends in a final state. :contentReference[oaicite:1]{index=1}

---

# Technologies Used

- Java
- Command Line Interface (CLI)
- Git Version Control
- Object-Oriented Programming

---

# Example Execution

## Example command session:
### ? SYMBOLS 0 1 2 3;
### ? INITIAL-STATE Q0;
### ? FINAL-STATES Q2;
### ? STATES Q1;
### ? TRANSITIONS 0 Q0 Q0, 1 Q0 Q1;
### ? PRINT;
### ? EXECUTE 123;

## Output example:
Q0 Q1 Q0 Q0 NO

---

# Project Structure

## FSMDesigner
## │
## ├── src/ # application source code
## ├── .gitignore
## └── README.md

---

# How to Run

1. Clone the repository

```bash
git clone https://github.com/edakirci/FSMDesigner.git
```
2. Compile the program
```bash
javac *.java
```
3. Run the program
```bash
java -jar fsm.jar
```
4. You can also start the program with a command file:
```bash
java -jar fsm.jar commands.txt
```
---

# Example Commands
## SYMBOLS
## STATES
## INITIAL-STATE
## FINAL-STATES
## TRANSITIONS
## PRINT
## EXECUTE
## LOAD
## COMPILE
## CLEAR
## EXIT
---
# Application Screenshots
## Main Screen
![Main Screen](screenshots/main.png)
---
# Course Information

## Course: SE116 – Introduction to Programmıng II
## Semester: Spring 2025







