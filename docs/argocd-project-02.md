# Project 02 Advanced ArgoCD

## Architecture

ArgoCD owns application deployment for two environments:

- `yas-dev` deploys `k8s/gitops/overlays/dev` into namespace `dev`.
- `yas-staging` deploys `k8s/gitops/overlays/staging` into namespace `staging`.

Both Applications use automated sync, prune, self-heal, and `CreateNamespace=true`. Jenkins builds and pushes images, updates the GitOps overlay image tags, and optionally commits and pushes those manifest changes. Jenkins does not run `kubectl apply` for dev or staging.

## Repository Structure

- `k8s/yas/`: existing Task 5 raw Kubernetes manifests, preserved.
- `k8s/gitops/base/`: self-contained ArgoCD/Kustomize base copied from the working raw manifests.
- `k8s/gitops/overlays/dev/`: dev namespace and image tags.
- `k8s/gitops/overlays/staging/`: staging namespace and image tags.
- `k8s/argocd/`: AppProject and ArgoCD Application resources.
- `scripts/update-gitops-images.py`: Jenkins helper for updating overlay image tags.

## Discrepancies Found

The requested service list is:

`product`, `cart`, `order`, `customer`, `inventory`, `tax`, `media`, `search`, `storefront-bff`, `storefront-ui`, `backoffice-bff`, `backoffice-ui`, `swagger-ui`, `sampledata`.

Current repository observations:

- The raw `k8s/yas` manifests also deploy `location`, `payment`, `promotion`, `rating`, `postgres`, `redis`, `zookeeper`, `kafka`, `elasticsearch`, `keycloak`, and `kafka-connect`. These were kept.
- The raw frontend workload names are `storefront-nextjs` and `backoffice-nextjs`, using images `anhhnus/storefront` and `anhhnus/backoffice`. They were kept to preserve the existing deployment.
- `swagger-ui` existed in Helm and Docker Compose configuration but not in raw `k8s/yas`; it was added to the GitOps base.
- `sampledata` starts a Spring web server and the BFFs route `/api/sampledata/**` to a `sampledata` Service. GitOps therefore keeps it as a Deployment and Service for the current demo.
- Helm charts exist, but their default repositories use `ghcr.io/nashtech-garage/...`; Jenkins and the raw manifests use Docker Hub `docker.io/anhhnus/...`. The GitOps implementation follows the active Jenkins/Docker Hub convention.
- The GitOps base creates the non-secret PostgreSQL ConfigMaps `postgres-init` and `postgres-config` from the existing repository SQL/config content. Runtime Secrets must still be created per environment before syncing.

## ArgoCD Installation

```sh
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl get pods -n argocd
```

For a private repository, configure credentials in ArgoCD before applying the Applications:

```sh
argocd repo add https://github.com/devops-yas/yas.git \
  --username <github-user> \
  --password <github-token>
```

## Apply ArgoCD Resources

```sh
kubectl apply -f k8s/argocd/yas-project.yaml
kubectl apply -f k8s/argocd/yas-dev-application.yaml
kubectl apply -f k8s/argocd/yas-staging-application.yaml
```

## Required Environment Secrets

Create these in both `dev` and `staging`. Replace placeholder values with Jenkins/Kubernetes secret-managed values.

An example manifest with placeholder values is available at `k8s/gitops/examples/secrets.example.yaml`. It is intentionally not included in the active GitOps base.

```sh
for ns in dev staging; do
  kubectl create namespace "$ns" --dry-run=client -o yaml | kubectl apply -f -

  kubectl -n "$ns" create secret generic yas-db-secret \
    --from-literal=POSTGRES_PASSWORD='<postgres-password>' \
    --from-literal=KC_DB_PASSWORD='<keycloak-db-password>' \
    --dry-run=client -o yaml | kubectl apply -f -

  kubectl -n "$ns" create secret generic keycloak-admin-secret \
    --from-literal=KC_BOOTSTRAP_ADMIN_USERNAME='<admin-user>' \
    --from-literal=KC_BOOTSTRAP_ADMIN_PASSWORD='<admin-password>' \
    --dry-run=client -o yaml | kubectl apply -f -

  kubectl -n "$ns" create secret generic storefront-bff-secret \
    --from-literal=KEYCLOAK_STOREFRONT_BFF_CLIENT_SECRET='<client-secret>' \
    --dry-run=client -o yaml | kubectl apply -f -

  kubectl -n "$ns" create secret generic backoffice-bff-secret \
    --from-literal=KEYCLOAK_BACKOFFICE_BFF_CLIENT_SECRET='<client-secret>' \
    --dry-run=client -o yaml | kubectl apply -f -
done
```

