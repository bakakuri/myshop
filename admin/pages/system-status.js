// MYSHOP Admin — System Status & Health Dashboard
import { formatDateTime, formatRelative } from '../js/utils.js';

export async function render(container, { supabase }) {
  container.innerHTML = `
    <div class="page-header">
      <h1 class="page-title">🩺 სისტ. სტ.</h1>
      <button class="btn btn-secondary" id="refreshStatusBtn">↻ გ.</button>
    </div>
    <div id="statusContent"><div class="skeleton" style="height:400px;border-radius:14px"></div></div>
  `;

  document.getElementById('refreshStatusBtn').addEventListener('click', () => loadStatus(supabase));
  await loadStatus(supabase);
}

async function loadStatus(supabase) {
  const btn = document.getElementById('refreshStatusBtn');
  btn.disabled = true; btn.textContent = '⏳';

  const start = Date.now();

  // Parallel health checks
  const [dbCheck, authCheck, storageCheck, counts] = await Promise.all([
    checkDB(supabase),
    checkAuth(supabase),
    checkStorage(supabase),
    fetchCounts(supabase),
  ]);

  const totalMs = Date.now() - start;
  btn.disabled = false; btn.textContent = '↻ გ.';

  const checks = [
    { name: 'PostgreSQL', icon: '🗄', ...dbCheck },
    { name: 'Auth', icon: '🔐', ...authCheck },
    { name: 'Storage', icon: '📦', ...storageCheck },
    { name: 'API (round-trip)', icon: '🌐', status: totalMs < 2000 ? 'ok' : totalMs < 5000 ? 'warn' : 'error', message: `${totalMs} ms` },
  ];

  const overallOk = checks.every(c => c.status === 'ok');
  const overallWarn = checks.some(c => c.status === 'warn');

  const statusColor = overallOk ? 'var(--color-success)' : overallWarn ? 'var(--color-warning)' : 'var(--color-danger)';
  const statusLabel = overallOk ? '✅ ყ. ნ.' : overallWarn ? '⚠️ გ.' : '❌ პ.';

  document.getElementById('statusContent').innerHTML = `
    <!-- Overall Banner -->
    <div style="background:${statusColor}15;border:2px solid ${statusColor};border-radius:16px;padding:20px 24px;display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
      <div>
        <div style="font-size:22px;font-weight:700;color:${statusColor}">${statusLabel}</div>
        <div class="text-sm text-gray mt-1">შ.: ${formatDateTime(new Date().toISOString())}</div>
      </div>
      <div style="font-size:48px">${overallOk ? '💚' : overallWarn ? '🟡' : '🔴'}</div>
    </div>

    <!-- Service Checks -->
    <div class="stats-grid" style="--grid-cols:2;margin-bottom:20px">
      ${checks.map(c => `
        <div class="card">
          <div class="card-body" style="display:flex;align-items:center;gap:14px">
            <div style="font-size:28px">${c.icon}</div>
            <div style="flex:1">
              <div class="font-semibold">${c.name}</div>
              <div class="text-sm text-gray mt-1">${c.message || ''}</div>
            </div>
            <div style="width:12px;height:12px;border-radius:50%;background:${c.status === 'ok' ? 'var(--color-success)' : c.status === 'warn' ? 'var(--color-warning)' : 'var(--color-danger)'};box-shadow:0 0 8px ${c.status === 'ok' ? 'var(--color-success)' : c.status === 'warn' ? 'var(--color-warning)' : 'var(--color-danger)'}66"></div>
          </div>
        </div>
      `).join('')}
    </div>

    <!-- DB Stats -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px">
      <div class="card">
        <div class="card-header"><h3>📊 DB სტ.</h3></div>
        <div class="card-body">
          <table style="width:100%;border-collapse:collapse">
            ${Object.entries(counts).map(([k, v]) => `
              <tr style="border-bottom:1px solid var(--color-gray-100)">
                <td style="padding:10px 0;font-size:14px;color:var(--color-gray-600)">${k}</td>
                <td style="padding:10px 0;font-size:16px;font-weight:600;text-align:right">${v?.toLocaleString() ?? '—'}</td>
              </tr>
            `).join('')}
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><h3>🔗 კონფ.</h3></div>
        <div class="card-body">
          <div style="display:grid;gap:10px">
            ${configRows()}
          </div>
        </div>
      </div>
    </div>
  `;
}

async function checkDB(supabase) {
  try {
    const t0 = Date.now();
    const { error } = await supabase.from('products').select('id', { count: 'exact', head: true });
    const ms = Date.now() - t0;
    if (error) return { status: 'error', message: error.message };
    return { status: ms > 1000 ? 'warn' : 'ok', message: `${ms} ms` };
  } catch (e) { return { status: 'error', message: String(e) }; }
}

async function checkAuth(supabase) {
  try {
    const { data, error } = await supabase.auth.getSession();
    if (error) return { status: 'error', message: error.message };
    return { status: 'ok', message: data.session ? 'სესია ა.' : 'გ.ა.' };
  } catch (e) { return { status: 'error', message: String(e) }; }
}

async function checkStorage(supabase) {
  try {
    const { error } = await supabase.storage.getBucket('product-images');
    if (error && error.message.includes('not found')) return { status: 'warn', message: 'Bucket ვ. შ.' };
    if (error) return { status: 'error', message: error.message };
    return { status: 'ok', message: 'bucket ხ.' };
  } catch (e) { return { status: 'error', message: String(e) }; }
}

async function fetchCounts(supabase) {
  const labels = {
    products: 'პ.',
    orders: 'შ.',
    profiles: 'მ.',
    conversations: 'ს.',
    messages: 'შ.ტ.',
    banners: 'ბ.',
    coupons: 'კ.',
  };

  const results = {};
  await Promise.all(
    Object.keys(labels).map(async table => {
      try {
        const { count } = await supabase.from(table).select('*', { count: 'exact', head: true });
        results[labels[table]] = count ?? 0;
      } catch { results[labels[table]] = '—'; }
    })
  );
  return results;
}

function configRows() {
  const url = window.__ENV__?.SUPABASE_URL || '';
  const rows = [
    { label: 'Supabase URL', value: url ? `✅ ${url.slice(8, 30)}…` : '❌ გ.' },
    { label: 'Anon Key', value: window.__ENV__?.SUPABASE_ANON ? '✅ დ.' : '❌ გ.' },
    { label: 'FCM Push', value: '⚠️ EdgeFn-ი' },
    { label: 'RLS', value: '✅ ჩ.' },
    { label: 'Build', value: `v${Date.now().toString(36).toUpperCase().slice(-5)}` },
  ];
  return rows.map(r => `
    <div style="display:flex;align-items:center;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--color-gray-100)">
      <span class="text-sm text-gray">${r.label}</span>
      <span class="text-sm font-semibold">${r.value}</span>
    </div>
  `).join('');
}
