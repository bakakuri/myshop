// MYSHOP Admin Panel — Shared Utilities

// ─── Formatting ───────────────────────────────────────────────────────────────
export function formatPrice(amount) {
  if (amount == null) return '—';
  return new Intl.NumberFormat('ka-GE', {
    style: 'currency',
    currency: 'GEL',
    minimumFractionDigits: 2,
  }).format(amount);
}

export function formatDate(isoString) {
  if (!isoString) return '—';
  return new Intl.DateTimeFormat('ka-GE', {
    year: 'numeric', month: 'short', day: 'numeric',
  }).format(new Date(isoString));
}

export function formatDateTime(isoString) {
  if (!isoString) return '—';
  return new Intl.DateTimeFormat('ka-GE', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(isoString));
}

export function formatRelative(isoString) {
  if (!isoString) return '—';
  const diff = Date.now() - new Date(isoString).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1)  return 'ახლა';
  if (mins < 60) return `${mins} წ. წინ`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)  return `${hrs} სთ. წინ`;
  const days = Math.floor(hrs / 24);
  if (days < 7)  return `${days} დ. წინ`;
  return formatDate(isoString);
}

// ─── Status Labels ────────────────────────────────────────────────────────────
export const ORDER_STATUS = {
  pending:    { label: 'მომლოდინე',    badge: 'badge-pending' },
  confirmed:  { label: 'დადასტურებ.',  badge: 'badge-confirmed' },
  processing: { label: 'მუშავდება',    badge: 'badge-processing' },
  shipped:    { label: 'გაგზავნილი',   badge: 'badge-shipped' },
  delivered:  { label: 'ჩაბარებული',   badge: 'badge-delivered' },
  cancelled:  { label: 'გაუქმებული',  badge: 'badge-cancelled' },
};

export function orderStatusBadge(status) {
  const s = ORDER_STATUS[status] || { label: status, badge: 'badge-draft' };
  return `<span class="badge ${s.badge}">${s.label}</span>`;
}

export const STAFF_ROLES = {
  owner:           'მფლობელი',
  admin:           'ადმინისტრატორი',
  order_manager:   'შეკვეთების მენეჯერი',
  content_manager: 'კონტენტ მენეჯერი',
  support_agent:   'მხარდაჭერის თანამშრომელი',
};

// ─── DOM Helpers ──────────────────────────────────────────────────────────────
export function el(tag, attrs = {}, ...children) {
  const element = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === 'className') element.className = v;
    else if (k === 'innerHTML') element.innerHTML = v;
    else if (k.startsWith('on') && typeof v === 'function') element.addEventListener(k.slice(2).toLowerCase(), v);
    else element.setAttribute(k, v);
  }
  for (const child of children) {
    if (typeof child === 'string') element.insertAdjacentHTML('beforeend', child);
    else if (child) element.appendChild(child);
  }
  return element;
}

export function qs(selector, root = document) { return root.querySelector(selector); }
export function qsa(selector, root = document) { return [...root.querySelectorAll(selector)]; }

// ─── Modal Helpers ────────────────────────────────────────────────────────────
export function openModal(id) {
  document.getElementById(id)?.classList.add('open');
  document.body.style.overflow = 'hidden';
}

export function closeModal(id) {
  document.getElementById(id)?.classList.remove('open');
  document.body.style.overflow = '';
}

export function createModal({ id, title, body, footer, size = '' }) {
  const existing = document.getElementById(id);
  if (existing) existing.remove();

  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.id = id;
  overlay.innerHTML = `
    <div class="modal ${size}">
      <div class="modal-header">
        <h2 class="modal-title">${title}</h2>
        <button class="btn btn-icon btn-ghost modal-close" aria-label="დახურვა">✕</button>
      </div>
      <div class="modal-body">${body}</div>
      ${footer ? `<div class="modal-footer">${footer}</div>` : ''}
    </div>
  `;

  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) closeModal(id);
  });
  overlay.querySelectorAll('.modal-close').forEach(btn => {
    btn.addEventListener('click', () => closeModal(id));
  });

  document.body.appendChild(overlay);
  requestAnimationFrame(() => openModal(id));
  return overlay;
}