The `postgres-init` and `postgres-config` ConfigMaps are active GitOps resources in `k8s/gitops/base/postgres-configmaps.yaml`, so no manual ConfigMap creation is required for those two resources.

## Sample Data Initialization

`sampledata` is a normal Deployment because the application exposes the seeding operation through HTTP and does not exit on its own. After the environment is healthy, initialize sample data once through the existing BFF route:

```sh
curl -X POST http://storefront.yas.local.com/api/sampledata/storefront/sampledata \
  -H 'Content-Type: application/json' \
  -d '{}'
```

The sample SQL deletes and reinserts known product/media sample rows, so rerun it intentionally rather than on every ArgoCD sync.

## Dev Flow

1. A commit lands on `main`.
2. Jenkins tests, builds, and pushes Docker images tagged with the short Git SHA.
3. Jenkins runs `scripts/update-gitops-images.py --environment dev --tag <short-sha> --services <changed-services>`.
4. If `PUSH_GITOPS_CHANGES=true`, Jenkins commits and pushes `k8s/gitops/overlays/dev/kustomization.yaml`.
5. ArgoCD detects the Git change and syncs `yas-dev` into namespace `dev`.

## Staging Flow

1. A Git release tag such as `v1.2.3` is built.
2. Jenkins builds all Docker-image services and pushes images tagged `v1.2.3`.
3. Jenkins runs `scripts/update-gitops-images.py --environment staging --tag v1.2.3 --services <services>`.
4. If `PUSH_GITOPS_CHANGES=true`, Jenkins commits and pushes `k8s/gitops/overlays/staging/kustomization.yaml` to `main`.
5. ArgoCD syncs `yas-staging` into namespace `staging`.

## Jenkins Credentials

Use Jenkins credentials, not source files:

- `docker-hub-credentials`: Docker Hub username/password for `docker.io/anhhnus`.
- `gitops-repo-credentials`: Git username/token allowed to push GitOps manifest updates.
- Existing security credentials such as `sonarcloud-token` and `snyk-token` remain unchanged.

`PUSH_GITOPS_CHANGES` defaults to `false`, so local or trial pipeline runs update the workspace and show the diff without committing or pushing.

## Sync And Health Checks

```sh
kubectl get applications -n argocd
argocd app get yas-dev
argocd app get yas-staging
argocd app history yas-dev
argocd app history yas-staging
kubectl get all -n dev
kubectl get all -n staging
kubectl get deploy,svc -n dev | grep sampledata
kubectl get deploy,svc -n staging | grep sampledata
```

## Rollback

```sh
argocd app history yas-dev
argocd app rollback yas-dev <history-id>

argocd app history yas-staging
argocd app rollback yas-staging <history-id>
```

You can also revert the Git commit that changed the relevant overlay image tags and let ArgoCD sync the reverted desired state.

## Local Validation Notes

Tools available locally in this environment: `python3`.

Tools not available locally: `kubectl`, `kustomize`, `helm`, `yq`. Because of that, cluster dry-runs, Kustomize builds, and Helm rendering were documented but not executed here.

Commands to run on a workstation with the tools installed:

```sh
kustomize build k8s/gitops/overlays/dev
kustomize build k8s/gitops/overlays/staging
kubectl kustomize k8s/gitops/overlays/dev
kubectl kustomize k8s/gitops/overlays/staging
kubectl apply --dry-run=client -k k8s/gitops/overlays/dev
kubectl apply --dry-run=client -k k8s/gitops/overlays/staging
kubectl apply --dry-run=client -f k8s/argocd/yas-project.yaml
kubectl apply --dry-run=client -f k8s/argocd/yas-dev-application.yaml
kubectl apply --dry-run=client -f k8s/argocd/yas-staging-application.yaml
```

Report evidence to capture:

- Jenkins build showing Docker images pushed with a short SHA for dev.
- Jenkins release-tag build showing Docker images pushed with `vX.Y.Z`.
- Git diff or commit updating `k8s/gitops/overlays/dev/kustomization.yaml`.
- Git diff or commit updating `k8s/gitops/overlays/staging/kustomization.yaml`.
- ArgoCD UI or CLI showing `yas-dev` and `yas-staging` synced and healthy.
- `kubectl get all -n dev` and `kubectl get all -n staging`.
- `kubectl get deploy,svc -n dev` and `kubectl get deploy,svc -n staging` showing `sampledata` as a Deployment and Service.
- Evidence that sample data was initialized once through the HTTP endpoint after deployment.
