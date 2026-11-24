"""배포 교안에 들어가는 개념 그림을 만든다 (docs/images/fig_*.png).

    python tools/make_figures.py

matplotlib 으로 상자와 화살표를 그린다. 글꼴은 맑은 고딕(Windows 기본).
화면 캡처(d*, e*, f*, g*, h*.jpg)는 이 파일이 아니라 실제 AWS·GitHub 화면을 찍은 것이다.
"""
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyArrowPatch, FancyBboxPatch

plt.rcParams["font.family"] = "Malgun Gothic"
plt.rcParams["axes.unicode_minus"] = False

OUT = Path(__file__).resolve().parent.parent / "docs" / "images"
OUT.mkdir(parents=True, exist_ok=True)

NAVY, BLUE, CYAN, GREEN, ORANGE, RED, GREY = "#1f3a5f", "#03588C", "#61dafb", "#2e7d32", "#e8890c", "#c0392b", "#6b7280"
FILL = {"navy": "#e3ebf5", "cyan": "#e0f7fd", "green": "#e6f4ea", "orange": "#fff1dc", "red": "#fdecea",
        "grey": "#f1f3f5", "white": "#ffffff", "teal": "#e0f5f3"}
EDGE = {"navy": NAVY, "cyan": "#1aa3c8", "green": GREEN, "orange": ORANGE, "red": RED, "grey": GREY,
        "white": GREY, "teal": "#1f9e93"}


def canvas(w, h):
    """가로 w, 세로 h 인치 그림. 좌표는 (0~w*10, 0~h*10)"""
    fig, ax = plt.subplots(figsize=(w, h), dpi=150)
    ax.set_xlim(0, w * 10)
    ax.set_ylim(0, h * 10)
    ax.axis("off")
    return fig, ax


def box(ax, x, y, w, h, text, color="navy", size=10, bold=False, align="center", dashed=False, va="center"):
    """(x, y) 는 왼쪽 아래. text 는 여러 줄 가능."""
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.3,rounding_size=1.2",
                                fc=FILL[color], ec=EDGE[color], lw=1.4, clip_on=False,
                                linestyle="--" if dashed else "-"))
    tx = x + w / 2 if align == "center" else x + 1.2
    ty = y + h / 2 if va == "center" else y + h - 1.2
    ax.text(tx, ty, text, ha=align, va=va, fontsize=size,
            fontweight="bold" if bold else "normal", color="#1f2937", linespacing=1.5)


def arrow(ax, x1, y1, x2, y2, label="", color=NAVY, both=False, rad=0.0, size=8.5, lx=0, ly=0, dashed=False):
    ax.add_patch(FancyArrowPatch((x1, y1), (x2, y2), arrowstyle="<|-|>" if both else "-|>",
                                 mutation_scale=13, color=color, lw=1.5,
                                 connectionstyle=f"arc3,rad={rad}", linestyle="--" if dashed else "-", clip_on=False))
    if label:
        ax.text((x1 + x2) / 2 + lx, (y1 + y2) / 2 + ly, label, ha="center", va="center", fontsize=size,
                color=color, bbox=dict(fc="white", ec="none", pad=1.5), linespacing=1.4)


def note(ax, x, y, text, color=GREY, size=8.5, ha="center", bold=False):
    ax.text(x, y, text, ha=ha, va="center", fontsize=size, color=color, linespacing=1.45,
            fontweight="bold" if bold else "normal")


def title(ax, text, x=None, y=None):
    xl, yl = ax.get_xlim(), ax.get_ylim()
    ax.text(x if x is not None else xl[1] / 2, y if y is not None else yl[1] - 2.5, text,
            ha="center", va="top", fontsize=12, fontweight="bold", color=NAVY)


def save(fig, name):
    fig.savefig(OUT / name, bbox_inches="tight", pad_inches=0.12, facecolor="white")
    plt.close(fig)
    print("그림:", name)



