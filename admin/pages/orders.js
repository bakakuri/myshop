// MYSHOP Admin — Orders Management Page
import { formatPrice, formatDateTime, formatRelative, orderStatusBadge, ORDER_STATUS, renderPagination, confirmDialog, createModal, closeModal, openModal } from '../js/utils.js';

const PER_PAGE = 20;
let state = { orders: [], total: 0, page: 1, search: '', status: '', loading: false };

export async function render(container, { supabase }) {
  container.innerHTML = renderShell();
  bindEvents(supabase);
  await loadOrders(supabase);
}

function renderShell() {
  const statusOptions = Object.entries(ORDER_STATUS)
    .map(([k, v]) => `<option value="${k}">${v.label}</option>`)
    .join('');

  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">შეკვეთები</h1>
        <p class="page-subtitle" id="orderCount">იტვირთება…</p>
      </div>
      <button class="btn btn-secondary" id="exportBtn">📥 CSV-ს ექსპორტი</button>
    </div>

    <div class="card">
      <!-- Filter bar -->
      <div class="filter-bar">
        <div class="search-input-wrap">
          <span class="search-icon">🔍</span>
          <input type="text" class="form-control" id="orderSearch"
                 placeholder="შეკვეთის # ან მომხმარებელი…">
        </div>
        <select class="form-control" id="statusFilter" style="width:180px">
          <option value="">ყველა სტატუსი</option>
          ${statusOptions}
        </select>
        <select class="form-control" id="sortSelect" style="width:160px">
          <option value="created_at:desc">უახლესი პირველი</option>
          <option value="created_at:asc">ძველი პირველი</option>
          <option value="total:desc">ყველაზე ძვირი</option>
        </select>
        <button class="btn btn-secondary btn-sm" id="clearFilters">გასუფთავება</button>
      </div>

      <!-- Table -->
      <div class="table-container" style="border:none;border-radius:0">
        <table class="data-table">
          <thead>
            <tr>
              <th style="width:40px"><input type="checkbox" id="selectAll" title="ყველა"></th>
              <th>შეკვეთა #</th>
              <th>მომხმარებელი</th>
              <th>თარიღი</th>
              <th>სტატუსი</th>
              <th>პოზ.</th>
              <th>ჯამი</th>
              <th class="col-actions">მოქმედება</th>
            </tr>
          </thead>
          <tbody id="ordersBody">
            <tr><td colspan="8"><div class="skeleton" style="height:200px;margin:8px"></div></td></tr>
          </tbody>
        </table>
      </div>
      <div id="ordersPagination"></div>
    </div>
  `;
}

function bindEvents(supabase) {
  let searchTimer;
  document.getElementById('orderSearch').addEventListener('input', e => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => { state.search = e.target.value.trim(); state.page = 1; loadOrders(supabase); }, 400);
  });

  document.getElementById('statusFilter').addEventListener('change', e => {
    state.status = e.target.value; state.page = 1; loadOrders(supabase);
  });

  document.getElementById('sortSelect').addEventListener('change', () => { state.page = 1; loadOrders(supabase); });

  document.getElementById('clearFilters').addEventListener('click', () => {
    document.getElementById('orderSearch').value = '';
    document.getElementById('statusFilter').value = '';
    document.getElementById('sortSelect').value = 'created_at:desc';
    state.search = ''; state.status = ''; state.page = 1;
    loadOrders(supabase);
  });

  document.getElementById('exportBtn').addEventListener('click', () => exportCSV(supabase));

  document.getElementById('selectAll').addEventListener('change', e => {
    document.querySelectorAll('.order-check').forEach(cb => cb.checked = e.target.checked);
  });
}

async function loadOrders(supabase) {
  if (state.loading) return;
  state.loading = true;
  const tbody = document.getElementById('ordersBody');
  tbody.innerHTML = `<tr><td colspan="8"><div class="skeleton" style="height:200px;margin:8px;border-radius:8px"></div></td></tr>`;

  try {
    const [sortField, sortDir] = (document.getElementById('sortSelect')?.value || 'created_at:desc').split(':');

    let query = supabase
      .from('orders')
      .select(`id, order_number, status, total, created_at,
               profiles:customer_id(id, full_name, email, phone),
               order_items(id)`, { count: 'exact' });

    if (state.status) query = query.eq('status', state.status);
    if (state.search) {
      query = query.or(`order_number.ilike.%${state.search}%`);
    }

    query = query.order(sortField, { ascending: sortDir === 'asc' })
                 .range((state.page - 1) * PER_PAGE, state.page * PER_PAGE - 1);

    const { data: orders, count, error } = await query;
    if (error) throw error;

    state.orders = orders || [];
    state.total  = count || 0;

    document.getElementById('orderCount').textContent = `სულ ${state.total} შეკვეთა`;

    if (!state.orders.length) {
      tbody.innerHTML = `<tr><td colspan="8"><div class="empty-state"><div class="empty-state-icon">📦</div><div class="empty-state-desc">შეკვეთები არ მოიძებნა</div></div></td></tr>`;
    } else {
      tbody.innerHTML = state.orders.map(o => renderOrderRow(o)).join('');
      tbody.querySelectorAll('.btn-view-order').forEach(btn => {
        btn.addEventListener('click', () => openOrderDetail(supabase, btn.dataset.id));
      });
      tbody.querySelectorAll('.status-select').forEach(sel => {
        sel.addEventListener('change', e => updateStatus(supabase, sel.dataset.id, e.target.value));
      });
    }

    document.getElementById('ordersPagination').innerHTML = renderPagination({
      total: state.total, page: state.page, perPage: PER_PAGE,
      onPage: (p) => { state.page = p; loadOrders(supabase); }
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8"><div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-desc">${err.message}</div></div></td></tr>`;
  } finally {
    state.loading = false;
  }
}

