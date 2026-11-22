# MYSHOP — ქართული E-Commerce პლატფორმა

> `ge.myshop.app` — Android + Web Admin + Supabase Backend

---

## პროექტის სტრუქტურა

```
myshop/
├── android/              # Kotlin + Jetpack Compose Android App
│   ├── app/src/main/java/ge/myshop/app/
│   │   ├── data/repository/   # Supabase repositories
│   │   ├── di/                # Hilt DI modules
│   │   ├── domain/model/      # Kotlin data models
│   │   ├── presentation/      # Composable screens & ViewModels
│   │   └── ui/theme/          # Material 3 theme (purple/gold)
│   └── app/src/main/res/      # Resources (strings.xml, fonts...)
│
├── admin/                # Web Admin Panel (vanilla HTML/CSS/JS)
│   ├── css/              # Design system (variables, base)
│   ├── js/               # Core modules (app, router, utils...)
│   ├── pages/            # Page modules (ES modules)
│   ├── index.html        # SPA shell
│   └── login.html        # Auth page
│
├── supabase/
│   └── migrations/       # PostgreSQL migrations (001–004)
│
├── docs/                 # Documentation
│   └── termux-workflow.md
│
└── .github/workflows/    # CI/CD (GitHub Actions)
    ├── android-debug.yml
    ├── android-release.yml
    ├── deploy-admin.yml
    └── supabase-migrations.yml
```

---

## ტექნოლოგიები

| სფერო | ტექნოლოგია |
|-------|------------|
| Android | Kotlin + Jetpack Compose + Material 3 |
| DI | Hilt |
| Backend | Supabase (PostgreSQL + Auth + Realtime + Storage) |
| Admin Panel | Vanilla HTML/CSS/JS (ES Modules, Zero framework) |
| Fonts | FiraGO (ქართული), Inter |
| Design | Purple `#5B21B6` / Gold `#F59E0B` / White |
| CI/CD | GitHub Actions |
| Hosting | Vercel (admin), GitHub Releases (APK) |

---

## გაშვება

### Android (ლოკალური)

```bash
cd android
cp ../env.example.local.properties local.properties  # და შეავსე
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Admin Panel (ლოკალური)

```bash
cd admin
# შექმენი env.js ფაილი:
cat > js/env.js << 'EOF'
window.__ENV__ = {
  SUPABASE_URL: 'https://your-ref.supabase.co',
  SUPABASE_ANON: 'eyJ...',
};
EOF
# გაუშვი სერვერი
npx serve . -p 8080
# გახსენი http://localhost:8080
```

### Supabase Migrations

```bash
supabase link --project-ref YOUR_REF
supabase db push
```

---

## GitHub Actions CI/CD

| Workflow | ტრიგერი | შედეგი |
|----------|---------|--------|
| `android-debug.yml` | push → `main`/`develop` | debug APK artifact |
| `android-release.yml` | tag `v*.*.*` | signed AAB + APK → GitHub Release |
| `deploy-admin.yml` | push → `main` (admin/) | Vercel production deploy |
| `supabase-migrations.yml` | push → `main` (migrations/) | db push |

---

## GitHub Secrets (დასაყენებელი)

| Secret | აღწერა |
|--------|--------|
| `SUPABASE_URL` | Supabase project URL |
| `SUPABASE_ANON_KEY` | Public anon key |
| `SUPABASE_DB_PASSWORD` | DB password (migrations) |
| `SUPABASE_PROJECT_REF` | Project ref ID |
| `VERCEL_TOKEN` | Vercel deploy token |
| `RELEASE_KEYSTORE_BASE64` | Keystore (base64) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

---

## Admin Panel-ის გვერდები

| გვ. | URL | წვდომა |
|----|-----|--------|
| Dashboard | `#dashboard` | ყველა |
| შეკვეთები | `#orders` | owner, admin, order_manager, support |
| ჩატები | `#conversations` | ყველა |
| პროდუქტები | `#products` | owner, admin, content_manager |
| კატეგ. | `#categories` | owner, admin, content_manager |
| მომხ. | `#customers` | owner, admin, support |
| აქც. | `#promotions` | owner, admin, content_manager |
| მთ. გვ. | `#home-content` | owner, admin, content_manager |
| შეტ. | `#notifications` | owner, admin |
| პ. | `#staff` | owner |
| პარ. | `#settings` | owner, admin |
| ცვ. ი. | `#audit-log` | owner, admin |
| სისტ. | `#system-status` | owner, admin |

---

## უსაფრთხოება

- ✅ RLS ჩართულია ყველა ცხრილზე
- ✅ Service-role key არასოდეს browser/Android-ში
- ✅ Atomic order creation → PostgreSQL RPC
- ✅ PKCE auth flow Android-ზე
- ✅ Internal staff notes დამალულია კლიენტისგან
- ✅ Role-based admin access control
- ❌ Online payment processing — **არ არის**, შეკვეთები → კერძო ჩატი

---

## Termux-ზე დეველოპმენტი

იხილეთ [`docs/termux-workflow.md`](docs/termux-workflow.md)
