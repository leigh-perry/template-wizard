
# Template Wizard
## About
This simple app creates a replica of a directory performing string substitution. This is
enough to create a new Java, Kotlin, Scala etc project based on an existing app (or a
skeleton template app). Or it be used for any other banal clone-transform task that can
be expressed as a series of string substitutions.

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
