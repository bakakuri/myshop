// MYSHOP Admin — Promotions & Discounts Page
import { formatDate, formatPrice, confirmDialog, createModal, closeModal } from '../js/utils.js';

export async function render(container, { supabase }) {
  container.innerHTML = renderShell();
  await Promise.all([loadBanners(supabase), loadCoupons(supabase)]);
  bindEvents(supabase);
}

function renderShell() {
  return `
    <div class="page-header">
      <h1 class="page-title">აქციები & ფასდაკლება</h1>
    </div>

    <div class="tabs" id="promoTabs">
      <button class="tab-btn active" data-tab="banners">🖼 ბანერები</button>
      <button class="tab-btn" data-tab="coupons">🏷 კუპონები</button>
      <button class="tab-btn" data-tab="flash">⚡ ფლეშ-გაყიდვა</button>
    </div>

    <!-- Banners Tab -->
    <div id="tab-banners">
      <div class="page-header" style="margin-bottom:16px">
        <p class="page-subtitle">სლაიდ-შოუ ბანერები მთავარ გვ-ზე</p>
        <button class="btn btn-primary" id="addBannerBtn">+ ბანერი</button>
      </div>
      <div id="bannersList"><div class="skeleton" style="height:200px;border-radius:14px"></div></div>
    </div>

    <!-- Coupons Tab -->
    <div id="tab-coupons" style="display:none">
      <div class="page-header" style="margin-bottom:16px">
        <p class="page-subtitle">ფასდაკლების კოდები</p>
        <button class="btn btn-primary" id="addCouponBtn">+ კოდი</button>
      </div>
      <div id="couponsList"><div class="skeleton" style="height:200px;border-radius:14px"></div></div>
    </div>

    <!-- Flash Sale Tab -->
    <div id="tab-flash" style="display:none">
      <div class="page-header" style="margin-bottom:16px">
        <p class="page-subtitle">დროებითი სპეც-შეთავაზებები</p>
        <button class="btn btn-primary" id="addFlashBtn">+ ფლეშ-გაყ.</button>
      </div>
      <div id="flashList"><div class="skeleton" style="height:200px;border-radius:14px"></div></div>
    </div>
  `;
}

function bindEvents(supabase) {
  // Tab switching
  document.querySelectorAll('.tab-btn[data-tab]').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      ['banners','coupons','flash'].forEach(t => {
        document.getElementById(`tab-${t}`).style.display = t === btn.dataset.tab ? '' : 'none';
      });
    });
  });

  document.getElementById('addBannerBtn')?.addEventListener('click', () => openBannerForm(supabase, null));
  document.getElementById('addCouponBtn')?.addEventListener('click', () => openCouponForm(supabase, null));
  document.getElementById('addFlashBtn')?.addEventListener('click', () => openFlashForm(supabase, null));
}

// ─── BANNERS ─────────────────────────────────────────────────────────────────
async function loadBanners(supabase) {
  const { data: banners } = await supabase
    .from('banners')
    .select('*')
    .order('sort_order', { ascending: true });

  const list = document.getElementById('bannersList');
  if (!banners?.length) {
    list.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">🖼</div><div class="empty-state-desc">ბანერი ჯერ არ არის</div></div>`;
    return;
  }

  list.innerHTML = `
    <div style="display:grid;gap:16px">
      ${banners.map(b => `
        <div class="card" style="overflow:visible">
          <div class="card-body" style="display:grid;grid-template-columns:200px 1fr;gap:16px;align-items:center">
            <div style="height:100px;background:var(--color-gray-100);border-radius:10px;overflow:hidden">
              ${b.image_url ? `<img src="${b.image_url}" style="width:100%;height:100%;object-fit:cover" alt="">` : `<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--color-gray-400);font-size:30px">🖼</div>`}
            </div>
            <div>
              <div class="font-semibold">${b.title || 'უსათაურო'}</div>
              <div class="text-sm text-gray mt-2">${b.subtitle || ''}</div>
              ${b.action_url ? `<div class="text-xs text-gray mt-1">🔗 ${b.action_url}</div>` : ''}
              <div class="flex items-center gap-3 mt-3">
                <span class="badge ${b.is_active ? 'badge-active' : 'badge-draft'}">${b.is_active ? 'ჩართ.' : 'გამ.'}</span>
                <span class="text-xs text-gray">რ.ნ. ${b.sort_order ?? 0}</span>
                <button class="btn btn-sm btn-secondary btn-edit-banner" data-id="${b.id}">✏️ რედ.</button>
                <button class="btn btn-sm btn-danger btn-del-banner" data-id="${b.id}" data-title="${b.title}">🗑</button>
              </div>
            </div>
          </div>
        </div>
      `).join('')}
    </div>
  `;

  list.querySelectorAll('.btn-edit-banner').forEach(btn => btn.addEventListener('click', () => openBannerForm(supabase, btn.dataset.id)));
  list.querySelectorAll('.btn-del-banner').forEach(btn => btn.addEventListener('click', async () => {
    if (await confirmDialog(`„${btn.dataset.title}" ბანერი წაიშლება.`)) {
      await supabase.from('banners').delete().eq('id', btn.dataset.id);
      window.__APP__.toast.success('ბანერი წაიშალა'); loadBanners(supabase);
    }
  }));
}

