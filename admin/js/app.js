// MYSHOP Admin Panel — Core App Module
import { initSupabase, getUser, requireAuth } from './supabase.js';
import { initRouter } from './router.js';
import { initToast } from './toast.js';

// ─── Global State ───────────────────────────────────────────────────────────
export const AppState = {
  user: null,
  userRole: null,
  sidebarCollapsed: false,
  currentPage: null,
};

// ─── Badge Counters ──────────────────────────────────────────────────────────
let badgeTimerId = null;

export async function refreshBadges() {
  const { supabase } = window.__APP__;
  if (!supabase || !AppState.user) return;

  try {
    const [ordersRes, chatsRes] = await Promise.all([
      supabase.from('orders').select('id', { count: 'exact', head: true }).eq('status', 'pending'),
      supabase.from('conversations').select('id', { count: 'exact', head: true }).eq('status', 'open'),
    ]);
    updateNavBadge('orders', ordersRes.count ?? 0);
    updateNavBadge('conversations', chatsRes.count ?? 0);
  } catch (e) {
    console.error('Badge refresh error:', e);
  }
}

function updateNavBadge(key, count) {
  const badge = document.querySelector(`[data-badge="${key}"]`);
  if (!badge) return;
  if (count > 0) {
    badge.textContent = count > 99 ? '99+' : count;
    badge.style.display = '';
  } else {
    badge.style.display = 'none';
  }
}

// ─── Sidebar ─────────────────────────────────────────────────────────────────
export function initSidebar() {
  const sidebar  = document.getElementById('sidebar');
  const topbar   = document.getElementById('topbar');
  const main     = document.getElementById('mainContent');
  const toggle   = document.getElementById('sidebarToggle');

  // Restore saved preference
  const saved = localStorage.getItem('myshop_sidebar_collapsed');
  if (saved === 'true') {
    setSidebarCollapsed(true, { animate: false });
  }

  toggle?.addEventListener('click', () => {
    setSidebarCollapsed(!AppState.sidebarCollapsed);
  });

  // Active nav item
  document.querySelectorAll('.nav-item[data-page]').forEach(item => {
    item.addEventListener('click', () => {
      const page = item.dataset.page;
      window.__APP__.router.navigate(page);
    });
  });

  // Tooltip on collapsed
  document.querySelectorAll('.nav-item[data-page]').forEach(item => {
    item.title = item.dataset.tooltip || '';
  });
}

export function setSidebarCollapsed(collapsed, { animate = true } = {}) {
  AppState.sidebarCollapsed = collapsed;
  localStorage.setItem('myshop_sidebar_collapsed', collapsed);

  const sidebar = document.getElementById('sidebar');
  const topbar  = document.getElementById('topbar');
  const main    = document.getElementById('mainContent');

  if (!animate) {
    document.body.style.transition = 'none';
  }

  sidebar?.classList.toggle('collapsed', collapsed);
  topbar?.classList.toggle('sidebar-collapsed', collapsed);
  main?.classList.toggle('sidebar-collapsed', collapsed);

  if (!animate) {
    requestAnimationFrame(() => {
      document.body.style.transition = '';
    });
  }
}

// ─── Set active nav item ─────────────────────────────────────────────────────
export function setActiveNav(page) {
  document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
  const target = document.querySelector(`.nav-item[data-page="${page}"]`);
  target?.classList.add('active');

  // Update breadcrumb
  const breadcrumb = document.getElementById('breadcrumbCurrent');
  if (breadcrumb) {
    breadcrumb.textContent = target?.dataset.tooltip || formatPageTitle(page);
  }

  document.title = `${target?.dataset.tooltip || formatPageTitle(page)} — MYSHOP Admin`;
}

function formatPageTitle(page) {
  return page.charAt(0).toUpperCase() + page.slice(1).replace(/-/g, ' ');
}

// ─── Topbar User Info ─────────────────────────────────────────────────────────
function renderUserInfo() {
  const { user, userRole } = AppState;
  if (!user) return;

  const name   = user.user_metadata?.full_name || user.email?.split('@')[0] || 'Admin';
  const initials = name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  const roleLabel = { owner: 'მფლობელი', admin: 'ადმინი', order_manager: 'შეკვეთები', content_manager: 'კონტენტი', support_agent: 'მხარდაჭერა' }[userRole] || 'სტაფი';

  const avatarEl = document.getElementById('sidebarUserAvatar');
  const nameEl   = document.getElementById('sidebarUserName');
  const roleEl   = document.getElementById('sidebarUserRole');
  const topbarAvatar = document.getElementById('topbarAvatar');

  if (avatarEl) avatarEl.textContent = initials;
  if (nameEl)   nameEl.textContent = name;
  if (roleEl)   roleEl.textContent = roleLabel;
  if (topbarAvatar) topbarAvatar.textContent = initials;
}

// ─── App Initialization ────────────────────────────────────────────────────
async function init() {
  window.__APP__ = {};

  // Init core services
  const supabase = await initSupabase();
  window.__APP__.supabase = supabase;

  const toast  = initToast();
  window.__APP__.toast = toast;

  // Auth check
  const user = await getUser(supabase);
  if (!user) {
    window.location.href = 'login.html';
    return;
  }

  // Load role
  const { data: profile } = await supabase
    .from('profiles')
    .select('role')
    .eq('id', user.id)
    .single();

  AppState.user = user;
  AppState.userRole = profile?.role ?? 'support_agent';

  // Check admin access
  const allowedRoles = ['owner', 'admin', 'order_manager', 'content_manager', 'support_agent'];
  if (!allowedRoles.includes(AppState.userRole)) {
    window.location.href = 'login.html?error=access_denied';
    return;
  }

  // Render layout
  renderUserInfo();
  initSidebar();

  // Init router
  const router = initRouter();
  window.__APP__.router = router;

  // Initial badge load & periodic refresh
  await refreshBadges();
  badgeTimerId = setInterval(refreshBadges, 60_000);

  // Listen for auth changes
  supabase.auth.onAuthStateChange((event) => {
    if (event === 'SIGNED_OUT') {
      clearInterval(badgeTimerId);
      window.location.href = 'login.html';
    }
  });

  // Logout button
  document.getElementById('logoutBtn')?.addEventListener('click', async () => {
    await supabase.auth.signOut();
  });
}

document.addEventListener('DOMContentLoaded', init);
