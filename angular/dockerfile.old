FROM node:20 AS build
WORKDIR /app
COPY ./angular/package*.json ./angular/
COPY ./angular/src ./angular/src/
COPY ./angular/nginx.conf /etc/nginx/conf.d/default.conf
WORKDIR /app/angular
RUN npm install -g @angular/cli && npm install
WORKDIR /app
COPY . .

WORKDIR /app/angular
RUN npm run build

FROM nginx:alpine
# COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/angular/dist/buy-01 /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
