#!/bin/sh
echo "SSL_CERT_NAME: $SSL_CERT_NAME"
echo "SSL_CERT_KEY_NAME: $SSL_CERT_KEY_NAME"
envsubst '\$SSL_CERT_NAME \$SSL_CERT_KEY_NAME' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
exec "$@"
