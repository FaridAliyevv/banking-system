// Shared helpers for all pages.
// Requests go to the same origin as the page: nginx proxies them to the gateway.
const API = '';

function getToken() {
  return localStorage.getItem('token');
}

function parseJwt(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64).split('').map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join('')
    );
    return JSON.parse(json);
  } catch (e) {
    return null;
  }
}

// Token data is used for the UI only. The services enforce the real permissions.
function currentUser() {
  const token = getToken();
  if (!token) return null;
  const payload = parseJwt(token);
  if (!payload) return null;
  if (payload.exp && payload.exp * 1000 < Date.now()) return null;
  return { id: Number(payload.sub), role: payload.role };
}

function logout() {
  localStorage.removeItem('token');
  location.href = 'index.html';
}

function esc(value) {
  return String(value === null || value === undefined ? '' : value).replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function fmtDate(v) {
  if (!v) return '';
  const d = Array.isArray(v)
    ? new Date(v[0], v[1] - 1, v[2], v[3] || 0, v[4] || 0, v[5] || 0)
    : new Date(v);
  return isNaN(d) ? String(v) : d.toLocaleString('en-US');
}

function money(v) {
  return Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function showMsg(el, text, type) {
  el.textContent = text;
  el.className = 'msg ' + (type || 'info');
  el.hidden = false;
}

function hideMsg(el) {
  el.hidden = true;
}

async function api(path, options = {}) {
  const headers = Object.assign({}, options.headers || {});
  const token = getToken();
  if (token) headers['Authorization'] = 'Bearer ' + token;
  if (options.body) headers['Content-Type'] = 'application/json';

  const res = await fetch(API + path, Object.assign({}, options, { headers }));
  const text = await res.text();

  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch (e) { data = text; }
  }

  if (res.status === 401 && !options.noRedirect) {
    localStorage.removeItem('token');
    location.href = 'index.html';
    throw new Error('Session expired, please sign in again');
  }

  if (!res.ok) {
    let msg;
    if (res.status === 403) {
      msg = "You don't have access to this operation";
    } else if (typeof data === 'string' && data) {
      msg = data;
    } else if (data && (data.message || data.error)) {
      msg = data.message || data.error;
    } else if (data) {
      msg = JSON.stringify(data);
    } else {
      msg = 'Error ' + res.status;
    }
    throw new Error(msg);
  }
  return data;
}
