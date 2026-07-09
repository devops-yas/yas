# Linkerd service mesh cho YAS

README này mô tả cách triển khai phần Service Mesh nâng cao cho YAS bằng Linkerd trên K3s.

## Prerequisites

- K3s cluster đang chạy.
- `kubectl` truy cập được cluster.
- Linkerd CLI đã được cài đặt.
- Linkerd control plane đã được cài đặt.
- Linkerd Viz đã được cài đặt.
- Namespace ứng dụng là `yas`.
- Deployment `backoffice-bff` tồn tại trong namespace `yas`.

Nếu dùng K3s và máy không có `kubectl`, có thể dùng:

```bash
alias kubectl='sudo k3s kubectl'
```

## Manifest chính

- `k8s/gitops/base/09-backoffice-bff.yaml`: thêm `linkerd.io/inject: enabled` dưới `spec.template.metadata.annotations` cho `backoffice-bff`.
- `k8s/service-mesh/linkerd/backoffice-bff-server.yaml`: định nghĩa Linkerd `Server` cho port `80`.
- `k8s/service-mesh/linkerd/backoffice-bff-authorization-policy.yaml`: policy chỉ cho phép selected meshed client.
- `k8s/service-mesh/linkerd/backoffice-bff-serviceprofile-retry.yaml`: retry policy demo/template cho HTTP 5xx.
- `k8s/service-mesh/linkerd/curl-test-pod.yaml`: allowed và denied curl clients.
- `k8s/service-mesh/linkerd/retry-demo.yaml`: controlled retry demo với service `linkerd-retry-demo` và client `linkerd-retry-client`.

Ghi chú về manifest nguồn: Argo CD app trong repo trỏ tới `k8s/gitops/overlays/dev`, overlay này dùng `k8s/gitops/base`. Cụm lab hiện được ghi nhận chạy namespace `yas`, nên manifest policy dùng namespace `yas`.

## Triển khai

Từ root repository:

```bash
./scripts/service-mesh/apply-linkerd-yas-policy.sh
```

Script sẽ:

- Apply các manifest Linkerd policy/test.
- Kiểm tra annotation inject của `backoffice-bff`.
- Chỉ restart deployment `backoffice-bff` nếu thiếu annotation inject.
- Chạy các lệnh verify cơ bản.

Nếu muốn apply thủ công:

```bash
kubectl apply -f k8s/service-mesh/linkerd/backoffice-bff-server.yaml
kubectl apply -f k8s/service-mesh/linkerd/backoffice-bff-authorization-policy.yaml
kubectl apply -f k8s/service-mesh/linkerd/backoffice-bff-serviceprofile-retry.yaml
kubectl apply -f k8s/service-mesh/linkerd/curl-test-pod.yaml
```

## Verify

```bash
kubectl get pods -n linkerd
kubectl get pods -n linkerd-viz
kubectl -n yas get pods | grep backoffice-bff
linkerd -n yas check --proxy
linkerd viz stat deploy -n yas
```

Kết quả mong đợi:

- Linkerd control plane Running.
- Linkerd Viz Running.
- `backoffice-bff` chạy `2/2 Running`.
- `linkerd -n yas check --proxy` pass.
- `linkerd viz stat deploy -n yas` có `MESHED 1/1`, success rate, RPS và latency.

## Authorization test

Allowed client:

```bash
kubectl -n yas exec deploy/backoffice-bff-allowed-curl -c curl -- \
  curl -i --max-time 5 http://backoffice-bff/login
```

Observed: request không bị Linkerd policy chặn và nhận `HTTP/1.1 200 OK`.

Denied client:

```bash
kubectl -n yas exec deploy/backoffice-bff-denied-curl -c curl -- \
  curl -i --max-time 5 http://backoffice-bff/login
```

Observed: request bị Linkerd policy chặn với `HTTP/1.1 403 Forbidden`.

## Retry test

`backoffice-bff` chưa có endpoint HTTP 500 ổn định trong repository, nên manifest này cung cấp controlled retry demo để thu bằng chứng thật:

```bash
k8s/service-mesh/linkerd/retry-demo.yaml
```

Chạy test:

```bash
kubectl -n yas exec deploy/linkerd-retry-client -c curl -- \
  curl -i --max-time 5 http://linkerd-retry-demo/flaky
kubectl -n yas logs deploy/linkerd-retry-demo --tail=30
linkerd viz stat deploy -n yas
```

Expected:

- Curl cuối cùng trả HTTP 200.
- Log server có ít nhất hai dòng: request đầu `500`, request retry `200`.
- `linkerd viz stat deploy -n yas` hiển thị traffic cho retry demo.

Observed:

- Curl trả `HTTP/1.0 200 OK`.
- Body trả `retry succeeded after request #2`.
- Log server có `flaky request #1 -> 500` và `flaky request #2 -> 200`.

## Rollback

Rollback policy/test manifests:

```bash
./scripts/service-mesh/rollback-linkerd-yas-policy.sh
```

Mặc định script không xóa annotation inject và không gỡ Linkerd khỏi cluster.

Nếu muốn remove annotation inject khỏi `backoffice-bff`:

```bash
REMOVE_INJECTION=true ./scripts/service-mesh/rollback-linkerd-yas-policy.sh
```

Rollback không xóa YAS workloads, không xóa Linkerd control plane và không xóa Linkerd Viz.

## Troubleshooting

- `kubectl` không tồn tại trên K3s: dùng `sudo k3s kubectl` hoặc alias `kubectl='sudo k3s kubectl'`.
- Dashboard làm trình duyệt treo: dùng CLI evidence như `linkerd viz stat deploy -n yas`.
- Nếu lệnh `stat` cũ bị deprecated: dùng `linkerd viz stat`.
- Nếu allowed curl bị deny: kiểm tra pod đã inject `2/2 Running` và service account đúng là `backoffice-bff-allowed-curl`.
- Nếu denied curl không bị deny: kiểm tra `AuthorizationPolicy` đã target đúng `Server` và `Server` chọn đúng pod label `app=backoffice-bff`.
