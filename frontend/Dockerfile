# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve the production static files using Nginx
FROM nginx:stable-alpine
# Copy the compiled static assets from Stage 1 to Nginx public folder
COPY --from=build /app/dist /usr/share/nginx/html
# Copy custom nginx configuration to handle routing if needed
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
