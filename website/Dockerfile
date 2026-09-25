FROM node:26-alpine AS build
WORKDIR /repo

COPY website/package.json website/package-lock.json website/
RUN cd website && npm ci

COPY doc/ doc/
COPY CHANGELOG.md .
COPY website/ website/

WORKDIR /repo/website
RUN npm run generate

FROM nginx:alpine
COPY --from=build /repo/website/.output/public /usr/share/nginx/html
COPY website/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
