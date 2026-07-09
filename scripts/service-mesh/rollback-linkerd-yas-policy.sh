#!/usr/bin/env bash
set -euo pipefail

# K3s users can run this first if kubectl is not on PATH:
# alias kubectl='sudo k3s kubectl'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MANIFEST_DIR="${ROOT_DIR}/k8s/service-mesh/linkerd"
NAMESPACE="${NAMESPACE:-yas}"
DEPLOYMENT="${DEPLOYMENT:-backoffice-bff}"
REMOVE_INJECTION="${REMOVE_INJECTION:-false}"

kubectl delete -f "${MANIFEST_DIR}/curl-test-pod.yaml" --ignore-not-found
kubectl delete -f "${MANIFEST_DIR}/retry-demo.yaml" --ignore-not-found
kubectl delete -f "${MANIFEST_DIR}/backoffice-bff-serviceprofile-retry.yaml" --ignore-not-found
kubectl delete -f "${MANIFEST_DIR}/backoffice-bff-authorization-policy.yaml" --ignore-not-found
kubectl delete -f "${MANIFEST_DIR}/backoffice-bff-server.yaml" --ignore-not-found

if [[ "${REMOVE_INJECTION}" == "true" ]]; then
  echo "Removing Linkerd injection annotation from ${DEPLOYMENT} and restarting only that deployment."
  kubectl -n "${NAMESPACE}" patch deploy "${DEPLOYMENT}" --type=json \
    -p='[{"op":"remove","path":"/spec/template/metadata/annotations/linkerd.io~1inject"}]' || true
  kubectl -n "${NAMESPACE}" rollout restart deploy "${DEPLOYMENT}"
  kubectl -n "${NAMESPACE}" rollout status deploy "${DEPLOYMENT}"
else
  echo "Leaving Linkerd injection annotation in place. Set REMOVE_INJECTION=true to remove it."
fi
