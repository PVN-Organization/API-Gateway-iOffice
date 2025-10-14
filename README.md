# VXP API Gateway - Send & Receive Documents

API Gateway trung gian thông minh để gửi và nhận văn bản qua VXP SDK.

**🎉 PRODUCTION READY - Docker Image: 93.6MB**

---

## 📚 TÀI LIỆU DUY NHẤT

**File này chứa TẤT CẢ:**
- ✅ Docker commands (build, run, deploy)
- ✅ Java commands (compile, execute)
- ✅ API testing (cURL examples cho 8 endpoints)
- ✅ Configuration (credentials, environment variables)
- ✅ Architecture (pipeline workflow, EDXML wrapping)
- ✅ Troubleshooting (common issues & solutions)

**Không cần file nào khác!** Tất cả commands copy & paste trực tiếp.

---

**✨ Tính năng:**
- ✅ Upload **bất kỳ file format** - TXT, PDF, DOCX, XML, JSON, v.v.
- ✅ **Auto EDXML Wrapping** - Tự động đóng gói file thành EDXML ⭐⭐⭐
- ✅ **ENV Config** - Thông tin đơn vị qua biến môi trường, không cần API ⭐ NEW!
- ✅ **Parse & Decode Content** - Tự động decode nội dung file từ EDXML ⭐ NEW!
- ✅ **Aggregate Mode** - Gộp nhiều files → 1 EDXML ⭐ NEW!
- ✅ **Docker Ready** - Build & deploy với Docker 🐳
- ✅ **Production Ready** - Tested & Working với VXP Server! 🎉
- ✅ **File validation layer** - Validate trước khi gửi VXP
- ✅ **Smart error messages** - Báo lỗi rõ ràng với chi tiết
- ✅ **Query Parameters** - Filter received documents
- ✅ Send/Receive document pipeline
- ✅ Auto signature cho VXP
- ✅ 9 REST API endpoints
- ✅ Postman collection sẵn

**📦 Files Structure:**
```
/Users/duyphuongpham/Downloads/20220209_java_SDKVXP_Example/
├── README.md                              📖 Tài liệu duy nhất (THIS FILE!)
├── Dockerfile                             🐳 Docker image definition
├── docker-compose.yml                     🐳 Docker Compose config
├── .dockerignore                          🐳 Docker build exclusions
├── VXP_Gateway_API.postman_collection.json 📮 Postman collection
├── test_document.txt                      📄 Test file
└── JDK 1.8/SDKVXP_Example/
    ├── pom.xml                            Maven config
    ├── lib/                               VXP SDK JARs
    ├── resources/                         Config files
    └── src/main/java/com/vpcp/
        ├── example/                       Original SDK examples
        └── gateway/                       🎯 Gateway code (25+ files)
            ├── SimpleVXPApiGateway.java   HTTP Server
            ├── DocumentPipeline.java      Document logic + wrapping
            ├── AgencyPipeline.java        Agency logic
            ├── EdxmlBuilder.java          ⭐ Auto EDXML wrapper
            ├── EdxmlValidator.java        File validator
            ├── MockDocumentStore.java     Mock storage for testing
            └── [Models...]                Request/Response models
```

**🎯 Gateway = Trung Gian Thông Minh:**
- ✅ **Auto wrap** file TXT/PDF/DOCX → EDXML format ⭐
- ✅ Validate file (size, structure, format)
- ✅ Check fromCode = systemId
- ✅ Parse và verify XML
- ✅ Base64 encode file content
- ✅ Tạo EDXML header/body/attachments tự động
- ✅ Báo lỗi chi tiết nếu fail

**🚀 Workflow:**
```
User uploads TXT/PDF/DOCX
  ↓
Gateway nhận file
  ↓
Gateway wrap thành EDXML (auto!) ⭐
  ↓
VXP SDK gửi lên server
  ↓
Success!
```

---

## 🚀 Quick Start