async function openBannerForm(supabase, bannerId) {
  let banner = null;
  if (bannerId) {
    const { data } = await supabase.from('banners').select('*').eq('id', bannerId).single();
    banner = data;
  }

  createModal({
    id: 'bannerModal',
    title: banner ? 'ბანერის რედ.' : 'ახ. ბანერი',
    body: `
      <form id="bannerForm">
        <div class="form-group"><label class="form-label required">სათაური</label>
          <input class="form-control" name="title" value="${banner?.title || ''}" required></div>
        <div class="form-group"><label class="form-label">ქვე-სათაური</label>
          <input class="form-control" name="subtitle" value="${banner?.subtitle || ''}"></div>
        <div class="form-group"><label class="form-label required">სურათის URL</label>
          <input class="form-control" name="image_url" value="${banner?.image_url || ''}" required placeholder="https://…"></div>
        <div class="form-row cols-2">
          <div class="form-group"><label class="form-label">ბმული</label>
            <input class="form-control" name="action_url" value="${banner?.action_url || ''}" placeholder="https://…"></div>
          <div class="form-group"><label class="form-label">ბმ. ტექსტი</label>
            <input class="form-control" name="action_label" value="${banner?.action_label || ''}"></div>
        </div>
        <div class="form-row cols-2">
          <div class="form-group"><label class="form-label">რ.ნ.</label>
            <input class="form-control" name="sort_order" type="number" value="${banner?.sort_order ?? 0}"></div>
          <div style="display:flex;align-items:flex-end;padding-bottom:4px">
            <label class="toggle-wrap">
              <div class="toggle"><input type="checkbox" name="is_active" ${banner?.is_active !== false ? 'checked' : ''}>
                <div class="toggle-track"></div><div class="toggle-thumb"></div></div>
              <span class="text-sm">ჩართული</span>
            </label>
          </div>
        </div>
      </form>
    `,
    footer: `<button class="btn btn-secondary modal-close">გაუქ.</button><button class="btn btn-primary" id="saveBannerBtn">${banner ? 'შენახ.' : 'შექმ.'}</button>`,
  });

  document.getElementById('saveBannerBtn').addEventListener('click', async () => {
    const form = document.getElementById('bannerForm');
    const fd = new FormData(form);
    const data = { title: fd.get('title'), subtitle: fd.get('subtitle')||null, image_url: fd.get('image_url'), action_url: fd.get('action_url')||null, action_label: fd.get('action_label')||null, sort_order: parseInt(fd.get('sort_order'))||0, is_active: form.querySelector('[name="is_active"]').checked };
    if (!data.title || !data.image_url) { window.__APP__.toast.error('სათ. & სურ. სავ.'); return; }
    const btn = document.getElementById('saveBannerBtn'); btn.disabled = true;
    const { error } = banner ? await supabase.from('banners').update(data).eq('id', banner.id) : await supabase.from('banners').insert(data);
    if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; }
    else { window.__APP__.toast.success('შენახ.'); closeModal('bannerModal'); loadBanners(supabase); }
  });
}

