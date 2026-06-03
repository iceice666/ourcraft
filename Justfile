set shell := ["bash", "-eu", "-o", "pipefail", "-c"]

gradle := "./gradlew"

default:
    @just --list

# Build the project.
build:
    {{gradle}} build

# Run all tests.
test:
    {{gradle}} test

# Clean generated Gradle output.
clean:
    {{gradle}} clean

# Run the app. Pass args with: just run "surprise-me"
run args="":
    {{gradle}} run --args="{{args}}"

# Run one JUnit test class or pattern.
single-test pattern:
    {{gradle}} test --tests "{{pattern}}"

# Show the Gradle dependency report.
dependencies:
    {{gradle}} dependencies
