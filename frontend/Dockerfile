FROM node:22 as build

WORKDIR /app
COPY . .
RUN npm install && npm run build --prod

FROM nginx:alpine
# Copy built Angular app to NGINX's public directory
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
