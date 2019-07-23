FROM alpine:latest AS correctedAlpineScripts

RUN apk upgrade --update && \
    apk add \
        git \
        dos2unix && \
    git clone https://github.com/blacklabelops-legacy/alpine.git && \
    dos2unix /alpine/imagescripts/*.sh



FROM alpine:latest AS my-alpine

# Reprint of blacklabelops/alpine, to force arch-specific image

ARG ALPINE_IMAGE_BUILD_DATE=undefined

ENV BLACKLABELOPS_HOME=/var/blacklabelops

RUN apk upgrade --update && \
    apk add \
      bash \
      tini \
      su-exec \
      curl && \
    # Network fix
    echo 'hosts: files mdns4_minimal [NOTFOUND=return] dns mdns4' >> /etc/nsswitch.conf && \
    # Wait for it - Waiting For Available Ports And Servers
    curl https://raw.githubusercontent.com/vishnubob/wait-for-it/55c54a5abdfb32637b563b28cc088314b162195e/wait-for-it.sh -o /usr/bin/wait-for-it && \
    # Blacklabelops Feature Script Folder
    mkdir -p ${BLACKLABELOPS_HOME} && \
    # Clean up
    apk del curl && \
    rm -rf /var/cache/apk/* && \
    rm -rf /tmp/* && \
    rm -rf /var/log/*

COPY --from=correctedAlpineScripts /alpine/imagescripts/ ${BLACKLABELOPS_HOME}/

LABEL maintainer="Steffen Bleul <sbl@blacklabelops.com>" \
      com.blacklabelops.image.name.alpine="alpine-base-image" \
      con.blacklabelops.image.builddate.alpine=${ALPINE_IMAGE_BUILD_DATE}


FROM alpine:latest AS correctedNginxScripts

RUN apk upgrade --update && \
    apk add \
        git \
        dos2unix && \
    git clone https://github.com/blacklabelops-legacy/nginx.git && \
    dos2unix /nginx/imagescripts/*.sh


FROM my-alpine

# Reprint of blacklabelops/nginx

# Build time arguments
#Values: latest or version number (e.g. 1.8.1-r0)
ARG NGINX_VERSION=latest
#Permissions, set the linux user id and group id
ARG CONTAINER_UID=1000
ARG CONTAINER_GID=1000
# Image Build Date By Buildsystem
ARG BUILD_DATE=undefined

# install dev tools
ENV NGINX_DIRECTORY=/etc/nginx

RUN export CONTAINER_USER=nginx && \
    export CONTAINER_GROUP=nginx && \
    # Add user
    addgroup -g $CONTAINER_GID $CONTAINER_GROUP && \
    adduser -u $CONTAINER_UID -G $CONTAINER_GROUP -h /home/$CONTAINER_USER -s /bin/bash -S $CONTAINER_USER && \
    apk add --update \
      ca-certificates \
      curl \
      openssl \
      apache2-utils && \
    if  [ "${NGINX_VERSION}" = "latest" ]; \
      then apk add nginx ; \
      else apk add "nginx=${NGINX_VERSION}" ; \
    fi && \
    rm -f /${NGINX_DIRECTORY}/nginx.conf && \
    rm -rf ${NGINX_DIRECTORY}/conf.d ${NGINX_DIRECTORY}/default.d && \
    mkdir -p /var/log/nginx && \
    touch /var/log/nginx/error.log && \
    mkdir -p ${NGINX_DIRECTORY}/default.d && \
    mkdir -p ${NGINX_DIRECTORY}/conf.d && \
    mkdir -p ${NGINX_DIRECTORY}/keys && \
    mkdir -p /run/nginx && \
    chown -R $CONTAINER_UID:$CONTAINER_GID ${NGINX_DIRECTORY} /var/log/nginx && \
    chown -R $CONTAINER_UID:$CONTAINER_GID ${NGINX_DIRECTORY} /var/lib/nginx && \
    chown -R $CONTAINER_UID:$CONTAINER_GID ${NGINX_DIRECTORY} /run/nginx && \
    rm -rf /var/cache/apk/* && rm -rf /tmp/*

# Image Metadata
LABEL com.blacklabelops.application.nginx.version=$NGINX_VERSION \
      com.blacklabelops.application.nginx.userid=$CONTAINER_UID \
      com.blacklabelops.application.nginx.groupid=$CONTAINER_GID \
      com.blacklabelops.image.builddate.nginx=${BUILD_DATE}

EXPOSE 80 443

USER root
COPY --from=correctedNginxScripts /nginx/imagescripts/*.sh /opt/nginx/

ENTRYPOINT ["/sbin/tini","--","/opt/nginx/docker-entrypoint.sh"]
VOLUME ["/home/nginx","/var/log/nginx"]
CMD ["nginx"]
