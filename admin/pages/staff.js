// MYSHOP Admin — Staff & Roles Management (owner-only)
import { confirmDialog, createModal, closeModal, formatDateTime, STAFF_ROLES } from '../js/utils.js';
import { AppState } from '../js/app.js';

export async function render(container, { supabase }) {
  if (AppState.userRole !== 'owner') {
    container.innerHTML = `<div class="empty-state" style="padding:120px"><div class="empty-state-icon">🔒</div><div class="empty-state-desc">მხ. მფლ. ხელმ.</div></div>`;
    return;
  }

  container.innerHTML = `
    <div class="page-header">
      <h1 class="page-title">👥 პ. & მ.</h1>
      <button class="btn btn-primary" id="inviteStaffBtn">+ პ. მ.</button>
    </div>
    <div class="card">
      <div id="staffTableWrap"><div class="skeleton" style="height:300px"></div></div>
    </div>

    <div class="card mt-20">
      <div class="card-header"><h3>🔑 მ. პ.</h3><p class="text-sm text-gray mt-1">ი. ნებ. ა. სისტ.</p></div>
      <div class="card-body">
        <div class="table-container">
          <table class="data-table">
            <thead><tr><th>ო.</th><th>შ.</th><th>პ.კ.</th><th>ს.მ.</th><th>მ.მ.</th><th>ა.</th><th>პ.ო.</th></tr></thead>
            <tbody>
              ${renderPermMatrix()}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `;

  document.getElementById('inviteStaffBtn').addEventListener('click', () => openInviteForm(supabase));
  await loadStaff(supabase);
}

function renderPermMatrix() {
  const roles = ['owner', 'admin', 'order_manager', 'content_manager', 'support_agent'];
  const perms = {
    owner: [true,true,true,true,true,true,true],
    admin: [true,true,true,true,true,true,false],
    order_manager: [false,true,false,false,false,false,false],
    content_manager: [false,false,false,true,false,false,false],
    support_agent: [false,true,false,false,false,false,false],
  };
  const roleLabels = { owner:'მფლ.', admin:'ადმ.', order_manager:'შ.მ.', content_manager:'კ.მ.', support_agent:'მხ.' };
  return roles.map(r => `<tr>
    <td class="font-semibold">${roleLabels[r]}</td>
    ${perms[r].map(p => `<td style="text-align:center">${p ? '✅' : '➖'}</td>`).join('')}
  </tr>`).join('');
}

