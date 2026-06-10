import json
from pathlib import Path


ROOT = Path(r"D:/java/workspace/stock-mgr")
JSON_PATH = ROOT / "data/test-data/delivery_split_2026.json"
SQL_PATH = ROOT / "data/test-data/delivery_split_2026.sql"


def esc(value):
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float)):
        return str(value)
    return "'" + str(value).replace("'", "''") + "'"


def parse_closed(value):
    if isinstance(value, str) and len(value) >= 10:
        return value[:10].replace("T", " ")
    return value


def main():
    obj = json.loads(JSON_PATH.read_text(encoding="utf-8"))
    rows = obj["records"]
    cols = [
        "row_no",
        "sale_deadline",
        "brand",
        "english_brand",
        "maker",
        "delivery_date",
        "item_code",
        "item_name_jp",
        "item_name_en",
        "fob_price",
        "qty_delivered",
        "a_split",
        "a_total",
        "a_remain",
        "b_split",
        "b_total",
        "b_remain",
        "c_split",
        "c_total",
        "c_remain",
        "free_sale",
        "closed_at",
    ]

    lines = [
        "-- Generated from 2026年納品書振分まとめ (1).xlsx",
        "-- Source sheet: まとめ納品書",
        "DROP TABLE IF EXISTS delivery_split_test;",
        "CREATE TABLE delivery_split_test (",
        "  row_no INT NOT NULL,",
        "  sale_deadline INT NULL,",
        "  brand VARCHAR(100) NULL,",
        "  english_brand VARCHAR(100) NULL,",
        "  maker VARCHAR(100) NULL,",
        "  delivery_date VARCHAR(100) NULL,",
        "  item_code VARCHAR(50) NULL,",
        "  item_name_jp VARCHAR(255) NULL,",
        "  item_name_en VARCHAR(255) NULL,",
        "  fob_price DECIMAL(12,2) NULL,",
        "  qty_delivered INT NULL,",
        "  a_split INT NULL,",
        "  a_total INT NULL,",
        "  a_remain INT NULL,",
        "  b_split INT NULL,",
        "  b_total INT NULL,",
        "  b_remain INT NULL,",
        "  c_split INT NULL,",
        "  c_total INT NULL,",
        "  c_remain INT NULL,",
        "  free_sale INT NULL,",
        "  closed_at DATETIME NULL,",
        "  PRIMARY KEY (row_no)",
        ");",
    ]

    for start in range(0, len(rows), 200):
        part = rows[start : start + 200]
        values = []
        for r in part:
            values.append(
                "("
                + ", ".join(
                    [
                        esc(r.get("rowNo")),
                        esc(r.get("販売期限")),
                        esc(r.get("ブランド")),
                        esc(r.get("英語ブランド")),
                        esc(r.get("メーカー")),
                        esc(r.get("納品日")),
                        esc(r.get("品番")),
                        esc(r.get("品名\nシリーズ名・ダマ/3層/本焼・鋼材・形")),
                        esc(r.get("name ")),
                        esc(r.get("FOB\n価格")),
                        esc(r.get("納品数")),
                        esc(r.get("A組振分")),
                        esc(r.get("A合計")),
                        esc(r.get("A残")),
                        esc(r.get("B組振分")),
                        esc(r.get("B合計")),
                        esc(r.get("B残")),
                        esc(r.get("C組振分")),
                        esc(r.get("C合計")),
                        esc(r.get("C残")),
                        esc(r.get("自由\n販売")),
                        esc(parse_closed(r.get("締め済"))),
                    ]
                )
                + ")"
            )
        lines.append(f"INSERT INTO delivery_split_test ({', '.join(cols)}) VALUES")
        lines.append(",\n".join(values) + ";")

    SQL_PATH.write_text("\n".join(lines), encoding="utf-8")
    print(SQL_PATH)
    print(len(rows))


if __name__ == "__main__":
    main()
