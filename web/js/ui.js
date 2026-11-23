// ── UI helpers ────────────────────────────────────────────────
export function $(sel, root = document) { return root.querySelector(sel); }
export function $$(sel, root = document) { return [...root.querySelectorAll(sel)]; }

export function el(tag, cls, html = '') {
  const e = document.createElement(tag);
  if (cls) e.className = cls;
  if (html) e.innerHTML = html;
  return e;
}

export function stars(n) {
  return Array.from({ length: 5 }, (_, i) => i < Math.floor(n) ? '★' : (i < n ? '½' : '☆')).join('');
}

export function fmt(n) { return n.toLocaleString('ka-GE') + '₾'; }

export function toast(msg, type = 'success') {
  const t = el('div', `toast toast-${type}`, msg);
  document.body.appendChild(t);
  setTimeout(() => t.classList.add('show'), 10);
  setTimeout(() => { t.classList.remove('show'); setTimeout(() => t.remove(), 300); }, 2500);
}

export function showPage(pageId) {
  $$('.page').forEach(p => p.classList.remove('active'));
  const pg = $(`#pg-${pageId}`);
  if (pg) pg.classList.add('active');
  $$('.nav-btn').forEach(b => b.classList.toggle('active', b.dataset.page === pageId));
  updateBadge();
}

export function updateBadge() {
  import('./state.js').then(({ cartCount }) => {
    const cnt = cartCount();
    const badge = $('.cart-badge');
    if (badge) {
      badge.textContent = cnt;
      badge.style.display = cnt ? '' : 'none';
    }
  });
}

export function openModal(id) { $(`#${id}`)?.classList.add('on'); }
export function closeModal(id) { $(`#${id}`)?.classList.remove('on'); }

export function scrollTop() { window.scrollTo({ top: 0, behavior: 'smooth' }); }

// Render stars safely
export function renderStars(rating) {
  const full = Math.floor(rating);
  const half = rating - full >= 0.5;
  let s = '';
  for (let i = 0; i < 5; i++) {
    s += i < full ? '★' : (i === full && half ? '⭐' : '☆');
  }
  return s;
}
