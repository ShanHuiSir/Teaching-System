// Shared Toast utility

(function() {
  var container = document.createElement('div');
  container.className = 'toast-container';
  document.body.appendChild(container);

  window.showToast = function(message, type) {
    type = type || 'info';
    var toast = document.createElement('div');
    toast.className = 'toast toast--' + type;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(function() { toast.remove(); }, 3000);
  };

  window.showError = function(message) { showToast(message, 'error'); };
  window.showSuccess = function(message) { showToast(message, 'success'); };

  // Extract error message from fetch response
  window.extractError = async function(res) {
    try { var data = await res.json(); return data.message || data.error || '操作失败'; }
    catch(e) { return '请求失败 (HTTP ' + res.status + ')'; }
  };
})();
