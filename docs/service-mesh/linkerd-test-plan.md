# Test plan Service Mesh Linkerd cho YAS

| Test ID | Goal | Command | Expected result | Screenshot/log cần thu thập | Status |
|---|---|---|---|---|---|
| SM-01 | Kiểm tra Linkerd control plane | `linkerd check` | Tất cả check quan trọng pass | Log terminal `linkerd check` | PASS |
| SM-02 | Kiểm tra Linkerd Viz | `linkerd viz check` | Linkerd Viz và Prometheus sẵn sàng | Log terminal `linkerd viz check` | PASS |
| SM-03 | Kiểm tra sidecar injection cho `backoffice-bff` | `kubectl -n yas get pods \| grep backoffice-bff` | Pod `backoffice-bff` hiển thị `2/2 Running` | Log terminal pod status | PASS |
| SM-04 | Kiểm tra proxy của workload đã mesh | `linkerd -n yas check --proxy` | Proxy check pass | Log terminal proxy check | PASS |
| SM-05 | Kiểm tra metrics/topology bằng Linkerd Viz | `linkerd viz stat deploy -n yas` | `backoffice-bff` có `MESHED 1/1`, success rate, RPS, latency | Log terminal hoặc screenshot dashboard | PASS |
| SM-06 | Kiểm tra authorization allow | `kubectl -n yas exec deploy/backoffice-bff-allowed-curl -c curl -- curl -i --max-time 5 http://backoffice-bff/login` | Request không bị Linkerd policy chặn và nhận HTTP 200 | Log curl từ allowed client | PASS |
| SM-07 | Kiểm tra authorization deny | `kubectl -n yas exec deploy/backoffice-bff-denied-curl -c curl -- curl -i --max-time 5 http://backoffice-bff/login` | Request bị Linkerd policy chặn với HTTP 403 | Log curl từ denied client | PASS |
| SM-08 | Kiểm tra retry policy cho HTTP 500 bằng controlled demo | `kubectl -n yas exec deploy/linkerd-retry-client -c curl -- curl -i --max-time 5 http://linkerd-retry-demo/flaky` | Curl cuối cùng nhận HTTP 200 nhờ retry | Curl output, log server, `linkerd viz stat` | PASS |
| SM-09 | Kiểm tra log retry demo | `kubectl -n yas logs deploy/linkerd-retry-demo --tail=30` | Log có ít nhất hai request: lần đầu `500`, lần retry `200` | Log terminal server | PASS |

## Ghi chú

- Các test SM-06 và SM-07 cần apply manifest trong `k8s/service-mesh/linkerd/` trước.
- Endpoint allow ổn định đã dùng là `/login`, trả `HTTP/1.1 200 OK`. Request tới `/` có thể không phản hồi trong 5 giây do hành vi ứng dụng, nên không dùng làm bằng chứng chính.
- Test retry không được đánh dấu pass nếu chưa có curl output HTTP 200 và log server cho thấy request đầu `500`, request retry `200`.
