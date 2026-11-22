// MYSHOP Admin — Categories Management Page
import { formatDate, confirmDialog, createModal, closeModal, getImageUrl } from '../js/utils.js';

export async function render(container, { supabase }) {
  container.innerHTML = renderShell();
  await loadCategories(supabase);
  document.getElementById('addCatBtn').addEventListener('click', () => openCategoryForm(supabase, null));
}

function renderShell() {
  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">კატეგორიები</h1>
        <p class="page-subtitle">კატალოგის სტრუქტურა</p>
      </div>
      <button class="btn btn-primary" id="addCatBtn">+ კატ. დამატება</button>
    </div>
    <div class="card" id="catsCard">
      <div class="skeleton" style="height:300px;margin:16px;border-radius:8px"></div>
    </div>
  `;
}

async function loadCategories(supabase) {
  const { data: cats } = await supabase
    .from('categories')
    .select(`id, name, slug, description, image_url, sort_order, is_active, parent_id,
             products(id)`)
    .order('sort_order', { ascending: true });

  const card = document.getElementById('catsCard');
  if (!cats?.length) {
    card.innerHTML = `<div class="empty-state" style="padding:60px"><div class="empty-state-icon">📁</div><div class="empty-state-desc">კატეგ. ჯერ არ არის</div></div>`;
    return;
  }

  // Build tree: root-level first, then children
  const roots    = cats.filter(c => !c.parent_id);
  const children = cats.filter(c => c.parent_id);

  card.innerHTML = `
    <div class="filter-bar" style="border-bottom:1px solid var(--color-gray-100)">
      <span class="text-sm font-semibold text-gray">სულ ${cats.length} კატეგ.</span>
      <span class="text-xs text-gray" style="margin-left:auto">რიგის შეცვლა — drag & drop (dev mode-ში)</span>
    </div>
    <div class="table-container" style="border:none;border-radius:0">
      <table class="data-table">
        <thead>
          <tr>
            <th>სახელი</th>
            <th>Slug</th>
            <th>მშობელი</th>
            <th>პროდ.</th>
            <th>რ.ნ.</th>
            <th>სტ.</th>
            <th class="col-actions">მოქ.</th>
          </tr>
        </thead>
        <tbody id="catBody">
          ${roots.map(cat => renderCatRow(cat, cats, children, supabase)).join('')}
        </tbody>
      </table>
    </div>
  `;

  document.querySelectorAll('.btn-edit-cat').forEach(btn => btn.addEventListener('click', () => openCategoryForm(supabase, btn.dataset.id)));
  document.querySelectorAll('.btn-del-cat').forEach(btn => btn.addEventListener('click', async () => {
    if (await confirmDialog(`„${btn.dataset.name}" წაიშლება.`)) {
      await supabase.from('categories').delete().eq('id', btn.dataset.id);
      window.__APP__.toast.success('კატ. წაიშალა'); loadCategories(supabase);
    }
  }));
}

function renderCatRow(cat, all, children, supabase, indent = 0) {
  const kids = children.filter(c => c.parent_id === cat.id);
  const imgUrl = cat.image_url ? getImageUrl(supabase, 'category-images', cat.image_url) : null;
  const parentName = cat.parent_id ? all.find(c => c.id === cat.parent_id)?.name || '—' : '—';

  const rows = `
    <tr>
      <td>
        <div class="flex items-center gap-2">
          ${indent > 0 ? `<span style="display:inline-block;width:${indent * 20}px"></span><span style="color:var(--color-gray-300)">↳</span>` : ''}
          ${imgUrl ? `<img src="${imgUrl}" style="width:32px;height:32px;object-fit:cover;border-radius:6px" alt="">` : `<div style="width:32px;height:32px;background:var(--color-gray-100);border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:14px">📁</div>`}
          <span class="font-semibold text-sm">${cat.name}</span>
        </div>
      </td>
      <td class="text-xs text-gray">${cat.slug}</td>
      <td class="text-sm text-gray">${parentName}</td>
      <td class="text-sm text-gray">${cat.products?.length ?? 0}</td>
      <td class="text-sm text-gray">${cat.sort_order ?? 0}</td>
      <td><span class="badge ${cat.is_active ? 'badge-active' : 'badge-draft'}">${cat.is_active ? 'ჩართ.' : 'გამ.'}</span></td>
      <td class="col-actions">
        <div class="flex gap-2 justify-end">
          <button class="btn btn-sm btn-secondary btn-edit-cat" data-id="${cat.id}">✏️</button>
          <button class="btn btn-sm btn-danger btn-del-cat" data-id="${cat.id}" data-name="${cat.name}">🗑</button>
        </div>
      </td>
    </tr>
  `;

  return rows + kids.map(k => renderCatRow(k, all, children, supabase, indent + 1)).join('');
}

