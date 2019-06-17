#
FROM rabbitmq:management-alpine
#
COPY "rays-routes.json" "/etc/rabbitmq/rays-routes.json"
RUN ["echo", "'management.load_definitions = /etc/rabbitmq/rays-routes.json'", ">>", "/etc/rabbitmq/rabbitmq.conf"]
#