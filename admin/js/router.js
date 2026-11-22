// MYSHOP Admin Panel — Hash-based SPA Router
import { setActiveNav } from './app.js';

// Page registry — maps route key to { title, loader, permission }
const PAGES = {
  dashboard:      { title: 'დეშბორდი',         file: 'pages/dashboard.js',    roles: ['owner','admin','order_manager','content_manager','support_agent'] },
  orders:         { title: 'შეკვეთები',         file: 'pages/orders.js',        roles: ['owner','admin','order_manager','support_agent'] },
  conversations:  { title: 'საუბრები',          file: 'pages/conversations.js', roles: ['owner','admin','order_manager','support_agent'] },
  products:       { title: 'პროდუქტები',        file: 'pages/products.js',      roles: ['owner','admin','content_manager'] },
  categories:     { title: 'კატეგორიები',       file: 'pages/categories.js',    roles: ['owner','admin','content_manager'] },
  customers:      { title: 'მომხმარებლები',     file: 'pages/customers.js',     roles: ['owner','admin','support_agent'] },
  promotions:     { title: 'აქციები & ფასდაკლება', file: 'pages/promotions.js', roles: ['owner','admin','content_manager'] },
  'home-content': { title: 'მთავარი გვ. კონტ.', file: 'pages/home-content.js',  roles: ['owner','admin','content_manager'] },
  notifications:  { title: 'შეტყობინებები',     file: 'pages/notifications.js', roles: ['owner','admin','content_manager'] },
  staff:          { title: 'სტაფი & წვდომა',    file: 'pages/staff.js',          roles: ['owner'] },
  settings:       { title: 'მაღაზიის პარამ.',   file: 'pages/settings.js',       roles: ['owner','admin'] },
  'audit-log':    { title: 'ცვლილებების ჟურნალი', file: 'pages/audit-log.js',   roles: ['owner','admin'] },
  'system-status':{ title: 'სისტემის სტატუსი',  file: 'pages/system-status.js', roles: ['owner','admin'] },
};

export function initRouter() {
  const contentEl = document.getElementById('pageContent');

  async function navigate(page) {
    const route = PAGES[page] || PAGES['dashboard'];
    const { AppState } = await import('./app.js');
    const role = AppState.userRole;

    // Permission check
    if (!route.roles.includes(role)) {
      showAccessDenied(contentEl);
      return;
    }

    // Show loader
    contentEl.innerHTML = renderPageLoader();
    setActiveNav(page);

    try {
      const module = await import(`../${route.file}`);
      await module.render(contentEl, { supabase: window.__APP__.supabase });
    } catch (err) {
      console.error(`Page load error [${page}]:`, err);
      contentEl.innerHTML = renderPageError(err.message);
    }
  }

  function handleHash() {
    const hash = window.location.hash.slice(1) || 'dashboard';
    navigate(hash);
  }

  window.addEventListener('hashchange', handleHash);
  handleHash();

  return {
    navigate(page) {
      window.location.hash = page;
    }
  };
}

function renderPageLoader() {
  return `
    <div class="flex flex-col gap-6" style="padding:8px 0">
      <div class="skeleton" style="height:36px;width:220px;border-radius:8px"></div>
      <div class="stats-grid">
        ${Array(4).fill(`<div class="skeleton" style="height:120px;border-radius:14px"></div>`).join('')}
      </div>
      <div class="skeleton" style="height:360px;border-radius:14px"></div>
    </div>`;
}

function renderPageError(msg) {
  return `
    <div class="empty-state">
      <div class="empty-state-icon">⚠️</div>
      <div class="empty-state-title">გვერდი ვერ ჩაიტვირთა</div>
      <div class="empty-state-desc">${msg || 'უცნობი შეცდომა'}</div>
      <button class="btn btn-primary mt-4" onclick="location.reload()">ხელახლა ცდა</button>
    </div>`;
}

function showAccessDenied(el) {
  el.innerHTML = `
    <div class="empty-state">
      <div class="empty-state-icon">🔒</div>
      <div class="empty-state-title">წვდომა შეზღუდულია</div>
      <div class="empty-state-desc">თქვენ არ გაქვთ ამ გვერდის ნახვის უფლება.</div>
    </div>`;
}
