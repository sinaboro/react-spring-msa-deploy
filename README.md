# React + Spring MSA 배포 실습 - Docker · AWS EC2 · GitHub Actions

[react-spring-mas-postgresql](https://github.com/sinaboro/react-spring-mas-postgresql) **기초 과정**의 MSA 로그인 게시판을
Docker 이미지로 만들어 Docker Hub 에 올리고, AWS EC2 에 배포한 뒤, **push 하면 자동으로 배포**되게(CI/CD) 하는 교안과 실습 코드입니다.

```text
git push ──▶ GitHub Actions ──▶ 이미지 4개 빌드 ──▶ Docker Hub ──▶ EC2 (docker compose pull / up -d)

EC2 안
  브라우저 ──▶ :80 frontend(nginx) ─┬─ 화면(React)
                                    └─ /api ──▶ api-gateway ──▶ user-service ──▶ db(userdb)
                                                            └─▶ post-service ──▶ db(postdb)
  (바깥에 열린 포트는 80 하나. 나머지는 도커 네트워크 안에서만 보인다)
```

## 폴더 구성

```text
backend/                     MSA 기초 과정 서비스 3개 + Dockerfile (서비스 하나를 골라 굽는 공용 파일)
frontend/                    React + Dockerfile(빌드 → nginx) + nginx.conf (/api 를 게이트웨이로)
db/init.sql                  userdb, postdb 만들기
docker-compose.yml           내 PC 에서 빌드해서 운영처럼 실행 (http://localhost)
deploy/docker-compose.prod.yml   EC2 에서 Docker Hub 이미지를 받아 실행
.github/workflows/deploy.yml     push → 빌드 → Docker Hub → EC2 배포
docs/guide/                  배포 교안 원고 (0~8장, 부록 A·B) → PDF · 웹 교안
tools/                       교안 빌드 (그림 make_figures, 캡처 강조 상자 annotate, PDF build_guide, 웹 build_site)
.env.example                 내 PC 용 .env 본보기 (DOCKERHUB_USERNAME)
```

## 교안

- 웹 교안: https://sinaboro.github.io/react-spring-msa-deploy/
- PDF: [docs/React_Spring_MSA_배포_학습교안.pdf](docs/React_Spring_MSA_배포_학습교안.pdf)

| 장 | 내용 |
|---|---|
| 0 | 과정 안내 - 배포 한눈에 보기, 계정 준비, 요금 |
| 1 | 배포를 위해 바꾼 코드 - `/api` 와 nginx, 환경 변수 |
| 2 | Docker 이미지 만들기 - multi-stage, 내 PC 에서 운영처럼 실행 |
| 3 | Docker Hub 에 올리기 - Access Token(Read & Write) |
| 4 | AWS EC2 만들기 - 서울 t3.micro Ubuntu, 키 페어, 보안 그룹 (화면 캡처) |
| 5 | 서버 준비와 첫 배포 - SSH, 스왑, Docker 설치, 손으로 배포 |
| 6 | GitHub Actions - Variables·Secrets, 워크플로 읽기 |
| 7 | 자동 배포 확인 - push → 서버 반영, IP 가 바뀔 때 |
| 8 | 정리와 삭제 - 요금이 나가지 않게, 더 나아가기 |
| A·B | 오류 해결, 확인 문제 풀이 |

## 원본(MSA 기초)과 달라진 코드

| 파일 | 바뀐 것 |
|---|---|
| `frontend/src/api.js` | API 주소 `http://localhost:8080/api` → `/api` |
| `frontend/vite.config.js` | 개발 서버가 `/api` 를 `localhost:8080` 으로 넘기는 proxy |
| `backend/*/application.yml` | DB 비밀번호를 환경 변수 `DB_PASSWORD` 로 |
| `docker-compose.yml`, `deploy/docker-compose.prod.yml` | 컨테이너 시간대 `TZ: Asia/Seoul` (없으면 글 작성 시각이 UTC) |
| 새 파일 | `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf`, compose 2개, 워크플로 |

## 내 PC 에서 실행해 보기

```powershell
docker compose up -d --build       # 이미지 4개 빌드 + DB 까지 5개 실행 (처음 몇 분)
# http://localhost
docker compose down                # 끄기
```

강사 김대철
