# 3장. Docker Hub 에 이미지 올리기

## 학습 목표

- 이미지 이름 `사용자이름/저장소:태그` 의 뜻을 안다.
- Docker Hub **Access Token** 을 만들고, 권한(Read & Write)을 바르게 고른다.
- 내 PC 에서 만든 이미지 4개를 Docker Hub 에 올린다(push).

## 3-1. 이미지 이름 읽기

```text
rlaeocjf / msa-user-service : latest
└─ 사용자 이름  └─ 저장소(이미지) 이름  └─ 태그(버전 이름)
```

- **사용자 이름**: Docker Hub 계정 이름. 다른 사람의 이름으로는 올릴 수 없다.
- **저장소 이름**: 이미지마다 하나. 처음 push 하면 Docker Hub 에 저절로 생긴다.
- **태그**: 같은 저장소 안의 버전 이름. `latest` 는 "가장 최근" 이라는 약속일 뿐 특별한 기능은 없다. 6장의 GitHub Actions 는 `latest` 와 함께 **커밋 번호** 태그도 붙여서 "어느 코드로 만든 이미지인지" 를 남긴다.

`postgres:18` 처럼 사용자 이름이 없는 이미지는 Docker 가 관리하는 **공식 이미지**다.

## 3-2. Access Token 만들기

Docker Hub 에 로그인할 때 비밀번호 대신 **Access Token**(접근 토큰)을 쓴다. 토큰은 권한을 좁게 줄 수 있고, 잃어버리면 그 토큰만 지우면 된다. 6장에서 GitHub Actions 도 이 토큰으로 로그인한다.

1. https://hub.docker.com 에 로그인한다.
2. 오른쪽 위 프로필 → **Account settings** → 왼쪽 **Personal access tokens** → **Generate new token**.
3. 설명(Description)에 `msa-deploy` 처럼 적는다. 만료 기간은 실습 기간에 맞게 고른다.
4. **Access permissions 는 `Read & Write`** 를 고른다. ← 가장 중요
5. **Generate** → 나온 토큰(`dckr_pat_...`)을 복사한다. **이 화면을 닫으면 다시 볼 수 없다.** 메모장에 잠깐 붙여 두고, 6장에서 GitHub Secrets 에 넣은 뒤 지운다.

> **권한을 `Read-only` 로 만들면 push 가 막힌다.** 교안을 만들 때 실제로 겪은 실수다. GitHub Actions 로그에 이런 줄이 나온다.
>
> `unexpected status from HEAD request to ...: 401 Unauthorized ... insufficient scopes`
>
> 로그인(login)은 성공하므로 헷갈리기 쉽다. 토큰을 `Read & Write` 로 새로 만들어 바꾸면 된다.

토큰은 비밀번호와 같다. 화면 캡처, 카톡, GitHub 코드 어디에도 남기지 않는다.

## 3-3. 로그인하고 올리기

**[PS]** Docker Hub 로그인 - 비밀번호를 물으면 **토큰**을 붙여 넣는다 (화면에 보이지 않는 것이 정상)

```powershell
docker login -u <내DockerHub아이디>
```

```text
Password:
Login Succeeded
```

2장에서 만든 이미지 이름은 `local/msa-...` 였다. 내 사용자 이름으로 다시 굽자. 저장소 폴더에 `.env` 파일을 만들면 compose 가 자동으로 읽는다.

**[PS]** `.env` 만들기 - `.env.example` 을 복사해서 사용자 이름만 바꾼다

```powershell
copy .env.example .env
notepad .env          # DOCKERHUB_USERNAME=<내DockerHub아이디> 로 고치고 저장
docker compose config --images
```

```text
rlaeocjf/msa-user-service:latest
rlaeocjf/msa-post-service:latest
rlaeocjf/msa-api-gateway:latest
rlaeocjf/msa-frontend:latest
postgres:18
```

(줄의 순서는 위와 다를 수 있다) 이름이 내 아이디로 바뀌었으면 빌드하고 올린다. 2장에서 한 번 빌드했으므로 캐시 덕분에 빠르다.

**[PS]** 빌드 후 4개만 push (postgres:18 은 남의 공식 이미지라 올리지 않는다)

```powershell
docker compose build
docker compose push user-service post-service api-gateway frontend
```

```text
[+] Pushing 4/4
 ✔ Pushing rlaeocjf/msa-user-service:latest
 ✔ Pushing rlaeocjf/msa-post-service:latest
 ✔ Pushing rlaeocjf/msa-api-gateway:latest
 ✔ Pushing rlaeocjf/msa-frontend:latest
```

> 서비스 이름을 적어 **우리 것 4개만** 고른다. `db` 의 `postgres:18` 은 남의 이미지라 우리 계정으로 올릴 수 없고, 올릴 까닭도 없다. 서버도 Docker Hub 에서 공식 이미지를 바로 받는다.

https://hub.docker.com 의 내 프로필(Repositories)에 저장소 4개가 생겼다. 처음 push 한 저장소는 **공개(Public)** 로 만들어진다. 공개 이미지는 누구나 받을(pull) 수 있지만, 올리기(push)는 나만 할 수 있다.

![Docker Hub - 처음 push 하면 저장소가 저절로 생긴다 (공개)](../images/c03_dockerhub_repos.jpg)

> **[배포 개념] 이미지 창고(레지스트리)가 있으면 빌드와 실행을 나눌 수 있다.** 빌드는 힘이 센 컴퓨터(내 PC, GitHub Actions)에서 하고, 메모리 1GB 짜리 서버는 **다 만든 이미지를 받아서 켜기만** 한다. 서버에 JDK, Gradle, Node.js, 소스 코드가 하나도 필요 없다.

> **이미지 안에 비밀 값이 있으면 안 된다.** 공개 이미지는 누구나 받아서 열어 볼 수 있다. 이 교안의 이미지에는 비밀번호·열쇠가 없고, 서버에서 켤 때 환경 변수로 넣는다(1장). Dockerfile 에 `ENV DB_PASSWORD=...` 같은 줄을 쓰지 않는 까닭이다.

## 확인 문제

1. `rlaeocjf/msa-frontend:latest` 에서 사용자 이름, 저장소 이름, 태그는 각각 무엇인가?
2. `docker login` 은 성공했는데 push 할 때 `insufficient scopes` 가 나왔다. 무엇을 고쳐야 하나?
3. 공개 저장소에 올린 이미지에 DB 비밀번호가 들어 있으면 왜 위험한가?
