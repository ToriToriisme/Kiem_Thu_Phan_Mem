# ===== Stage 1: Build =====
# Dùng Node để cài dependencies và build ra file tĩnh (HTML/CSS/JS)
FROM node:22-alpine AS build

WORKDIR /app

# Copy package.json trước để Docker cache layer này -
# lần build sau nếu không đổi dependencies thì khỏi npm install lại
COPY package*.json ./
RUN npm install

# Copy toàn bộ source code còn lại rồi build
COPY . .
RUN npm run build

# ===== Stage 2: Serve =====
# Image cuối cùng chỉ chứa Nginx + file tĩnh đã build - rất nhẹ,
# không mang theo node_modules hay source code gốc
FROM nginx:alpine

# Dùng config Nginx riêng để xử lý client-side routing của React Router
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy kết quả build từ Stage 1 sang
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]