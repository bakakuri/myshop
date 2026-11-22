// MYSHOP Admin — Customers Management Page
import { formatDate, formatDateTime, formatPrice, renderPagination, createModal, closeModal } from '../js/utils.js';

const PER_PAGE = 25;
let state = { customers: [], total: 0, page: 1, search: '' };

export async function render(container, { supabase }) {
  container.innerHTML = renderShell();
  bindEvents(supabase);
  await loadCustomers(supabase);
}

function renderShell() {
  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">მომხმარებლები</h1>
        <p class="page-subtitle" id="custCount">იტვირთება…</p>
      </div>
      <button class="btn btn-secondary" id="exportCustBtn">📥 CSV</button>
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="search-input-wrap">
          <span class="search-icon">🔍</span>
          <input type="text" class="form-control" id="custSearch" placeholder="სახელი ან ელ. ფოსტა…">
        </div>
      </div>
      <div class="table-container" style="border:none;border-radius:0">
        <table class="data-table">
          <thead>
            <tr>
              <th>მომხმარებელი</th>
              <th>ტელ.</th>
              <th>შეკვ.</th>
              <th>სულ გად.</th>
              <th>რეგ. თ.</th>
              <th class="col-actions">მოქ.</th>
            </tr>
          </thead>
          <tbody id="custBody">
            <tr><td colspan="6"><div class="skeleton" style="height:200px;margin:8px;border-radius:8px"></div></td></tr>
          </tbody>
        </table>
      </div>
      <div id="custPagination"></div>
    </div>
  `;
}

function bindEvents(supabase) {
  let timer;
  document.getElementById('custSearch').addEventListener('input', e => {
    clearTimeout(timer);
    timer = setTimeout(() => { state.search = e.target.value.trim(); state.page = 1; loadCustomers(supabase); }, 400);
  });
  document.getElementById('exportCustBtn').addEventListener('click', () => exportCustomers(supabase));
}

async function loadCustomers(supabase) {
  const tbody = document.getElementById('custBody');
  tbody.innerHTML = `<tr><td colspan="6"><div class="skeleton" style="height:200px;margin:8px;border-radius:8px"></div></td></tr>`;

  let query = supabase
    .from('profiles')
    .select(`id, full_name, email, phone, created_at, role`, { count: 'exact' })
    .eq('role', 'customer');

  if (state.search) {
    query = query.or(`full_name.ilike.%${state.search}%,email.ilike.%${state.search}%`);
  }

  const { data, count } = await query
    .order('created_at', { ascending: false })
    .range((state.page - 1) * PER_PAGE, state.page * PER_PAGE - 1);

  state.customers = data || [];
  state.total = count || 0;
  document.getElementById('custCount').textContent = `სულ ${state.total} მომხმ.`;

  if (!state.customers.length) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state"><div class="empty-state-icon">👥</div><div class="empty-state-desc">მომხმ. ვერ მოიძებნა</div></div></td></tr>`;
    return;
  }

  // Batch fetch order counts + totals
  const ids = state.customers.map(c => c.id);
  const { data: orders } = await supabase
    .from('orders')
    .select('customer_id, total')
    .in('customer_id', ids)
    .not('status', 'eq', 'cancelled');

  const orderMap = {};
  (orders || []).forEach(o => {
    if (!orderMap[o.customer_id]) orderMap[o.customer_id] = { count: 0, total: 0 };
    orderMap[o.customer_id].count++;
    orderMap[o.customer_id].total += o.total || 0;
  });

  tbody.innerHTML = state.customers.map(c => {
    const initials = (c.full_name || c.email || '?').slice(0,2).toUpperCase();
    const om = orderMap[c.id] || { count: 0, total: 0 };
    return `
      <tr style="cursor:pointer" onclick="">
        <td>
          <div class="flex items-center gap-3">
            <div style="width:36px;height:36px;border-radius:50%;background:var(--color-primary-100);flex-shrink:0;display:flex;align-items:center;justify-content:center;font-weight:700;color:var(--color-primary);font-size:13px">${initials}</div>
            <div>
              <div class="font-semibold text-sm">${c.full_name || '—'}</div>
              <div class="text-xs text-gray">${c.email || ''}</div>
            </div>
          </div>
        </td>
        <td class="text-sm text-gray">${c.phone || '—'}</td>
        <td class="text-sm font-medium">${om.count}</td>
        <td class="text-sm font-semibold">${formatPrice(om.total)}</td>
        <td class="text-xs text-gray">${formatDate(c.created_at)}</td>
        <td class="col-actions">
          <button class="btn btn-sm btn-secondary" onclick="event.stopPropagation()" data-id="${c.id}" data-name="${c.full_name || c.email}">
            👁 დეტ.
          </button>
        </td>
      </tr>
    `;
  }).join('');

  tbody.querySelectorAll('[data-id]').forEach(btn => {
    btn.addEventListener('click', e => { e.stopPropagation(); openCustomerDetail(supabase, btn.dataset.id, btn.dataset.name); });
  });

  document.getElementById('custPagination').innerHTML = renderPagination({
    total: state.total, page: state.page, perPage: PER_PAGE,
    onPage: (p) => { state.page = p; loadCustomers(supabase); }
  });
}

