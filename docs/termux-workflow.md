# MYSHOP — Termux-ზე დეველოპმენტი

> Android-ზე Termux-ის გამოყენებით Git commit-ებისა და GitHub Actions-ის ტრიგერი.

## 1. Termux-ის მომზადება

```bash
pkg update && pkg upgrade -y
pkg install git gh openssh -y

# GitHub-ში login (PAT ან SSH)
gh auth login
# → GitHub.com → HTTPS → Yes → Paste your token
```

## 2. რეპოს კლონი

```bash
mkdir -p ~/projects && cd ~/projects
git clone https://github.com/YOUR_USERNAME/myshop.git
cd myshop
```

## 3. Admin Panel-ის ლოკალური ტესტი

```bash
pkg install nodejs -y
cd admin

# მარტივი HTTP სერვერი
npx serve . -p 8080
# Browser-ში: http://localhost:8080

# ან Python-ით
python -m http.server 8080
```

ლოკალური env-ისთვის შექმენი `admin/js/env.js`:
```javascript
// admin/js/env.js  ← .gitignore-ში
window.__ENV__ = {
  SUPABASE_URL: 'https://your-ref.supabase.co',
  SUPABASE_ANON: 'eyJ...',
};
```

## 4. Android Build-ი Termux-იდან

> **შენიშვნა**: Termux-ში პირდაპირ Gradle-ის გაშვება რთულია ARM ლიმიტების გამო.  
> რეკომენდებული მიდგომა: **GitHub Actions-ის ტრიგერი commit-ის გზით**.

```bash
# ფაილების შეცვლა
cd ~/projects/myshop
nano android/app/src/main/res/values/strings.xml

# Commit და push → GitHub Actions ავტომატურად build-ავს
git add -A
git commit -m "feat: update strings"
git push origin main
```

## 5. GitHub Actions-ის მონიტორი

```bash
# Actions-ის სტატუსი
gh run list --limit 5

# კონკრეტული run-ის ლოგები
gh run view <RUN_ID> --log

# APK-ის გადმოწერა (debug)
gh run download <RUN_ID> --name "myshop-debug-*" -D ~/downloads/
```

## 6. Release Tag-ი (APK-ის release)

```bash
# Version tag-ი → ტრიგერი release workflow-ისთვის
git tag v1.0.0
git push origin v1.0.0

# GitHub Release-ი ავტომატურად შეიქმნება signed APK-ით
```

## 7. Supabase Migration-ები

```bash
pkg install curl -y

# Supabase CLI-ის ინსტალაცია
curl -fsSL https://raw.githubusercontent.com/supabase/supabase/master/bin/install.sh | bash

# ან npm-ით
npm install -g supabase

# Migrate
supabase login
supabase link --project-ref your-project-ref
supabase db push
```

## 8. GitHub Secrets-ის დაყენება

```bash
# Supabase credentials
gh secret set SUPABASE_URL --body "https://your-ref.supabase.co"
gh secret set SUPABASE_ANON_KEY --body "eyJ..."
gh secret set SUPABASE_DB_PASSWORD --body "your-db-password"
gh secret set SUPABASE_PROJECT_REF --body "your-ref"

# Vercel
gh secret set VERCEL_TOKEN --body "your-vercel-token"

# Android signing (base64-encoded keystore)
base64 -w 0 myshop-release.jks | gh secret set RELEASE_KEYSTORE_BASE64
gh secret set KEYSTORE_PASSWORD --body "your-password"
gh secret set KEY_ALIAS --body "myshop-key"
gh secret set KEY_PASSWORD --body "your-key-password"
```

## 9. Admin Panel-ი Vercel-ზე

```bash
# Vercel CLI ინსტალაცია
npm install -g vercel

# პირველი deploy
cd admin
vercel login
vercel --prod

# Environment Variables Vercel-ში
vercel env add SUPABASE_URL production
vercel env add SUPABASE_ANON_KEY production
```

## 10. სასარგებლო ბრძანებები

```bash
# Git log
git log --oneline -10

# GitHub Actions live log
gh run watch

# Supabase Studio-ს ბრაუზერში გახსნა
gh browse --repo supabase/supabase

# APK-ის ინფო
aapt dump badging app-debug.apk | grep version
```
