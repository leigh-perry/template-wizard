
# Template Wizard
## About
This simple app creates a replica of a directory performing string substitution. This is
enough to create a new Java or Scala project based on an existing app (or a
skeleton template app).

## Build
```
sbt assembly
```

## Usage
```
java -jar target/scala-2.12/template-wizard-assembly-1.0.0.jar \
    --template-dir /Users/lperry/dev/lp/console-app-template \
    --destination-dir /Users/lperry/temp \
    --substitutions "SomeApplication=AnotherApp,SomeFifo=MpscQueue,ContextName=CorrelationId"
```