async function openCategoryForm(supabase, catId) {
  let cat = null;
  const { data: allCats } = await supabase.from('categories').select('id, name').order('name');

  if (catId) {
    const { data } = await supabase.from('categories').select('*').eq('id', catId).single();
    cat = data;
  }

  const parentOptions = (allCats || [])
    .filter(c => c.id !== catId)
    .map(c => `<option value="${c.id}" ${cat?.parent_id === c.id ? 'selected' : ''}>${c.name}</option>`)
    .join('');

  createModal({
    id: 'catModal',
    title: cat ? 'კატ. რედ.' : 'ახ. კატეგ.',
    body: `
      <form id="catForm">
        <div class="form-row cols-2">
          <div class="form-group">
            <label class="form-label required">სახელი</label>
            <input class="form-control" name="name" value="${cat?.name || ''}" required>
          </div>
          <div class="form-group">
            <label class="form-label required">Slug (URL)</label>
            <input class="form-control" name="slug" value="${cat?.slug || ''}" required placeholder="magalituri-kategoriya">
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">მშობელი კატ.</label>
          <select class="form-control" name="parent_id">
            <option value="">— მთავარი —</option>
            ${parentOptions}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">აღწერა</label>
          <textarea class="form-control" name="description" rows="2">${cat?.description || ''}</textarea>
        </div>
        <div class="form-row cols-2">
          <div class="form-group">
            <label class="form-label">სურათის URL</label>
            <input class="form-control" name="image_url" value="${cat?.image_url || ''}" placeholder="https://…">
          </div>
          <div class="form-group">
            <label class="form-label">რ.ნ. (sort_order)</label>
            <input class="form-control" name="sort_order" type="number" min="0" value="${cat?.sort_order ?? 0}">
          </div>
        </div>
        <label class="toggle-wrap mt-2">
          <div class="toggle">
            <input type="checkbox" name="is_active" ${cat?.is_active !== false ? 'checked' : ''}>
            <div class="toggle-track"></div>
            <div class="toggle-thumb"></div>
          </div>
          <span class="text-sm">კატ. ჩართულია (ჩანს აპლიკაციაში)</span>
        </label>
      </form>
    `,
    footer: `
      <button class="btn btn-secondary modal-close">გაუქ.</button>
      <button class="btn btn-primary" id="saveCatBtn">${cat ? 'შენახვა' : 'შექმნა'}</button>
    `,
  });

  // Auto-slug from name
  document.querySelector('[name="name"]').addEventListener('input', e => {
    const slugField = document.querySelector('[name="slug"]');
    if (!cat) slugField.value = slugify(e.target.value);
  });

  document.getElementById('saveCatBtn').addEventListener('click', async () => {
    const form = document.getElementById('catForm');
    const fd   = new FormData(form);
    const data = {
      name:        fd.get('name'),
      slug:        fd.get('slug'),
      description: fd.get('description') || null,
      image_url:   fd.get('image_url') || null,
      parent_id:   fd.get('parent_id') || null,
      sort_order:  parseInt(fd.get('sort_order')) || 0,
      is_active:   form.querySelector('[name="is_active"]').checked,
    };

    if (!data.name || !data.slug) { window.__APP__.toast.error('სახელი და slug სავალდ.'); return; }

    const btn = document.getElementById('saveCatBtn');
    btn.disabled = true; btn.textContent = '…';

    const { error } = cat
      ? await supabase.from('categories').update(data).eq('id', cat.id)
      : await supabase.from('categories').insert(data);

    if (error) { window.__APP__.toast.error(error.message); btn.disabled = false; btn.textContent = 'შენახ.'; }
    else {
      window.__APP__.toast.success(cat ? 'კატ. განახლდა' : 'კატ. შეიქმნა');
      closeModal('catModal');
      await loadCategories(supabase);
    }
  });
}

function slugify(str) {
  // Georgian → lowercase latin fallback + standard slugify
  return str.toLowerCase().replace(/\s+/g, '-').replace(/[^\w-]/g, '').slice(0, 60);
}
