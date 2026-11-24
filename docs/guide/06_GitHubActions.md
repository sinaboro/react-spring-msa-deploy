# 6장. GitHub Actions - 비밀 값 넣기와 워크플로 읽기

## 학습 목표

- **CI/CD** 와 GitHub Actions 의 **워크플로·잡·스텝** 을 설명한다.
- 저장소에 **Variables**(비밀 아님)와 **Secrets**(비밀)를 나눠 넣는다.
- `deploy.yml` 을 위에서 아래로 읽으며, 5장에서 손으로 한 일과 짝을 맞춘다.

## 6-1. CI/CD 와 GitHub Actions

- **CI (Continuous Integration, 지속적 통합)**: push 할 때마다 자동으로 빌드·검사해서 "합친 코드가 깨지지 않았는지" 바로 안다.
- **CD (Continuous Delivery/Deployment, 지속적 배포)**: 빌드가 통과하면 서버까지 자동으로 내보낸다.

**GitHub Actions** 는 GitHub 이 빌려주는 컴퓨터(러너, runner)에서 우리가 적은 명령을 실행해 주는 서비스다. 명령은 저장소의 `.github/workflows/*.yml` 파일에 적는다. 공개 저장소는 무료다.

| 낱말 | 뜻 | 이 교안에서 |
|---|---|---|
| 워크플로(workflow) | yml 파일 하나. 언제(`on`) 무엇을 할지 | `deploy.yml` |
| 잡(job) | 러너 한 대에서 도는 일 묶음. 잡끼리는 따로 돈다 | `build` (4개), `deploy` |
| 스텝(step) | 잡 안에서 차례로 하는 한 가지 일 | "소스 받기", "SSH 키 준비" ... |
| 액션(action) | 남이 만들어 둔 스텝 (`uses:`) | `actions/checkout`, `docker/build-push-action` |
| 러너(runner) | 잡을 실행하는 GitHub 의 임시 컴퓨터 (Ubuntu) | 잡이 끝나면 사라진다 |

![deploy.yml 이 하는 일 - 이미지 4개를 동시에 빌드한 뒤 배포하고 확인한다](../images/fig_workflow_flow.png)

## 6-2. Variables 와 Secrets

러너는 매번 새로 만들어지는 남의 컴퓨터다. Docker Hub 토큰, EC2 열쇠, DB 비밀번호를 워크플로 파일에 적으면 공개 저장소에 그대로 드러난다. 그래서 값은 **저장소 설정**에 넣고, 워크플로에서는 이름으로만 부른다.

![비밀 값의 길 - GitHub Secrets 에서 러너를 거쳐 서버의 .env 로](../images/fig_secrets_flow.png)

| 종류 | 쓰는 법 | 로그에서 | 넣을 값 |
|---|---|---|---|
| **Variables** | `${{ vars.이름 }}` | 그대로 보인다 | `DOCKERHUB_USERNAME`, `EC2_HOST`, `EC2_USER` |
| **Secrets** | `${{ secrets.이름 }}` | `***` 로 가려진다 | `DOCKERHUB_TOKEN`, `EC2_SSH_KEY`, `DB_PASSWORD`, `JWT_SECRET` |

Secrets 는 **한 번 넣으면 GitHub 화면에서도 다시 볼 수 없다**(고치기만 된다). 그래서 5장의 `server.env` 처럼 값을 따로 메모해 두었다가 넣는다.

### Variables 넣기 (3개)

저장소 → **Settings** → 왼쪽 **Secrets and variables** → **Actions** → **Variables** 탭 → **New repository variable**.

![Variable 넣기 - Name 과 Value 를 적고 Add variable](../images/f01_variables_form.jpg)

| Name | Value (예시) |
|---|---|
| `DOCKERHUB_USERNAME` | `rlaeocjf` (내 Docker Hub 아이디) |
| `EC2_HOST` | `43.201.21.211` (4장에서 적은 퍼블릭 IP) |
| `EC2_USER` | `ubuntu` |

