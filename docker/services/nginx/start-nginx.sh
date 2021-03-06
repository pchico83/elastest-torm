#!bin/bash

mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default-template.conf

echo 'ET_SECURITY ENV VAR:' $ET_SECURITY

if [ $ET_SECURITY = false ]
then
    echo 'Nginx without basic authentication'
else
    echo 'Nginx with basic authentication'
    htpasswd -b -c /etc/nginx/conf.d/nginx.htpasswd $ET_USER $ET_PASS
    
fi

# SSL Certificate
sudo mkdir /etc/nginx/ssl
sudo openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -subj '/CN=localhost/O=ElasTest/C=EU' -keyout /etc/nginx/ssl/nginx.key -out /etc/nginx/ssl/nginx.crt

#nginx -g "daemon off;"
envsubst < /etc/nginx/conf.d/default-template.conf > /etc/nginx/conf.d/default.conf 
rm /etc/nginx/conf.d/default-template.conf
nginx -g 'daemon off;' || cat /etc/nginx/conf.d/default.conf

sleep 60

