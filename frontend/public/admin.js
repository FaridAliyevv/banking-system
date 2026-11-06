(function () {
  const user = currentUser();
  if (!user) { location.href = 'index.html'; return; }
  if (!user.role || !String(user.role).includes('ADMIN')) { location.href = 'dashboard.html'; return; }

  const $ = id => document.getElementById(id);
  const msg = $('msg');
  const TYPES = ['DEBIT', 'CREDIT', 'SAVINGS'];
  const STATUSES = ['ACTIVE', 'BLOCKED', 'CLOSED'];

  $('logout').onclick = logout;
  function ok(t) { showMsg(msg, t, 'success'); }
  function fail(e) { showMsg(msg, e.message, 'error'); }
  const sel = (cls, values, current) =>
    `<select class="${cls}">${values.map(v => `<option${v === current ? ' selected' : ''}>${v}</option>`).join('')}</select>`;

  // ---------- tabs ----------
  const loaders = { users: loadUsers, accounts: loadAccounts, transactions: loadTransactions };
  function openTab(name) {
    hideMsg(msg);
    document.querySelectorAll('#main-tabs button').forEach(b => b.classList.toggle('active', b.dataset.tab === name));
    Object.keys(loaders).forEach(k => { $('tab-' + k).hidden = k !== name; });
    loaders[name]().catch(fail);
  }
  document.querySelectorAll('#main-tabs button').forEach(b => b.onclick = () => openTab(b.dataset.tab));

  // ---------- users ----------
  async function loadUsers() {
    const list = await api('/users');
    $('users-body').innerHTML = list.map(u => `
      <tr>
        <td>${esc(u.id)}</td><td>${esc(u.firstName)}</td><td>${esc(u.lastName)}</td>
        <td>${esc(u.email)}</td><td>${esc(u.role)}</td>
        <td>${u.id === user.id ? '' : `<button class="small danger" data-del-user="${esc(u.id)}" type="button">Delete</button>`}</td>
      </tr>`).join('');
  }
  $('users-body').onclick = async e => {
    const id = e.target.dataset && e.target.dataset.delUser;
    if (!id) return;
    if (!confirm('Delete user #' + id + '?')) return;
    try {
      await api('/users/' + id, { method: 'DELETE' });
      ok('User deleted');
      loadUsers();
    } catch (err) { fail(err); }
  };

  // ---------- accounts ----------
  async function loadAccounts() {
    const list = await api('/accounts');
    $('accounts-body').innerHTML = list.map(a => `
      <tr data-id="${esc(a.id)}">
        <td>${esc(a.id)}</td><td>${esc(a.userId)}</td><td>${esc(a.iban)}</td><td>${money(a.balance)}</td>
        <td>${sel('acc-type', TYPES, a.accountType)}</td>
        <td>${sel('acc-status', STATUSES, a.accountStatus)}</td>
        <td style="white-space:nowrap">
          <button class="small" data-save-acc="${esc(a.id)}" type="button">Save</button>
          <button class="small danger" data-del-acc="${esc(a.id)}" type="button">Delete</button>
        </td>
      </tr>`).join('');
  }
  $('accounts-body').onclick = async e => {
    const d = e.target.dataset || {};
    try {
      if (d.saveAcc) {
        const row = e.target.closest('tr');
        await api('/accounts/' + d.saveAcc, {
          method: 'PUT',
          body: JSON.stringify({
            accountType: row.querySelector('.acc-type').value,
            accountStatus: row.querySelector('.acc-status').value
          })
        });
        ok('Account updated');
        loadAccounts();
      } else if (d.delAcc) {
        if (!confirm('Delete account #' + d.delAcc + '?')) return;
        await api('/accounts/' + d.delAcc, { method: 'DELETE' });
        ok('Account deleted');
        loadAccounts();
      }
    } catch (err) { fail(err); }
  };
  $('create-account-form').onsubmit = async e => {
    e.preventDefault();
    hideMsg(msg);
    try {
      await api('/accounts', {
        method: 'POST',
        body: JSON.stringify({ userId: Number($('ca-user').value), accountType: $('ca-type').value })
      });
      ok('Account opened');
      $('ca-user').value = '';
      loadAccounts();
    } catch (err) { fail(err); }
  };

  // ---------- transactions ----------
  async function loadTransactions() {
    const list = await api('/transactions');
    list.sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)));
    $('tx-body').innerHTML = list.map(t => `
      <tr>
        <td>${esc(t.id)}</td><td>${esc(fmtDate(t.createdAt))}</td><td>${esc(t.transactionType)}</td>
        <td>${esc(t.fromIban || '—')}</td><td>${esc(t.toIban || '—')}</td><td>${money(t.amount)}</td>
        <td><span class="badge ${esc(t.transactionStatus)}">${esc(t.transactionStatus)}</span></td>
        <td>${esc(t.description)}</td>
      </tr>`).join('');
  }

  openTab('users');
})();
