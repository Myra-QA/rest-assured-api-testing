# REST Assured API Testing

[![Java API Tests](https://github.com/Myra-QA/rest-assured-api-testing/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/Myra-QA/rest-assured-api-testing/actions/workflows/maven.yml)

A Java API automation project using REST Assured, JUnit 5, and Maven, with [DummyJSON](https://dummyjson.com/) as the practice API.

## Tech Stack
- Java 17
- REST Assured
- JUnit 5
- Maven
- JSON Schema Validation
- GitHub Actions

## Test Coverage
- CRUD operations for carts
- Authentication and authorization
- Response validation (status codes, body, headers, and response time)
- Negative and boundary testing
- Pagination
- JSON Schema validation

## Project Structure
```text
src/test/
├── java/
│   ├── config/       # Request specifications
│   ├── helpers/      # Authentication helper
│   ├── testdata/     # Test payloads
│   └── tests/        # API test cases
└── resources/
    └── schemas/      # JSON schemas
```

## Run Tests

Run all tests:
```bash
mvn clean test
```

Generate the HTML test report:
```bash
mvn surefire-report:report
```

The HTML report is generated at `target/reports/surefire.html`.

## CI

GitHub Actions runs the test suite on pushes and pull requests. Surefire reports are uploaded as workflow artifacts.

## Notes

DummyJSON simulates certain write operations. Successful POST, PUT, and DELETE responses do not necessarily mean that data is persisted.