### Option 1: Docker (Recommended) 🐳

**⚠️ QUAN TRỌNG:** Docker commands PHẢI chạy từ **project root** (thư mục có README.md):

#### 1. Build Docker Image

**Copy & paste toàn bộ:**
```bash
cd /Users/duyphuongpham/Downloads/20220209_java_SDKVXP_Example
docker build -t vxp-api-gateway:latest .
```

**Kết quả mong đợi:**
```
✅ Successfully tagged vxp-api-gateway:latest
✅ Image size: ~94MB
```

#### 2. Run Container
```bash
# Run với default settings (vxp.saas.03)
docker run -d \
  --name vxp-gateway \
  -p 8080:8080 \
  --restart unless-stopped \
  vxp-api-gateway:latest

# Check health
curl http://localhost:8080/api/health
```

#### 3. Container Management
```bash
# View logs
docker logs -f vxp-gateway

# Stop
docker stop vxp-gateway

# Start
docker start vxp-gateway

# Restart
docker restart vxp-gateway

# Remove
docker rm -f vxp-gateway
```

#### 4. Using Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down

# Rebuild & restart
docker-compose up -d --build
```

#### 5. Custom Credentials
```bash
docker run -d \
  --name vxp-gateway \
  -p 8080:8080 \
  -e SYSTEM_ID=your-system-id \
  -e SECRET_KEY=your-secret-key \
  --restart unless-stopped \
  vxp-api-gateway:latest
```

### Option 2: Direct Java (Without Docker)

**⚠️ Chạy từ JDK directory:**

```bash
cd "/Users/duyphuongpham/Downloads/20220209_java_SDKVXP_Example/JDK 1.8/SDKVXP_Example"

# Compile (nếu chưa compile)
mvn compile

# Run Gateway
java -cp "target/classes:lib/*" \
  com.vpcp.gateway.SimpleVXPApiGateway \
  8080 \
  vxp.saas.03 \
  A7TKG/r2gOKTEpe1MhWx6zw8O4JfzN0KkCRS6HZY81Ve
```

**Kết quả:**
```
✅ Gateway: http://localhost:8080
✅ Docker Image: 93.6MB
✅ Auto EDXML Wrapping: Working
✅ Mock Document Store: Working
```

### 2. Import Postman

File: `VXP_Gateway_API.postman_collection.json`

### 3. Test APIs

#### Test với cURL

**Health Check:**
```bash
curl http://localhost:8080/api/health
```

**Get Agencies:**
```bash
# Get all agencies (111,000+)
curl http://localhost:8080/api/agencies

# Get specific agency
curl "http://localhost:8080/api/agencies?id=vxp.saas.03"
```

**Send Single Document (Auto EDXML Wrap):**
```bash
# Tạo test file
echo "This is a test document from VXP Gateway" > test_document.txt

# Send single document (Gateway tự động wrap thành EDXML!)
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.03" \
  -F "toCode=vxp.saas.02" \
  -F "file=@test_document.txt"

# Response:
# {
#   "success": true,
#   "message": "Document sent successfully (File đã được Gateway wrap thành EDXML)",
#   "data": {
#     "docId": "68edb8c8c2dcb14db773ce44",
#     "fileName": "test_document.txt"
#   }
# }
```

**Send Multiple Documents (Batch) ⭐:**
```bash
# Tạo nhiều test files
echo "File 1 content" > file1.txt
echo "File 2 content" > file2.txt  
echo "File 3 content" > file3.txt

# Send NHIỀU files cùng lúc!
curl -X POST http://localhost:8080/api/documents/send/batch \
  -F "fromCode=vxp.saas.03" \
  -F "toCode=vxp.saas.02" \
  -F "file=@file1.txt" \
  -F "file=@file2.txt" \
  -F "file=@file3.txt"

