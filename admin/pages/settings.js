// MYSHOP Admin — Store Settings (owner/admin)
import { AppState } from '../js/app.js';

export async function render(container, { supabase }) {
  container.innerHTML = `
    <div class="page-header">
      <h1 class="page-title">⚙️ პარ.</h1>
    </div>
    <div class="tabs" id="settingsTabs">
      <button class="tab-btn active" data-tab="store">🏪 მ.-ი</button>
      <button class="tab-btn" data-tab="delivery">🚚 მ.</button>
      <button class="tab-btn" data-tab="contact">📞 კ.</button>
      ${AppState.userRole === 'owner' ? '<button class="tab-btn" data-tab="danger">⚠️ ო.პ.</button>' : ''}
    </div>
    <div id="tab-store"></div>
    <div id="tab-delivery" style="display:none"></div>
    <div id="tab-contact" style="display:none"></div>
    ${AppState.userRole === 'owner' ? '<div id="tab-danger" style="display:none"></div>' : ''}
  `;

  document.querySelectorAll('.tab-btn[data-tab]').forEach(btn => {
    btn.addEventListener('click', async () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      ['store','delivery','contact','danger'].forEach(t => {
        const el = document.getElementById(`tab-${t}`);
        if (el) el.style.display = t === btn.dataset.tab ? '' : 'none';
      });
      if (btn.dataset.tab === 'delivery') await loadDeliverySettings(supabase);
      if (btn.dataset.tab === 'contact') await loadContactSettings(supabase);
      if (btn.dataset.tab === 'danger') loadDangerZone(supabase);
    });
  });

  await loadStoreSettings(supabase);
}

async function getSettings(supabase, keys) {
  const { data } = await supabase.from('app_settings').select('key, value').in('key', keys);
  return Object.fromEntries((data || []).map(r => [r.key, r.value]));
}

async function saveSetting(supabase, key, value) {
  return supabase.from('app_settings').upsert({ key, value }, { onConflict: 'key' });
}

// ─── STORE SETTINGS ───────────────────────────────────────────────────────────
async function loadStoreSettings(supabase) {
  const s = await getSettings(supabase, ['store_name','store_tagline','store_logo_url','store_currency','store_timezone','store_language','store_meta_description']);
  const el = document.getElementById('tab-store');
  el.innerHTML = `
    <div class="card">
      <div class="card-header"><h3>🏪 მ. ი.</h3></div>
      <div class="card-body">
        <form id="storeForm">
          <div class="form-group"><label class="form-label required">სახ.</label>
            <input class="form-control" name="store_name" value="${s.store_name || 'MYSHOP'}" required></div>
          <div class="form-group"><label class="form-label">სლ.</label>
            <input class="form-control" name="store_tagline" value="${s.store_tagline || ''}"></div>
          <div class="form-group"><label class="form-label">ლოგ. URL</label>
            <input class="form-control" name="store_logo_url" value="${s.store_logo_url || ''}" placeholder="https://…"></div>
          <div class="form-group"><label class="form-label">მ. ა.</label>
            <textarea class="form-control" name="store_meta_description" rows="2">${s.store_meta_description || ''}</textarea></div>
          <div class="form-row cols-3">
            <div class="form-group"><label class="form-label">ვ.</label>
              <select class="form-control" name="store_currency">
                <option value="GEL" ${s.store_currency === 'GEL' ? 'selected' : ''}>GEL (₾)</option>
                <option value="USD" ${s.store_currency === 'USD' ? 'selected' : ''}>USD ($)</option>
              </select></div>
            <div class="form-group"><label class="form-label">ე.ს.</label>
              <select class="form-control" name="store_timezone">
                <option value="Asia/Tbilisi" ${s.store_timezone === 'Asia/Tbilisi' ? 'selected' : ''}>Asia/Tbilisi</option>
                <option value="Europe/London" ${s.store_timezone === 'Europe/London' ? 'selected' : ''}>Europe/London</option>
              </select></div>
            <div class="form-group"><label class="form-label">ე.</label>
              <select class="form-control" name="store_language">
                <option value="ka" ${s.store_language === 'ka' ? 'selected' : ''}>ქართ. (ka)</option>
                <option value="en" ${s.store_language === 'en' ? 'selected' : ''}>English (en)</option>
              </select></div>
          </div>
        </form>
      </div>
      <div class="card-footer"><button class="btn btn-primary" id="saveStoreBtn">შ.</button></div>
    </div>
  `;
  document.getElementById('saveStoreBtn').addEventListener('click', async () => {
    const form = document.getElementById('storeForm'); const fd = new FormData(form);
    const keys = ['store_name','store_tagline','store_logo_url','store_meta_description','store_currency','store_timezone','store_language'];
    const btn = document.getElementById('saveStoreBtn'); btn.disabled = true;
    await Promise.all(keys.map(k => saveSetting(supabase, k, fd.get(k))));
    window.__APP__.toast.success('შ.'); btn.disabled = false;
  });
}