# ---------------------------------------------------------------- 1. 서두 개념도: 배포 한눈에 보기
def deploy_overview():
    fig, ax = canvas(10.4, 6.6)
    title(ax, "배포 한눈에 보기 - push 하면 EC2 까지 자동으로")

    # 위 줄: 개발자 → GitHub → Actions → Docker Hub
    box(ax, 1, 46, 15, 11, "내 PC\n\n코드 수정\n→ git push", "cyan", 9)
    box(ax, 23, 46, 15, 11, "GitHub\n\n저장소 (main)", "grey", 9)
    box(ax, 45, 44, 24, 15, "GitHub Actions  (CI/CD)\n\n① 이미지 4개 빌드\n② Docker Hub 에 올리기\n③ EC2 에 배포 명령", "orange", 9)
    box(ax, 77, 46, 25, 11, "Docker Hub\n\nrlaeocjf/msa-*:latest\n(공개 이미지 저장소)", "navy", 9)
    arrow(ax, 16.5, 51.5, 22.5, 51.5, "push")
    arrow(ax, 38.5, 51.5, 44.5, 51.5, "자동 실행")
    arrow(ax, 69.5, 54, 76.5, 54, "push")

    # 아래: EC2
    box(ax, 22, 3, 81, 34, "", "grey", dashed=True)
    note(ax, 62.5, 34.5, "AWS EC2  (서울 · t3.micro · Ubuntu)   docker compose 로 컨테이너 5개", "#374151", 9, bold=True)
    box(ax, 25, 13, 14, 13, "frontend\n(nginx :80)\n\nReact 화면\n+ /api 넘기기", "cyan", 8.5)
    box(ax, 45, 13, 14, 13, "api-gateway\n:8080\n\n정문·토큰 검사", "orange", 8.5)
    box(ax, 65, 20, 14, 9, "user-service\n:8081", "navy", 8.5)
    box(ax, 65, 6, 14, 9, "post-service\n:8082", "green", 8.5)
    box(ax, 86, 10, 14, 15, "db\nPostgreSQL\n\nuserdb\npostdb", "grey", 8.5)
    arrow(ax, 39.5, 19.5, 44.5, 19.5)
    arrow(ax, 59.5, 21, 64.5, 24)
    arrow(ax, 59.5, 17, 64.5, 11)
    arrow(ax, 79.5, 24, 85.5, 20)
    arrow(ax, 79.5, 11, 85.5, 14)
    # Docker Hub → EC2 pull
    arrow(ax, 89.5, 45.5, 89.5, 37.5, "pull", color=BLUE)
    arrow(ax, 57, 43.5, 57, 37.5, "SSH 로 명령", color=ORANGE, lx=7)

    # 사용자
    box(ax, 1, 13, 14, 13, "사용자\n브라우저\n\nhttp://EC2주소", "cyan", 8.5)
    arrow(ax, 15.5, 19.5, 24.5, 19.5, "80 만 열림", color=RED, size=8)
    note(ax, 62, 1, "8080·8081·8082·5432 는 바깥에 열지 않는다 (도커 네트워크 안에서 서비스 이름으로만 찾는다)", GREY, 8)
    save(fig, "fig_deploy_overview.png")


# ---------------------------------------------------------------- 2. 이미지·컨테이너·레지스트리
def image_container():
    fig, ax = canvas(10.4, 3.8)
    title(ax, "Dockerfile → 이미지 → 컨테이너")
    box(ax, 1, 8, 17, 18, "Dockerfile\n\n만드는 방법을\n적은 설명서", "grey", 9)
    box(ax, 31, 8, 18, 18, "이미지 (image)\n\n실행에 필요한 것을\n모두 담은 꾸러미", "navy", 9)
    box(ax, 62, 8, 17, 18, "컨테이너\n\n이미지를 켜서\n돌고 있는 프로그램", "green", 9)
    box(ax, 87, 8, 16, 18, "Docker Hub\n\n이미지를 올려 두고\n어디서든 받는 곳", "orange", 9)
    arrow(ax, 18.5, 17, 30.5, 17, "docker\nbuild")
    arrow(ax, 49.5, 17, 61.5, 17, "docker run\n(compose up)")
    arrow(ax, 43, 26.5, 93, 26.5, "push (올리기)", rad=-0.18, color=ORANGE, ly=5)
    arrow(ax, 93, 7.5, 72, 7.5, "pull (받기)", rad=-0.25, color=BLUE, ly=-4)
    save(fig, "fig_image_container.png")


