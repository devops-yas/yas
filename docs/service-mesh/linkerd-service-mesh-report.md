# Báo cáo Service Mesh với Linkerd cho YAS

## 1. Tổng quan yêu cầu

Yêu cầu Service Mesh nâng cao của đồ án cần chứng minh các năng lực chính:

- Bật TLS/mTLS giữa các service chạy trên Kubernetes.
- Có công cụ quan sát topology/flow/metrics.
- Có kịch bản kiểm thử retry khi service trả HTTP 500.
- Có kịch bản kiểm thử authorization policy: service được phép và không được phép kết nối.
- Có YAML manifest, bằng chứng log/screenshot, test plan, hướng dẫn triển khai và rollback.

## 2. Công nghệ sử dụng

Nhóm sử dụng Linkerd làm service mesh cho cụm K3s và ứng dụng YAS.

- Linkerd control plane: quản lý proxy, identity, policy và mTLS.
- Linkerd sidecar proxy: chạy cùng workload được inject.
- Linkerd Viz: cung cấp metrics, stat, tap và dashboard quan sát traffic.
- Prometheus đi kèm Linkerd Viz: lưu metrics phục vụ quan sát.

## 3. Vì sao dùng Linkerd Viz thay Kiali

Đề bài gợi ý Kiali vì Kiali thường được dùng cùng Istio. Trong triển khai này, service mesh được chọn là Linkerd, nên công cụ tương đương đúng hệ sinh thái là Linkerd Viz.

Linkerd Viz hỗ trợ:

- Xem metrics theo deployment/service.
- Xem success rate, RPS và latency.
- Dùng `linkerd viz stat` để thu thập bằng chứng CLI.
- Dùng `linkerd viz tap` hoặc dashboard để quan sát flow request.

Vì vậy, Linkerd Viz được dùng thay Kiali để đáp ứng phần topology/flow observation của yêu cầu.

## 4. Những gì đã triển khai

Trạng thái cụm đã hoàn tất thủ công:

- Linkerd control plane đã được cài đặt.
- Linkerd Viz và Prometheus đã được cài đặt.
- `linkerd check` pass.
- `linkerd viz check` pass.
- Namespace ứng dụng: `yas`.
- Deployment `backoffice-bff` đã được inject thủ công và pod chạy `2/2 Running`.
- `linkerd -n yas check --proxy` pass.
- `linkerd viz stat deploy -n yas` hiển thị `backoffice-bff` với `MESHED 1/1`, `SUCCESS 100.00%`, RPS và latency.

Các thay đổi trong repository:

- Thêm annotation `linkerd.io/inject: enabled` vào pod template của `backoffice-bff` để việc inject sidecar có thể tái lập bằng manifest.
- Tạo `Server` Linkerd cho port HTTP `80` của `backoffice-bff`.
- Tạo `MeshTLSAuthentication` và `AuthorizationPolicy` demo cho phép client được chỉ định gọi vào `backoffice-bff`.
- Tạo `ServiceProfile` demo cho retry route của `backoffice-bff` khi HTTP 5xx.
- Tạo controlled retry demo `linkerd-retry-demo` để có endpoint `/flaky` trả HTTP 500 lần đầu và HTTP 200 ở lần retry.
- Tạo allowed/denied curl clients để kiểm thử authorization.
- Tạo script apply và rollback policy.

## 5. Safe incremental rollout

Triển khai được giới hạn để giảm rủi ro:

- Chỉ inject workload stateless `backoffice-bff`.
- Không inject toàn bộ namespace `yas`.
- Không chỉnh sửa PostgreSQL, Redis, Kafka, Elasticsearch, Zookeeper hoặc Keycloak.
- Không xóa hoặc recreate workload YAS khi apply/rollback policy.

## 6. mTLS

Linkerd tự động cấp identity cho các workload đã được inject và bật mTLS giữa các meshed workloads. Khi `backoffice-bff` và client test đều có sidecar proxy, traffic giữa chúng đi qua Linkerd proxy và được bảo vệ bằng mTLS.

Manifest authorization dùng `MeshTLSAuthentication` để chỉ cho phép identity:

```text
backoffice-bff-allowed-curl.yas.serviceaccount.identity.linkerd.cluster.local
```

Client dùng service account khác, ví dụ `backoffice-bff-denied-curl`, sẽ bị chặn bởi policy.

## 7. Evidence commands

Chạy các lệnh sau để thu thập bằng chứng:

