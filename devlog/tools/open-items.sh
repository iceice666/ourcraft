#!/usr/bin/env bash
# List unchecked items from "Open questions / blockers" and "Next session"
# sections across every devlog entry under devlog/YYYYMMDD/.
#
# Usage: devlog/tools/open-items.sh

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
devlog_dir="$repo_root/devlog"

if [[ ! -d "$devlog_dir" ]]; then
    echo "no devlog/ directory at $devlog_dir" >&2
    exit 1
fi

shopt -s nullglob
files=("$devlog_dir"/[0-9]*/*.md)
shopt -u nullglob

if (( ${#files[@]} == 0 )); then
    echo "no devlog entries found"
    exit 0
fi

found_any=0
for file in "${files[@]}"; do
    rel="${file#$repo_root/}"
    output="$(awk -v relpath="$rel" '
        BEGIN { section = ""; printed_header = 0; last_section = "" }
        /^## / {
            if ($0 ~ /^## Open questions \/ blockers/) { section = "Open questions / blockers"; next }
            else if ($0 ~ /^## Next session/)          { section = "Next session"; next }
            else                                        { section = ""; next }
        }
        section != "" && /^- \[ \]/ {
            if (!printed_header) {
                print relpath
                printed_header = 1
            }
            if (section != last_section) {
                print "  " section ":"
                last_section = section
            }
            line = $0
            sub(/^- \[ \] /, "", line)
            print "    - " line
        }
    ' "$file")"
    if [[ -n "$output" ]]; then
        (( found_any > 0 )) && echo
        printf '%s\n' "$output"
        found_any=1
    fi
done

if (( found_any == 0 )); then
    echo "no open items found"
fi
