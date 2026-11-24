# Dockerfile (frontend) - React 를 빌드해서 nginx 로 내려주는 이미지
#
# 두 단계(multi-stage)로 만든다.
#   1단계 build : Node.js 로 npm run build → dist 폴더 (HTML·JS·CSS 파일)
#   2단계 run   : 가벼운 nginx 이미지에 dist 와 nginx.conf 만 복사
#   → 최종 이미지에는 Node.js 도 node_modules 도 없다. 크기가 작고(약 50MB) 빠르다.

# ---------- 1단계: 빌드 ----------
FROM node:22-alpine AS build
WORKDIR /app

# package.json 을 먼저 복사해 npm ci 를 한다.
#   소스만 고치고 패키지는 그대로면, 이 단계는 Docker 가 저장해 둔 결과(캐시)를 다시 써서 빨라진다.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- 2단계: 실행 ----------
FROM nginx:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