function renderOrderRow(o) {
  const statusOptions = Object.entries(ORDER_STATUS)
    .map(([k, v]) => `<option value="${k}" ${k === o.status ? 'selected' : ''}>${v.label}</option>`)
    .join('');

  return `
    <tr>
      <td><input type="checkbox" class="order-check" value="${o.id}"></td>
      <td>
        <span class="font-semibold" style="color:var(--color-primary)">#${o.order_number}</span>
      </td>
      <td>
        <div class="font-medium">${o.profiles?.full_name || '—'}</div>
        <div class="text-xs text-gray">${o.profiles?.email || ''}</div>
      </td>
      <td>
        <div class="text-sm">${formatDateTime(o.created_at)}</div>
      </td>
      <td>
        <select class="form-control status-select" data-id="${o.id}"
                style="height:30px;font-size:12px;padding:0 28px 0 8px;min-width:130px">
          ${statusOptions}
        </select>
      </td>
      <td class="text-sm text-gray">${o.order_items?.length ?? 0} პ.</td>
      <td class="font-semibold">${formatPrice(o.total)}</td>
      <td class="col-actions">
        <button class="btn btn-sm btn-primary btn-view-order" data-id="${o.id}">დეტ.</button>
      </td>
    </tr>
  `;
}

async function updateStatus(supabase, orderId, newStatus) {
  const { error } = await supabase
    .from('orders')
    .update({ status: newStatus })
    .eq('id', orderId);

  const toast = window.__APP__.toast;
  if (error) {
    toast.error('სტატუსის შეცვლა ვერ მოხერხდა');
  } else {
    toast.success('სტატუსი განახლდა');
  }
}

