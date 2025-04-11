#!/bin/bash

echo -e "Testing serial requests...\n\n"

# Test httpbin through proxy
curl -x http://localhost:8080 "http://httpbin.org/get"
curl -x http://localhost:8080 -X POST "http://httpbin.org/post" -H "Content-Type: application/json" -d '{"test": "data"}'
curl -x http://localhost:8080 -X PUT "http://httpbin.org/put" -H "Content-Type: application/json" -d '{"test": "update"}'
curl -x http://localhost:8080 -X PATCH "http://httpbin.org/patch" -H "Content-Type: application/json" -d '{"test": "patch"}'

# Test local server through proxy
curl -x http://localhost:8080 "http://localhost:4000/get?request_id=req1"
curl -x http://localhost:8080 -X POST "http://localhost:4000/post" -H "Content-Type: application/json" -d '{"request_id": "req2", "data": "test data"}'
curl -x http://localhost:8080 -X PUT "http://localhost:4000/put" -H "Content-Type: application/json" -d '{"request_id": "req3", "data": "update data"}'
curl -x http://localhost:8080 -X PATCH "http://localhost:4000/patch" -H "Content-Type: application/json" -d '{"request_id": "req4", "data": "update data"}'

echo -e "\n\nTesting parallel requests...\n\n"

# Test httpbin through proxy
curl -x http://localhost:8080 "http://httpbin.org/get" &
curl -x http://localhost:8080 -X POST "http://httpbin.org/post" -H "Content-Type: application/json" -d '{"test": "data"}' &
curl -x http://localhost:8080 -X PUT "http://httpbin.org/put" -H "Content-Type: application/json" -d '{"test": "update"}' &
curl -x http://localhost:8080 -X PATCH "http://httpbin.org/patch" -H "Content-Type: application/json" -d '{"test": "patch"}' &

# Test local server through proxy
curl -x http://localhost:8080 "http://localhost:4000/get?request_id=req1" &
curl -x http://localhost:8080 -X POST "http://localhost:4000/post" -H "Content-Type: application/json" -d '{"request_id": "req2", "data": "test data"}' &
curl -x http://localhost:8080 -X PUT "http://localhost:4000/put" -H "Content-Type: application/json" -d '{"request_id": "req3", "data": "update data"}' &
curl -x http://localhost:8080 -X PATCH "http://localhost:4000/patch" -H "Content-Type: application/json" -d '{"request_id": "req4", "data": "update data"}' &

wait
echo -e "\n\nAll tests completed!"
