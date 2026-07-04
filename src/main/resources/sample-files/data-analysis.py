"""数据分析作业 — 学生成绩统计与可视化"""

import csv
import statistics
from collections import defaultdict

def load_scores(filepath: str) -> list[dict]:
    """从 CSV 文件加载学生成绩数据"""
    rows = []
    with open(filepath, encoding="utf-8") as f:
        for r in csv.DictReader(f):
            r["score"] = int(r["score"])
            rows.append(r)
    return rows

def analyze(scores: list[dict]):
    """计算每个科目的统计指标"""
    by_subject = defaultdict(list)
    for r in scores:
        by_subject[r["subject"]].append(r["score"])

    for subject, values in sorted(by_subject.items()):
        mean = statistics.mean(values)
        stdev = statistics.stdev(values) if len(values) > 1 else 0
        print(f"{subject}: 平均={mean:.1f}, 标准差={stdev:.1f}, 最高={max(values)}, 最低={min(values)}")

def top_students(scores: list[dict], n: int = 5):
    """按总分排名前 N 名学生"""
    totals = defaultdict(int)
    for r in scores:
        totals[r["name"]] += r["score"]
    ranked = sorted(totals.items(), key=lambda x: -x[1])
    print(f"\n=== 总分前 {n} 名 ===")
    for i, (name, total) in enumerate(ranked[:n], 1):
        print(f"{i}. {name} — {total} 分")

if __name__ == "__main__":
    data = load_scores("scores.csv")
    analyze(data)
    top_students(data)
