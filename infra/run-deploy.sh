#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

source "$SCRIPT_DIR/deploy.env"

cd "$SCRIPT_DIR"
ansible-playbook deploy-yc-fatjar.yaml