// ─── DELIVERY SETTINGS ────────────────────────────────────────────────────────
async function loadDeliverySettings(supabase) {
  const s = await getSettings(supabase, ['delivery_zones','delivery_free_threshold','delivery_processing_days','delivery_message']);
  const el = document.getElementById('tab-delivery');
  const zones = s.delivery_zones || [{ name: 'თბ.', price: 5 }];

  el.innerHTML = `
    <div class="card">
      <div class="card-header"><h3>🚚 მ. პ.</h3></div>
      <div class="card-body">
        <form id="deliveryForm">
          <div class="form-group"><label class="form-label">უფ. ს. ლ. (₾)</label>
            <input class="form-control" name="free_threshold" type="number" step="0.01" value="${s.delivery_free_threshold || ''}" placeholder="0 = ყ. გ."></div>
          <div class="form-group"><label class="form-label">დ. დ.</label>
            <input class="form-control" name="processing_days" type="number" value="${s.delivery_processing_days || 1}" min="0"></div>
          <div class="form-group"><label class="form-label">მ. შ.</label>
            <textarea class="form-control" name="delivery_message" rows="2" placeholder="მ. 1-3 სამ. დ.">${s.delivery_message || ''}</textarea></div>

          <div class="form-group">
            <label class="form-label">ზ.</label>
            <div id="zonesContainer">
              ${zones.map((z, i) => zoneRow(i, z)).join('')}
            </div>
            <button type="button" class="btn btn-sm btn-secondary mt-2" id="addZoneBtn">+ ზ.</button>
          </div>
        </form>
      </div>
      <div class="card-footer"><button class="btn btn-primary" id="saveDeliveryBtn">შ.</button></div>
    </div>
  `;

  let zoneCount = zones.length;
  document.getElementById('addZoneBtn').addEventListener('click', () => {
    const c = document.getElementById('zonesContainer');
    const div = document.createElement('div');
    div.innerHTML = zoneRow(zoneCount++, { name:'', price: 0 });
    c.append(div.firstElementChild);
    c.querySelectorAll('.btn-del-zone').forEach(b => b.onclick = () => b.closest('.zone-row').remove());
  });
  el.querySelectorAll('.btn-del-zone').forEach(b => b.onclick = () => b.closest('.zone-row').remove());

  document.getElementById('saveDeliveryBtn').addEventListener('click', async () => {
    const form = document.getElementById('deliveryForm'); const fd = new FormData(form);
    const names = fd.getAll('zone_name'); const prices = fd.getAll('zone_price');
    const zonesData = names.map((n, i) => ({ name: n, price: parseFloat(prices[i]) || 0 })).filter(z => z.name);
    const btn = document.getElementById('saveDeliveryBtn'); btn.disabled = true;
    await Promise.all([
      saveSetting(supabase, 'delivery_zones', zonesData),
      saveSetting(supabase, 'delivery_free_threshold', parseFloat(fd.get('free_threshold'))||0),
      saveSetting(supabase, 'delivery_processing_days', parseInt(fd.get('processing_days'))||1),
      saveSetting(supabase, 'delivery_message', fd.get('delivery_message')),
    ]);
    window.__APP__.toast.success('შ.'); btn.disabled = false;
  });
}

function zoneRow(i, z) {
  return `<div class="zone-row flex gap-2 mb-2 items-center">
    <input class="form-control" name="zone_name" value="${z.name}" placeholder="ზ. სახ." style="flex:1">
    <input class="form-control" name="zone_price" type="number" step="0.01" value="${z.price}" placeholder="₾" style="width:80px">
    <button type="button" class="btn btn-sm btn-danger btn-del-zone">🗑</button>
  </div>`;
}

