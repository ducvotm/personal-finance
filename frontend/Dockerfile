# Build stage for running tests
FROM node:20-alpine AS test
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run test:run

# Build stage for production
FROM node:20-alpine AS production
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production stage with nginx
FROM nginx:alpine
COPY --from=production /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]