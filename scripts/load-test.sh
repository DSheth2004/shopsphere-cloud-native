#!/usr/bin/env bash
set -euo pipefail
k6 run load-tests/order-flow.js
