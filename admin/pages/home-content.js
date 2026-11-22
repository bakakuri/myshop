// MYSHOP Admin — Home Content Editor
// Controls what customers see on the app's home screen (banners handled in promotions.js)
import { confirmDialog, createModal, closeModal } from '../js/utils.js';

export async function render(container, { supabase }) {
  container.innerHTML = `
    <div class="page-header">
      <h1 class="page-title">🏠 მთავარი გვ. კონტ.</h1>
      <p class="page-subtitle" style="margin-top:4px">მომხმ. ხილ. სექციები</p>
    </div>
    <div class="tabs" id="hcTabs">
      <button class="tab-btn active" data-tab="sections">📋 სექ.</button>
      <button class="tab-btn" data-tab="featured">⭐ რეკ. პ.</button>
      <button class="tab-btn" data-tab="announcement">📢 გ.</button>
    </div>
    <div id="tab-sections"><div class="skeleton" style="height:300px;border-radius:14px"></div></div>
    <div id="tab-featured" style="display:none"><div class="skeleton" style="height:300px;border-radius:14px"></div></div>
    <div id="tab-announcement" style="display:none"><div class="skeleton" style="height:200px;border-radius:14px"></div></div>
  `;

  document.querySelectorAll('.tab-btn[data-tab]').forEach(btn => {
    btn.addEventListener('click', async () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      ['sections','featured','announcement'].forEach(t => {
        document.getElementById(`tab-${t}`).style.display = t === btn.dataset.tab ? '' : 'none';
      });
      if (btn.dataset.tab === 'featured') await loadFeatured(supabase);
      if (btn.dataset.tab === 'announcement') await loadAnnouncement(supabase);
    });
  });

  await loadSections(supabase);
}

// ─── SECTIONS ─────────────────────────────────────────────────────────────────
async function loadSections(supabase) {
  const { data: sections } = await supabase.from('home_sections').select('*').order('sort_order');
  const el = document.getElementById('tab-sections');

  el.innerHTML = `
    <div class="page-header" style="margin-bottom:16px">
      <p class="page-subtitle">ჩარ./გამ. სექ.</p>
      <button class="btn btn-primary" id="addSectionBtn">+ სექ.</button>
    </div>
    <div style="display:grid;gap:12px" id="sectionsList">
      ${(sections || []).map(s => `
        <div class="card" style="overflow:visible">
          <div class="card-body" style="display:flex;align-items:center;gap:16px">
            <div style="font-size:28px;width:48px;text-align:center">${sectionIcon(s.type)}</div>
            <div style="flex:1">
              <div class="font-semibold">${s.title}</div>
              <div class="text-xs text-gray mt-1">${sectionTypeLabel(s.type)} · რ.ნ.${s.sort_order}</div>
              ${s.subtitle ? `<div class="text-sm text-gray mt-1">${s.subtitle}</div>` : ''}
            </div>
            <div class="flex items-center gap-2">
              <label class="toggle-wrap">
                <div class="toggle"><input type="checkbox" class="section-toggle" data-id="${s.id}" ${s.is_active ? 'checked' : ''}>
                  <div class="toggle-track"></div><div class="toggle-thumb"></div></div>
                <span class="text-xs">${s.is_active ? 'ჩ.' : 'გ.'}</span>
              </label>
              <button class="btn btn-sm btn-secondary btn-edit-section" data-id="${s.id}">✏️</button>
              <button class="btn btn-sm btn-danger btn-del-section" data-id="${s.id}" data-title="${s.title}">🗑</button>
            </div>
          </div>
        </div>
      `).join('')}
    </div>
  `;

  el.querySelectorAll('.section-toggle').forEach(chk => {
    chk.addEventListener('change', async () => {
      await supabase.from('home_sections').update({ is_active: chk.checked }).eq('id', chk.dataset.id);
      const label = chk.closest('.toggle-wrap').querySelector('.text-xs');
      if (label) label.textContent = chk.checked ? 'ჩ.' : 'გ.';
      window.__APP__.toast.success(chk.checked ? 'ჩართ.' : 'გამ.');
    });
  });
  el.querySelectorAll('.btn-edit-section').forEach(btn => btn.addEventListener('click', () => openSectionForm(supabase, btn.dataset.id)));
  el.querySelectorAll('.btn-del-section').forEach(btn => btn.addEventListener('click', async () => {
    if (await confirmDialog(`„${btn.dataset.title}" სექ. წ.?`)) {
      await supabase.from('home_sections').delete().eq('id', btn.dataset.id);
      window.__APP__.toast.success('სექ. წ.'); loadSections(supabase);
    }
  }));
  document.getElementById('addSectionBtn').addEventListener('click', () => openSectionForm(supabase, null));
}

