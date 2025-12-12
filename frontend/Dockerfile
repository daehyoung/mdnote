# Build stage skipped (using local dist)
# FROM node:lts-alpine as build
# WORKDIR /app
# COPY package*.json ./
# RUN npm install
# COPY . .
# ENV NODE_OPTIONS=--max-old-space-size=4096
# RUN npm run build

FROM nginx:stable-alpine
COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
