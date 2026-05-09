# Data Validation and Import Rules

This document explains the validation logic, row classification, and test cases implemented in the Customer Account Reconciliation Service.

## 1. Row Classification

Every row processed from the CSV is classified into one of three statuses:

|    Status     | Description                                                                                                                                                            | Resulting Ownership         |
|:-------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------|
| **IMPORTED**  | Row structure is valid and **at least one** valid identifier exists.                                                                                                   | `SAME_PERSON` or `MISMATCH` |
|  **INVALID**  | Validation/parsing failure or **zero** valid identifiers exist. (Rows missing a `request_id` are assigned an auto-generated `INVALID-REQ-UUID` to ensure persistence). | `NO_VALID_IDENTIFIERS`      |
| **DUPLICATE** | `request_id` has already been processed (in current file or database).                                                                                                 | N/A (Row is skipped)        |

## 2. Validation Rules

### A. Row Structure & Required Fields

A row is marked **INVALID** if:

* It does not contain exactly 6 columns.
* Any of these required fields are empty: `request_id`, `customer_reference`, `identifier_1`, `submitted_at`.
* The `submitted_at` timestamp is not in ISO-8601 format (e.g., `2026-04-27T10:15:30Z`).

### B. Identifier Validation

Identifiers are validated based on Oman banking rules:

* **Oman IBAN**: Must start with `OM`, length 23, alphanumeric, and pass Mod-97 checksum.
* **Account Number**: Exactly 14 digits, not all zeros.
* **CIF**: Exactly 7 digits, not all zeros.

If an identifier fails these rules, it is stored as `UNKNOWN` and marked invalid. It will **not** participate in ownership matching.

## 3. Test Cases (Sample Data)

The following cases are covered in [`sample-data/customer-identifiers-2.csv`](../sample-data/customer-identifiers-2.csv):

|    Request ID     | Case                 | Expected Status | Reason                                      |
|:-----------------:|:---------------------|:----------------|:--------------------------------------------|
|    **REQ-001**    | All Valid            | `IMPORTED`      | All identifiers match same CIF.             |
|    **REQ-002**    | Mismatch             | `IMPORTED`      | Valid identifiers but different CIFs.       |
|    **REQ-003**    | Partial Validity     | `IMPORTED`      | At least one identifier is valid.           |
|    **REQ-004**    | All Invalid          | `INVALID`       | Zero valid identifiers found.               |
|    **REQ-005**    | Bad Timestamp        | `INVALID`       | "yesterday" is not a valid ISO date.        |
|      **N/A**      | Missing ID           | `INVALID`       | `request_id` column is empty.               |
|    **REQ-007**    | Missing Customer     | `INVALID`       | `customer_reference` column is empty.       |
|    **REQ-008**    | Missing identifier_1 | `INVALID`       | `identifier_1` column is empty.             |
|  **REQ-010..17**  | Format Violations    | `INVALID`       | All identifiers in row are invalid formats. |
| **REQ-001** (2nd) | Duplicate            | `DUPLICATE`     | `request_id` already exists in file.        |
|    **REQ-019**    | Malformed            | `INVALID`       | Too few columns provided.                   |

### Data Overview:

- `totalRows`: 19
- `importedRows`: 3 (REQ-001, REQ-002, REQ-003)
- `duplicateRows`: 1 (The second REQ-001)
- `invalidRows`: 15 (All other failure cases)

## 4. Querying Results

* **Successfully Imported**: `GET /api/v1/account-checks/imported`
* **Validation Failures**: `GET /api/v1/account-checks/failed` (or `/failures`)
* **Batch Summary**: `GET /api/v1/account-checks/batches/{batchId}`
