#!/usr/bin/env bash
# validate-promql.sh — M4: valida todas as queries PromQL antes de aplicar nos dashboards
# Uso: ./validate-promql.sh [http://prometheus:9090]

set -euo pipefail

PROMETHEUS="${1:-http://localhost:9090}"
FAILED=0
PASSED=0

validate() {
  local label="$1"
  local query="$2"
  local result
  result=$(curl -sf "$PROMETHEUS/api/v1/query" \
    --data-urlencode "query=$query" \
    --max-time 10 2>/dev/null | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('status','error'))" 2>/dev/null || echo "error")
  if [ "$result" = "success" ]; then
    echo "  OK   $label"
    PASSED=$((PASSED + 1))
  else
    echo "  FAIL $label"
    echo "       Query: $query"
    FAILED=$((FAILED + 1))
  fi
}

echo "=== Validando queries PromQL contra $PROMETHEUS ==="
echo ""

echo "[Dashboard 1] Hardware Overview"
validate "CPU usage rate"          'rate(process_cpu_usage[1m]) * 100'
validate "Memory used bytes"       'jvm_memory_used_bytes{area="heap"}'
validate "Memory max bytes"        'jvm_memory_max_bytes{area="heap"}'
validate "Disk read rate"          'rate(disk_read_bytes_total[5m])'
validate "Network receive rate"    'rate(http_server_requests_seconds_count[5m])'

echo ""
echo "[Dashboard 2] API Health"
validate "P99 latency"             'histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))'
validate "P95 latency"             'histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))'
validate "P50 latency"             'histogram_quantile(0.50, rate(http_server_requests_seconds_bucket[5m]))'
validate "Request rate total"      'rate(http_server_requests_seconds_count[5m])'
validate "Error rate 5xx"          'sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))'
validate "Request count by status" 'sum by (status) (rate(http_server_requests_seconds_count[5m]))'

echo ""
echo "[Dashboard 3] JVM Internals"
validate "Heap used"               'jvm_memory_used_bytes{area="heap"}'
validate "Heap max"                'jvm_memory_max_bytes{area="heap"}'
validate "Non-heap used"           'jvm_memory_used_bytes{area="nonheap"}'
validate "GC pause avg"            'rate(jvm_gc_pause_seconds_sum[5m]) / rate(jvm_gc_pause_seconds_count[5m])'
validate "Live threads"            'jvm_threads_live_threads'
validate "Daemon threads"          'jvm_threads_daemon_threads'

echo ""
echo "[Dashboard 4] Operational / SLO"
validate "SLO availability 24h"   '(1 - sum(rate(http_server_requests_seconds_count{status=~"5.."}[24h])) / sum(rate(http_server_requests_seconds_count[24h]))) * 100'
validate "Avg P99 1h"             'avg(histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[1h])))'
validate "Services up"            'up'

echo ""
echo "============================================"
echo "  Resultado: ${PASSED} OK  |  ${FAILED} FAIL"
echo "============================================"

if [ "$FAILED" -gt 0 ]; then
  echo ""
  echo "ATENÇÃO: Corrigir queries acima ANTES de aplicar dashboards no Grafana (M4)"
  exit 1
fi

echo ""
echo "Todas as queries válidas — prosseguir com provisionamento Grafana"
exit 0
