# Build Stage
FROM node:22 AS builder
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build:ssr

# Run Stage
FROM node:22-alpine
WORKDIR /app

# Copy browser + server builds separately
COPY --from=builder /app/dist/frontend /app/dist/frontend
COPY --from=builder /app/dist/frontend-server /app/dist/frontend-server

COPY --from=builder /app/package.json /app/
RUN npm install --only=production

# Run the SSR server
CMD ["node", "dist/frontend-server/main.js"]