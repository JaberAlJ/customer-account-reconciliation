# Customer Account Reconciliation API

This document describes the implemented REST API for the Customer Account Reconciliation service.

---

# Base URL

```text
http://localhost:8080
```

---

# API Overview

## Account Check APIs

Base path:

```text
/api/v1/account-checks
```

## Import Batch APIs

Base path:

```text
/api/v1/import-batches
```

---

# Root Endpoint

## Welcome Page

### GET /

Returns a simple HTML welcome page.

### Response

```html
<h1>Customer Account Reconciliation</h1>
<p>Starter project for the Customer Account Reconciliation assignment</p>
```

---

# Account Check Endpoints

## 1. Import CSV File

Processes a CSV file containing customer identifiers and performs reconciliation validation.

### Endpoint

```http
POST /api/v1/account-checks/import
```

### Content-Type

```text
multipart/form-data
```

### Request Parameters

| Parameter  | Type           | Required  | Description         |
|:-----------|:---------------|:----------|:--------------------|
| file       | MultipartFile  | Yes       | CSV file to upload  |

### Example Request

```bash
curl -X POST -F "file=@customer-identifiers.csv" http://localhost:8080/api/v1/account-checks/import
```

### Success Response

#### Status

```text
200 OK
```

#### Response Body

Returns an [ImportBatch](#importbatch-model) object.

### Error Response

#### Status

```text
500 Internal Server Error
```

Returned when the import process fails.

---

## 2. Get Request By Request ID

Fetches a single processed request using its unique request ID.

### Endpoint

```http
GET /api/v1/account-checks/{requestId}
```

### Path Variables

| Variable   | Type    | Description                |
|:-----------|:--------|:---------------------------|
| requestId  | String  | Unique request identifier  |

### Example Request

```http
GET /api/v1/account-checks/REQ-10001
```

### Success Response

#### Status

```text
200 OK
```

#### Response Body

Returns an [AccountCheckRequest](#accountcheckrequest-model) object.

### Not Found Response

#### Status

```text
404 Not Found
```

---

## 3. List All Requests

Returns all processed requests.

Can optionally filter by customer reference.

### Endpoint

```http
GET /api/v1/account-checks
```

### Query Parameters

| Parameter          | Type    | Required  | Description                            |
|:-------------------|:--------|:----------|:---------------------------------------|
| customerReference  | String  | No        | Filter requests by customer reference  |

### Example Requests

#### Get All Requests

```http
GET /api/v1/account-checks
```

#### Filter By Customer Reference

```http
GET /api/v1/account-checks?customerReference=CUST-1001
```

### Success Response

#### Status

```text
200 OK
```

#### Response Body

Returns an array of [AccountCheckRequest](#accountcheckrequest-model) objects.

---

## 4. Get Failed Validation Rows

Returns all rows that failed validation.

### Endpoint

```http
GET /api/v1/account-checks/failures
```

### Success Response

#### Status

```text
200 OK
```

#### Response Body

Returns an array of [AccountCheckRequest](#accountcheckrequest-model) objects where:

```text
status = "INVALID"
```

---

## 5. Get Successfully Imported Rows

Returns all successfully imported rows.

### Endpoint

```http
GET /api/v1/account-checks/imported
```

### Success Response

#### Status

```text
200 OK
```

#### Response Body

Returns an array of [AccountCheckRequest](#accountcheckrequest-model) objects where:

```text
status = "IMPORTED"
```

---

# Import Batch Endpoints

## 1. List All Import Batches

Returns all CSV import batches and their processing summaries.

### Endpoint

```http
GET /api/v1/import-batches
```

### Success Response

#### Status

```text
200 OK
```

#### Response Body

Returns an array of [ImportBatch](#importbatch-model)  objects.

---

## 2. Get Import Batch By ID

Returns details for a specific import batch.

### Endpoint

```http
GET /api/v1/import-batches/{batchId}
```

### Path Variables

| Variable  | Type  | Description              |
|:----------|:------|:-------------------------|
| batchId   | UUID  | Unique batch identifier  |

### Example Request

```http
GET /api/v1/import-batches/550e8400-e29b-41d4-a716-446655440000
```

### Success Response

#### Status

```text
200 OK
```

#### Response Body

Returns an [ImportBatch](#importbatch-model)  object.

### Not Found Response

#### Status

```text
404 Not Found
```

---

# Data Models

## ImportBatch Model

|     Field      | Type                       | Description                            |
|:--------------:|:---------------------------|----------------------------------------|
|       id       | UUID                       | Unique identifier for the batch        |
|   importDate   | LocalDateTime              | Timestamp when the batch was processed |
|    fileName    | String                     | Original uploaded CSV filename         |
|   totalRows    | Integer                    | Total number of rows in the CSV        |
|  importedRows  | Integer                    | Number of successfully imported rows   |
| duplicateRows  | Integer                    | Number of duplicate rows skipped       |
|  invalidRows   | Integer                    | Number of invalid rows                 |
|      rows      | Array<AccountCheckRequest> | List of processed rows                 |

---

## AccountCheckRequest Model

|       Field        | Type          | Description                                 |
|:------------------:|---------------|---------------------------------------------|
|     requestId      | String        | Unique request identifier                   |
| customerReference  | String        | Customer reference ID                       |
|    identifier1     | String        | First identifier                            |
|    identifier2     | String        | Second identifier                           |
|    identifier3     | String        | Third identifier                            |
|       status       | String        | Processing status (`IMPORTED` or `INVALID`) |
| validationSummary  | String        | Validation result summary                   |
|  ownershipResult   | String        | Reconciliation ownership result             |
|    submittedAt     | LocalDateTime | Request submission timestamp                |
|      batchId       | UUID          | Associated import batch ID                  |

---
