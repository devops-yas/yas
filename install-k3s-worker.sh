#!/usr/bin/env bash
set -euo pipefail

MASTER_IP="100.118.54.48"
K3S_VERSION="v1.35.5+k3s1"

# Đổi tên này cho từng máy worker
NODE_NAME="${1:-yas-worker-quocky}"

echo "=== Kiểm tra Tailscale ==="

if ! command -v tailscale >/dev/null 2>&1; then
    echo "Chưa cài Tailscale."
    echo "Hãy cài và đăng nhập Tailscale trước khi chạy script."
    exit 1
fi

sudo systemctl enable --now tailscaled

WORKER_IP="$(tailscale ip -4 | head -n 1)"

if [[ -z "$WORKER_IP" ]]; then
    echo "Không lấy được IP Tailscale."
    echo "Hãy chạy: sudo tailscale up"
    exit 1
fi

echo "Node name : $NODE_NAME"
echo "Worker IP: $WORKER_IP"
echo "Master IP: $MASTER_IP"

echo
read -rsp "Dán K3S node token từ master: " K3S_TOKEN
echo

if [[ -z "$K3S_TOKEN" ]]; then
    echo "Token không được để trống."
    exit 1
fi

echo "=== Kiểm tra kết nối đến master ==="

if ! timeout 5 bash -c "echo >/dev/tcp/${MASTER_IP}/6443" 2>/dev/null; then
    echo "Không kết nối được tới ${MASTER_IP}:6443."
    echo "Kiểm tra master K3s, Tailscale hoặc firewall."
    exit 1
fi

echo "=== Cài đặt K3s agent ==="

curl -sfL https://get.k3s.io | sudo env \
    K3S_URL="https://${MASTER_IP}:6443" \
    K3S_TOKEN="$K3S_TOKEN" \
    INSTALL_K3S_VERSION="$K3S_VERSION" \
    INSTALL_K3S_EXEC="agent --node-name ${NODE_NAME} --node-ip ${WORKER_IP} --flannel-iface tailscale0" \
    sh -

echo
echo "=== Trạng thái K3s agent ==="

sudo systemctl enable --now k3s-agent
sudo systemctl status k3s-agent --no-pager

echo
echo "Worker đã được cài đặt:"
echo "  Node name : $NODE_NAME"
echo "  Node IP   : $WORKER_IP"
echo "  Master    : https://${MASTER_IP}:6443"
get.k3s.io
