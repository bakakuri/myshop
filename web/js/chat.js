// ── Chat bot ──────────────────────────────────────────────────
import { $, el } from './ui.js';
import { state, addChatMsg } from './state.js';
import { QUICK_REPLIES, BOT_RESPONSES } from './data.js';

export function renderChat() {
  const msgs = $('#chat-msgs');
  if (!msgs) return;
  msgs.innerHTML = '';

  if (!state.chatMsgs.length) {
    addMessage({ type: 'bot', text: BOT_RESPONSES.greeting, time: now() });
  }
  state.chatMsgs.forEach(m => appendBubble(msgs, m));
  msgs.scrollTop = msgs.scrollHeight;

  // Quick replies
  const qr = $('.chat-quick');
  if (qr) {
    qr.innerHTML = '';
    QUICK_REPLIES.forEach(r => {
      const btn = el('button', 'cq-btn', r);
      btn.addEventListener('click', () => sendMsg(r));
      qr.appendChild(btn);
    });
  }

  // Input
  const inp = $('.chat-inp');
  const send = $('.chat-send');
  inp?.addEventListener('keydown', e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMsg(inp.value); } });
  send?.addEventListener('click', () => sendMsg(inp?.value));
}

function sendMsg(text) {
  text = (text || '').trim();
  if (!text) return;
  const inp = $('.chat-inp');
  if (inp) inp.value = '';

  addMessage({ type: 'me', text, time: now() });
  scrollChat();

  // Typing indicator
  const msgs = $('#chat-msgs');
  const typing = el('div', 'chat-bubble-row');
  typing.id = 'typing-indicator';
  typing.innerHTML = `<div class="c-av bot">🤖</div><div class="c-bubble bot"><div class="typing"><div class="td"></div><div class="td"></div><div class="td"></div></div></div>`;
  msgs?.appendChild(typing);
  scrollChat();

  setTimeout(() => {
    typing.remove();
    const reply = getBotReply(text);
    addMessage({ type: 'bot', text: reply, time: now() });
    scrollChat();
  }, 800 + Math.random() * 600);
}

function getBotReply(text) {
  const t = text.toLowerCase();
  if (t.includes('გამარჯობა') || t.includes('hello') || t.includes('hi')) return BOT_RESPONSES.greeting;
  if (t.includes('მიწოდება') || t.includes('delivery')) return BOT_RESPONSES.delivery;
  if (t.includes('გარანტია') || t.includes('warranty')) return BOT_RESPONSES.warranty;
  if (t.includes('დაბრუნება') || t.includes('return')) return BOT_RESPONSES.return;
  if (t.includes('ფასი') || t.includes('price')) return BOT_RESPONSES.price;
  const pool = BOT_RESPONSES.default;
  return pool[Math.floor(Math.random() * pool.length)];
}

function addMessage(msg) {
  addChatMsg(msg);
  const msgs = $('#chat-msgs');
  if (msgs) appendBubble(msgs, msg);
}

function appendBubble(container, msg) {
  const row = el('div', `chat-bubble-row ${msg.type === 'me' ? 'me' : ''}`);
  const av = msg.type === 'bot' ? `<div class="c-av bot">🤖</div>` : `<div class="c-av me">👤</div>`;
  row.innerHTML = `${msg.type === 'bot' ? av : ''}
    <div>
      <div class="c-bubble ${msg.type}">${escHtml(msg.text)}</div>
      <div class="c-time">${msg.time}</div>
    </div>
    ${msg.type === 'me' ? av : ''}`;
  container.appendChild(row);
}

function scrollChat() {
  const msgs = $('#chat-msgs');
  if (msgs) setTimeout(() => { msgs.scrollTop = msgs.scrollHeight; }, 50);
}

function now() {
  return new Date().toLocaleTimeString('ka-GE', { hour: '2-digit', minute: '2-digit' });
}

function escHtml(s) {
  return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
