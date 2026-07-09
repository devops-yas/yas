#!/usr/bin/env bash
set -euo pipefail

# K3s users can run this first if kubectl is not on PATH:
# alias kubectl='sudo k3s kubectl'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MANIFEST_DIR="${ROOT_DIR}/k8s/service-mesh/linkerd"
NAMESPACE="${NAMESPACE:-yas}"
DEPLOYMENT="${DEPLOYMENT:-backoffice-bff}"

kubectl apply -f "${MANIFEST_DIR}/backoffice-bff-server.yaml"
kubectl apply -f "${MANIFEST_DIR}/backoffice-bff-authorization-policy.yaml"
kubectl apply -f "${MANIFEST_DIR}/backoffice-bff-serviceprofile-retry.yaml"
kubectl apply -f "${MANIFEST_DIR}/curl-test-pod.yaml"
kubectl apply -f "${MANIFEST_DIR}/retry-demo.yaml"

if ! kubectl -n "${NAMESPACE}" get deploy "${DEPLOYMENT}" \
  -o jsonpath='{.spec.template.metadata.annotations.linkerd\.io/inject}' | grep -qx enabled; then
  echo "Adding Linkerd pod-template injection annotation to ${DEPLOYMENT}."
  kubectl -n "${NAMESPACE}" patch deploy "${DEPLOYMENT}" --type='merge' \
    -p '{"spec":{"template":{"metadata":{"annotations":{"linkerd.io/inject":"enabled"}}}}}'
  kubectl -n "${NAMESPACE}" rollout restart deploy "${DEPLOYMENT}"
fi

kubectl -n "${NAMESPACE}" rollout status deploy "${DEPLOYMENT}"
kubectl -n "${NAMESPACE}" get pods | grep "${DEPLOYMENT}"
linkerd -n "${NAMESPACE}" check --proxy
linkerd viz stat deploy -n "${NAMESPACE}"
