function initAssignmentPage(mode) {
  var statusEl = document.getElementById('page-status');
  var banner = document.getElementById('summary-banner');
  var tbody = document.getElementById('table-body');
  var form = document.getElementById('work-form');
  var select = document.getElementById('student-select');
  var submitBtn = form ? form.querySelector('button[type="submit"]') : null;
  var submitBtnText = submitBtn ? submitBtn.textContent : '保存作业提交';
  if (!tbody) return;

  function escapeHtml(v) {
    return String(v ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#039;');
  }

  function setStatus(msg, err) { statusEl.textContent = msg; statusEl.classList.toggle('error-text', !!err); }

  // Load students into select
  async function loadStudents() {
    try {
      var res = await fetch('/api/students');
      if (!res.ok) return;
      var students = await res.json();
      select.innerHTML = '<option value="">请选择学生</option>' + students.map(function(s) {
        return '<option value="' + s.id + '">' + escapeHtml(s.studentNo) + ' - ' + escapeHtml(s.name) + '</option>';
      }).join('');
    } catch(e) {}
  }

  // Empty state HTML
  function emptyState() {
    var icons = { 'pending': '&#128203;', 'ai-reviewed': '&#129302;', 'completed': '&#9989;' };
    var texts = { 'pending': '暂无待审批作业', 'ai-reviewed': '暂无AI已审批作业', 'completed': '暂无已完成作业' };
    return '<tr><td colspan="7"><div class="md-empty-state"><div class="md-empty-state__icon">' + (icons[mode] || '&#128196;') + '</div><div class="md-empty-state__text">' + (texts[mode] || '暂无数据') + '</div></div></td></tr>';
  }

  // Load filtered submissions + evaluation status
  async function loadData() {
    setStatus('正在加载...');
    try {
      var sRes = await fetch('/api/submissions');
      if (!sRes.ok) { var msg = await extractError(sRes); throw new Error(msg); }
      var submissions = await sRes.json();

      var evalMap = {};
      for (var i = 0; i < submissions.length; i++) {
        try {
          var er = await fetch('/api/submissions/' + submissions[i].id + '/evaluation');
          if (er.ok) evalMap[submissions[i].id] = await er.json();
        } catch(e) {}
      }

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
        tbody.innerHTML = emptyState();
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
      showError(e.message);
    }
  }

  // Form submit with anti-duplicate
  if (form) {
    form.addEventListener('submit', async function(e) {
      e.preventDefault();
      if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = '保存中...'; }
      var data = Object.fromEntries(new FormData(form));
      data.studentId = Number(data.studentId);
      try {
        var res = await fetch('/api/submissions', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
        if (!res.ok) { var err = await extractError(res); throw new Error(err); }
        var result = await res.json();
        form.reset();
        setStatus('已保存：' + result.title);
        showSuccess('已保存作业：' + result.title);
        await loadData();
      } catch(err) {
        setStatus(err.message, true);
        showError(err.message);
      } finally {
        if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = submitBtnText; }
      }
    });
  }

  loadStudents();
  loadData();
}
