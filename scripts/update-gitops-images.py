#!/usr/bin/env python3
import argparse
from pathlib import Path


IMAGE_BY_SERVICE = {
    "product": "anhhnus/product-service",
    "cart": "anhhnus/cart-service",
    "order": "anhhnus/order-service",
    "customer": "anhhnus/customer-service",
    "inventory": "anhhnus/inventory-service",
    "tax": "anhhnus/tax-service",
    "media": "anhhnus/media-service",
    "search": "anhhnus/search-service",
    "storefront-bff": "anhhnus/storefront-bff",
    "storefront": "anhhnus/storefront",
    "backoffice-bff": "anhhnus/backoffice-bff",
    "backoffice": "anhhnus/backoffice",
    "sampledata": "anhhnus/sampledata-service",
    "location": "anhhnus/location-service",
    "payment": "anhhnus/payment-service",
    "payment-paypal": "anhhnus/payment-paypal-service",
    "promotion": "anhhnus/promotion-service",
    "rating": "anhhnus/rating-service",
    "recommendation": "anhhnus/recommendation-service",
    "webhook": "anhhnus/webhook-service",
}


def update_images(kustomization: Path, image_names: set[str], new_tag: str) -> list[str]:
    lines = kustomization.read_text().splitlines()
    updated = []
    current_image = None

    for index, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith("- name: "):
            current_image = stripped.removeprefix("- name: ").strip()
            continue
        if current_image in image_names and stripped.startswith("newTag: "):
            indent = line[: len(line) - len(line.lstrip())]
            lines[index] = f"{indent}newTag: {new_tag}"
            updated.append(current_image)
            current_image = None

    missing = sorted(image_names - set(updated))
    if missing:
        raise SystemExit(f"Missing image entries in {kustomization}: {', '.join(missing)}")

    kustomization.write_text("\n".join(lines) + "\n")
    return sorted(updated)


def main() -> None:
    parser = argparse.ArgumentParser(description="Update YAS GitOps image tags for one environment.")
    parser.add_argument("--environment", choices=["dev", "staging"], required=True)
    parser.add_argument("--tag", required=True)
    parser.add_argument("--services", required=True, help="Comma-separated Jenkins service names.")
    args = parser.parse_args()

    services = [service.strip() for service in args.services.split(",") if service.strip()]
    if not services:
        raise SystemExit("At least one service must be provided with --services.")

    unknown = sorted(service for service in services if service not in IMAGE_BY_SERVICE)
    if unknown:
        raise SystemExit(f"Unsupported GitOps service(s): {', '.join(unknown)}")

    images = {IMAGE_BY_SERVICE[service] for service in services}
    kustomization = Path("k8s/gitops/overlays") / args.environment / "kustomization.yaml"
    updated = update_images(kustomization, images, args.tag)
    print(f"Updated {args.environment} images to {args.tag}: {', '.join(updated)}")


if __name__ == "__main__":
    main()