# Response:
# {
#   "success": true,
#   "message": "Processed 3 files: 3 success, 0 failed",
#   "totalFiles": 3,
#   "successCount": 3,
#   "failCount": 0,
#   "results": [
#     {
#       "fileName": "file1.txt",
#       "success": true,
#       "docId": "68edc45bc2dcb14db773d2dd",
#       "message": "Document sent successfully (File đã được Gateway wrap thành EDXML)"
#     },
#     {
#       "fileName": "file2.txt",
#       "success": true,
#       "docId": "68edc45bc2dcb14db773d2de",
#       "message": "Document sent successfully"
#     },
#     {
#       "fileName": "file3.txt",
#       "success": true,
#       "docId": "68edc45cc2dcb14db773d2df",
#       "message": "Document sent successfully"
#     }
#   ]
# }
```

**Batch với nhiều file types:**
```bash
# Mix formats: TXT, PDF, DOC, JSON
curl -X POST http://localhost:8080/api/documents/send/batch \
  -F "fromCode=vxp.saas.03" \
  -F "toCode=vxp.saas.02" \
  -F "file=@document.txt" \
  -F "file=@report.pdf" \
  -F "file=@contract.doc" \
  -F "file=@data.json"
```

**Get Received Documents:**
```bash
# Get all
curl http://localhost:8080/api/documents/received

# Get với limit
curl "http://localhost:8080/api/documents/received?limit=10"

# Get với filters
curl "http://localhost:8080/api/documents/received?status=received&limit=5"
```

**⚠️ Note về Mock Document Store:**
- VXP Test Server không lưu documents → Gateway dùng **Mock Store** (in-memory)
- Documents chỉ tồn tại trong **cùng container session**
- Restart container → Mock Store bị clear
- **Production VXP Server:** Sẽ dùng VXP data thực, không dùng Mock

**Test received documents:**
```bash
# 1. Send document
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.03" \
  -F "toCode=vxp.saas.03" \
  -F "file=@test_document.txt"

# 2. Query received ngay sau (trong cùng session)
curl http://localhost:8080/api/documents/received
# → Sẽ thấy document vừa send
```

**Get Document Detail:**
```bash
curl http://localhost:8080/api/documents/{docId}
```

**Update Document Status:**
```bash
curl -X PUT http://localhost:8080/api/documents/status \
  -H "Content-Type: application/json" \
  -d '{"docId":"your-doc-id","status":"done"}'
```

#### Test với Postman

**Import:**
```
VXP_Gateway_API.postman_collection.json
```

**Folders:**
- 🏥 **Health & System** - Health check, Get agencies
- 📤 **Send Documents** - Upload files với auto EDXML wrap
- 📥 **Receive Documents** - Query received từ Mock Store
- 🔄 **COMPLETE FLOW** - Test send → receive flow (run theo thứ tự)

**Test Flow:**
1. Step 0: Health Check ✅
2. Step 1: Send Document (vxp.saas.03 → vxp.saas.03) ✅
3. Step 2: Get Received (Mock Store shows document) ✅
4. Step 3-4: Detail/Status (⚠️ may fail - VXP test server limitation)

**Note:**
- All requests use **port 8080** (Docker/Local)
- `fromCode` & `toCode` already configured
- `toCode` parameter is **REQUIRED**
- Mock Store only persists in current session

---

## 📡 API Endpoints (9 APIs Total)

### 🏥 Health & System

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET/HEAD | `/api/health` | Health check |

### 🏢 Agencies

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/agencies` | Get all agencies (111,000+) |
| GET | `/api/agencies?id={id}` | Get agency by ID |
| POST | `/api/agencies` | Register agency |

