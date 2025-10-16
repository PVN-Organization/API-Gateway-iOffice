# 🚀 Hướng dẫn Deploy thủ công lên máy 62.72.57.70

## Bước 1: Chuẩn bị file deployment

File đã được tạo sẵn: `vxp-gateway-deploy.tar.gz`

Hoặc bạn có thể tạo lại:
```bash
cd /Users/duyphuongpham/Downloads/20220209_java_SDKVXP_Example
tar -czf vxp-gateway-deploy.tar.gz \
    "JDK 1.8/SDKVXP_Example/target/classes/" \
    "JDK 1.8/SDKVXP_Example/lib/" \
    "JDK 1.8/SDKVXP_Example/resources/" \
    Dockerfile \
    docker-compose.yml
```

## Bước 2: Copy file lên server

**Cách 1: Dùng SCP (nếu có password)**
```bash
scp vxp-gateway-deploy.tar.gz root@62.72.57.70:/root/
```

**Cách 2: Dùng SFTP client (FileZilla, WinSCP, etc.)**
- Host: 62.72.57.70
- User: root
- Copy file `vxp-gateway-deploy.tar.gz` vào `/root/`

**Cách 3: Dùng rsync**
```bash
rsync -avz vxp-gateway-deploy.tar.gz root@62.72.57.70:/root/
```

## Bước 3: SSH vào server và deploy

```bash
ssh root@62.72.57.70
```

Sau khi SSH thành công, chạy các lệnh sau:

```bash
cd /root

# Giải nén file
tar -xzf vxp-gateway-deploy.tar.gz

# Dừng container cũ
docker stop vxp-gateway
docker rm vxp-gateway

# Build Docker image mới
docker build -t vxp-api-gateway .

# Chạy container mới
docker run -d \
    --name vxp-gateway \
    --restart unless-stopped \
    -p 8080:8080 \
    -e VXP_ENDPOINT="http://vxpdungchung.vnpt.vn" \
    -e VXP_SYSTEM_ID="vxp.saas.03" \
    -e VXP_SECRET_KEY="AwyOIWljooz1hxgZsxpEBjCoj5rg9dMMsLJl0YydfZpd" \
    -e VXP_STORE_PATH="/app/store" \
    -e VXP_MAX_CONN="50" \
    -e VXP_RETRY="3" \
    -e GATEWAY_PORT="8080" \
    -e ORG_ORGAN_ID="vxp.saas.03" \
    -e ORG_ORGANIZATION_IN_CHARGE="Đơn vị test vxp 3" \
    -e ORG_ORGAN_NAME="Đơn vị test vxp 3" \
    -e ORG_ORGAN_ADD="Số 1, Hà Nội" \
    -e ORG_EMAIL="contact-vxp3@example.vn" \
    -e ORG_TELEPHONE="+84 24 1234 5678" \
    -e ORG_FAX="+84 24 8765 4321" \
    -e ORG_WEBSITE="https://vxp3.example.vn" \
    vxp-api-gateway

# Kiểm tra container
docker ps | grep vxp-gateway

# Xem logs
docker logs vxp-gateway
```

## Bước 4: Test API

```bash
# Test health check
curl -X GET http://62.72.57.70:8080/api/health

# Test gửi 3 files với aggregate
curl -X POST "http://62.72.57.70:8080/api/documents/send/batch" \
  -H "Content-Type: multipart/form-data" \
  -F "files=@/path/to/file1.txt" \
  -F "files=@/path/to/file2.txt" \
  -F "files=@/path/to/file3.txt" \
  -F 'metaJson={"from":{"organId":"vxp.saas.03","organizationInCharge":"Đơn vị test vxp 3","organName":"Đơn vị test vxp 3"},"to":[{"organId":"vxp.saas.02","organizationInCharge":"Đơn vị test vxp 2","organName":"Đơn vị test vxp 2"}],"subject":"Test nhiều files","content":"Test gửi nhiều files cùng lúc"}' \
  -F "fromCode=vxp.saas.03" \
  -F "toCode=vxp.saas.02" \
  -F "aggregate=true"
```

## ✅ Các thay đổi chính trong version mới:

1. **ManualEdxmlBuilder.java**: Builder mới tự tạo XML EDXML, hỗ trợ nhiều attachments
2. **DocumentPipeline.java**: Sử dụng ManualEdxmlBuilder thay vì EdxmlAggregateBuilder
3. **Fix bug**: Giờ gửi 3 files sẽ tạo EDXML với 3 attachments (không còn chỉ 1 nữa)

## 🔍 Debug

Nếu có vấn đề, xem logs:
```bash
docker logs -f vxp-gateway
```

Xem logs chi tiết về attachments:
```bash
docker logs vxp-gateway 2>&1 | grep "MANUAL-EDXML"
```

## 📊 Kết quả mong đợi

Khi gửi 3 files với `aggregate=true`, bạn sẽ thấy trong logs:
```
[MANUAL-EDXML] Building EDXML with 3 attachments
[MANUAL-EDXML] Attachment 1: file1.txt (cid:xxx-xxx-xxx)
[MANUAL-EDXML] Attachment 2: file2.txt (cid:yyy-yyy-yyy)
[MANUAL-EDXML] Attachment 3: file3.txt (cid:zzz-zzz-zzz)
[MANUAL-EDXML] Created EDXML with 3 attachments: /path/to/file.edxml
```

Và khi parse EDXML nhận được, sẽ có 3 attachments trong response JSON.