async function openOrderDetail(supabase, orderId) {
  const modal = createModal({
    id: 'orderDetailModal',
    title: 'შეკვეთის დეტალები',
    body: `<div class="skeleton" style="height:300px;border-radius:8px"></div>`,
    size: 'modal-lg',
  });

  const { data: order, error } = await supabase
    .from('orders')
    .select(`*,
             profiles:customer_id(full_name, email, phone),
             order_items(*, product:product_id(name, images), variant:variant_id(name))`)
    .eq('id', orderId)
    .single();

  const body = modal.querySelector('.modal-body');

  if (error || !order) {
    body.innerHTML = `<p class="text-danger">შეკვეთა ვერ ჩაიტვირთა</p>`;
    return;
  }

  body.innerHTML = `
    <div class="form-row cols-2">
      <div>
        <div class="form-label">შეკვეთის #</div>
        <div class="font-bold" style="font-size:18px">#${order.order_number}</div>
      </div>
      <div>
        <div class="form-label">სტატუსი</div>
        ${orderStatusBadge(order.status)}
      </div>
    </div>

    <div style="border-top:1px solid var(--color-gray-100);padding-top:16px">
      <div class="form-label">მომხმარებელი</div>
      <div class="font-semibold">${order.profiles?.full_name || '—'}</div>
      <div class="text-sm text-gray">${order.profiles?.email || ''} ${order.profiles?.phone ? '· ' + order.profiles.phone : ''}</div>
    </div>

    ${order.delivery_address ? `
    <div style="border-top:1px solid var(--color-gray-100);padding-top:16px">
      <div class="form-label">მიწოდების მისამართი</div>
      <div class="text-sm">${order.delivery_address}</div>
    </div>` : ''}

    ${order.notes ? `
    <div style="border-top:1px solid var(--color-gray-100);padding-top:16px">
      <div class="form-label">კომენტარი</div>
      <div class="text-sm">${order.notes}</div>
    </div>` : ''}

    <div style="border-top:1px solid var(--color-gray-100);padding-top:16px">
      <div class="form-label" style="margin-bottom:12px">პროდუქტები</div>
      ${(order.order_items || []).map(item => `
        <div class="flex items-center gap-3" style="padding:10px 0;border-bottom:1px solid var(--color-gray-100)">
          ${item.product?.images?.[0]
            ? `<img src="${item.product.images[0]}" style="width:48px;height:48px;object-fit:cover;border-radius:8px" alt="">`
            : `<div style="width:48px;height:48px;background:var(--color-gray-100);border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:20px">📷</div>`}
          <div style="flex:1">
            <div class="font-semibold text-sm">${item.product_name || item.product?.name || '—'}</div>
            ${item.variant?.name ? `<div class="text-xs text-gray">${item.variant.name}</div>` : ''}
            <div class="text-xs text-gray">x${item.quantity} · ${formatPrice(item.unit_price)}</div>
          </div>
          <div class="font-semibold">${formatPrice(item.unit_price * item.quantity)}</div>
        </div>
      `).join('')}
    </div>

    <div class="flex justify-between items-center" style="border-top:1px solid var(--color-gray-100);padding-top:16px;margin-top:4px">
      <span class="font-semibold">სულ ჯამი:</span>
      <span class="font-bold" style="font-size:20px;color:var(--color-primary)">${formatPrice(order.total)}</span>
    </div>

    <div style="margin-top:16px">
      <label class="form-label">სტატუსის შეცვლა</label>
      <select class="form-control" id="detailStatusSel">
        ${Object.entries(ORDER_STATUS).map(([k, v]) => `<option value="${k}" ${k === order.status ? 'selected' : ''}>${v.label}</option>`).join('')}
      </select>
    </div>
  `;

  // Add footer buttons
  const footer = document.createElement('div');
  footer.className = 'modal-footer';
  footer.innerHTML = `
    <button class="btn btn-secondary modal-close">დახურვა</button>
    <button class="btn btn-primary" id="saveDetailStatus">სტატუსის შენახვა</button>
  `;
  modal.querySelector('.modal').appendChild(footer);
  footer.querySelectorAll('.modal-close').forEach(b => b.addEventListener('click', () => closeModal('orderDetailModal')));
  footer.querySelector('#saveDetailStatus').addEventListener('click', async () => {
    const newStatus = document.getElementById('detailStatusSel').value;
    await updateStatus(supabase, orderId, newStatus);
    closeModal('orderDetailModal');
    loadOrders(supabase);
  });
}

async function exportCSV(supabase) {
  const { data: orders } = await supabase
    .from('orders')
    .select(`order_number, status, total, created_at, delivery_address,
             profiles:customer_id(full_name, email, phone)`)
    .order('created_at', { ascending: false });

  if (!orders?.length) return;

  const rows = [
    ['#', 'სტატუსი', 'ჯამი', 'მომხმარებელი', 'ელ. ფოსტა', 'ტელეფონი', 'მისამართი', 'თარიღი'],
    ...orders.map(o => [
      o.order_number, o.status, o.total,
      o.profiles?.full_name || '', o.profiles?.email || '', o.profiles?.phone || '',
      o.delivery_address || '', formatDateTime(o.created_at),
    ])
  ];

  const csv = rows.map(r => r.map(v => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n');
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' });
  const url  = URL.createObjectURL(blob);
  const a    = document.createElement('a');
  a.href = url; a.download = `orders-${new Date().toISOString().slice(0,10)}.csv`;
  a.click(); URL.revokeObjectURL(url);
}