async function openCustomerDetail(supabase, userId, name) {
  createModal({
    id: 'custDetailModal',
    title: name,
    size: 'modal-lg',
    body: `<div class="skeleton" style="height:280px;border-radius:8px"></div>`,
  });

  const [profileRes, ordersRes, addrsRes] = await Promise.all([
    supabase.from('profiles').select('*').eq('id', userId).single(),
    supabase.from('orders').select('id, order_number, status, total, created_at').eq('customer_id', userId).order('created_at', { ascending: false }).limit(10),
    supabase.from('addresses').select('*').eq('user_id', userId),
  ]);

  const { data: profile } = profileRes;
  const { data: orders }  = ordersRes;
  const { data: addrs }   = addrsRes;

  const body = document.querySelector('#custDetailModal .modal-body');
  body.innerHTML = `
    <div class="form-row cols-2">
      <div>
        <div class="form-label">სრული სახელი</div>
        <div class="font-semibold">${profile?.full_name || '—'}</div>
      </div>
      <div>
        <div class="form-label">ელ. ფოსტა</div>
        <div>${profile?.email || '—'}</div>
      </div>
      <div>
        <div class="form-label">ტელეფონი</div>
        <div>${profile?.phone || '—'}</div>
      </div>
      <div>
        <div class="form-label">რეგ. თ.</div>
        <div class="text-sm text-gray">${formatDateTime(profile?.created_at)}</div>
      </div>
    </div>

    <div style="border-top:1px solid var(--color-gray-100);padding-top:16px">
      <div class="form-label" style="margin-bottom:12px">მისამართები</div>
      ${addrs?.length
        ? addrs.map(a => `<div class="text-sm" style="padding:8px;background:var(--color-gray-50);border-radius:8px;margin-bottom:8px">${a.label || ''}: ${a.city || ''}, ${a.address_line || ''}</div>`).join('')
        : `<div class="text-sm text-gray">მისამართი არ არის</div>`}
    </div>

    <div style="border-top:1px solid var(--color-gray-100);padding-top:16px">
      <div class="form-label" style="margin-bottom:12px">ბოლო შეკვ.</div>
      ${orders?.length
        ? orders.map(o => `
          <div class="flex items-center justify-between" style="padding:8px 0;border-bottom:1px solid var(--color-gray-100)">
            <span class="font-semibold text-sm" style="color:var(--color-primary)">#${o.order_number}</span>
            <span class="text-xs text-gray">${formatDate(o.created_at)}</span>
            <span class="font-semibold">${formatPrice(o.total)}</span>
          </div>`).join('')
        : `<div class="text-sm text-gray">შეკვ. არ არის</div>`}
    </div>
  `;
}

async function exportCustomers(supabase) {
  const { data } = await supabase.from('profiles').select('full_name, email, phone, created_at').eq('role', 'customer').order('created_at', { ascending: false });
  if (!data?.length) return;
  const rows = [['სახელი','ელ.ფოსტა','ტელ.','რეგ.თ.'], ...data.map(c => [c.full_name, c.email, c.phone, formatDateTime(c.created_at)])];
  const csv = rows.map(r => r.map(v => `"${String(v||'').replace(/"/g,'""')}"`).join(',')).join('\n');
  const blob = new Blob(['﻿'+csv], { type: 'text/csv' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob); a.download = 'customers.csv'; a.click();
}
