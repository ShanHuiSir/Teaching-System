function initAssignmentPage(mode) {
  var statusEl = document.getElementById('page-status');
  var banner = document.getElementById('summary-banner');
  var tbody = document.getElementById('table-body');
  var form = document.getElementById('work-form');
  var select = document.getElementById('student-select');
  var lastReloadAt = 0;
  var loadSeq = 0;
  if (!tbody) return;

  function escapeHtml(v) {
    return String(v ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#039;');
  }

  function setStatus(msg, err) { statusEl.textContent = msg; statusEl.classList.toggle('error-text', !!err); }

  function notifyAssignmentChanged(reason) {
    try {
      localStorage.setItem('assignment-data-version', JSON.stringify({
        reason: reason,
        time: Date.now()
      }));
    } catch(e) {}
  }

  function reloadSoon() {
    var now = Date.now();
    if (now - lastReloadAt < 800) return;
    lastReloadAt = now;
    loadData();
  }

  // Load students into select
  async function loadStudents() {
    try {
      var res = await fetch('/api/students?_=' + Date.now(), { cache: 'no-store' });
      if (!res.ok) return;
      var students = await res.json();
      select.innerHTML = '<option value="">请选择学生</option>' + students.map(function(s) {
        return '<option value="' + s.id + '">' + escapeHtml(s.studentNo) + ' - ' + escapeHtml(s.name) + '</option>';
      }).join('');
    } catch(e) {}
  }

  // Load filtered submissions + evaluation status
  async function loadData() {
    var currentSeq = ++loadSeq;
    setStatus('正在加载...');
    try {
      var sRes = await fetch('/api/submissions?_=' + Date.now(), { cache: 'no-store' });
      if (!sRes.ok) throw new Error('加载失败');
      var submissions = await sRes.json();

      var evalMap = {};
      for (var i = 0; i < submissions.length; i++) {
        try {
          var er = await fetch('/api/submissions/' + submissions[i].id + '/evaluation?_=' + Date.now(), { cache: 'no-store' });
          if (er.ok) evalMap[submissions[i].id] = await er.json();
        } catch(e) {}
      }
      if (currentSeq !== loadSeq) return;

      var filtered = [];
      for (var j = 0; j < submissions.length; j++) {
        var ev = evalMap[submissions[j].id];
        var s = ev ? ev.status : 0;
        if (mode === 'pending' && s === 0) filtered.push(submissions[j]);
        else if (mode === 'ai-reviewed' && s === 1) filtered.push(submissions[j]);
        else if (mode === 'completed' && s === 2) filtered.push(submissions[j]);
      }

      if (mode === 'pending') banner.textContent = '当前有 ' + filtered.length + ' 份作业待审批';
      else if (mode === 'ai-reviewed') banner.textContent = '当前有 ' + filtered.length + ' 份作业待教师复核';
      else banner.textContent = '当前有 ' + filtered.length + ' 份作业已完成';

      if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7">暂无数据</td></tr>';
      } else {
        tbody.innerHTML = filtered.map(function(sub) {
          var ev = evalMap[sub.id] || {};
          if (mode === 'pending') {
            return '<tr><td>' + escapeHtml(sub.studentName) + '</td><td>' + escapeHtml(sub.title) + '</td><td>' + escapeHtml(sub.fileName) + '</td><td>' + escapeHtml(sub.workType) + '</td><td>' + escapeHtml((sub.submittedAt||'').replace('T',' ').slice(0,16)) + '</td><td><a class="md-btn md-btn--tonal md-btn--sm" href="/evaluation?submissionId=' + sub.id + '&studentId=' + sub.studentId + '&studentName=' + encodeURIComponent(sub.studentName) + '&fileName=' + encodeURIComponent(sub.fileName) + '">审批</a></td></tr>';
          } else if (mode === 'ai-reviewed') {
            return '<tr><td>' + escapeHtml(sub.studentName) + '</td><td>' + escapeHtml(sub.title) + '</td><td>' + escapeHtml(sub.fileName) + '</td><td>' + escapeHtml(sub.workType) + '</td><td>' + (ev.aiScore!=null?ev.aiScore:'--') + '</td><td>' + escapeHtml((sub.submittedAt||'').replace('T',' ').slice(0,16)) + '</td><td><a class="md-btn md-btn--tonal md-btn--sm" href="/evaluation?submissionId=' + sub.id + '&studentId=' + sub.studentId + '&studentName=' + encodeURIComponent(sub.studentName) + '&fileName=' + encodeURIComponent(sub.fileName) + '">复核</a></td></tr>';
          } else {
            return '<tr><td>' + escapeHtml(sub.studentName) + '</td><td>' + escapeHtml(sub.title) + '</td><td>' + escapeHtml(sub.fileName) + '</td><td>' + escapeHtml(sub.workType) + '</td><td>' + (ev.teacherScore!=null?ev.teacherScore:'--') + '</td><td>' + escapeHtml((sub.submittedAt||'').replace('T',' ').slice(0,16)) + '</td><td><a class="md-btn md-btn--tonal md-btn--sm" href="/evaluation?submissionId=' + sub.id + '&studentId=' + sub.studentId + '&studentName=' + encodeURIComponent(sub.studentName) + '&fileName=' + encodeURIComponent(sub.fileName) + '">查看</a></td></tr>';
          }
        }).join('');
      }
      setStatus('已加载 ' + filtered.length + ' 条');
    } catch(e) {
      setStatus(e.message, true);
    }
  }

  // Form submit
  if (form) {
    form.addEventListener('submit', async function(e) {
      e.preventDefault();
      var data = Object.fromEntries(new FormData(form));
      data.studentId = Number(data.studentId);
      try {
        var res = await fetch('/api/submissions', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });
        var result = await res.json().catch(function() { return {}; });
        if (!res.ok) throw new Error(result.message || '保存失败');
        form.reset();
        setStatus('已保存：' + result.title);
        notifyAssignmentChanged('submission-created');
        await loadData();
      } catch(err) {
        setStatus(err.message, true);
      }
    });
  }

  window.addEventListener('storage', function(event) {
    if (event.key === 'assignment-data-version') reloadSoon();
  });
  window.addEventListener('focus', reloadSoon);
  window.addEventListener('pageshow', reloadSoon);
  document.addEventListener('visibilitychange', function() {
    if (!document.hidden) reloadSoon();
  });
  window.setInterval(function() {
    if (!document.hidden) reloadSoon();
  }, 3000);

  loadStudents();
  loadData();
}
