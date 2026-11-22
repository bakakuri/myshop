// MYSHOP Admin — Conversations (Chat) Management Page
import { formatRelative, formatDateTime, createModal, closeModal } from '../js/utils.js';

let state = { conversations: [], active: null, messages: [], realtimeSub: null };

export async function render(container, { supabase }) {
  container.innerHTML = renderShell();
  await loadConversations(supabase);
  bindTopbarEvents(supabase);
}

function renderShell() {
  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">საუბრები</h1>
        <p class="page-subtitle">მომხმარებლებთან ჩათ-კომუნიკაცია</p>
      </div>
      <div class="flex gap-3">
        <select class="form-control" id="convStatusFilter" style="width:160px">
          <option value="">ყველა</option>
          <option value="open">ღია</option>
          <option value="resolved">დახურული</option>
        </select>
      </div>
    </div>

    <div style="display:grid;grid-template-columns:360px 1fr;gap:0;height:calc(100vh - 200px);border:1px solid var(--color-gray-200);border-radius:var(--radius-lg);overflow:hidden;background:#fff">

      <!-- Conversation List -->
      <div style="border-right:1px solid var(--color-gray-200);display:flex;flex-direction:column">
        <div style="padding:12px 16px;border-bottom:1px solid var(--color-gray-100)">
          <input type="text" class="form-control" id="convSearch" placeholder="ძებნა…" style="height:34px;font-size:13px">
        </div>
        <div style="flex:1;overflow-y:auto" id="convList">
          ${[1,2,3,4,5].map(() => `
            <div style="padding:14px 16px;border-bottom:1px solid var(--color-gray-100)">
              <div class="skeleton" style="height:16px;width:70%;margin-bottom:8px"></div>
              <div class="skeleton" style="height:12px;width:50%"></div>
            </div>
          `).join('')}
        </div>
      </div>

      <!-- Chat Area -->
      <div style="display:flex;flex-direction:column" id="chatArea">
        <div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--color-gray-400);flex-direction:column;gap:12px">
          <span style="font-size:40px">💬</span>
          <span class="text-sm">საუბარი ჯერ არ არჩეული</span>
        </div>
      </div>

    </div>
  `;
}

function bindTopbarEvents(supabase) {
  let timer;
  document.getElementById('convSearch')?.addEventListener('input', e => {
    clearTimeout(timer);
    timer = setTimeout(() => loadConversations(supabase, e.target.value), 350);
  });
  document.getElementById('convStatusFilter')?.addEventListener('change', e => {
    loadConversations(supabase, '', e.target.value);
  });
}

async function loadConversations(supabase, search = '', status = '') {
  let query = supabase
    .from('conversations')
    .select(`id, status, created_at, updated_at,
             profiles:customer_id(full_name, email),
             orders:order_id(order_number),
             messages(id, content, created_at, is_internal_note)`)
    .order('updated_at', { ascending: false })
    .limit(50);

  if (status) query = query.eq('status', status);

  const { data: convs } = await query;
  state.conversations = convs || [];

  const list = document.getElementById('convList');
  if (!state.conversations.length) {
    list.innerHTML = `<div class="empty-state" style="padding:48px 16px"><div class="empty-state-icon">💬</div><div class="empty-state-desc">საუბარი არ მოიძებნა</div></div>`;
    return;
  }

  let filtered = state.conversations;
  if (search) {
    const q = search.toLowerCase();
    filtered = filtered.filter(c =>
      c.profiles?.full_name?.toLowerCase().includes(q) ||
      c.profiles?.email?.toLowerCase().includes(q) ||
      c.orders?.order_number?.toString().includes(q)
    );
  }

  list.innerHTML = filtered.map(c => renderConvItem(c)).join('');
  list.querySelectorAll('.conv-item').forEach(el => {
    el.addEventListener('click', () => openConversation(supabase, el.dataset.id));
  });
}

function renderConvItem(c) {
  const lastMsg = c.messages?.filter(m => !m.is_internal_note).sort((a,b) => new Date(b.created_at) - new Date(a.created_at))[0];
  const isActive = state.active?.id === c.id;

  return `
    <div class="conv-item ${isActive ? 'active' : ''}" data-id="${c.id}"
         style="padding:14px 16px;border-bottom:1px solid var(--color-gray-100);cursor:pointer;
                background:${isActive ? 'var(--color-primary-50)' : 'transparent'};transition:background 0.15s">
      <div class="flex items-center gap-3">
        <div style="width:36px;height:36px;border-radius:50%;background:var(--color-primary-100);flex-shrink:0;display:flex;align-items:center;justify-content:center;font-weight:700;color:var(--color-primary)">
          ${(c.profiles?.full_name || c.profiles?.email || '?')[0].toUpperCase()}
        </div>
        <div style="flex:1;overflow:hidden">
          <div class="flex items-center justify-between">
            <span class="font-semibold text-sm truncate">${c.profiles?.full_name || c.profiles?.email || 'უცნობი'}</span>
            <span class="text-xs text-gray" style="flex-shrink:0;margin-left:8px">${formatRelative(c.updated_at)}</span>
          </div>
          <div class="text-xs text-gray truncate">${lastMsg?.content || 'შეტყობინება არ არის'}</div>
          <div class="flex items-center gap-2 mt-1">
            ${c.orders?.order_number ? `<span class="chip" style="font-size:10px;padding:1px 7px">#${c.orders.order_number}</span>` : ''}
            <span class="badge ${c.status === 'open' ? 'badge-active' : 'badge-draft'}" style="font-size:10px">${c.status === 'open' ? 'ღია' : 'დახ.'}</span>
          </div>
        </div>
      </div>
    </div>
  `;
}

async function openConversation(supabase, convId) {
  // Unsubscribe old channel
  if (state.realtimeSub) {
    await supabase.removeChannel(state.realtimeSub);
    state.realtimeSub = null;
  }

  state.active = state.conversations.find(c => c.id === convId);

  // Update list highlight
  document.querySelectorAll('.conv-item').forEach(el => {
    const isActive = el.dataset.id === convId;
    el.style.background = isActive ? 'var(--color-primary-50)' : '';
    el.style.borderLeft = isActive ? '3px solid var(--color-primary)' : '';
  });

  renderChatArea(state.active);

  // Load messages
  const { data: msgs } = await supabase
    .from('messages')
    .select(`id, content, is_from_staff, is_internal_note, created_at,
             profiles:sender_id(full_name, email, role)`)
    .eq('conversation_id', convId)
    .order('created_at', { ascending: true });

  state.messages = msgs || [];
  renderMessages();

  // Real-time subscription
  state.realtimeSub = supabase
    .channel(`conv-${convId}`)
    .on('postgres_changes', { event: 'INSERT', schema: 'public', table: 'messages', filter: `conversation_id=eq.${convId}` },
      async (payload) => {
        // Fetch sender info
        const { data: fullMsg } = await supabase
          .from('messages')
          .select(`id, content, is_from_staff, is_internal_note, created_at, profiles:sender_id(full_name, email)`)
          .eq('id', payload.new.id)
          .single();
        if (fullMsg) {
          state.messages.push(fullMsg);
          renderMessages();
        }
      }
    )
    .subscribe();
}

function renderChatArea(conv) {
  const chatArea = document.getElementById('chatArea');
  const name = conv.profiles?.full_name || conv.profiles?.email || 'უცნობი';

  chatArea.innerHTML = `
    <div style="padding:14px 20px;border-bottom:1px solid var(--color-gray-100);display:flex;align-items:center;justify-content:space-between">
      <div>
        <div class="font-semibold">${name}</div>
        <div class="text-xs text-gray">${conv.profiles?.email || ''} ${conv.orders?.order_number ? '· #' + conv.orders.order_number : ''}</div>
      </div>
      <div class="flex gap-2">
        <label class="toggle-wrap">
          <div class="toggle">
            <input type="checkbox" id="internalToggle">
            <div class="toggle-track"></div>
            <div class="toggle-thumb"></div>
          </div>
          <span class="text-sm text-gray">შიდა შენიშვნა</span>
        </label>
        ${conv.status === 'open'
          ? `<button class="btn btn-sm btn-secondary" id="resolveBtn">✓ დახურვა</button>`
          : `<button class="btn btn-sm btn-success" id="reopenBtn">↺ გახსნა</button>`
        }
      </div>
    </div>

    <!-- Messages -->
    <div style="flex:1;overflow-y:auto;padding:20px;display:flex;flex-direction:column;gap:10px" id="messagesWrap"></div>

    <!-- Input -->
    <div style="padding:14px 16px;border-top:1px solid var(--color-gray-100)">
      <div id="internalNote" style="display:none;background:var(--color-warning-bg);border-radius:8px 8px 0 0;padding:6px 12px;font-size:12px;color:var(--color-warning)">
        🔒 შიდა შენიშვნა — მომხმარებელი ვერ ნახავს
      </div>
      <div class="flex gap-3 items-end">
        <textarea id="msgInput" class="form-control" style="min-height:40px;max-height:120px;resize:none;line-height:1.5;border-radius:10px"
                  placeholder="შეტყობინება…" rows="1"></textarea>
        <button class="btn btn-primary" id="sendBtn" style="flex-shrink:0">📤 გაგზავნა</button>
      </div>
    </div>
  `;

  document.getElementById('internalToggle').addEventListener('change', e => {
    document.getElementById('internalNote').style.display = e.target.checked ? 'block' : 'none';
    document.getElementById('msgInput').style.borderRadius = e.target.checked ? '0 0 10px 10px' : '10px';
    document.getElementById('msgInput').placeholder = e.target.checked ? 'შიდა შენიშვნა (არ ჩანს მომხ-ს)…' : 'შეტყობინება…';
  });

  document.getElementById('sendBtn').addEventListener('click', () => sendMessage(window.__APP__.supabase, conv.id));
  document.getElementById('msgInput').addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(window.__APP__.supabase, conv.id); }
  });

  document.getElementById('resolveBtn')?.addEventListener('click', () => updateConvStatus(window.__APP__.supabase, conv.id, 'resolved'));
  document.getElementById('reopenBtn')?.addEventListener('click', () => updateConvStatus(window.__APP__.supabase, conv.id, 'open'));
}

function renderMessages() {
  const wrap = document.getElementById('messagesWrap');
  if (!wrap) return;

  wrap.innerHTML = state.messages.map(m => {
    const isStaff = m.is_from_staff;
    const isInternal = m.is_internal_note;

    return `
      <div style="display:flex;justify-content:${isStaff ? 'flex-end' : 'flex-start'}">
        <div style="max-width:72%;background:${isInternal ? 'var(--color-warning-bg)' : isStaff ? 'var(--color-primary)' : 'var(--color-gray-100)'};
                    color:${isStaff && !isInternal ? 'white' : 'var(--color-gray-800)'};
                    border-radius:${isStaff ? '14px 14px 2px 14px' : '14px 14px 14px 2px'};
                    padding:10px 14px;border:${isInternal ? '1px solid var(--color-warning)' : 'none'}">
          ${isInternal ? '<div style="font-size:10px;color:var(--color-warning);font-weight:600;margin-bottom:4px">🔒 STAFF NOTE</div>' : ''}
          <div style="font-size:14px;line-height:1.5;white-space:pre-wrap">${escapeHtml(m.content)}</div>
          <div style="font-size:10px;opacity:0.6;margin-top:5px;text-align:right">
            ${m.profiles?.full_name || 'Staff'} · ${formatRelative(m.created_at)}
          </div>
        </div>
      </div>
    `;
  }).join('');

  wrap.scrollTop = wrap.scrollHeight;
}

async function sendMessage(supabase, convId) {
  const input = document.getElementById('msgInput');
  const content = input.value.trim();
  if (!content) return;

  const isInternal = document.getElementById('internalToggle').checked;
  const { AppState } = await import('../js/app.js');

  input.value = '';
  input.style.height = 'auto';

  const { error } = await supabase.from('messages').insert({
    conversation_id: convId,
    sender_id: AppState.user?.id,
    content,
    is_from_staff: true,
    is_internal_note: isInternal,
  });

  if (error) window.__APP__.toast.error('შეტყობინება ვერ გაიგზავნა');
}

async function updateConvStatus(supabase, convId, newStatus) {
  await supabase.from('conversations').update({ status: newStatus }).eq('id', convId);
  await loadConversations(supabase);
  if (state.active?.id === convId) {
    state.active = { ...state.active, status: newStatus };
    renderChatArea(state.active);
    renderMessages();
  }
}

function escapeHtml(str) {
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
