# Build Stage
FROM node:18-alpine as builder

WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build --prod --skip-tests

# Production Stage
FROM nginx:alpine

COPY --from=builder /app/dist/browser /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 1001
