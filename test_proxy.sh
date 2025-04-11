# Script to test the handling of parallel requests to the proxy client

# GET request
curl -x http://localhost:8080 "http://httpbin.org/get" &

# POST request
curl -x http://localhost:8080 -X POST "http://httpbin.org/post" \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}' &

# PUT request
curl -x http://localhost:8080 -X PUT "http://httpbin.org/put" \
  -H "Content-Type: application/json" \
  -d '{"test": "update"}' &

# PATCH request
curl -x http://localhost:8080 -X PATCH "http://httpbin.org/patch" \
  -H "Content-Type: application/json" \
  -d '{"test": "patch"}' &

# Wait for all requests to complete
wait

echo -e "\n\nAll requests from httpbin completed"

# GET request
curl -x http://localhost:8080 "http://localhost:4000/get?request_id=req1" &

# POST request
curl -x http://localhost:8080 -X POST "http://localhost:4000/post" \
  -H "Content-Type: application/json" \
  -d '{"request_id": "req2", "data": "test data"}' &

# PUT request
curl -x http://localhost:8080 -X PUT "http://localhost:4000/put" \
  -H "Content-Type: application/json" \
  -d '{"request_id": "req3", "data": "update data"}' &

# PATCH request
curl -x http://localhost:8080 -X PATCH "http://localhost:4000/patch" \
  -H "Content-Type: application/json" \
  -d '{"request_id": "req4", "data": "update data"}' &

# Wait for all requests to complete
wait

echo -e "\n\nAll requests from simple server completed"