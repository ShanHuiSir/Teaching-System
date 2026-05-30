function initAssignmentPage(mode) {
  var EvaluationStatus = {
    PENDING: 0,
    AI_REVIEWED: 1,
    TEACHER_CONFIRMED: 2
  };
  var statusEl = document.getElementById('page-status');
  var banner = document.getElementById('summary-banner');
  var tbody = document.getElementById('table-body');
  var form = document.getElementById('work-form');
  var select = document.getElementById('student-select');
  var submitBtn = form ? form.querySelector('button[type="submit"]') : null;
  var submitBtnText = submitBtn ? submitBtn.textContent : '保存作业提交';
  var lastReloadAt = 0;
  var loadSeq = 0;
  var lastNotifiedCount = -1;
  var studentCache = {};
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

  function goToStatusPage(targetMode) {
    window.location.href = '/assignments/' + targetMode + '?_=' + Date.now();
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
      studentCache = {};
      for (var i = 0; i < students.length; i++) {
        studentCache[students[i].id] = students[i];
      }
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
    var currentSeq = ++loadSeq;
    setStatus('正在加载...');
    try {
      var sRes = await fetch('/api/submissions');
      if (!sRes.ok) { var msg = await extractError(sRes); throw new Error(msg); }
      var submissions = await sRes.json();

      var eRes = await fetch('/api/evaluations?_=' + Date.now(), { cache: 'no-store' });
      if (!eRes.ok) throw new Error('评价结果加载失败');
      var evaluations = await eRes.json();

      var evalMap = {};
      for (var i = 0; i < evaluations.length; i++) {
        evalMap[evaluations[i].submissionId] = evaluations[i];
      }
      if (currentSeq !== loadSeq) return;

      var filtered = [];
      for (var j = 0; j < submissions.length; j++) {
        var ev = evalMap[submissions[j].id];
        var s = ev ? ev.status : EvaluationStatus.PENDING;
        if (mode === 'pending' && s === EvaluationStatus.PENDING) filtered.push(submissions[j]);
        else if (mode === 'ai-reviewed' && s === EvaluationStatus.AI_REVIEWED) filtered.push(submissions[j]);
        else if (mode === 'completed' && s === EvaluationStatus.TEACHER_CONFIRMED) filtered.push(submissions[j]);
      }

      if (mode === 'pending') { if (banner) banner.textContent = '当前有 ' + filtered.length + ' 份作业待审批'; }
      else if (mode === 'ai-reviewed') { if (banner) banner.textContent = '当前有 ' + filtered.length + ' 份作业待教师复核'; }
      else { if (banner) banner.textContent = '当前有 ' + filtered.length + ' 份作业已完成'; }

      // Update status pill counts
      var pendingCount = 0, aiCount = 0, doneCount = 0;
      for (var k = 0; k < submissions.length; k++) {
        var s = evalMap[submissions[k].id];
        var st = s ? s.status : EvaluationStatus.PENDING;
        if (st === EvaluationStatus.PENDING) pendingCount++;
        else if (st === EvaluationStatus.AI_REVIEWED) aiCount++;
        else if (st === EvaluationStatus.TEACHER_CONFIRMED) doneCount++;
      }
      var pcP = document.getElementById('pill-count-pending');
      var pcA = document.getElementById('pill-count-ai');
      var pcD = document.getElementById('pill-count-done');
      if (pcP) pcP.textContent = pendingCount;
      if (pcA) pcA.textContent = aiCount;
      if (pcD) pcD.textContent = doneCount;

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
      if (mode === 'pending' || mode === 'ai-reviewed') {
        if (filtered.length === 0) lastNotifiedCount = 0;
        if (filtered.length > 0 && filtered.length > lastNotifiedCount) {
          var newItems = [];
          for (var n = 0; n < Math.min(filtered.length, 3); n++) {
            var sub = filtered[n];
            var stu = studentCache[sub.studentId] || {};
            newItems.push({
              studentName: sub.studentName,
              studentNo: stu.studentNo || '',
              className: stu.className || '',
              submissionTitle: sub.title,
              note: sub.remark || '',
              submissionId: sub.id,
              studentId: sub.studentId
            });
          }
          var msg = filtered.length + '份新作业' + (mode === 'pending' ? '待审批' : '待复核');
          showSnackbar({
            message: msg,
            items: newItems,
            onView: function(item) {
              if (item && item.submissionId) {
                window.location.href = '/evaluation?submissionId=' + item.submissionId + '&studentId=' + item.studentId + '&studentName=' + encodeURIComponent(item.studentName) + '&fileName=' + encodeURIComponent(filtered[0].fileName || '');
              }
            }
          });
        }
        lastNotifiedCount = filtered.length;
      }
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