![Variables 를 넣은 모습 - 값이 그대로 보인다 (EC2_USER 는 아래에 이어진다)](../images/f02_variables_list.jpg)

### Secrets 넣기 (4개)

같은 화면의 **Secrets** 탭 → **New repository secret**.

![New secret - 이름과 값을 넣는다. 넣은 뒤에는 값을 다시 볼 수 없다](../images/f04_secret_new.jpg)

| Name | Secret 에 붙여 넣을 값 |
|---|---|
| `DOCKERHUB_TOKEN` | 3장에서 만든 Access Token (`dckr_pat_...`, **Read & Write**) |
| `EC2_SSH_KEY` | `msa-deploy-key.pem` 파일의 **내용 전체** |
| `DB_PASSWORD` | `server.env` 의 DB_PASSWORD 값 |
| `JWT_SECRET` | `server.env` 의 JWT_SECRET 값 |

**[PS]** `.pem` 내용을 클립보드에 복사하기 - 메모장으로 열어 복사해도 된다

```powershell
Get-Content $key -Raw | Set-Clipboard
```

> `$key` 는 5-1 에서 만든 변수다. PowerShell 창을 새로 열었다면 5-1 의 첫 줄(`$key = ...`)을 다시 친다.

`-----BEGIN RSA PRIVATE KEY-----` 줄부터 `-----END RSA PRIVATE KEY-----` 줄까지 **빠짐없이** 붙여 넣는다. 한 줄이라도 빠지면 6-4 의 SSH 스텝이 `Load key ... invalid format` 으로 실패한다.

> **DB_PASSWORD 는 5장의 값과 같아야 한다.** PostgreSQL 은 **처음 켤 때** 받은 비밀번호로 DB 를 만들고, 그 뒤로는 `POSTGRES_PASSWORD` 가 바뀌어도 DB 비밀번호를 바꾸지 않는다(데이터는 볼륨에 남아 있으므로). 다른 값을 넣으면 서비스가 `password authentication failed` 로 DB 에 접속하지 못한다. (부록 A)

![Secrets 4개를 넣은 모습 - 값은 보이지 않는다](../images/f05_secrets_list.jpg)

다 넣었으면 내 PC 의 `server.env` 와 메모장에 적어 둔 토큰은 지운다. 이제 비밀 값은 **GitHub Secrets 와 서버의 `.env`** 두 곳에만 있다.

## 6-3. 워크플로 읽기 ① - 언제, 그리고 build 잡

<!-- file: .github/workflows/deploy.yml -->

