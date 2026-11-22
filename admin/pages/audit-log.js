// MYSHOP Admin — Audit Log Viewer (owner/admin)
import { formatDateTime } from '../js/utils.js';

const PER_PAGE = 50;

const ACTION_ICONS = {
  order_status_changed: '📦',
  product_created: '➕',
  product_updated: '✏️',
  product_deleted: '🗑',
  category_created: '➕',
  category_updated: '✏️',
  category_deleted: '🗑',
  coupon_created: '🏷',
  coupon_updated: '🏷',
  coupon_deleted: '🗑',
  staff_role_changed: '🔑',
  staff_invited: '📧',
  staff_deactivated: '🚫',
  settings_changed: '⚙️',
  banner_created: '🖼',
  banner_updated: '🖼',
  banner_deleted: '🖼',
  notification_sent: '🔔',
};

let state = { page: 1, search: '', action: '', loading: false };

export async function render(container, { supabase }) {
  container.innerHTML = `
    <div class="page-header">
      <h1 class="page-title">📋 ცვ. ი.</h1>
    </div>
    <div class="card">
      <div class="card-body" style="padding-bottom:0">
        <div class="filter-bar" style="display:flex;gap:12px;flex-wrap:wrap;align-items:center">
          <input class="form-control" id="auditSearch" placeholder="🔍 ძ..." style="max-width:220px;flex:1" value="${state.search}">
          <select class="form-control" id="auditAction" style="max-width:200px">
            <option value="">ყ. ი.</option>
            ${Object.keys(ACTION_ICONS).map(a => `<option value="${a}" ${state.action===a?'selected':''}>${ACTION_ICONS[a]} ${a.replace(/_/g,' ')}</option>`).join('')}
          </select>
          <button class="btn btn-secondary" id="auditClearBtn">✕ გ.</button>
          <button class="btn btn-secondary" id="auditExportBtn">⬇ CSV</button>
        </div>
      </div>
      <div id="auditTableWrap" style="margin-top:16px"><div class="skeleton" style="height:400px"></div></div>
    </div>
  `;

  document.getElementById('auditSearch').addEventListener('input', debounce(e => { state.search = e.target.value; state.page = 1; loadAudit(supabase); }, 400));
  document.getElementById('auditAction').addEventListener('change', e => { state.action = e.target.value; state.page = 1; loadAudit(supabase); });
  document.getElementById('auditClearBtn').addEventListener('click', () => {
    state = { page: 1, search: '', action: '', loading: false };
    document.getElementById('auditSearch').value = '';
    document.getElementById('auditAction').value = '';
    loadAudit(supabase);
  });
  document.getElementById('auditExportBtn').addEventListener('click', () => exportAudit(supabase));

  await loadAudit(supabase);
}

async function loadAudit(supabase) {
  if (state.loading) return;
  state.loading = true;
  const wrap = document.getElementById('auditTableWrap');

  let query = supabase
    .from('audit_logs')
    .select(`*, actor:actor_id(email, full_name:profiles!inner(full_name))`, { count: 'exact' })
    .order('created_at', { ascending: false })
    .range((state.page - 1) * PER_PAGE, state.page * PER_PAGE - 1);

  if (state.search) query = query.or(`description.ilike.%${state.search}%,entity_id.ilike.%${state.search}%`);
  if (state.action) query = query.eq('action', state.action);

  const { data: logs, count, error } = await query;
  state.loading = false;

  if (error) { wrap.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">❌</div><div class="empty-state-desc">${error.message}</div></div>`; return; }
  if (!logs?.length) { wrap.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">📋</div><div class="empty-state-desc">ი. ჩ. არ</div></div>`; return; }

  wrap.innerHTML = `
    <div class="table-container">
      <table class="data-table">
        <thead><tr><th>დ.დ.</th><th>ი.</th><th>ო.</th><th>ობ.</th><th>ა.</th><th>IP</th></tr></thead>
        <tbody>
          ${logs.map(l => `
            <tr>
              <td class="text-xs text-gray" style="white-space:nowrap">${formatDateTime(l.created_at)}</td>
              <td class="text-sm">${l.actor?.full_name || l.actor?.email || 'სისტ.'}</td>
              <td>
                <div class="flex items-center gap-1">
                  <span>${ACTION_ICONS[l.action] || '📋'}</span>
                  <span class="text-xs">${l.action?.replace(/_/g,' ') || '—'}</span>
                </div>
              </td>
              <td class="text-xs text-gray">${l.entity_type ? `${l.entity_type} #${l.entity_id?.slice(0,8) || ''}` : '—'}</td>
              <td class="text-xs">${l.description || '—'}</td>
              <td class="text-xs text-gray">${l.ip_address || '—'}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
    <div style="padding:12px 16px;border-top:1px solid var(--color-gray-200);display:flex;align-items:center;justify-content:space-between">
      <span class="text-xs text-gray">სულ ${count || 0} ჩ.</span>
      <div class="flex gap-2">
        ${state.page > 1 ? `<button class="btn btn-sm btn-secondary" id="auditPrev">← წ.</button>` : ''}
        <span class="text-xs text-gray" style="padding:6px 8px">გვ. ${state.page}</span>
        ${count > state.page * PER_PAGE ? `<button class="btn btn-sm btn-secondary" id="auditNext">შ. →</button>` : ''}
      </div>
    </div>
  `;

  document.getElementById('auditPrev')?.addEventListener('click', () => { state.page--; loadAudit(supabase); });
  document.getElementById('auditNext')?.addEventListener('click', () => { state.page++; loadAudit(supabase); });
}

async function exportAudit(supabase) {
  const { data: logs } = await supabase
    .from('audit_logs')
    .select('created_at, action, entity_type, entity_id, description, ip_address')
    .order('created_at', { ascending: false })
    .limit(500);

  if (!logs?.length) { window.__APP__.toast.warning('ი. ე.'); return; }

  const rows = [['თ.', 'ი.', 'ო.ტ.', 'ო.ID', 'ა.', 'IP']];
  logs.forEach(l => rows.push([l.created_at, l.action, l.entity_type||'', l.entity_id||'', l.description||'', l.ip_address||'']));
  const csv = '﻿' + rows.map(r => r.map(c => `"${String(c).replace(/"/g,'""')}"`).join(',')).join('\r\n');
  const a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
  a.download = `audit-log-${new Date().toISOString().slice(0,10)}.csv`;
  a.click();
}

function debounce(fn, ms) { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; }
