#!/usr/bin/env bash

# Source this file in your shell:
#   source scripts/wiki-aliases.sh
# Optional fallback helpers; Codex/Cursor prompt flow is primary in M1.

alias wiki-ingest='bash scripts/wiki-ops.sh ingest'
alias wiki-lint='bash scripts/wiki-ops.sh lint'
alias wiki-query='bash scripts/wiki-ops.sh query'

echo "Loaded aliases: wiki-ingest, wiki-lint, wiki-query"

