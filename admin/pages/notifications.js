// MYSHOP Admin — Push Notification Sender
import { formatDateTime } from '../js/utils.js';

export async function render(container, { supabase }) {
  container.innerHTML = `
    <div class="page-header">
      <h1 class="page-title">🔔 შეტყობინ.</h1>
    </div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;align-items:start">
      <!-- Compose Panel -->
      <div class="card">
        <div class="card-header"><h3>✍️ ახ. შეტ.</h3></div>
        <div class="card-body">
          <form id="notifForm">
            <div class="form-group"><label class="form-label required">სათ.</label>
              <input class="form-control" id="nTitle" required placeholder="მ.შ. ახ. კოლ. ჩამ."></div>
            <div class="form-group"><label class="form-label required">ტ.</label>
              <textarea class="form-control" id="nBody" rows="3" required placeholder="შ. ტ..."></textarea></div>
            <div class="form-group"><label class="form-label">სეგ.</label>
              <select class="form-control" id="nSegment">
                <option value="all">ყვ. მომხ.</option>
                <option value="customers_with_orders">შ.-ს ი.</option>
                <option value="customers_no_orders">შ.-ს გ.</option>
                <option value="inactive_30d">30 დ. ი.</option>
              </select></div>
            <div class="form-group"><label class="form-label">ბ. URL</label>
              <input class="form-control" id="nUrl" placeholder="myshop://product/..."></div>
            <div id="nPreviewBox" style="display:none;background:var(--color-gray-50);border:1px solid var(--color-gray-200);border-radius:12px;padding:14px;margin-bottom:16px">
              <div class="text-xs text-gray mb-2">📱 გად.:</div>
              <div class="font-semibold text-sm" id="nPrevTitle"></div>
              <div class="text-sm text-gray mt-1" id="nPrevBody"></div>
            </div>
            <div class="flex gap-2">
              <button type="button" class="btn btn-secondary" id="nPreviewBtn" style="flex:1">👁 გად.</button>
              <button type="submit" class="btn btn-primary" id="nSendBtn" style="flex:1">🚀 გაგ.</button>
            </div>
          </form>
        </div>
      </div>

      <!-- History Panel -->
      <div class="card">
        <div class="card-header"><h3>📋 ისტ.</h3></div>
        <div id="notifHistory"><div class="skeleton" style="height:300px"></div></div>
      </div>
    </div>
  `;

  // Preview logic
  document.getElementById('nPreviewBtn').addEventListener('click', () => {
    const title = document.getElementById('nTitle').value.trim();
    const body = document.getElementById('nBody').value.trim();
    if (!title && !body) return;
    document.getElementById('nPrevTitle').textContent = title || '(სათ. ც.)';
    document.getElementById('nPrevBody').textContent = body || '(ტ. ც.)';
    document.getElementById('nPreviewBox').style.display = 'block';
  });

  document.getElementById('notifForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = document.getElementById('nTitle').value.trim();
    const body = document.getElementById('nBody').value.trim();
    const segment = document.getElementById('nSegment').value;
    const url = document.getElementById('nUrl').value.trim();
    if (!title || !body) { window.__APP__.toast.error('სათ. & ტ. სავ.'); return; }

    const btn = document.getElementById('nSendBtn');
    btn.disabled = true; btn.textContent = '⏳ ი...';

    try {
      // Insert notification record — actual FCM sending handled by Supabase Edge Function
      const { error } = await supabase.from('push_notifications').insert({
        title, body, segment, url: url || null, status: 'pending',
        sent_by: (await supabase.auth.getUser()).data.user?.id,
      });
      if (error) throw error;

      // Trigger edge function (best-effort — no crash if unavailable)
      supabase.functions.invoke('send-push', { body: { title, body, segment, url } }).catch(() => {});

      window.__APP__.toast.success('შეტ. გ.');
      document.getElementById('notifForm').reset();
      document.getElementById('nPreviewBox').style.display = 'none';
      await loadHistory(supabase);
    } catch (err) {
      window.__APP__.toast.error(err.message || 'შ. გ. ვ.');
    } finally {
      btn.disabled = false; btn.textContent = '🚀 გაგ.';
    }
  });

  await loadHistory(supabase);
}

async function loadHistory(supabase) {
  const { data: notifs } = await supabase
    .from('push_notifications')
    .select('*')
    .order('created_at', { ascending: false })
    .limit(20);

  const el = document.getElementById('notifHistory');

  if (!notifs?.length) {
    el.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">🔕</div><div class="empty-state-desc">შ. ჯ. გ. არ</div></div>`;
    return;
  }

  const segLabels = { all: 'ყვ.', customers_with_orders: 'შ.ი.', customers_no_orders: 'შ.გ.', inactive_30d: '30დ.ი.' };
  const statusBadge = { pending: 'badge-pending', sent: 'badge-active', failed: 'badge-danger' };

  el.innerHTML = `<div style="padding:0 16px 16px;display:flex;flex-direction:column;gap:10px">
    ${notifs.map(n => `
      <div style="border:1px solid var(--color-gray-200);border-radius:12px;padding:12px">
        <div class="flex items-start justify-between gap-2">
          <div>
            <div class="font-semibold text-sm">${n.title}</div>
            <div class="text-sm text-gray mt-1">${n.body}</div>
          </div>
          <span class="badge ${statusBadge[n.status] || 'badge-draft'}" style="flex-shrink:0">${n.status}</span>
        </div>
        <div class="flex gap-2 mt-2">
          <span class="chip text-xs">${segLabels[n.segment] || n.segment}</span>
          ${n.sent_count != null ? `<span class="chip text-xs">📱 ${n.sent_count}</span>` : ''}
          <span class="text-xs text-gray" style="margin-left:auto">${formatDateTime(n.created_at)}</span>
        </div>
      </div>
    `).join('')}
  </div>`;
}
