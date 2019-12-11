server {
  listen 80;
  listen [::]:80;
  server_name ststpcnt.com www.ststpcnt.com;

  root /home/aid/ststpcnt.com;

  index index.html index.html;

  location / {
    proxy_pass         http://localhost:8080/startstopcontinue/;
    proxy_redirect     http://ststpcnt.com/startstopcontinue/ /;
    proxy_redirect     http://www.ststpcnt.com/startstopcontinue/ /;
    proxy_http_version 1.1;
    proxy_set_header   X-Real-IP $remote_addr;
    proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header   Host $http_host;
    proxy_set_header   Upgrade $http_upgrade;
    proxy_set_header   Connection "Upgrade";
  }
}