```bash
kubectl get pods -n linkerd
kubectl get pods -n linkerd-viz
kubectl -n yas get pods | grep backoffice-bff
linkerd -n yas check --proxy
linkerd viz stat deploy -n yas
```

Kết quả mong đợi:

- Pod Linkerd control plane ở trạng thái `Running`.
- Pod Linkerd Viz ở trạng thái `2/2 Running` hoặc trạng thái ready tương ứng.
- Pod `backoffice-bff` ở trạng thái `2/2 Running`.
- `linkerd -n yas check --proxy` pass.
- `linkerd viz stat deploy -n yas` hiển thị `backoffice-bff`, `MESHED 1/1`, `SUCCESS 100.00%`, RPS và latency.

## 8. Checklist screenshot

Nên chụp các màn hình/log sau:

- `linkerd check` pass.
- `linkerd viz check` pass.
- `kubectl get pods -n linkerd`.
- `kubectl get pods -n linkerd-viz`.
- `kubectl -n yas get pods | grep backoffice-bff` hiển thị `2/2 Running`.
- `linkerd -n yas check --proxy` pass.
- `linkerd viz stat deploy -n yas` có `backoffice-bff`, `MESHED 1/1`, success rate, RPS, latency.
- Kết quả curl từ allowed client.
- Kết quả curl từ denied client.
- Nếu có endpoint HTTP 500 ổn định, chụp log/stat chứng minh retry.

## 9. Kết quả kiểm thử thực tế

Các kiểm thử CLI đã chạy trên namespace `yas`:

- `linkerd check`: pass.
- `linkerd viz check`: pass.
- `linkerd -n yas check --proxy`: pass.
- `kubectl -n yas get pods | grep backoffice-bff`: `backoffice-bff` chạy `2/2 Running`.
- `linkerd viz stat deploy -n yas`: hiển thị `backoffice-bff`, curl clients và retry demo đều `MESHED 1/1`.
- Authorization allow: `backoffice-bff-allowed-curl` gọi `http://backoffice-bff/login` nhận `HTTP/1.1 200 OK`.
- Authorization deny: `backoffice-bff-denied-curl` gọi `http://backoffice-bff/login` nhận `HTTP/1.1 403 Forbidden`.
- Retry demo: `linkerd-retry-client` gọi `http://linkerd-retry-demo/flaky` nhận `HTTP/1.0 200 OK` với body `retry succeeded after request #2`.
- Log retry demo có `flaky request #1 -> 500` và `flaky request #2 -> 200`.

Ghi chú: request allow tới `/` không trả response trong 5 giây do hành vi ứng dụng, nên bằng chứng allow ổn định dùng endpoint `/login`.

## 10. Demo script cho thuyết trình

1. Giới thiệu: YAS dùng Linkerd làm service mesh trên K3s.
2. Chứng minh control plane:

   ```bash
   linkerd check
   linkerd viz check
   ```

3. Chứng minh chỉ inject `backoffice-bff`:

   ```bash
   kubectl -n yas get pods | grep backoffice-bff
   linkerd -n yas check --proxy
   ```

4. Chứng minh metrics/topology bằng Linkerd Viz:

   ```bash
   linkerd viz stat deploy -n yas
   ```

5. Chứng minh authorization:

   ```bash
   kubectl -n yas exec deploy/backoffice-bff-allowed-curl -c curl -- curl -i --max-time 5 http://backoffice-bff/login
   kubectl -n yas exec deploy/backoffice-bff-denied-curl -c curl -- curl -i --max-time 5 http://backoffice-bff/login
   ```

6. Chứng minh retry bằng controlled demo:

   ```bash
   kubectl -n yas exec deploy/linkerd-retry-client -c curl -- curl -i --max-time 5 http://linkerd-retry-demo/flaky
   kubectl -n yas logs deploy/linkerd-retry-demo --tail=30
   linkerd viz stat deploy -n yas
   ```

## 11. Hạn chế

- Đây là proof-of-concept cho môn học, không phải full production rollout.
- AuthorizationPolicy demo chỉ allow một service account test. Nếu áp dụng lâu dài, cần bổ sung identity của các client thật.
- Retry trực tiếp trên `backoffice-bff` cần endpoint ổn định trả HTTP 500. Repository hiện chưa có endpoint 500 chuyên dụng cho `backoffice-bff`, nên bằng chứng retry nên dùng controlled demo `linkerd-retry-demo`.
- Topology/flow được quan sát bằng Linkerd Viz thay vì Kiali vì Kiali là công cụ thường đi với Istio.