```yaml
# =====================================================================
# deploy.yml - main 브랜치에 push 하면 자동으로 "빌드 → Docker Hub → EC2 배포"
#
# 흐름
#   ① build  : 이미지 4개를 동시에(matrix) 만들어 Docker Hub 에 올린다
#   ② deploy : EC2 에 SSH 로 접속해 새 이미지를 받아(pull) 다시 켠다(up -d)
#   ③ 확인   : http://EC2주소/api/posts 가 200 을 돌려줄 때까지 기다린다
#
# 저장소 Settings → Secrets and variables → Actions 에 미리 넣어 둘 값
#   Variables (비밀 아님, 로그에 그대로 보임)
#     DOCKERHUB_USERNAME  Docker Hub 사용자 이름        예) rlaeocjf
#     EC2_HOST            EC2 공인 IP 또는 주소          예) 3.35.xx.xx
#     EC2_USER            EC2 로그인 사용자              Ubuntu 이미지는 ubuntu
#   Secrets (비밀, 로그에 *** 로 가려짐)
#     DOCKERHUB_TOKEN     Docker Hub Access Token (Read & Write)
#     EC2_SSH_KEY         EC2 키 페어 .pem 파일의 내용 전체
#     DB_PASSWORD         운영 DB 비밀번호 (아무 긴 글자)
#     JWT_SECRET          JWT 서명 열쇠 (32글자 이상)
# =====================================================================

name: deploy

on:
  push:
    branches: [main]
  workflow_dispatch:          # Actions 화면의 "Run workflow" 단추로 직접 실행할 수도 있게

jobs:
  # ---------------------------------------------------------------- ① 이미지 빌드·업로드
  build:
    # 아직 Variables 를 넣지 않았으면(저장소를 막 만들었거나 포크했을 때) 건너뛴다 → 빨간 실패 표시가 생기지 않는다
    if: ${{ vars.DOCKERHUB_USERNAME != '' }}
    runs-on: ubuntu-latest
    strategy:
      matrix:                 # 아래 4가지를 동시에 실행한다 (작업 4개가 나란히 돈다)
        include:
          - image: msa-user-service
            context: backend
            service: user-service
          - image: msa-post-service
            context: backend
            service: post-service
          - image: msa-api-gateway
            context: backend
            service: api-gateway
          - image: msa-frontend
            context: frontend
            service: ""
    steps:
      - name: 소스 받기
        uses: actions/checkout@v5

      - name: Docker Buildx 준비
        uses: docker/setup-buildx-action@v4

      - name: Docker Hub 로그인
        uses: docker/login-action@v4
        with:
          username: ${{ vars.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: 이미지 빌드 후 Docker Hub 에 올리기
        uses: docker/build-push-action@v7
        with:
          context: ${{ matrix.context }}
          build-args: SERVICE=${{ matrix.service }}
          push: true
          # latest(늘 최신) + 커밋 번호(어느 코드로 만든 이미지인지 남기기) 두 가지 이름을 붙인다
          tags: |
            ${{ vars.DOCKERHUB_USERNAME }}/${{ matrix.image }}:latest
            ${{ vars.DOCKERHUB_USERNAME }}/${{ matrix.image }}:${{ github.sha }}
          # 빌드 중간 결과를 GitHub 에 저장해 두고 다음 빌드에서 다시 쓴다 (두 번째부터 빨라진다)
          cache-from: type=gha,scope=${{ matrix.image }}
          cache-to: type=gha,mode=max,scope=${{ matrix.image }}

  # ---------------------------------------------------------------- ② EC2 배포
  deploy:
    needs: build              # ① 이 4개 모두 성공해야 시작한다
    if: ${{ vars.EC2_HOST != '' }}   # EC2 를 아직 만들지 않았으면 배포는 건너뛴다 (이미지만 올린다)
    runs-on: ubuntu-latest
    env:
      SSH_HOST: ${{ vars.EC2_HOST }}
      SSH_USER: ${{ vars.EC2_USER }}
    steps:
      - name: 소스 받기 (서버에 보낼 compose 파일, init.sql)
        uses: actions/checkout@v5

      - name: SSH 키 준비
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.EC2_SSH_KEY }}" > ~/.ssh/ec2.pem
          chmod 600 ~/.ssh/ec2.pem
          # 서버의 지문(host key)을 미리 등록해 "정말 접속할까요? (yes/no)" 질문을 없앤다
          ssh-keyscan -H "$SSH_HOST" >> ~/.ssh/known_hosts

      - name: 서버에 보낼 .env 만들기 (비밀 값은 Secrets 에서)
        run: |
          cat > .env <<EOF
          DOCKERHUB_USERNAME=${{ vars.DOCKERHUB_USERNAME }}
          DB_PASSWORD=${{ secrets.DB_PASSWORD }}
          JWT_SECRET=${{ secrets.JWT_SECRET }}
          EOF

      - name: 파일 복사 (compose, .env, init.sql)
        run: |
          ssh -i ~/.ssh/ec2.pem "$SSH_USER@$SSH_HOST" "mkdir -p ~/app/db"
          scp -i ~/.ssh/ec2.pem deploy/docker-compose.prod.yml "$SSH_USER@$SSH_HOST:~/app/docker-compose.yml"
          scp -i ~/.ssh/ec2.pem .env "$SSH_USER@$SSH_HOST:~/app/.env"
          scp -i ~/.ssh/ec2.pem db/init.sql "$SSH_USER@$SSH_HOST:~/app/db/init.sql"

      - name: 새 이미지 받아서 다시 켜기
        run: |
          ssh -i ~/.ssh/ec2.pem "$SSH_USER@$SSH_HOST" "cd ~/app && docker compose pull && docker compose up -d && docker image prune -f"

      # ---------------------------------------------------------------- ③ 확인
      - name: 배포 확인 (최대 3분 기다림)
        run: |
          for i in $(seq 1 36); do
            code=$(curl -s -o /dev/null -w "%{http_code}" "http://$SSH_HOST/api/posts" || true)
            echo "$i 번째 확인: $code"
            if [ "$code" = "200" ]; then echo "배포 성공! http://$SSH_HOST"; exit 0; fi
            sleep 5
          done
          echo "3분 안에 응답이 없습니다. 서버에서 docker compose logs 를 확인하세요."
          exit 1
```