async function loadStaff(supabase) {
  const { data: staff } = await supabase
    .from('staff_members')
    .select('id, email, role, is_active, created_at, last_active_at, profiles:user_id(full_name, avatar_url)')
    .order('created_at');

  const wrap = document.getElementById('staffTableWrap');
  if (!staff?.length) { wrap.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">👥</div><div class="empty-state-desc">პ. ჯ. არ</div></div>`; return; }

  const roleLabels = { owner:'🏆 მფლ.', admin:'⚙️ ადმ.', order_manager:'📦 შ.მ.', content_manager:'✏️ კ.მ.', support_agent:'💬 მხ.' };
  const me = (await supabase.auth.getUser()).data.user?.id;

  wrap.innerHTML = `
    <div class="table-container">
      <table class="data-table">
        <thead><tr><th>პ.</th><th>ე-მ.</th><th>ო.</th><th>ბ.ვ.</th><th>სტ.</th><th class="col-actions">მ.</th></tr></thead>
        <tbody>
          ${staff.map(s => `
            <tr>
              <td>
                <div class="flex items-center gap-2">
                  <div style="width:34px;height:34px;border-radius:50%;background:var(--color-primary-100);display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:600;color:var(--color-primary);flex-shrink:0;overflow:hidden">
                    ${s.profiles?.avatar_url ? `<img src="${s.profiles.avatar_url}" style="width:100%;height:100%;object-fit:cover">` : (s.profiles?.full_name?.[0] || s.email[0]).toUpperCase()}
                  </div>
                  <span class="font-semibold text-sm">${s.profiles?.full_name || '—'}</span>
                </div>
              </td>
              <td class="text-sm text-gray">${s.email}</td>
              <td><span class="chip text-xs">${roleLabels[s.role] || s.role}</span></td>
              <td class="text-xs text-gray">${s.last_active_at ? formatDateTime(s.last_active_at) : '—'}</td>
              <td><span class="badge ${s.is_active ? 'badge-active' : 'badge-draft'}">${s.is_active ? 'ჩ.' : 'გ.'}</span></td>
              <td class="col-actions">
                <div class="flex gap-1 justify-end">
                  ${s.user_id !== me ? `
                    <button class="btn btn-sm btn-secondary btn-change-role" data-id="${s.id}" data-role="${s.role}" data-name="${s.profiles?.full_name || s.email}">🔑</button>
                    <button class="btn btn-sm ${s.is_active ? 'btn-danger' : 'btn-success'} btn-toggle-staff" data-id="${s.id}" data-active="${s.is_active}" data-name="${s.profiles?.full_name || s.email}">
                      ${s.is_active ? '🚫' : '✅'}
                    </button>
                  ` : '<span class="text-xs text-gray">(თ.)</span>'}
                </div>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;

  wrap.querySelectorAll('.btn-change-role').forEach(btn => btn.addEventListener('click', () => openChangeRoleModal(supabase, btn.dataset)));
  wrap.querySelectorAll('.btn-toggle-staff').forEach(btn => btn.addEventListener('click', async () => {
    const isActive = btn.dataset.active === 'true';
    const action = isActive ? 'გ.' : 'ჩ.';
    if (await confirmDialog(`${btn.dataset.name} ${action}?`)) {
      await supabase.from('staff_members').update({ is_active: !isActive }).eq('id', btn.dataset.id);
      window.__APP__.toast.success(isActive ? 'გ.' : 'ჩ.'); loadStaff(supabase);
    }
  }));
}

function openChangeRoleModal(supabase, { id, role, name }) {
  const roles = ['admin','order_manager','content_manager','support_agent'];
  const rl = { admin:'ადმ.', order_manager:'შ.მ.', content_manager:'კ.მ.', support_agent:'მხ.' };
  createModal({
    id: 'roleModal', title: `${name}-ის ო. შ.`,
    body: `<div class="form-group"><label class="form-label">ახ. ო.</label>
      <select class="form-control" id="newRoleSelect">
        ${roles.map(r => `<option value="${r}" ${r===role?'selected':''}>${rl[r]}</option>`).join('')}
      </select></div>`,
    footer: `<button class="btn btn-secondary modal-close">გ.</button><button class="btn btn-primary" id="saveRoleBtn">შ.</button>`,
  });
  document.getElementById('saveRoleBtn').addEventListener('click', async () => {
    const newRole = document.getElementById('newRoleSelect').value;
    await supabase.from('staff_members').update({ role: newRole }).eq('id', id);
    window.__APP__.toast.success('ო. შ.'); closeModal('roleModal'); loadStaff(supabase);
  });
}

function openInviteForm(supabase) {
  const roles = ['admin','order_manager','content_manager','support_agent'];
  const rl = { admin:'⚙️ ადმ.', order_manager:'📦 შ.მ.', content_manager:'✏️ კ.მ.', support_agent:'💬 მხ.' };
  createModal({
    id: 'inviteModal', title: 'პ. მ. მ.',
    body: `<form id="inviteForm">
      <div class="form-group"><label class="form-label required">ე-მ.</label>
        <input class="form-control" name="email" type="email" required placeholder="staff@..."></div>
      <div class="form-group"><label class="form-label required">ო.</label>
        <select class="form-control" name="role">
          ${roles.map(r => `<option value="${r}">${rl[r]}</option>`).join('')}
        </select></div>
      <div class="alert-info" style="background:var(--color-info-bg,#EFF6FF);border-left:4px solid var(--color-info,#3B82F6);padding:12px;border-radius:0 8px 8px 0;font-size:13px">
        💡 Supabase Auth-ი მ. ი. სს.-ს გ. ბ. ი. უ. ამ. ე-ს. მ. ი.
      </div>
    </form>`,
    footer: `<button class="btn btn-secondary modal-close">გ.</button><button class="btn btn-primary" id="sendInviteBtn">მ.</button>`,
  });
  document.getElementById('sendInviteBtn').addEventListener('click', async () => {
    const form = document.getElementById('inviteForm'); const fd = new FormData(form);
    const email = fd.get('email'); const role = fd.get('role');
    if (!email) return;
    const btn = document.getElementById('sendInviteBtn'); btn.disabled = true;
    // Record pending invite in DB; actual auth invite via Supabase Auth admin API (edge function)
    const { error } = await supabase.from('staff_invites').insert({ email, role, invited_by: (await supabase.auth.getUser()).data.user?.id });
    if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; }
    else { window.__APP__.toast.success('მ. გ.'); closeModal('inviteModal'); }
  });
}