// ─── COUPONS ──────────────────────────────────────────────────────────────────
async function loadCoupons(supabase) {
  const { data: coupons } = await supabase.from('coupons').select('*').order('created_at', { ascending: false });
  const list = document.getElementById('couponsList');

  if (!coupons?.length) {
    list.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">🏷</div><div class="empty-state-desc">კუპ. ჯ. არ</div></div>`;
    return;
  }

  list.innerHTML = `
    <div class="table-container">
      <table class="data-table">
        <thead><tr><th>კოდი</th><th>ტ.</th><th>ს.</th><th>მინ. შ.</th><th>გამ. ად.</th><th>ვად.</th><th>სტ.</th><th class="col-actions">მ.</th></tr></thead>
        <tbody>
          ${coupons.map(c => `
            <tr>
              <td><span class="chip">${c.code}</span></td>
              <td class="text-sm">${c.discount_type === 'percentage' ? '%' : '₾'}</td>
              <td class="font-semibold">${c.discount_type === 'percentage' ? c.discount_value + '%' : formatPrice(c.discount_value)}</td>
              <td class="text-sm text-gray">${c.min_order_amount ? formatPrice(c.min_order_amount) : '—'}</td>
              <td class="text-sm text-gray">${c.usage_count ?? 0}/${c.usage_limit ?? '∞'}</td>
              <td class="text-xs text-gray">${c.expires_at ? formatDate(c.expires_at) : '—'}</td>
              <td><span class="badge ${c.is_active ? 'badge-active' : 'badge-draft'}">${c.is_active ? 'ჩ.' : 'გ.'}</span></td>
              <td class="col-actions">
                <div class="flex gap-1 justify-end">
                  <button class="btn btn-sm btn-secondary btn-edit-coupon" data-id="${c.id}">✏️</button>
                  <button class="btn btn-sm btn-danger btn-del-coupon" data-id="${c.id}" data-code="${c.code}">🗑</button>
                </div>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;

  list.querySelectorAll('.btn-edit-coupon').forEach(btn => btn.addEventListener('click', () => openCouponForm(supabase, btn.dataset.id)));
  list.querySelectorAll('.btn-del-coupon').forEach(btn => btn.addEventListener('click', async () => {
    if (await confirmDialog(`„${btn.dataset.code}" წაიშლება.`)) {
      await supabase.from('coupons').delete().eq('id', btn.dataset.id);
      window.__APP__.toast.success('კუპ. წ.'); loadCoupons(supabase);
    }
  }));
}

async function openCouponForm(supabase, couponId) {
  let c = null;
  if (couponId) { const { data } = await supabase.from('coupons').select('*').eq('id', couponId).single(); c = data; }

  createModal({
    id: 'couponModal', title: c ? 'კუპ. რედ.' : 'ახ. კუპ.',
    body: `
      <form id="couponForm">
        <div class="form-row cols-2">
          <div class="form-group"><label class="form-label required">კოდი</label>
            <input class="form-control" name="code" value="${c?.code || ''}" required placeholder="SAVE20" style="text-transform:uppercase"></div>
          <div class="form-group"><label class="form-label required">ს. ტ.</label>
            <select class="form-control" name="discount_type">
              <option value="percentage" ${c?.discount_type === 'percentage' ? 'selected' : ''}>პროცენტი (%)</option>
              <option value="fixed" ${c?.discount_type === 'fixed' ? 'selected' : ''}>ფიქსირ. (₾)</option>
            </select></div>
        </div>
        <div class="form-row cols-2">
          <div class="form-group"><label class="form-label required">ს. ოდ.</label>
            <input class="form-control" name="discount_value" type="number" step="0.01" value="${c?.discount_value || ''}" required></div>
          <div class="form-group"><label class="form-label">მინ. შ.</label>
            <input class="form-control" name="min_order_amount" type="number" step="0.01" value="${c?.min_order_amount || ''}"></div>
        </div>
        <div class="form-row cols-2">
          <div class="form-group"><label class="form-label">გამ. ლიმ.</label>
            <input class="form-control" name="usage_limit" type="number" value="${c?.usage_limit || ''}"></div>
          <div class="form-group"><label class="form-label">ვადა</label>
            <input class="form-control" name="expires_at" type="date" value="${c?.expires_at?.slice(0,10) || ''}"></div>
        </div>
        <label class="toggle-wrap mt-2">
          <div class="toggle"><input type="checkbox" name="is_active" ${c?.is_active !== false ? 'checked' : ''}>
            <div class="toggle-track"></div><div class="toggle-thumb"></div></div>
          <span class="text-sm">ჩართული</span>
        </label>
      </form>
    `,
    footer: `<button class="btn btn-secondary modal-close">გ.</button><button class="btn btn-primary" id="saveCouponBtn">${c ? 'შ.' : 'შ.'}</button>`,
  });

  document.getElementById('saveCouponBtn').addEventListener('click', async () => {
    const form = document.getElementById('couponForm');
    const fd = new FormData(form);
    const data = { code: fd.get('code').toUpperCase(), discount_type: fd.get('discount_type'), discount_value: parseFloat(fd.get('discount_value')), min_order_amount: fd.get('min_order_amount') ? parseFloat(fd.get('min_order_amount')) : null, usage_limit: fd.get('usage_limit') ? parseInt(fd.get('usage_limit')) : null, expires_at: fd.get('expires_at') || null, is_active: form.querySelector('[name="is_active"]').checked };
    if (!data.code || !data.discount_value) { window.__APP__.toast.error('კოდი & სიდ. სავ.'); return; }
    const btn = document.getElementById('saveCouponBtn'); btn.disabled = true;
    const { error } = c ? await supabase.from('coupons').update(data).eq('id', c.id) : await supabase.from('coupons').insert(data);
    if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; }
    else { window.__APP__.toast.success('შ.'); closeModal('couponModal'); loadCoupons(supabase); }
  });
}