// ─── Confirm Dialog ───────────────────────────────────────────────────────────
export function confirmDialog(message, { title = 'დადასტურება', dangerLabel = 'წაშლა' } = {}) {
  return new Promise((resolve) => {
    const id = 'confirm-modal-' + Date.now();
    const modal = createModal({
      id,
      title,
      body: `<p class="text-sm text-gray">${message}</p>`,
      footer: `
        <button class="btn btn-secondary" data-action="cancel">გაუქმება</button>
        <button class="btn btn-danger" data-action="confirm">${dangerLabel}</button>
      `,
    });

    modal.querySelector('[data-action="cancel"]').addEventListener('click', () => {
      closeModal(id);
      setTimeout(() => modal.remove(), 300);
      resolve(false);
    });
    modal.querySelector('[data-action="confirm"]').addEventListener('click', () => {
      closeModal(id);
      setTimeout(() => modal.remove(), 300);
      resolve(true);
    });
  });
}

// ─── Form Helpers ─────────────────────────────────────────────────────────────
export function getFormData(formEl) {
  const data = {};
  const fd = new FormData(formEl);
  for (const [key, value] of fd.entries()) {
    data[key] = value;
  }
  return data;
}

export function setFormError(formEl, fieldName, message) {
  const field = formEl.querySelector(`[name="${fieldName}"]`);
  const existing = field?.parentElement?.querySelector('.form-error');
  if (existing) existing.remove();
  if (message && field) {
    const err = document.createElement('span');
    err.className = 'form-error';
    err.textContent = message;
    field.parentElement.appendChild(err);
    field.style.borderColor = 'var(--color-danger)';
  } else if (field) {
    field.style.borderColor = '';
  }
}

export function clearFormErrors(formEl) {
  formEl.querySelectorAll('.form-error').forEach(el => el.remove());
  formEl.querySelectorAll('.form-control').forEach(el => el.style.borderColor = '');
}

// ─── Debounce ─────────────────────────────────────────────────────────────────
export function debounce(fn, ms = 400) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), ms);
  };
}

// ─── Pagination ───────────────────────────────────────────────────────────────
export function renderPagination({ total, page, perPage, onPage }) {
  const totalPages = Math.ceil(total / perPage);
  if (totalPages <= 1) return '';

  const from = (page - 1) * perPage + 1;
  const to   = Math.min(page * perPage, total);

  const pages = [];
  for (let p = 1; p <= totalPages; p++) {
    if (p === 1 || p === totalPages || (p >= page - 1 && p <= page + 1)) {
      pages.push(p);
    } else if (pages[pages.length - 1] !== '…') {
      pages.push('…');
    }
  }

  const btns = pages.map(p => {
    if (p === '…') return `<span class="page-btn" style="cursor:default">…</span>`;
    return `<button class="page-btn ${p === page ? 'active' : ''}" data-page="${p}">${p}</button>`;
  }).join('');

  const html = `
    <div class="pagination">
      <span>${from}–${to} / ${total} ჩანაწერი</span>
      <div class="pagination-controls">
        <button class="page-btn" data-page="${page-1}" ${page<=1 ? 'disabled' : ''}>‹</button>
        ${btns}
        <button class="page-btn" data-page="${page+1}" ${page>=totalPages ? 'disabled' : ''}>›</button>
      </div>
    </div>
  `;

  // After insertion, attach listeners
  requestAnimationFrame(() => {
    document.querySelectorAll('.pagination-controls .page-btn[data-page]').forEach(btn => {
      btn.addEventListener('click', () => {
        const p = parseInt(btn.dataset.page);
        if (p >= 1 && p <= totalPages && p !== page) onPage(p);
      });
    });
  });

  return html;
}

// ─── Image URL Helper ─────────────────────────────────────────────────────────
export function getImageUrl(supabase, bucket, path) {
  if (!path) return null;
  if (path.startsWith('http')) return path;
  const { data } = supabase.storage.from(bucket).getPublicUrl(path);
  return data?.publicUrl ?? null;
}

// ─── Role Permission Check ────────────────────────────────────────────────────
export function hasPermission(userRole, minRole) {
  const hierarchy = ['support_agent', 'content_manager', 'order_manager', 'admin', 'owner'];
  return hierarchy.indexOf(userRole) >= hierarchy.indexOf(minRole);
}
