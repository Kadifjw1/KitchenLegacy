#!/usr/bin/env python3
"""Regression guard for the Forge 1.20.1 Prah armor-stand build fix."""

from pathlib import Path

SOURCE = Path(
    "src/main/java/ru/theframetrip/worldsmith/ability/prah/PrahAbilityManager.java"
)


def main() -> None:
    source = SOURCE.read_text(encoding="utf-8")
    forbidden_calls = (
        ".setSmall(",
    )

    found = [call for call in forbidden_calls if call in source]
    if found:
        joined = ", ".join(found)
        raise SystemExit(
            "Prah build regression: ArmorStand private API call detected: " + joined
        )

    print("Prah build fix verified: no private ArmorStand size setter is used.")


if __name__ == "__main__":
    main()