// ─── CONTACT SETTINGS ─────────────────────────────────────────────────────────
async function loadContactSettings(supabase) {
  const s = await getSettings(supabase, ['contact_phone','contact_email','contact_address','social_instagram','social_facebook','social_tiktok']);
  const el = document.getElementById('tab-contact');
  el.innerHTML = `
    <div class="card">
      <div class="card-header"><h3>📞 კ. ი.</h3></div>
      <div class="card-body">
        <form id="contactForm">
          <div class="form-row cols-2">
            <div class="form-group"><label class="form-label">ტ.</label><input class="form-control" name="contact_phone" value="${s.contact_phone || ''}"></div>
            <div class="form-group"><label class="form-label">ე-მ.</label><input class="form-control" name="contact_email" value="${s.contact_email || ''}"></div>
          </div>
          <div class="form-group"><label class="form-label">მ.</label><input class="form-control" name="contact_address" value="${s.contact_address || ''}"></div>
          <div class="form-group"><label class="form-label">Instagram</label><input class="form-control" name="social_instagram" value="${s.social_instagram || ''}" placeholder="@username"></div>
          <div class="form-group"><label class="form-label">Facebook</label><input class="form-control" name="social_facebook" value="${s.social_facebook || ''}" placeholder="https://fb.com/…"></div>
          <div class="form-group"><label class="form-label">TikTok</label><input class="form-control" name="social_tiktok" value="${s.social_tiktok || ''}" placeholder="@username"></div>
        </form>
      </div>
      <div class="card-footer"><button class="btn btn-primary" id="saveContactBtn">შ.</button></div>
    </div>
  `;
  document.getElementById('saveContactBtn').addEventListener('click', async () => {
    const form = document.getElementById('contactForm'); const fd = new FormData(form);
    const keys = ['contact_phone','contact_email','contact_address','social_instagram','social_facebook','social_tiktok'];
    const btn = document.getElementById('saveContactBtn'); btn.disabled = true;
    await Promise.all(keys.map(k => saveSetting(supabase, k, fd.get(k))));
    window.__APP__.toast.success('შ.'); btn.disabled = false;
  });
}

// ─── DANGER ZONE (owner only) ─────────────────────────────────────────────────
function loadDangerZone(supabase) {
  const el = document.getElementById('tab-danger');
  if (!el) return;
  el.innerHTML = `
    <div class="card" style="border:2px solid var(--color-danger)">
      <div class="card-header" style="background:var(--color-danger-bg,#FEF2F2)">
        <h3 style="color:var(--color-danger)">⚠️ ო. პ.</h3>
        <p class="text-sm text-gray mt-1">ამ ო.-ებ. შ. ვ. გ. ვ.</p>
      </div>
      <div class="card-body" style="display:grid;gap:16px">
        <div style="display:flex;align-items:center;justify-content:space-between;padding:16px;border:1px solid var(--color-danger);border-radius:12px">
          <div>
            <div class="font-semibold">🗑 ყ. ს. კ. გ.</div>
            <div class="text-sm text-gray mt-1">ყ. უ. კ. წ. — შ. ვ. გ. ვ.</div>
          </div>
          <button class="btn btn-danger" id="clearCartBtn">გ.</button>
        </div>
        <div style="display:flex;align-items:center;justify-content:space-between;padding:16px;border:1px solid var(--color-danger);border-radius:12px">
          <div>
            <div class="font-semibold">📧 ტ. ე-მ. გ.</div>
            <div class="text-sm text-gray mt-1">ტ. ე-მ. ია. ს Supabase-ი გ.</div>
          </div>
          <button class="btn btn-secondary" id="testEmailBtn">ტ.</button>
        </div>
        <div style="display:flex;align-items:center;justify-content:space-between;padding:16px;border:1px solid var(--color-danger);border-radius:12px">
          <div>
            <div class="font-semibold">🔄 ს. ბ. გ.</div>
            <div class="text-sm text-gray mt-1">ბ.-ის ს. გ. — Supabase ბ.-ი</div>
          </div>
          <button class="btn btn-secondary" id="clearCacheBtn">გ.</button>
        </div>
      </div>
    </div>
  `;

  document.getElementById('clearCartBtn').addEventListener('click', async () => {
    if (!confirm('ყ. კ. გ.? ი. ვ. გ. ვ.')) return;
    const { error } = await supabase.from('cart_items').delete().neq('id', '00000000-0000-0000-0000-000000000000');
    if (error) window.__APP__.toast.error(error.message);
    else window.__APP__.toast.success('კ. გ.');
  });

  document.getElementById('testEmailBtn').addEventListener('click', async () => {
    supabase.functions.invoke('test-email').catch(() => {});
    window.__APP__.toast.info('ტ. ე-მ. გ.');
  });

  document.getElementById('clearCacheBtn').addEventListener('click', () => {
    window.__APP__.toast.success('ს. გ. (CDN ე. ი. ა.)');
  });
}
