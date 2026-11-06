(function () {
  const user = currentUser();
  if (!user) { location.href = 'index.html'; return; }

  const $ = id => document.getElementById(id);
  const msg = $('msg');
  let accounts = [];
  let op = 'transfer';

  $('logout').onclick = logout;
  if (user.role && String(user.role).includes('ADMIN')) $('admin-link').hidden = false;

  function ok(t) { showMsg(msg, t, 'success'); window.scrollTo(0, 0); }
  function fail(e) { showMsg(msg, e.message, 'error'); window.scrollTo(0, 0); }

  // ---------- profile ----------
  async function loadProfile() {
    const u = await api('/users/' + user.id);
    $('p-first').value = u.firstName;
    $('p-last').value = u.lastName;
    $('p-email').value = u.email;
    $('who').textContent = u.firstName + ' ' + u.lastName;
  }

  $('profile-form').onsubmit = async e => {
    e.preventDefault();
    hideMsg(msg);
    const body = {
      firstName: $('p-first').value.trim(),
      lastName: $('p-last').value.trim(),
      email: $('p-email').value.trim()
    };
    const pass = $('p-pass').value;
    if (pass) {
      if (pass.length < 8) { fail(new Error('Password must contain at least 8 characters')); return; }
      body.password = pass;
    }
    try {
      await api('/users/' + user.id, { method: 'PUT', body: JSON.stringify(body) });
      $('p-pass').value = '';
      ok('Profile updated');
      loadProfile();
    } catch (err) { fail(err); }
  };

  // ---------- accounts ----------
  function fillSelects() {
    const opts = accounts.map(a =>
      `<option value="${esc(a.iban)}">${esc(a.iban)} (${esc(a.accountType)}, ${money(a.balance)})</option>`).join('');
    $('o-from').innerHTML = opts;
    const prev = $('h-iban').value;
    $('h-iban').innerHTML = opts;
    if (prev) $('h-iban').value = prev;
  }

  async function loadAccounts() {
    accounts = await api('/accounts/user/' + user.id);
    $('accounts-body').innerHTML = accounts.map(a => `
      <tr>
        <td>${esc(a.iban)}</td>
        <td>${esc(a.accountType)}</td>
        <td><span class="badge ${esc(a.accountStatus)}">${esc(a.accountStatus)}</span></td>
        <td>${money(a.balance)}</td>
        <td><button class="small" data-copy="${esc(a.iban)}" type="button">Copy IBAN</button></td>
      </tr>`).join('');
    $('no-accounts').hidden = accounts.length > 0;
    fillSelects();
  }

  $('accounts-body').onclick = e => {
    const iban = e.target.dataset && e.target.dataset.copy;
    if (iban && navigator.clipboard) {
      navigator.clipboard.writeText(iban).then(() => ok('IBAN copied: ' + iban));
    }
  };

  $('create-account-form').onsubmit = async e => {
    e.preventDefault();
    hideMsg(msg);
    try {
      await api('/accounts', {
        method: 'POST',
        body: JSON.stringify({ userId: user.id, accountType: $('a-type').value })
      });
      ok('Account opened');
      await loadAccounts();
    } catch (err) { fail(err); }
  };

  // ---------- transactions ----------
  function setOp(name) {
    op = name;
    document.querySelectorAll('[data-op]').forEach(b => b.classList.toggle('active', b.dataset.op === name));
    $('f-from').hidden = name === 'deposit';
    $('f-to').hidden = name === 'withdrawal';
    $('o-to').required = name === 'transfer' || name === 'deposit';
    $('o-to').placeholder = name === 'deposit' ? 'Your account IBAN' : 'Recipient IBAN';
  }
  document.querySelectorAll('[data-op]').forEach(b => b.onclick = () => setOp(b.dataset.op));
  setOp('transfer');

  $('op-form').onsubmit = async e => {
    e.preventDefault();
    hideMsg(msg);
    const amount = Number($('o-amount').value);
    if (!(amount > 0)) { fail(new Error('Amount must be greater than zero')); return; }
    const description = $('o-desc').value.trim();
    const from = $('o-from').value;
    const to = $('o-to').value.trim();
    let body;
    if (op === 'transfer') body = { fromIban: from, toIban: to, amount, description };
    else if (op === 'deposit') body = { toIban: to, amount, description };
    else body = { fromIban: from, amount, description };
    if (!from && op !== 'deposit') { fail(new Error('Open an account first')); return; }
    try {
      await api('/transactions/' + op, { method: 'POST', body: JSON.stringify(body) });
      ok('Operation completed');
      $('o-amount').value = '';
      $('o-desc').value = '';
      $('o-to').value = '';
      await loadAccounts();
      if ($('h-iban').value) loadHistory();
    } catch (err) {
      fail(err);
      loadAccounts().catch(() => {});
    }
  };

  // ---------- history ----------
  async function loadHistory() {
    const iban = $('h-iban').value;
    if (!iban) { $('history-body').innerHTML = ''; return; }
    try {
      const list = await api('/transactions/account/' + encodeURIComponent(iban));
      list.sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)));
      $('history-body').innerHTML = list.length ? list.map(t => {
        const incoming = t.toIban === iban && t.fromIban !== iban;
        const outgoing = t.fromIban === iban;
        const cls = incoming ? 'plus' : (outgoing ? 'minus' : '');
        const sign = incoming ? '+' : (outgoing ? '−' : '');
        return `<tr>
          <td>${esc(fmtDate(t.createdAt))}</td>
          <td>${esc(t.transactionType)}</td>
          <td>${esc(t.fromIban || '—')}</td>
          <td>${esc(t.toIban || '—')}</td>
          <td class="${cls}">${sign}${money(t.amount)}</td>
          <td><span class="badge ${esc(t.transactionStatus)}">${esc(t.transactionStatus)}</span></td>
          <td>${esc(t.description)}</td>
        </tr>`;
      }).join('') : '<tr><td colspan="7" class="muted">No transactions</td></tr>';
    } catch (err) { fail(err); }
  }
  $('h-load').onclick = () => { hideMsg(msg); loadHistory(); };

  // ---------- start ----------
  (async () => {
    try {
      await loadProfile();
      await loadAccounts();
      if (accounts.length) loadHistory();
    } catch (err) { fail(err); }
  })();
})();
