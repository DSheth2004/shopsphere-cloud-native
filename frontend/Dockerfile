# Static storefront served by unprivileged nginx; /api is reverse-proxied to the API gateway.
FROM nginxinc/nginx-unprivileged:1.29-alpine
LABEL org.opencontainers.image.source="https://github.com/DSheth2004/shopsphere-cloud-native" \
      org.opencontainers.image.title="shopsphere-frontend"
USER root
RUN apk upgrade --no-cache
ENV API_GATEWAY_URL=http://api-gateway:8080
COPY --chown=nginx:nginx nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --chown=nginx:nginx public/ /usr/share/nginx/html/
USER 101
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD wget -qO- http://127.0.0.1:8080/healthz || exit 1