# ---------------------------------------------------------------- 3. multi-stage 빌드
def multi_stage():
    fig, ax = canvas(10.4, 4.0)
    title(ax, "두 단계 빌드 (multi-stage) - 최종 이미지에는 실행에 필요한 것만")
    box(ax, 2, 6, 40, 26, "", "grey", dashed=True)
    note(ax, 22, 29.5, "1단계 build  (다 쓰면 버린다)", "#374151", 9, bold=True)
    box(ax, 5, 9, 16, 16, "백엔드\nJDK 21 + Gradle\n+ 소스 코드", "navy", 8.5)
    box(ax, 24, 9, 15, 16, "프론트\nNode.js 22\n+ node_modules", "cyan", 8.5)
    box(ax, 58, 6, 44, 26, "", "green", dashed=True)
    note(ax, 80, 29.5, "2단계 run  (Docker Hub 에 올라가는 것)", GREEN, 9, bold=True)
    box(ax, 61, 9, 18, 16, "JRE 21\n+ app.jar 하나\n(약 590MB)", "navy", 8.5)
    box(ax, 82, 9, 17, 16, "nginx\n+ dist 폴더\n(약 90MB)", "cyan", 8.5)
    arrow(ax, 42.5, 21, 57.5, 21, "jar 만 복사", size=8)
    arrow(ax, 42.5, 13, 57.5, 13, "dist 만 복사", size=8)
    save(fig, "fig_multi_stage.png")


# ---------------------------------------------------------------- 4. Variables / Secrets → 서버 .env
def secrets_flow():
    fig, ax = canvas(10.4, 4.6)
    title(ax, "비밀 값은 어디에 두나 - GitHub Secrets → 서버의 .env")
    box(ax, 2, 22, 27, 15, "Variables (비밀 아님)\n\nDOCKERHUB_USERNAME\nEC2_HOST · EC2_USER", "grey", 8.8)
    box(ax, 2, 2, 27, 18, "Secrets (비밀, 로그에 ***)\n\nDOCKERHUB_TOKEN\nEC2_SSH_KEY\nDB_PASSWORD · JWT_SECRET", "red", 8.8)
    box(ax, 36, 9, 22, 22, "GitHub Actions\n(deploy.yml)\n\n.env 파일을 만들어\nscp 로 서버에 복사", "orange", 8.8)
    box(ax, 72, 9, 30, 22, "EC2  ~/app/\n\ndocker-compose.yml\n.env  ← 비밀 값은 여기에만\ndb/init.sql", "green", 8.8)
    arrow(ax, 29.5, 29, 35.5, 24)
    arrow(ax, 29.5, 11, 35.5, 16)
    arrow(ax, 58.5, 20, 71.5, 20, "SSH · scp")
    note(ax, 52, 38.5, "저장소(코드)에는 비밀 값이 하나도 없다  ·  .env 와 *.pem 은 .gitignore 에", RED, 8.5, bold=True)
    save(fig, "fig_secrets_flow.png")


# ---------------------------------------------------------------- 5. 워크플로 흐름
def workflow_flow():
    fig, ax = canvas(10.4, 4.6)
    title(ax, "deploy.yml 이 하는 일 (push 한 번에 약 2~3분)")
    for i, (name, color) in enumerate([("msa-user-service", "navy"), ("msa-post-service", "green"),
                                       ("msa-api-gateway", "orange"), ("msa-frontend", "cyan")]):
        box(ax, 20, 30 - i * 8.5, 22, 6, f"build  {name}", color, 8.5)
        arrow(ax, 14.5, 18, 19.5, 33 - i * 8.5, rad=0)
        arrow(ax, 42.5, 33 - i * 8.5, 51.5, 18)
    box(ax, 1, 12, 13, 12, "git push\n(main)\n또는\nRun workflow", "grey", 8.5)
    box(ax, 52, 8, 24, 20, "deploy\n\n① .env 만들기\n② scp 로 파일 복사\n③ compose pull · up -d", "orange", 8.5)
    box(ax, 82, 8, 20, 20, "배포 확인\n\n/api/posts 가\n200 이 될 때까지\n5초마다 (최대 3분)", "green", 8.5)
    arrow(ax, 76.5, 18, 81.5, 18)
    note(ax, 31, 38.5, "① build : 4개가 동시에 (matrix)", "#374151", 8.5, bold=True)
    note(ax, 77, 32, "② needs: build - 4개 모두 성공해야 시작", "#374151", 8.5, bold=True)
    save(fig, "fig_workflow_flow.png")


if __name__ == "__main__":
    deploy_overview()
    image_container()
    multi_stage()
    secrets_flow()
    workflow_flow()