function sectionIcon(type) {
  return { categories:'🏷', products:'🛍', banner:'🖼', brands:'🏢', promo:'🎉' }[type] || '📋';
}
function sectionTypeLabel(type) {
  return { categories:'კატ. სლაიდ.', products:'პ. სია', banner:'ბ.', brands:'ბრ.', promo:'აქ.' }[type] || type;
}

async function openSectionForm(supabase, sectionId) {
  let s = null;
  if (sectionId) { const { data } = await supabase.from('home_sections').select('*').eq('id', sectionId).single(); s = data; }

  createModal({
    id: 'sectionModal', title: s ? 'სექ. რედ.' : 'ახ. სექ.',
    body: `<form id="sectionForm">
      <div class="form-group"><label class="form-label required">სათ.</label>
        <input class="form-control" name="title" value="${s?.title || ''}" required></div>
      <div class="form-group"><label class="form-label">ქვ.-სათ.</label>
        <input class="form-control" name="subtitle" value="${s?.subtitle || ''}"></div>
      <div class="form-row cols-2">
        <div class="form-group"><label class="form-label required">ტ.</label>
          <select class="form-control" name="type">
            <option value="categories" ${s?.type === 'categories' ? 'selected' : ''}>კატ. სლ.</option>
            <option value="products" ${s?.type === 'products' ? 'selected' : ''}>პ. სია</option>
            <option value="banner" ${s?.type === 'banner' ? 'selected' : ''}>ბ.</option>
            <option value="promo" ${s?.type === 'promo' ? 'selected' : ''}>აქ.</option>
          </select></div>
        <div class="form-group"><label class="form-label">რ.ნ.</label>
          <input class="form-control" name="sort_order" type="number" value="${s?.sort_order ?? 0}"></div>
      </div>
      <div class="form-group"><label class="form-label">ფილტ. JSON</label>
        <textarea class="form-control" name="config" rows="3" placeholder='{"category_id":"...","limit":10}'>${s?.config ? JSON.stringify(s.config, null, 2) : ''}</textarea>
        <span class="text-xs text-gray">კატ.-ს ID, ლი. ან სხ. კონფ.</span></div>
      <label class="toggle-wrap mt-2">
        <div class="toggle"><input type="checkbox" name="is_active" ${s?.is_active !== false ? 'checked' : ''}>
          <div class="toggle-track"></div><div class="toggle-thumb"></div></div>
        <span class="text-sm">ჩ.</span>
      </label>
    </form>`,
    footer: `<button class="btn btn-secondary modal-close">გ.</button><button class="btn btn-primary" id="saveSectionBtn">${s ? 'შ.' : 'შ.'}</button>`,
  });

  document.getElementById('saveSectionBtn').addEventListener('click', async () => {
    const form = document.getElementById('sectionForm'); const fd = new FormData(form);
    let config = null;
    try { const raw = fd.get('config').trim(); if (raw) config = JSON.parse(raw); } catch { window.__APP__.toast.error('JSON ფ. სინ.'); return; }
    const data = { title: fd.get('title'), subtitle: fd.get('subtitle')||null, type: fd.get('type'), sort_order: parseInt(fd.get('sort_order'))||0, config, is_active: form.querySelector('[name="is_active"]').checked };
    if (!data.title) { window.__APP__.toast.error('სათ. სავ.'); return; }
    const btn = document.getElementById('saveSectionBtn'); btn.disabled = true;
    const { error } = s ? await supabase.from('home_sections').update(data).eq('id', s.id) : await supabase.from('home_sections').insert(data);
    if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; }
    else { window.__APP__.toast.success('შ.'); closeModal('sectionModal'); loadSections(supabase); }
  });
}

