// vite.config.js - Vite(개발 서버·빌드 도구) 설정
//   plugins: [react()] → .jsx 파일의 JSX 를 브라우저가 이해하는 자바스크립트로 바꿔 준다.
//   개발 서버 주소는 기본값 http://localhost:5173
//
// [배포] server.proxy - 개발할 때(npm run dev)만 쓰이는 설정
//   React 가 "/api/posts" 로 요청하면 Vite 개발 서버가 대신 http://localhost:8080/api/posts (게이트웨이)로 보낸다.
//   운영(Docker)에서는 이 설정 대신 nginx 가 같은 일을 한다 (nginx.conf).
//   → 개발과 운영에서 React 코드(api.js 의 API_URL = "/api")가 똑같다.
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
