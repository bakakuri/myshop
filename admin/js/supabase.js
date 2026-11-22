// MYSHOP Admin Panel — Supabase Client
// SECURITY: Only anon key here. Service-role key NEVER in browser JS.
// All privileged ops go via RLS-enforced Supabase queries or Edge Functions.

const SUPABASE_URL  = window.__ENV__?.SUPABASE_URL  || '__SUPABASE_URL__';
const SUPABASE_ANON = window.__ENV__?.SUPABASE_ANON || '__SUPABASE_ANON_KEY__';

let _supabase = null;

export async function initSupabase() {
  if (_supabase) return _supabase;

  if (!window.supabase) {
    throw new Error('Supabase JS not loaded. Check script tags in index.html.');
  }

  _supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON, {
    auth: {
      autoRefreshToken: true,
      persistSession: true,
      detectSessionInUrl: false,
      storageKey: 'myshop-admin-session',
    },
    global: {
      headers: { 'x-client-info': 'myshop-admin/1.0' },
    },
  });

  return _supabase;
}

export async function getUser(supabase) {
  const { data: { user }, error } = await supabase.auth.getUser();
  if (error || !user) return null;
  return user;
}

export async function requireAuth(supabase) {
  const user = await getUser(supabase);
  if (!user) {
    window.location.href = 'login.html';
    return null;
  }
  return user;
}

// Convenience re-export for pages that import this directly
export function getSupabase() { return _supabase; }