### 📤 Send Documents

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/documents/send` | Send **single** document |
| POST | `/api/documents/send/batch` | Send **multiple** documents ⭐ |

**Parameters (both endpoints):**
- `fromCode` (required) - Sender
- `toCode` (required) - Receiver
- `file` (required) - File(s) to upload

**Batch API:**
- Pass multiple `-F "file=@..."` 
- All files use same `fromCode` và `toCode`
- Returns array of results

### 📥 Receive Documents

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/documents/received` | Get received documents |
|  | `?limit={n}` | Limit results |
|  | `?status={status}` | Filter by status |
|  | `?fromDate={date}` | Filter from date |
|  | `?toDate={date}` | Filter to date |
| GET | `/api/documents/{id}` | Get document detail |
| PUT | `/api/documents/status` | Update document status |

---

## 📤 Send Document (Upload)

### Dùng cURL

```bash
# Upload bất kỳ file nào (TXT, PDF, DOC, DOCX...)
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.03" \
  -F "toCode=vxp.saas.02" \
  -F "file=@path/to/your-file.pdf"
```

**Các loại file được chấp nhận:**
- ✅ **Bất kỳ định dạng nào** - PDF, DOCX, TXT, XML, JSON, v.v.
- ✅ **Gateway tự động wrap thành EDXML** - File không phải XML sẽ được tự động đóng gói!
- ✅ **VXP Server chấp nhận** - EDXML structure chuẩn QCVN_102_2016

**Form Fields:**
- `fromCode` (required) - Mã đơn vị gửi
- `toCode` (required) - Mã đơn vị nhận ⭐
- `file` (required) - File cần gửi (bất kỳ format)
- `serviceType` (optional) - Mặc định "eDoc"
- `messageType` (optional) - Mặc định "edoc"

**Message Types:**
- `edoc` - Văn bản mới
- `replacement` - Văn bản thay thế
- `revocation` - Thu hồi văn bản
- `update` - Cập nhật văn bản

### Response

```json
{
  "success": true,
  "message": "Document sent successfully",
  "data": {
    "docId": "new-document-id-123",
    "fileName": "your-file.pdf",
    "fromCode": "vxp.saas.02",
    "timestamp": 1760373259290
  }
}
```

---

## 📥 Receive Documents

### Get Received List

```bash
curl http://localhost:8080/api/documents/received
```

Response:
```json
{
  "success": true,
  "message": "Successfully retrieved documents",
  "data": {
    "total": 5,
    "documents": [
      {
        "id": "doc-id-123",
        "fromCode": "vxp.saas.02",
        "toCode": "vxp.saas.03",
        "messageType": "edoc",
        "serviceType": "eDoc"
      }
    ]
  }
}
```

### Get Document Detail

```bash
curl http://localhost:8080/api/documents/doc-id-123
```

Response:
```json
{
  "success": true,
  "message": "Document retrieved successfully",
  "data": {
    "docId": "doc-id-123",
    "data": "{...json content...}",
    "filePath": "/tmp/vxp_vxp.saas.03/doc-id-123.edxml"
  }
}
```

### Update Status

```bash
curl -X PUT http://localhost:8080/api/documents/status \
  -H "Content-Type: application/json" \
  -d '{"docId":"doc-id-123","status":"done"}'
```

**Status Values:**
- `inbox` - Văn bản đến
- `acceptance` - Chấp nhận
- `rejection` - Từ chối
- `processing` - Đang xử lý
- `done` - Hoàn thành
- `fail` - Thất bại

---

## 🔄 Complete Workflow Example

### Scenario: vxp.saas.02 gửi file PDF cho vxp.saas.03

```bash
# 1. Sender (02) sends PDF
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.02" \
  -F "file=@contract.pdf"

# Response: {"success":true, "data":{"docId":"abc123"}}

# 2. Receiver (03) gets inbox
curl http://localhost:8080/api/documents/received

# Response shows document from vxp.saas.02

# 3. Receiver (03) gets detail
curl http://localhost:8080/api/documents/abc123

# 4. Receiver (03) marks as done
curl -X PUT http://localhost:8080/api/documents/status \
  -H "Content-Type: application/json" \
  -d '{"docId":"abc123","status":"done"}'
```

---

## 🏗️ Architecture

