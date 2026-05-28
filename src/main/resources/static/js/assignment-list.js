function initAssignmentPage(mode) {
  var tbody = document.getElementById('table-body');
  var statusEl = document.getElementById('page-status');
  var banner = document.getElementById('summary-banner');
  if (!tbody) return;

  function escapeHtml(v) {
    return String(v ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#039;');
  }

  function setStatus(msg, err) { statusEl.textContent = msg; statusEl.classList.toggle('error-text', !!err); }

  function filterByMode(submission, evalMap) {
    var ev = evalMap[submission.id];
    var status = ev ? ev.status : 0;
    if (mode === 'pending') return status === 0;
    if (mode === 'ai-reviewed') return status === 1;
    if (mode === 'completed') return status === 2;
    return false;
  }

  async function load() {
    setStatus('正在加载...');
    try {
      var sRes = await fetch('/api/submissions');
      if (!sRes.ok) throw new Error('作业提交记录加载失败');
      var submissions = await sRes.json();

      var evalMap = {};
      var evalRes = await fetch('/api/submissions');
      var allSubs = await evalRes.json();
      for (var i = 0; i < allSubs.length; i++) {
        try {
          var er = await fetch('/api/submissions/' + allSubs[i].id + '/evaluation');
          if (er.ok) evalMap[allSubs[i].id] = await er.json();
        } catch(e) {}
      }

      var filtered = [];
      for (var j = 0; j < submissions.length; j++) {
        if (filterByMode(submissions[j], evalMap)) filtered.push(submissions[j]);
      }

      if (mode === 'pending') {
        banner.textContent = '当前有 ' + filtered.length + ' 份作业待审批';
      } else if (mode === 'ai-reviewed') {
        banner.textContent = '当前有 ' + filtered.length + ' 份作业待教师复核';
      } else {
        banner.textContent = '当前有 ' + filtered.length + ' 份作业已完成';
      }

      if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7">暂无数据</td></tr>';
      } else {
        var cols;
        if (mode === 'pending') cols = 6;
        else cols = 7;
        tbody.innerHTML = filtered.map(function(sub) {
          var ev = evalMap[sub.id] || {};
          if (mode === 'pending') {
            return '<tr><td>' + escapeHtml(sub.studentName) + '</td><td>' + escapeHtml(sub.title) + '</td><td>' + escapeHtml(sub.fileName) + '</td><td>' + escapeHtml(sub.workType) + '</td><td>' + escapeHtml((sub.submittedAt || '').replace('T',' ').slice(0,16)) + '</td><td><a class="md-btn md-btn--tonal md-btn--sm" href="/evaluation?submissionId=' + sub.id + '&studentId=' + sub.studentId + '&studentName=' + encodeURIComponent(sub.studentName) + '&fileName=' + encodeURIComponent(sub.fileName) + '">审批</a></td></tr>';
          } else if (mode === 'ai-reviewed') {
            return '<tr><td>' + escapeHtml(sub.studentName) + '</td><td>' + escapeHtml(sub.title) + '</td><td>' + escapeHtml(sub.fileName) + '</td><td>' + escapeHtml(sub.workType) + '</td><td>' + (ev.aiScore != null ? ev.aiScore : '--') + '</td><td>' + escapeHtml((sub.submittedAt || '').replace('T',' ').slice(0,16)) + '</td><td><a class="md-btn md-btn--tonal md-btn--sm" href="/evaluation?submissionId=' + sub.id + '&studentId=' + sub.studentId + '&studentName=' + encodeURIComponent(sub.studentName) + '&fileName=' + encodeURIComponent(sub.fileName) + '">复核</a></td></tr>';
          } else {
            return '<tr><td>' + escapeHtml(sub.studentName) + '</td><td>' + escapeHtml(sub.title) + '</td><td>' + escapeHtml(sub.fileName) + '</td><td>' + escapeHtml(sub.workType) + '</td><td>' + (ev.teacherScore != null ? ev.teacherScore : '--') + '</td><td>' + escapeHtml((sub.submittedAt || '').replace('T',' ').slice(0,16)) + '</td><td><a class="md-btn md-btn--tonal md-btn--sm" href="/evaluation?submissionId=' + sub.id + '&studentId=' + sub.studentId + '&studentName=' + encodeURIComponent(sub.studentName) + '&fileName=' + encodeURIComponent(sub.fileName) + '">查看</a></td></tr>';
          }
        }).join('');
      }
      setStatus('已加载 ' + filtered.length + ' 条');
    } catch(e) {
      setStatus(e.message, true);
    }
  }

  load();
}
