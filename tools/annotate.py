"""annotate.py - 교안 화면 캡처에 "여기를 보세요" 분홍 상자를 그린다.

    python tools/annotate.py            # tools/annot_base/*.jpg (상자 없는 원본) → docs/images/*.jpg (상자 그린 것)
    python tools/annotate.py --sheet    # 확인용으로 모든 결과를 한 장씩 모은 미리보기도 만든다

원본(annot_base)은 고치지 않는다. 상자 위치를 바꾸려면 아래 BOXES 의 좌표만 고치고 다시 실행한다.
좌표는 원본 그림의 픽셀 (왼쪽, 위, 오른쪽, 아래).
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
BASE = ROOT / "tools" / "annot_base"
OUT = ROOT / "docs" / "images"
PINK = (255, 32, 140)

# 그림 → 강조할 상자 목록
BOXES = {
    "a01_aws_signup.jpg": [(322, 378, 577, 412), (322, 465, 577, 500), (322, 508, 577, 544)],
    "c03_dockerhub_repos.jpg": [(268, 238, 1537, 358)],
    "d01_ec2_dashboard.jpg": [(1300, 8, 1410, 36), (228, 404, 350, 438)],
    "d02_launch_ubuntu.jpg": [(28, 128, 594, 161), (191, 347, 277, 459), (28, 486, 881, 545)],
    "d03_launch_type.jpg": [(28, 333, 658, 407), (470, 188, 520, 215)],
    "d04_keypair.jpg": [(30, 83, 498, 118), (38, 160, 264, 230), (31, 257, 155, 295), (411, 412, 498, 447)],
    "d05_network_storage.jpg": [(19, 151, 301, 193), (18, 218, 585, 267), (18, 302, 275, 342), (18, 453, 303, 490)],
    "d06_launch_success.jpg": [(15, 23, 1553, 85), (1433, 405, 1553, 439)],
    "d07_instances.jpg": [(334, 110, 405, 138), (1010, 110, 1090, 138)],
    "h01_instances_newip.jpg": [(1010, 110, 1090, 138), (589, 110, 690, 138)],
    "e01_site_first.jpg": [(572, 130, 713, 157)],
    "e02_site_kst.jpg": [(572, 130, 713, 187), (572, 190, 713, 218)],
    "f01_variables_form.jpg": [(15, 143, 945, 185), (15, 287, 945, 495), (15, 516, 138, 557)],
    "f02_variables_list.jpg": [(93, 190, 191, 240), (15, 482, 945, 588)],
    "f04_secret_new.jpg": [(15, 128, 945, 170), (15, 201, 945, 416), (15, 424, 118, 465)],
    "f05_secrets_list.jpg": [(15, 190, 103, 240), (770, 405, 945, 446), (15, 482, 945, 601)],
    "g01_run_progress.jpg": [(25, 25, 730, 70), (376, 280, 678, 409)],
    "g02_workflows.jpg": [(31, 180, 1185, 273), (31, 266, 1185, 335)],
    "g03_run_dispatch.jpg": [(831, 88, 945, 127), (1005, 167, 1129, 209)],
    "g04_run_success.jpg": [(617, 115, 705, 167), (27, 190, 310, 415)],
    "g05_deploy_log.jpg": [(11, 331, 1128, 375), (45, 395, 210, 511)],
    "i01_terminate_menu.jpg": [(1050, 8, 1160, 45), (1050, 132, 1217, 163)],
    "i02_terminate_confirm.jpg": [(20, 48, 666, 106), (586, 295, 666, 329)],
    "i03_terminating.jpg": [(334, 145, 405, 172)],
    "i04_keypair_delete.jpg": [(19, 106, 486, 140), (433, 154, 486, 189)],
    "k01_profile_menu.jpg": [(365, 248, 635, 278)],
    "k02_token_list.jpg": [(1159, 90, 1301, 130), (40, 320, 495, 353)],
    "k03_token_default.jpg": [(28, 303, 528, 345)],
    "k04_token_permissions.jpg": [(28, 420, 528, 456)],
    "k05_token_readwrite.jpg": [(28, 303, 528, 345), (97, 415, 182, 453)],
    "vs_view_menu.jpg": [(150, 330, 362, 354)],
    "vs_terminal_ps_deploy.jpg": [(12, 38, 280, 64), (1304, 54, 1555, 84)],
    "vs_terminal_cmd.jpg": [(1323, 6, 1377, 32), (12, 88, 163, 114)],
    "vs_terminal_profiles.jpg": [(15, 96, 165, 122), (15, 239, 165, 266), (245, 308, 280, 334)],
    "vs_layout_jsx.jpg": [(80, 258, 355, 288), (435, 509, 725, 541)],
}


def draw(name, boxes):
    im = Image.open(BASE / name).convert("RGB")
    d = ImageDraw.Draw(im)
    w = max(3, round(min(im.size) / 160))            # 그림 크기에 맞춘 선 굵기
    for x0, y0, x1, y1 in boxes:
        d.rounded_rectangle((x0, y0, x1, y1), radius=w * 3, outline=PINK, width=w)
    im.save(OUT / name, quality=92)
    return im


def main():
    made = []
    for name, boxes in BOXES.items():
        if not (BASE / name).exists():
            print("원본 없음:", name)
            continue
        made.append((name, draw(name, boxes)))
    print(f"상자 그림 {len(made)}장")
    if "--sheet" in sys.argv:   # 확인용 미리보기 (6장씩 한 장)
        sheet_dir = ROOT / "tools" / "annot_preview"
        sheet_dir.mkdir(exist_ok=True)
        for k in range(0, len(made), 6):
            group = made[k:k + 6]
            thumbs = []
            for name, im in group:
                t = im.copy()
                t.thumbnail((760, 420))
                thumbs.append(t)
            sheet = Image.new("RGB", (1540, 3 * 440), "white")
            for i, t in enumerate(thumbs):
                sheet.paste(t, ((i % 2) * 770, (i // 2) * 440))
            sheet.save(sheet_dir / f"sheet{k // 6}.jpg", quality=85)


if __name__ == "__main__":
    main()
