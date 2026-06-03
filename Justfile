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

# Verify the dev environment has everything needed to build and commit.
check-env:
    #!/usr/bin/env bash
    set -u
    bad=0
    say_ok()   { printf '  ✅  %s\n' "$1"; }
    say_bad()  { printf '  ❌  %s\n' "$1"; bad=1; }

    echo "🔎  Checking dev environment…"

    # Java 21+
    if command -v java >/dev/null 2>&1; then
        ver=$(java -version 2>&1 | awk -F'"' '/version/ {print $2; exit}')
        major=${ver%%.*}
        if [ "${major:-0}" -ge 21 ] 2>/dev/null; then
            say_ok "java ${ver} (>= 21)"
        else
            say_bad "java ${ver} (need 21+)"
        fi
    else
        say_bad "java not on PATH (need Java 21+)"
    fi

    # Gradle: prefer ./gradlew, fall back to system gradle
    if [ -x ./gradlew ]; then
        say_ok "./gradlew present"
    elif command -v gradle >/dev/null 2>&1; then
        gver=$(gradle --version 2>/dev/null | awk '/^Gradle/ {print $2; exit}')
        say_ok "system gradle ${gver}"
    else
        say_bad "no ./gradlew and no system gradle on PATH"
    fi

    # Python 3 — used by .claude/hooks/*.sh
    if command -v python3 >/dev/null 2>&1; then
        say_ok "python3 $(python3 --version 2>&1 | awk '{print $2}')"
    else
        say_bad "python3 not on PATH (needed by Claude pre-commit hooks)"
    fi

    # Git
    if command -v git >/dev/null 2>&1; then
        say_ok "git $(git --version | awk '{print $3}')"
    else
        say_bad "git not on PATH"
    fi

    echo
    if [ "$bad" -ne 0 ]; then
        echo "❌  Environment incomplete — install the missing tools above."
        echo "    Nix/direnv users: \`nix develop\` should set most of these up."
        exit 1
    fi
    echo "✅  Environment ready."
