(function () {
  var logoutBtn = document.querySelector('.logout-link');
  if (!logoutBtn) return;

  var STORAGE_KEY = 'saved-accounts';
  var COLLAPSED_KEY = 'sidebar-collapsed';

  // ── Build dialog DOM ──
  var overlay = document.createElement('div');
  overlay.className = 'md-dialog-overlay hidden';
  overlay.id = 'logout-dialog';
  overlay.innerHTML =
    '<div class="md-dialog" style="width:min(400px,100%);">' +
      '<div class="md-dialog__title">确认退出</div>' +
      '<div class="md-dialog__body">' +
        '<div class="logout-radio-group">' +
          '<label class="logout-radio">' +
            '<input type="radio" name="logout-mode" value="clear">' +
            '<span class="logout-radio__content">' +
              '<span class="logout-radio__title">退出并清除登录记录</span>' +
              '<span class="logout-radio__desc">下次在该设备需要重新登录当前账号</span>' +
            '</span>' +
          '</label>' +
          '<label class="logout-radio">' +
            '<input type="radio" name="logout-mode" value="keep" checked>' +
            '<span class="logout-radio__content">' +
              '<span class="logout-radio__title">退出并保留登录记录</span>' +
              '<span class="logout-radio__desc">下次在该设备支持自动登录当前账号</span>' +
            '</span>' +
          '</label>' +
        '</div>' +
      '</div>' +
      '<div class="md-dialog__actions">' +
        '<button class="md-btn md-btn--outlined" type="button" id="logout-cancel">取消</button>' +
        '<button class="md-btn md-btn--danger" type="button" id="logout-confirm">确定退出</button>' +
      '</div>' +
    '</div>';
  document.body.appendChild(overlay);

  var cancelBtn = document.getElementById('logout-cancel');
  var confirmBtn = document.getElementById('logout-confirm');

  function open() {
    overlay.classList.remove('hidden');
    cancelBtn.focus();
  }

  function close() {
    overlay.classList.add('hidden');
    logoutBtn.focus();
  }

  // ── Event bindings ──
  logoutBtn.addEventListener('click', function (e) {
    e.preventDefault();
    open();
  });

  cancelBtn.addEventListener('click', close);

  overlay.addEventListener('click', function (e) {
    if (e.target === overlay) close();
  });

  confirmBtn.addEventListener('click', function () {
    var checked = overlay.querySelector('input[name="logout-mode"]:checked');
    var mode = checked ? checked.value : 'keep';

    if (mode === 'clear') {
      localStorage.clear();
    } else {
      var savedAccounts = localStorage.getItem(STORAGE_KEY);
      var sidebarCollapsed = localStorage.getItem(COLLAPSED_KEY);
      localStorage.clear();
      if (savedAccounts) localStorage.setItem(STORAGE_KEY, savedAccounts);
      if (sidebarCollapsed) localStorage.setItem(COLLAPSED_KEY, sidebarCollapsed);
    }
    location.href = '/';
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && !overlay.classList.contains('hidden')) close();
  });
})();
