from http.server import HTTPServer, BaseHTTPRequestHandler
import json
from urllib.parse import parse_qs, urlparse
import os

class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    def _send_response(self, status_code=200):
        self.send_response(status_code)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()

    def _read_body(self):
        content_length = int(self.headers.get('Content-Length', 0))
        return self.rfile.read(content_length) if content_length > 0 else None

    def _handle_request(self):
        parsed_url = urlparse(self.path)
        query_params = parse_qs(parsed_url.query)
        
        body = self._read_body()
        body_str = body.decode('utf-8') if body else None
        
        try:
            json_body = json.loads(body_str) if body_str else None
        except:
            json_body = None

        response = {
            "method": self.command,
            "path": parsed_url.path,
            "headers": dict(self.headers),
            "query_params": query_params,
            "body": body_str,
            "json": json_body,
            "client_address": self.client_address[0]
        }

        self._send_response()
        self.wfile.write(json.dumps(response, indent=2).encode())

    def do_GET(self):
        self._handle_request()

    def do_POST(self):
        self._handle_request()

    def do_PUT(self):
        self._handle_request()

    def do_PATCH(self):
        self._handle_request()

def run_server(port=None):
    if port is None:
        port = int(os.getenv('PORT', 4000))
    server_address = ('', port)
    httpd = HTTPServer(server_address, SimpleHTTPRequestHandler)
    print(f'Starting server on port {port}...')
    httpd.serve_forever()

if __name__ == '__main__':
    run_server()
