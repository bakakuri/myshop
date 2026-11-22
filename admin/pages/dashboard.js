// MYSHOP Admin — Dashboard Page
import { formatPrice, formatRelative, orderStatusBadge } from '../js/utils.js';

export async function render(container, { supabase }) {
  container.innerHTML = renderShell();

  try {
    await Promise.all([
      loadStats(supabase),
      loadRecentOrders(supabase),
      loadRecentChats(supabase),
      loadSalesChart(supabase),
    ]);
  } catch (err) {
    console.error('Dashboard error:', err);
  }
}

function renderShell() {
  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">დეშბორდი</h1>
        <p class="page-subtitle" id="dashDate"></p>
      </div>
      <button class="btn btn-secondary" onclick="window.__APP__.router?.navigate('orders')">
        📦 ყველა შეკვეთა
      </button>
    </div>

    <!-- Stats -->
    <div class="stats-grid" id="statsGrid">
      ${[1,2,3,4].map(() => `<div class="skeleton" style="height:120px;border-radius:14px"></div>`).join('')}
    </div>

    <!-- Charts + Recent -->
    <div style="display:grid;grid-template-columns:1fr 380px;gap:24px;margin-top:24px" id="dashGrid">
      <!-- Sales chart placeholder -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">გაყიდვები — ბოლო 7 დღე</span>
        </div>
        <div class="card-body" id="salesChartWrap" style="min-height:200px">
          <div class="skeleton" style="height:180px;border-radius:8px"></div>
        </div>
      </div>

      <!-- Order status breakdown -->
      <div class="card">
        <div class="card-header"><span class="card-title">შეკვეთების სტატუსი</span></div>
        <div class="card-body" id="statusBreakdown">
          <div class="skeleton" style="height:180px;border-radius:8px"></div>
        </div>
      </div>
    </div>

    <!-- Recent orders & chats -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:24px;margin-top:24px">
      <div class="card" id="recentOrdersCard">
        <div class="card-header">
          <span class="card-title">ბოლო შეკვეთები</span>
          <button class="btn btn-ghost btn-sm" onclick="window.__APP__.router?.navigate('orders')">ყველა →</button>
        </div>
        <div id="recentOrdersList"><div class="skeleton" style="height:200px;margin:16px"></div></div>
      </div>
      <div class="card" id="recentChatsCard">
        <div class="card-header">
          <span class="card-title">ღია საუბრები</span>
          <button class="btn btn-ghost btn-sm" onclick="window.__APP__.router?.navigate('conversations')">ყველა →</button>
        </div>
        <div id="recentChatsList"><div class="skeleton" style="height:200px;margin:16px"></div></div>
      </div>
    </div>
  `;
}

async function loadStats(supabase) {
  // Parallel stat queries
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const todayISO = today.toISOString();

  const monthStart = new Date(today.getFullYear(), today.getMonth(), 1).toISOString();

  const [
    ordersTotal,
    ordersToday,
    ordersMonth,
    customers,
    pendingOrders,
    openChats,
  ] = await Promise.all([
    supabase.from('orders').select('total', { count: 'exact' }),
    supabase.from('orders').select('id', { count: 'exact' }).gte('created_at', todayISO),
    supabase.from('orders').select('total').gte('created_at', monthStart),
    supabase.from('profiles').select('id', { count: 'exact' }).eq('role', 'customer'),
    supabase.from('orders').select('id', { count: 'exact' }).eq('status', 'pending'),
    supabase.from('conversations').select('id', { count: 'exact' }).eq('status', 'open'),
  ]);

  const monthRevenue = (ordersMonth.data || []).reduce((s, o) => s + (o.total || 0), 0);
  const totalRevenue = (ordersTotal.data || []).reduce((s, o) => s + (o.total || 0), 0);

  document.getElementById('statsGrid').innerHTML = `
    <div class="stat-card">
      <div class="stat-card-icon purple">💰</div>
      <div class="stat-card-value">${formatPrice(monthRevenue)}</div>
      <div class="stat-card-label">თვის შემოსავალი</div>
      <div class="stat-card-change up">↑ ამ თვე</div>
    </div>
    <div class="stat-card">
      <div class="stat-card-icon gold">📦</div>
      <div class="stat-card-value">${ordersToday.count ?? 0}</div>
      <div class="stat-card-label">დღის შეკვეთები</div>
      <div class="stat-card-change" style="color:var(--color-gold)">სულ: ${ordersTotal.count ?? 0}</div>
    </div>
    <div class="stat-card">
      <div class="stat-card-icon green">👥</div>
      <div class="stat-card-value">${customers.count ?? 0}</div>
      <div class="stat-card-label">დარეგისტრირებული</div>
      <div class="stat-card-change up">↑ მომხმარებლები</div>
    </div>
    <div class="stat-card">
      <div class="stat-card-icon red">⏳</div>
      <div class="stat-card-value">${pendingOrders.count ?? 0}</div>
      <div class="stat-card-label">მომლოდინე შეკვ.</div>
      <div class="stat-card-change" style="color:var(--color-warning)">💬 ღია ჩათი: ${openChats.count ?? 0}</div>
    </div>
  `;
}

async function loadRecentOrders(supabase) {
  const { data: orders } = await supabase
    .from('orders')
    .select(`id, order_number, status, total, created_at,
             profiles:customer_id(full_name, email)`)
    .order('created_at', { ascending: false })
    .limit(6);

  const list = document.getElementById('recentOrdersList');
  if (!orders?.length) {
    list.innerHTML = `<div class="empty-state" style="padding:40px 16px"><div class="empty-state-icon">📦</div><div class="empty-state-desc">შეკვეთები ჯერ არ არის</div></div>`;
    return;
  }

  list.innerHTML = orders.map(o => `
    <div class="flex items-center gap-3" style="padding:12px 20px;border-bottom:1px solid var(--color-gray-100);cursor:pointer"
         onclick="window.__APP__.router?.navigate('orders');window.pendingOrderId='${o.id}'">
      <div>
        <div class="font-semibold text-sm">#${o.order_number}</div>
        <div class="text-xs text-gray">${o.profiles?.full_name || o.profiles?.email || 'უცნობი'}</div>
      </div>
      <div style="margin-left:auto;text-align:right">
        ${orderStatusBadge(o.status)}
        <div class="text-xs text-gray mt-2">${formatRelative(o.created_at)}</div>
      </div>
      <div class="font-semibold text-sm" style="min-width:80px;text-align:right">${formatPrice(o.total)}</div>
    </div>
  `).join('');
}

async function loadRecentChats(supabase) {
  const { data: chats } = await supabase
    .from('conversations')
    .select(`id, status, created_at,
             profiles:customer_id(full_name, email),
             orders:order_id(order_number)`)
    .eq('status', 'open')
    .order('created_at', { ascending: false })
    .limit(6);

  const list = document.getElementById('recentChatsList');
  if (!chats?.length) {
    list.innerHTML = `<div class="empty-state" style="padding:40px 16px"><div class="empty-state-icon">💬</div><div class="empty-state-desc">ღია საუბარი არ არის</div></div>`;
    return;
  }

  list.innerHTML = chats.map(c => `
    <div class="flex items-center gap-3" style="padding:12px 20px;border-bottom:1px solid var(--color-gray-100);cursor:pointer"
         onclick="window.__APP__.router?.navigate('conversations')">
      <div style="width:36px;height:36px;background:var(--color-primary-100);border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:700;color:var(--color-primary);flex-shrink:0">
        ${(c.profiles?.full_name || c.profiles?.email || '?')[0].toUpperCase()}
      </div>
      <div>
        <div class="font-semibold text-sm">${c.profiles?.full_name || c.profiles?.email || 'უცნობი'}</div>
        <div class="text-xs text-gray">${c.orders?.order_number ? `#${c.orders.order_number}` : 'შეკვეთის გარეშე'}</div>
      </div>
      <div style="margin-left:auto;text-align:right">
        <span class="badge badge-active">ღია</span>
        <div class="text-xs text-gray mt-2">${formatRelative(c.created_at)}</div>
      </div>
    </div>
  `).join('');
}

async function loadSalesChart(supabase) {
  // Build 7-day sales data
  const days = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() - i);
    days.push({ date: d, label: formatDayLabel(d), revenue: 0 });
  }

  const from = days[0].date.toISOString();
  const { data: orders } = await supabase
    .from('orders')
    .select('total, created_at')
    .gte('created_at', from)
    .not('status', 'eq', 'cancelled');

  (orders || []).forEach(o => {
    const d = new Date(o.created_at);
    d.setHours(0, 0, 0, 0);
    const slot = days.find(day => day.date.getTime() === d.getTime());
    if (slot) slot.revenue += o.total || 0;
  });

  const max = Math.max(...days.map(d => d.revenue), 1);

  // Simple SVG bar chart
  const W = 560, H = 180, PAD = 40, BPAD = 30;
  const barW = Math.floor((W - PAD * 2) / days.length * 0.5);
  const gap  = Math.floor((W - PAD * 2) / days.length);
  const chartH = H - BPAD;

  const bars = days.map((d, i) => {
    const h = Math.round((d.revenue / max) * (chartH - 20));
    const x = PAD + i * gap + gap / 2 - barW / 2;
    const y = chartH - h;
    return `
      <rect x="${x}" y="${y}" width="${barW}" height="${h}" rx="4"
            fill="var(--color-primary-300)" opacity="0.9">
        <title>${formatPrice(d.revenue)}</title>
      </rect>
      <text x="${x + barW/2}" y="${chartH + 20}" text-anchor="middle"
            font-size="11" fill="var(--color-gray-500)">${d.label}</text>
      ${h > 20 ? `<text x="${x + barW/2}" y="${y - 5}" text-anchor="middle"
            font-size="10" fill="var(--color-primary)" font-weight="600">${d.revenue > 0 ? formatPrice(d.revenue).replace('₾','') + '₾' : ''}</text>` : ''}
    `;
  }).join('');

  document.getElementById('salesChartWrap').innerHTML = `
    <svg viewBox="0 0 ${W} ${H+10}" width="100%" style="overflow:visible">
      <line x1="${PAD}" y1="10" x2="${PAD}" y2="${chartH}" stroke="var(--color-gray-200)" stroke-width="1"/>
      <line x1="${PAD}" y1="${chartH}" x2="${W - PAD}" y2="${chartH}" stroke="var(--color-gray-200)" stroke-width="1"/>
      ${bars}
    </svg>
  `;

  // Status breakdown
  const { data: statuses } = await supabase
    .from('orders')
    .select('status');

  const counts = {};
  (statuses || []).forEach(o => { counts[o.status] = (counts[o.status] || 0) + 1; });
  const statusColors = { pending:'#F59E0B', confirmed:'#059669', processing:'#2563EB', shipped:'#5B21B6', delivered:'#065F46', cancelled:'#DC2626' };
  const statusLabels = { pending:'მომლოდინე', confirmed:'დადასტ.', processing:'მუშავდება', shipped:'გაგზ.', delivered:'ჩაბარ.', cancelled:'გაუქ.' };
  const total = Object.values(counts).reduce((s, c) => s + c, 0) || 1;

  document.getElementById('statusBreakdown').innerHTML = Object.entries(counts).map(([status, count]) => `
    <div style="margin-bottom:14px">
      <div class="flex items-center justify-between text-sm mb-1">
        <span style="color:${statusColors[status] || '#6B7280'};font-weight:600">${statusLabels[status] || status}</span>
        <span class="font-semibold">${count} (${Math.round(count/total*100)}%)</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" style="width:${Math.round(count/total*100)}%;background:${statusColors[status] || 'var(--color-primary)'}"></div>
      </div>
    </div>
  `).join('') || '<p class="text-sm text-gray">მონაცემები არ არის</p>';
}

function formatDayLabel(d) {
  const days = ['კვ','ორ','სამ','ოთ','ხუ','პარ','შბ'];
  return days[d.getDay()];
}