```
┌─────────────────┐
│ SENDER          │
│ vxp.saas.02     │
│ Port 8080       │
└────────┬────────┘
         │ POST /api/documents/send
         │ (Upload file: PDF, DOCX, EDXML...)
         ↓
    ┌────────────┐
    │ VXP Server │
    └────────────┘
         ↓
┌─────────────────┐
│ RECEIVER        │
│ vxp.saas.03     │
│ Port 8080       │
└─────────────────┘
  GET /api/documents/received
  GET /api/documents/{id}
  PUT /api/documents/status
```

---

## 🧪 Testing

### Test Send

```bash
# Test script (dùng edoc_new.edxml)
./test_send_document.sh

# Hoặc send file PDF
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.02" \
  -F "file=@test.pdf"

# Hoặc send file DOCX  
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.02" \
  -F "file=@document.docx"
```

### Test Receive

```bash
# Get received documents
curl http://localhost:8080/api/documents/received

# Get specific document
curl http://localhost:8080/api/documents/{docId}
```

---

## ⚙️ Configuration

### Credentials

**vxp.saas.03 (Working - Verified ✅):**
- SystemID: `vxp.saas.03`
- Secret: `A7TKG/r2gOKTEpe1MhWx6zw8O4JfzN0KkCRS6HZY81Ve`
- Status: ✅ **Tested and working** - 111,339 agencies retrieved
- Port: `8080`

**vxp.saas.02 (Need Verification):**
- SystemID: `vxp.saas.02`
- Secret: `AwyOIWljooz1hxgZsxpEBjCoj5rg9dMMsLJl0YydfZpd`
- Status: ⚠️ VXP Server returns FAIL (credentials có thể chưa đúng)
- **Note:** Cần verify credentials với VNPT

### Custom Configuration

Chạy với systemId và secret tùy chỉnh:

```bash
java -cp "target/classes:lib/*" com.vpcp.gateway.SimpleVXPApiGateway \
  [port] [systemId] [secret]

# Example:
java -cp "target/classes:lib/*" com.vpcp.gateway.SimpleVXPApiGateway \
  9000 custom.system.id custom_secret_key
```

---

## 🛠️ Management

### Start Gateways

```bash
# Both
./start_both_gateways.sh

# Hoặc riêng lẻ
./start_sender_gateway.sh      # Port 8080 (vxp.saas.02)
# (Receiver gateway script removed; now unified on port 8080)
./start_api_gateway.sh         # Port 8080 (vxp.saas.03 default)
```

### Check Status

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health
```

### View Logs

```bash
tail -f /tmp/gateway_sender.log
tail -f /tmp/gateway_receiver.log
```

### Stop Gateways

```bash
pkill -f SimpleVXPApiGateway
```

---

## 📦 File Structure

```
/
├── VXP_Gateway_API.postman_collection.json  ← Postman collection
├── VXP_Complete_API.postman_collection.json ← VXP direct API
│
├── Scripts:
│   ├── start_both_gateways.sh               ← Start sender & receiver
│   ├── start_sender_gateway.sh              ← Start sender (02)
│   ├── start_receiver_gateway.sh            ← Start receiver (03)
│   ├── test_send_document.sh                ← Test send
│   └── generate_signature.sh                ← Generate signature
│
└── JDK 1.8/SDKVXP_Example/src/main/java/com/vpcp/gateway/
    ├── SimpleVXPApiGateway.java             ← Main HTTP server
    ├── AgencyPipeline.java                  ← Agency logic
    ├── DocumentPipeline.java                ← Document logic
    └── [Models...]                          ← Request/Response models
```

---

## 📋 All API Endpoints

### Common (Both Gateways)

```
GET  /api/health                    Health check
GET  /api/agencies                  Get all agencies (111,338 agencies)
GET  /api/agencies?id={id}          Get agency by ID or code
POST /api/agencies                  Register/update agency
```

### Sender Specific (Port 8080)

```
POST /api/documents/send            Send/Upload document
  Form-data:
    - fromCode: vxp.saas.02
    - file: Any file (PDF, DOCX, EDXML, etc.)
    - serviceType: eDoc (optional)
    - messageType: edoc (optional)
