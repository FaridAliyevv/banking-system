(function () {
  if (currentUser()) {
    location.href = 'dashboard.html';
    return;
  }

  const $ = id => document.getElementById(id);
  const msg = $('msg');

  function switchTab(login) {
    $('login-form').hidden = !login;
    $('register-form').hidden = login;
    $('tab-login').classList.toggle('active', login);
    $('tab-register').classList.toggle('active', !login);
    hideMsg(msg);
  }
  $('tab-login').onclick = () => switchTab(true);
  $('tab-register').onclick = () => switchTab(false);

  $('login-form').onsubmit = async e => {
    e.preventDefault();
    hideMsg(msg);
    try {
      const data = await api('/users/login', {
        method: 'POST',
        noRedirect: true,
        body: JSON.stringify({ email: $('l-email').value.trim(), password: $('l-pass').value })
      });
      localStorage.setItem('token', data.token);
      location.href = 'dashboard.html';
    } catch (err) {
      showMsg(msg, err.message === 'Invalid credentials' ? 'Invalid email or password' : err.message, 'error');
    }
  };

  $('register-form').onsubmit = async e => {
    e.preventDefault();
    hideMsg(msg);
    const password = $('r-pass').value;
    if (password.length < 8) {
      showMsg(msg, 'Password must contain at least 8 characters', 'error');
      return;
    }
    try {
      await api('/users', {
        method: 'POST',
        noRedirect: true,
        body: JSON.stringify({
          firstName: $('r-first').value.trim(),
          lastName: $('r-last').value.trim(),
          email: $('r-email').value.trim(),
          password: password
        })
      });
      $('l-email').value = $('r-email').value.trim();
      $('register-form').reset();
      switchTab(true);
      showMsg(msg, 'Registration successful. Please sign in.', 'success');
    } catch (err) {
      showMsg(msg, err.message, 'error');
    }
  };
})();
