# 부록 A. 오류 해결

증상을 먼저 찾고, **어디서(내 PC / Actions 로그 / EC2)** 확인하는지 본 뒤 고친다. 서버 쪽 문제는 거의 언제나 이 두 명령에서 실마리가 나온다.

**[EC2]** 상태와 로그

```bash
cd ~/app
docker compose ps                       # 꺼진(Exited) 서비스가 있나, db 가 healthy 인가
docker compose logs --tail 50 post-service   # 마지막 50줄
```

## A-1. Docker · 내 PC

| 증상 | 원인 | 해결 |
|---|---|---|
| `docker compose up` 이 `error during connect` | Docker Desktop 이 꺼져 있다 | Docker Desktop 실행, 고래 아이콘이 멈출 때까지 기다리기 |
| `Bind for 0.0.0.0:80 failed: port is already allocated` | 80 번을 다른 프로그램(IIS, 다른 컨테이너)이 쓰고 있다 | `docker ps` 로 겹치는 컨테이너 끄기. 아니면 compose 의 `"80:80"` 을 `"8000:80"` 으로 바꾸고 http://localhost:8000 |
| http://localhost 에서 목록이 비고 콘솔에 502 | 서비스가 아직 켜지는 중 | 30초쯤 기다렸다 새로 고침. `docker compose logs api-gateway` |

## A-2. Docker Hub

| 증상 | 원인 | 해결 |
|---|---|---|
| push 할 때 `denied: requested access to the resource is denied` | 이미지 이름의 사용자 이름이 내 아이디가 아니다 (`local/...`, 강사 아이디) | `.env` 의 `DOCKERHUB_USERNAME` 확인 → `docker compose build` 다시 (3장) |
| Actions 로그에 `401 Unauthorized ... insufficient scopes` | 토큰 권한이 Read-only | **Read & Write** 토큰을 새로 만들어 `DOCKERHUB_TOKEN` Update |
| Actions 로그에 `incorrect username or password` | `DOCKERHUB_USERNAME`(Variables) 오타, 또는 토큰을 잘못 붙여 넣음 | 두 값을 다시 넣기. 토큰 앞뒤 빈칸 주의 |

## A-3. SSH · EC2 접속

| 증상 | 원인 | 해결 |
|---|---|---|
| `UNPROTECTED PRIVATE KEY FILE!` / `bad permissions` | `.pem` 을 다른 사용자도 읽을 수 있다 | 5-1 의 `icacls` 두 줄 |
| `Connection timed out` | 보안 그룹에 22 가 없다, IP 가 틀렸다, 인스턴스가 꺼졌다 | EC2 콘솔에서 상태·IP·보안 그룹 인바운드 규칙 확인 |
| `Permission denied (publickey)` | 사용자 이름이 틀렸다(`ubuntu` 인지), 다른 키 파일 | `ssh -i <맞는.pem> ubuntu@<IP>` |
| `REMOTE HOST IDENTIFICATION HAS CHANGED!` | 같은 IP 에 다른 서버(인스턴스를 새로 만들었거나 IP 가 재사용됨) | `ssh-keygen -R <IP>` 후 다시 접속 |
| `docker: permission denied ... docker.sock` | `usermod` 뒤에 다시 접속하지 않았다 | `exit` 후 다시 `ssh` |

## A-4. GitHub Actions

| 증상 (실패한 스텝) | 원인 | 해결 |
|---|---|---|
| 모든 잡이 회색(건너뜀) | Variables `DOCKERHUB_USERNAME` 이 없다 | 6-2 대로 Variables 넣기 |
| build 는 초록, deploy 만 회색 | Variables `EC2_HOST` 가 없다 | `EC2_HOST` 넣기 |
| SSH 키 준비 / 파일 복사 - `exit code 255` | `EC2_HOST` 가 예전 IP (EC2 를 중지·시작함), 인스턴스가 꺼짐 | 새 IP 로 `EC2_HOST` Update → Run workflow (7-4) |
| 파일 복사 - `Load key ... invalid format`, `Permission denied (publickey)` | `EC2_SSH_KEY` 에 .pem 일부만 붙여 넣음 | BEGIN 줄부터 END 줄까지 전체를 다시 넣기 |
| 새 이미지 받아서 다시 켜기 - `DB_PASSWORD 가 .env 에 없습니다` | Secrets `DB_PASSWORD` 또는 `JWT_SECRET` 이 없다 | Secrets 4개가 다 있는지 확인 |
| 배포 확인 - `3분 안에 응답이 없습니다` | 서비스가 켜지다 죽었다 (아래 A-5) | EC2 에서 `docker compose ps`, `logs` |

## A-5. EC2 위의 서비스

| 증상 (`docker compose logs`) | 원인 | 해결 |
|---|---|---|
| `password authentication failed for user "postgres"` | DB 를 처음 만든 비밀번호와 지금 `.env` 의 `DB_PASSWORD` 가 다르다 | `DB_PASSWORD` Secret 을 처음 값으로 되돌린다. 데이터를 버려도 되면 **[EC2]** `docker compose down -v` 후 Run workflow (볼륨을 지우고 새 비밀번호로 다시 만든다) |
| 서비스가 `Exited (137)` 로 꺼짐 | 메모리 부족으로 강제 종료(OOM) | `free -m` 으로 스왑이 켜졌는지 확인 (5-3). 재부팅 뒤 스왑이 없으면 `/etc/fstab` 줄 확인 |
| 로그인하면 바로 로그아웃 / 401 | user-service 와 api-gateway 의 `JWT_SECRET` 이 다르다 | 둘 다 같은 `.env` 의 `JWT_SECRET` 을 쓰는지 compose 파일 확인 |
| 글 작성 시각이 9시간 이르다 | 컨테이너 시간대가 UTC | compose 의 `TZ: Asia/Seoul` (7-1) |
| `no space left on device` | 옛 이미지가 쌓여 디스크가 찼다 | **[EC2]** `docker image prune -a -f`, 디스크를 20GiB 로 만들었는지 확인 |

## A-6. 브라우저

| 증상 | 원인 | 해결 |
|---|---|---|
| `http://IP` 가 https 로 바뀌며 열리지 않는다 | 브라우저의 "HTTPS 우선" 설정 | 주소창에 `http://` 를 직접 쓰기, 또는 그 사이트만 "안전하지 않은 콘텐츠 허용" |
| 고쳐서 배포했는데 화면이 그대로 | 브라우저 캐시 | **Ctrl+Shift+R** |
| `/posts/3` 에서 새로 고침하면 404 | nginx 의 `try_files` 가 빠졌다 | `frontend/nginx.conf` 의 `location /` 확인 (1장) |
