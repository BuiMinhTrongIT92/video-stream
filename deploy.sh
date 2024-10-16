#!/bin/bash

# Di chuyển đến thư mục dự án của bạn
cd /home/video-stream || exit 1

# Cập nhật mã nguồn từ Git repository
# Chạy kiểm tra và build Maven
echo "Running Maven build..."
sudo mvn verify &&

# Tắt Docker Compose nếu đang chạy
echo "Shutting down Docker Compose..."
sudo docker compose down &&

# Xóa các Docker resources không cần thiết
echo "Pruning unused Docker objects..."
sudo docker system prune -f && sudo docker volume prune -f &&

# Xây dựng Docker image mới
echo "Building new Docker image..."
sudo docker build -t video-stream . &&

# Khởi động lại Docker Compose
echo "Starting Docker Compose..."
sudo docker compose up -d