위에서부터 끊어 읽자.

**`on:` - 언제 도나**

- `push: branches: [main]` : main 에 push 하면 돈다.
- `workflow_dispatch:` : Actions 화면의 **Run workflow** 단추로 손으로 돌릴 수도 있다. (코드는 그대로인데 다시 배포하고 싶을 때, 7장)

**`build` 잡 - 이미지 4개를 동시에**

- `if: ${{ vars.DOCKERHUB_USERNAME != '' }}` : Variables 를 아직 안 넣었으면 건너뛴다. 저장소를 막 Fork 했을 때 빨간 실패 표시 대신 회색 "건너뜀" 이 된다.
- `strategy: matrix: include:` : 아래 4가지 값으로 **같은 잡을 4번, 동시에** 돌린다. 스텝 안의 `${{ matrix.image }}` 같은 곳이 차례로 바뀐다. 2장의 compose 파일에서 서비스마다 적었던 `context`, `SERVICE` 와 똑같은 값이다.
- 스텝 4개는 순서대로 **소스 받기 → Buildx 준비 → Docker Hub 로그인 → 빌드하고 올리기** 다. 3장에서 손으로 한 `docker login`, `docker compose build`, `push` 와 같다.
- `tags:` 에 `latest` 와 `${{ github.sha }}`(커밋 번호) 두 이름을 붙인다. 서버는 `latest` 를 받고, 커밋 번호 태그는 "이 이미지가 어느 코드로 만들어졌나" 를 남긴다. 문제가 생기면 예전 커밋의 이미지로 되돌릴 수 있다(8장).
- `cache-from / cache-to: type=gha` : 2장의 Docker 캐시를 GitHub 에 저장해 두고 다음 빌드에서 다시 쓴다. 첫 빌드는 3분쯤, 그다음부터는 소스만 바뀌면 1분 안쪽으로 줄어든다.

## 6-4. 워크플로 읽기 ② - deploy 잡

**`needs: build`** : build 4개가 **모두 성공해야** 시작한다. 하나라도 실패하면 서버는 건드리지 않는다. 반쯤 바뀐 서버가 생기지 않게 막는 장치다.

**`if: ${{ vars.EC2_HOST != '' }}`** : EC2 를 아직 만들지 않았으면 이미지만 올리고 배포는 건너뛴다.

스텝을 5장과 짝지어 보면 이렇다.

| deploy 잡의 스텝 | 5장에서 손으로 한 일 |
|---|---|
| SSH 키 준비 - Secret 을 `~/.ssh/ec2.pem` 에 쓰고 `chmod 600` | `.pem` 내려받기, `icacls` 로 권한 좁히기 |
| SSH 키 준비 - `ssh-keyscan` 으로 서버 지문 등록 | 처음 접속할 때 `yes` 치기 |
| 서버에 보낼 `.env` 만들기 (Variables + Secrets) | `server.env` 만들기 |
| 파일 복사 - `ssh mkdir`, `scp` 3번 | 똑같다 |
| 새 이미지 받아서 다시 켜기 - `pull && up -d && image prune -f` | `docker compose pull`, `up -d` |
| 배포 확인 - `/api/posts` 가 200 이 될 때까지 5초마다 | 브라우저로 열어 보기, 502 면 기다리기 |

