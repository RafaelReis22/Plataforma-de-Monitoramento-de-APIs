export interface CpuCore {
  coreId: number;
  usage: number;
}

export interface HardwareMetrics {
  cpuUsagePercentage: number;
  cpuCores: number;
  cpuLoadAverage: number[];
  memoryUsedGb: number;
  memoryTotalGb: number;
  memoryUsagePercentage: number;
  swapUsedGb: number;
  swapTotalGb: number;
  diskUsedGb: number;
  diskTotalGb: number;
  diskUsagePercentage: number;
  networkRxKbps: number;
  networkTxKbps: number;
  cores: CpuCore[];
}

export interface EndpointMetric {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  path: string;
  count: number;
  p95Ms: number;
  errorRate: number;
  status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
}

export interface ApiHealthMetrics {
  totalRequests: number;
  requestsPerSecond: number;
  p50LatencyMs: number;
  p90LatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
  errorRatePercentage: number;
  statusDistribution: Record<string, number>;
  endpoints: EndpointMetric[];
}

export interface JvmMetrics {
  heapUsedMb: number;
  heapMaxMb: number;
  heapCommittedMb: number;
  nonHeapUsedMb: number;
  activeThreads: number;
  peakThreads: number;
  daemonThreads: number;
  gcPauseTotalMs: number;
  gcCollectionsCount: number;
  loadedClasses: number;
  jvmUptimeHours: number;
  javaVersion: string;
}

export interface AlertItem {
  id: string;
  name: string;
  service: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  status: 'FIRING' | 'RESOLVED';
  condition: string;
  currentValue: string;
  triggeredAt: string;
}

export interface SyntheticProbe {
  name: string;
  url: string;
  status: 'UP' | 'DEGRADED' | 'DOWN';
  latencyMs: number;
  sslDaysRemaining: number;
  uptime30d: number;
}

export interface UserProfile {
  username: string;
  role: string;
  tenantId: string;
  email: string;
  organization: string;
}
