cd src/
javac -cp jade.jar $(find . -name '*.java')
java -cp jade.jar:.: jade.Boot -gui -agents "env:EnvironmentAgent;nav:NavigatorAgent;spel:SpeleologistAgent"