두 가지만 더 보자.

- **`docker compose up -d`** 는 **이미지가 바뀐 컨테이너만** 새로 만든다. db 처럼 그대로인 컨테이너는 건드리지 않는다. 볼륨(`db_data`)도 그대로라 **글과 회원은 남는다.**
- **`docker image prune -f`** 는 새 이미지에 이름(`latest`)을 넘겨주고 이름 없이 남은 옛 이미지를 지운다. 배포할 때마다 590MB 씩 쌓여 디스크가 차는 것을 막는다.

> **[배포 개념] 배포 확인까지가 배포다.** `up -d` 는 "컨테이너를 켰다" 일 뿐, Spring 이 다 켜졌는지는 모른다. 마지막 스텝이 실제 주소로 요청을 보내 200 을 받아야 초록색 성공이 된다. 3분 안에 200 이 오지 않으면 빨간 실패로 알려 준다.

## 6-5. 첫 실행

Variables 와 Secrets 를 다 넣었으면 워크플로를 돌려 보자. 아직 코드를 고치지 않았으니 **Run workflow** 단추로 손으로 돌린다. (7장에서 push 로 돌린다)

저장소 → **Actions** → 왼쪽 **deploy** → 오른쪽 **Run workflow** → 브랜치 `main` → **Run workflow**.

![Run workflow - main 브랜치로 손으로 실행](../images/g03_run_dispatch.jpg)

잠시 뒤 목록 맨 위에 새 실행이 생긴다. 눌러 들어가면 build 4개가 동시에 돌고, 다 끝나면 deploy 가 이어진다.

![성공 - build 4개와 deploy 모두 초록색. 전체 약 2분](../images/g04_run_success.jpg)

**deploy** 잡을 눌러 마지막 스텝 **배포 확인** 을 펼쳐 보자.

![배포 확인 로그 - 502, 503 으로 기다리다 200 이 되면 성공](../images/g05_deploy_log.jpg)

```text
1 번째 확인: 502
2 번째 확인: 502
3 번째 확인: 502
4 번째 확인: 503
5 번째 확인: 200
배포 성공! http://3.35.37.211
```

- **502** : nginx 는 켜졌는데 api-gateway 가 아직 켜지는 중이다.
- **503** : 게이트웨이는 켜졌는데 뒤의 서비스(post-service)가 아직 준비 중이다.
- **200** : 모두 켜졌다.

이 숫자가 바뀌어 가는 모습이 1GB 서버에서 Spring 서비스 3개가 하나씩 깨어나는 과정이다.

> **교안을 만들 때 겪은 실패** - Secrets 를 넣고 처음 돈 실행(#2)은 build 가 `insufficient scopes` 로 실패했다. Docker Hub 토큰을 Read-only 로 만들었기 때문이다(3장). 토큰을 Read & Write 로 새로 만들어 `DOCKERHUB_TOKEN` 을 고친(Update) 뒤 다시 돌려 성공했다. 실패한 실행은 그 화면의 **Re-run all jobs** 로 다시 돌릴 수 있다.

## 확인 문제

1. Variables 와 Secrets 는 어떻게 다른가? `EC2_HOST` 를 Variables 에 둔 까닭은?
2. build 잡 4개 중 frontend 만 실패하면 deploy 잡은 어떻게 되나?
3. 배포 확인 로그의 502 와 503 은 각각 무엇을 뜻하나?
4. `DB_PASSWORD` Secret 을 5장에서 쓴 값과 다르게 넣으면 어떤 일이 생기나?