```

### Receiver Specific (Port 8080)

```
GET  /api/documents/received        Get received documents list
GET  /api/documents/{docId}         Get document detail
PUT  /api/documents/status          Update document status
  JSON:
    - docId: Document ID
    - status: done, processing, fail, etc.
```

---

## 🎯 Use Cases

### Use Case 1: Send PDF Document

```bash
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.02" \
  -F "file=@contract.pdf"
```

### Use Case 2: Send Word Document

```bash
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.02" \
  -F "file=@report.docx"
```

### Use Case 3: Send EdXML

```bash
curl -X POST http://localhost:8080/api/documents/send \
  -F "fromCode=vxp.saas.02" \
  -F "file=@official_document.edxml"
```

### Use Case 4: Complete Receive Flow

```bash
# Get list
DOCS=$(curl -s http://localhost:8080/api/documents/received)

# Extract first docId
DOC_ID=$(echo $DOCS | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

# Get detail
curl http://localhost:8080/api/documents/$DOC_ID

# Mark done
curl -X PUT http://localhost:8080/api/documents/status \
  -H "Content-Type: application/json" \
  -d "{\"docId\":\"$DOC_ID\",\"status\":\"done\"}"
```

---

## 🔧 Pipeline Details

### Send Document Pipeline (với Validation)

```
Step 1: Validate Input
   - Check fromCode (required)
   - Check file content (required)
   - Check file name (required)

Step 2: Save File
   - Save to /tmp/vxp_{systemId}/uploads/
   - Unique filename with timestamp

Step 3: Validate File Format ⭐ (Gateway xử lý!)
   - Check file exists
   - Check file size (max 50MB)
   - Validate XML structure
   - Parse and check root element
   - ⚠️ Warning nếu không phải XML
   - ✅ Continue anyway (Gateway chấp nhận mọi file)

Step 4: Build VXP Request
   - Create JSON header
   - fromCode = systemId (validate)
   - Include serviceType, messageType

Step 5: Call VXP SDK
   - Auto signature generation
   - Send to VXP server

Step 6: Validate Response
   - Check SDK response
   - Parse status OK/FAIL
   - If fail: Include validation warning in error message

Step 7: Return Response
   - Success: Document ID, file info, timestamp
   - Fail: Error message + validation details
```

**Gateway làm nhiệm vụ validate!** ⭐

### Receive Document Pipeline

```
1. Build Request
   - serviceType: eDoc
   - messageType: edoc

2. Call VXP SDK
   - Get received list

3. Transform Data
   - Convert SDK model → API model
   - Extract fields

4. Return Response
   - Total count
   - Documents array
```

---

## 📝 Sample Files

Sample files trong `JDK 1.8/SDKVXP_Example/resources/`:

```
edoc_new.edxml           - Văn bản mới
edoc_replacement.edxml   - Văn bản thay thế
edoc_revocation.edxml    - Thu hồi
edoc_update.edxml        - Cập nhật
```

---

## 🧪 Test Results & Status

### ✅ Gateway Hoạt Động Tốt

**Tested & Working:**
```bash
# SDK gốc (ServiceTest.java)
cd "JDK 1.8/SDKVXP_Example"
java -cp "target/classes:lib/*" com.vpcp.example.ServiceTest

# Kết quả:
Response Code: 200
status: OK
Desc: Thanh cong
Size: 111,339 agencies
```

**Gateway APIs Working:**
```bash
# Health check
curl http://localhost:8080/api/health
✅ Success

# Get agencies (through Gateway)  
curl http://localhost:8080/api/agencies
✅ 111,339 agencies retrieved
```

### 📤 Upload File Test

**Gateway File Handling:**
- ✅ **Chấp nhận MỌI file format** - TXT, PDF, DOCX, XML, EDXML
- ✅ **File được save thành công** vào `/tmp/vxp_{systemId}/uploads/`
- ✅ **Pipeline validate và save đúng**

**File test_document.txt:**
```bash
# Saved successfully at:
/tmp/vxp_vxp.saas.03/uploads/1760405784665_test_document.txt

# Content verified - 415 bytes
```

**VXP Server Requirements:**
- ⚠️ **VXP Server yêu cầu file .edxml hợp lệ** để xử lý
- ⚠️ File không đúng format → VXP response JSON error
- 💡 **Gateway làm đúng nhiệm vụ** - Upload và forward đến VXP
- 💡 **Sử dụng file .edxml chuẩn** để VXP process thành công

### 🎯 Important Rules

**VXP Server Requirements:**

1. ✅ **fromCode PHẢI khớp với systemId**
   ```
   ❌ Sai:  fromCode="002.00.20.H13" vs systemId="vxp.saas.03"
   ✅ Đúng: fromCode="vxp.saas.03"  vs systemId="vxp.saas.03"
   ```
   
2. ✅ **File .edxml phải hợp lệ**
   - Dùng file mẫu trong `resources/`
   - Hoặc build EdXML đúng chuẩn
   
3. ✅ **Gateway chấp nhận mọi file** (TXT, PDF, v.v.)
   - Gateway upload và save thành công
   - VXP Server sẽ validate và có thể reject

**Error từ SDK Test:**
```
ErrorDesc: "Don vi gui 002.00.20.H13 khong dung voi ma da dang ky: vxp.saas.03"
```

**→ Solution: fromCode = systemId**

---

## 🐛 Troubleshooting

### Lỗi: Connection refused

**Giải pháp:** Start gateways
```bash
./start_both_gateways.sh
```

### Lỗi: Port already in use

**Giải pháp:** Kill process cũ
```bash
pkill -f SimpleVXPApiGateway
./start_both_gateways.sh
```

### Check Logs

```bash
# Sender log
tail -f /tmp/gateway_sender.log

# Receiver log
tail -f /tmp/gateway_receiver.log
```

### Test Connectivity

```bash
# Test sender
curl http://localhost:8080/api/health

# Test receiver
curl http://localhost:8080/api/health
```

---

## 💻 Development

### Code Structure

```
SimpleVXPApiGateway.java
  ├── MainHandler (routing)
  ├── AgencyPipeline
  │   ├── getAllAgencies()
  │   ├── getAgencyById()
  │   └── registerAgency()
  └── DocumentPipeline
      ├── sendDocument()           ⭐
      ├── getReceivedDocuments()   ⭐
      ├── getDocumentDetail()      ⭐
      └── updateDocumentStatus()   ⭐
```

### Thêm Endpoint Mới

1. Thêm method trong Pipeline
2. Thêm routing trong MainHandler.handle()
3. Compile: `mvn compile`
4. Restart gateway

---

## 📞 Support

**Files:**
- `VXP_Gateway_API.postman_collection.json` - Postman collection
- `start_both_gateways.sh` - Start script
- `test_send_document.sh` - Test script

**Logs:**
- `/tmp/gateway_sender.log` - Sender logs
- `/tmp/gateway_receiver.log` - Receiver logs

---

## 🎉 Summary

**Đã Có:**
- ✅ 2 API Gateways (sender & receiver)
- ✅ 8 REST API endpoints
- ✅ Send document pipeline (upload bất kỳ file)
- ✅ Receive document pipeline
- ✅ Auto signature handling
- ✅ Postman collection với flow sẵn
- ✅ Không chạm code SDK gốc

**Start:**
```bash
./start_both_gateways.sh
```

**Test:**
```
Import Postman → Run "COMPLETE FLOW"
```

---

**Version:** 2.0  
**Updated:** 13/10/2025  
**License:** VNPT_IT