// ─── FLASH SALES ──────────────────────────────────────────────────────────────
async function loadFlashSales(supabase) {
  const { data: sales } = await supabase.from('flash_sales').select(`*, products:product_id(name)`).order('starts_at', { ascending: false });
  const list = document.getElementById('flashList');
  if (!sales?.length) { list.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">⚡</div><div class="empty-state-desc">ფლ.-გ. ჯ. არ</div></div>`; return; }

  list.innerHTML = `<div class="table-container"><table class="data-table"><thead><tr><th>პ.</th><th>ს.</th><th>იწყ.</th><th>მთ.</th><th>სტ.</th><th class="col-actions">მ.</th></tr></thead><tbody>
    ${sales.map(s => `<tr>
      <td class="font-semibold text-sm">${s.products?.name || '—'}</td>
      <td class="font-semibold">${s.discount_percentage}%</td>
      <td class="text-xs text-gray">${formatDate(s.starts_at)}</td>
      <td class="text-xs text-gray">${formatDate(s.ends_at)}</td>
      <td><span class="badge ${s.is_active ? 'badge-active' : 'badge-draft'}">${s.is_active ? 'ჩ.' : 'გ.'}</span></td>
      <td class="col-actions"><div class="flex gap-1 justify-end">
        <button class="btn btn-sm btn-danger btn-del-flash" data-id="${s.id}">🗑</button>
      </div></td>
    </tr>`).join('')}
  </tbody></table></div>`;

  list.querySelectorAll('.btn-del-flash').forEach(btn => btn.addEventListener('click', async () => {
    if (await confirmDialog('ფლ.-გ. წ.')) { await supabase.from('flash_sales').delete().eq('id', btn.dataset.id); loadFlashSales(supabase); }
  }));
}

async function openFlashForm(supabase, id) {
  const { data: products } = await supabase.from('products').select('id, name').eq('status','active').order('name');
  createModal({
    id: 'flashModal', title: 'ახ. ფლეშ-გ.',
    body: `<form id="flashForm">
      <div class="form-group"><label class="form-label required">პ.</label>
        <select class="form-control" name="product_id" required>${(products||[]).map(p => `<option value="${p.id}">${p.name}</option>`).join('')}</select></div>
      <div class="form-group"><label class="form-label required">ს.% (1-99)</label>
        <input class="form-control" name="discount_percentage" type="number" min="1" max="99" required></div>
      <div class="form-row cols-2">
        <div class="form-group"><label class="form-label required">დ. დ.</label><input class="form-control" name="starts_at" type="datetime-local" required></div>
        <div class="form-group"><label class="form-label required">დ. ბ.</label><input class="form-control" name="ends_at" type="datetime-local" required></div>
      </div>
    </form>`,
    footer: `<button class="btn btn-secondary modal-close">გ.</button><button class="btn btn-primary" id="saveFlashBtn">შ.</button>`,
  });

  document.getElementById('saveFlashBtn').addEventListener('click', async () => {
    const form = document.getElementById('flashForm'); const fd = new FormData(form);
    const data = { product_id: fd.get('product_id'), discount_percentage: parseInt(fd.get('discount_percentage')), starts_at: new Date(fd.get('starts_at')).toISOString(), ends_at: new Date(fd.get('ends_at')).toISOString(), is_active: true };
    const btn = document.getElementById('saveFlashBtn'); btn.disabled = true;
    const { error } = await supabase.from('flash_sales').insert(data);
    if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; }
    else { window.__APP__.toast.success('შ.'); closeModal('flashModal'); loadFlashSales(supabase); }
  });
}