// ─── FEATURED PRODUCTS ────────────────────────────────────────────────────────
async function loadFeatured(supabase) {
  const el = document.getElementById('tab-featured');

  const [{ data: featured }, { data: products }] = await Promise.all([
    supabase.from('featured_products').select(`*, products:product_id(id, name, price, images)`).order('sort_order'),
    supabase.from('products').select('id, name, price').eq('status','active').order('name'),
  ]);

  el.innerHTML = `
    <div class="page-header" style="margin-bottom:16px">
      <p class="page-subtitle">„რეკ." სია</p>
      <button class="btn btn-primary" id="addFeaturedBtn">+ პ. დამ.</button>
    </div>
    <div style="display:grid;gap:10px" id="featuredList">
      ${(featured||[]).map(f => `
        <div class="card" style="overflow:visible">
          <div class="card-body" style="display:flex;align-items:center;gap:14px">
            <div style="width:48px;height:48px;background:var(--color-gray-100);border-radius:10px;overflow:hidden;flex-shrink:0">
              ${f.products?.images?.[0] ? `<img src="${f.products.images[0]}" style="width:100%;height:100%;object-fit:cover">` : '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--color-gray-400)">🛍</div>'}
            </div>
            <div style="flex:1">
              <div class="font-semibold text-sm">${f.products?.name || '—'}</div>
              <div class="text-xs text-gray">რ.ნ. ${f.sort_order}</div>
            </div>
            <button class="btn btn-sm btn-danger btn-del-featured" data-id="${f.id}">🗑</button>
          </div>
        </div>
      `).join('')}
    </div>
  `;

  document.getElementById('addFeaturedBtn').addEventListener('click', () => {
    createModal({
      id: 'featuredModal', title: 'პ. დამ. „რეკ."',
      body: `<form id="featuredForm">
        <div class="form-group"><label class="form-label required">პ.</label>
          <select class="form-control" name="product_id">
            ${(products||[]).map(p => `<option value="${p.id}">${p.name}</option>`).join('')}
          </select></div>
        <div class="form-group"><label class="form-label">რ.ნ.</label>
          <input class="form-control" name="sort_order" type="number" value="0"></div>
      </form>`,
      footer: `<button class="btn btn-secondary modal-close">გ.</button><button class="btn btn-primary" id="saveFeaturedBtn">დამ.</button>`,
    });
    document.getElementById('saveFeaturedBtn').addEventListener('click', async () => {
      const form = document.getElementById('featuredForm'); const fd = new FormData(form);
      const btn = document.getElementById('saveFeaturedBtn'); btn.disabled = true;
      const { error } = await supabase.from('featured_products').insert({ product_id: fd.get('product_id'), sort_order: parseInt(fd.get('sort_order'))||0 });
      if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; }
      else { window.__APP__.toast.success('დამ.'); closeModal('featuredModal'); loadFeatured(supabase); }
    });
  });

  el.querySelectorAll('.btn-del-featured').forEach(btn => btn.addEventListener('click', async () => {
    if (await confirmDialog('„რეკ." სიიდ. ამ. წ.?')) {
      await supabase.from('featured_products').delete().eq('id', btn.dataset.id);
      window.__APP__.toast.success('ამ.'); loadFeatured(supabase);
    }
  }));
}

// ─── ANNOUNCEMENT BANNER ──────────────────────────────────────────────────────
async function loadAnnouncement(supabase) {
  const el = document.getElementById('tab-announcement');
  const { data: ann } = await supabase.from('app_settings').select('value').eq('key','announcement').maybeSingle();
  const v = ann?.value || {};

  el.innerHTML = `
    <div class="card">
      <div class="card-header"><h3>📢 გამ. ბ.</h3><p class="text-sm text-gray mt-1">ა. სარ. ბ. ყველ. ეკ.-ზე</p></div>
      <div class="card-body">
        <form id="annForm">
          <div class="form-group"><label class="form-label">ტ.</label>
            <input class="form-control" name="text" value="${v.text || ''}" placeholder="მიმ. გ..."></div>
          <div class="form-row cols-2">
            <div class="form-group"><label class="form-label">ბ. ფ.</label>
              <input class="form-control" name="bg_color" type="color" value="${v.bg_color || '#5B21B6'}"></div>
            <div class="form-group"><label class="form-label">ტ. ფ.</label>
              <input class="form-control" name="text_color" type="color" value="${v.text_color || '#FFFFFF'}"></div>
          </div>
          <div class="form-group"><label class="form-label">ბ.</label>
            <input class="form-control" name="link_url" value="${v.link_url || ''}" placeholder="https://…"></div>
          <label class="toggle-wrap mt-2">
            <div class="toggle"><input type="checkbox" name="is_active" ${v.is_active ? 'checked' : ''}>
              <div class="toggle-track"></div><div class="toggle-thumb"></div></div>
            <span class="text-sm">ჩ.</span>
          </label>
        </form>
      </div>
      <div class="card-footer">
        <button class="btn btn-primary" id="saveAnnBtn">შ.</button>
      </div>
    </div>
  `;

  document.getElementById('saveAnnBtn').addEventListener('click', async () => {
    const form = document.getElementById('annForm'); const fd = new FormData(form);
    const value = { text: fd.get('text'), bg_color: fd.get('bg_color'), text_color: fd.get('text_color'), link_url: fd.get('link_url')||null, is_active: form.querySelector('[name="is_active"]').checked };
    const btn = document.getElementById('saveAnnBtn'); btn.disabled = true;
    const { error } = await supabase.from('app_settings').upsert({ key: 'announcement', value }, { onConflict: 'key' });
    if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; }
    else { window.__APP__.toast.success('შ.'); btn.disabled = false; }
  });
}
