// Snackbar Card — bottom-left expandable notification
// Usage: showSnackbar({ message, items, onView, onLater })

(function () {
  var current = null;

  function Snackbar(options) {
    this.options = options || {};
    this.items = this.options.items || [];
    this.isExpanded = false;
    this.autoDismissTimer = null;
    this.dragStartY = 0;
    this.dragMoved = false;
    this.dragHandled = false;

    this._build();
    this._bindEvents();
    this.show();
  }

  Snackbar.prototype._build = function () {
    var self = this;

    // Root container
    this.el = document.createElement('div');
    this.el.className = 'md-snackbar-card';

    // Header card
    this.headerEl = document.createElement('div');
    this.headerEl.className = 'md-snackbar-card__header';

    // Close button (top-left corner)
    this.closeBtn = document.createElement('button');
    this.closeBtn.className = 'md-snackbar-card__close';
    this.closeBtn.setAttribute('aria-label', '关闭');
    this.closeBtn.innerHTML = '&#215;';
    this.headerEl.appendChild(this.closeBtn);

    // Message
    this.messageEl = document.createElement('div');
    this.messageEl.className = 'md-snackbar-card__message';
    this.messageEl.textContent = this.options.message || '';
    this.headerEl.appendChild(this.messageEl);

    // Drag handle indicator
    var handle = document.createElement('div');
    handle.className = 'md-snackbar-card__drag-handle';
    this.headerEl.appendChild(handle);

    this.el.appendChild(this.headerEl);

    // Expandable body
    this.bodyEl = document.createElement('div');
    this.bodyEl.className = 'md-snackbar-card__body';

    // Student info cards
    for (var i = 0; i < this.items.length; i++) {
      var item = this.items[i];

      var studentCard = document.createElement('div');
      studentCard.className = 'md-snackbar-card__student-card';

      var nameEl = document.createElement('div');
      nameEl.className = 'md-snackbar-card__student-name';
      nameEl.textContent = item.studentName || '';
      studentCard.appendChild(nameEl);

      var metaEl = document.createElement('div');
      metaEl.className = 'md-snackbar-card__student-meta';
      if (item.studentNo) {
        metaEl.innerHTML += '<span>学号: ' + escapeHtml(item.studentNo) + '</span>';
      }
      if (item.className) {
        metaEl.innerHTML += '<span>班级: ' + escapeHtml(item.className) + '</span>';
      }
      if (item.submissionTitle) {
        metaEl.innerHTML += '<span>作业: ' + escapeHtml(item.submissionTitle) + '</span>';
      }
      studentCard.appendChild(metaEl);

      this.bodyEl.appendChild(studentCard);

      // Student note card
      if (item.note) {
        var noteCard = document.createElement('div');
        noteCard.className = 'md-snackbar-card__note-card';

        var noteLabel = document.createElement('div');
        noteLabel.className = 'md-snackbar-card__note-label';
        noteLabel.textContent = '学生留言';
        noteCard.appendChild(noteLabel);

        var noteText = document.createElement('div');
        noteText.className = 'md-snackbar-card__note-text';
        noteText.textContent = item.note;
        noteCard.appendChild(noteText);

        this.bodyEl.appendChild(noteCard);
      }
    }

    // Action buttons
    var actionsEl = document.createElement('div');
    actionsEl.className = 'md-snackbar-card__actions';

    var viewBtn = document.createElement('button');
    viewBtn.className = 'md-btn md-btn--filled md-btn--sm';
    viewBtn.textContent = '查看';
    viewBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      if (self.options.onView) {
        self.options.onView(self.items[0]);
      }
      self.dismiss();
    });
    actionsEl.appendChild(viewBtn);

    var laterBtn = document.createElement('button');
    laterBtn.className = 'md-btn md-btn--text md-btn--sm';
    laterBtn.textContent = '稍后再看';
    laterBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      if (self.options.onLater) {
        self.options.onLater();
      }
      self.dismiss();
    });
    actionsEl.appendChild(laterBtn);

    this.bodyEl.appendChild(actionsEl);
    this.el.appendChild(this.bodyEl);

    document.body.appendChild(this.el);
  };

  Snackbar.prototype._bindEvents = function () {
    var self = this;

    // Close button
    this.closeBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      self.dismiss();
    });

    // Click header to toggle expand
    this.headerEl.addEventListener('click', function () {
      if (!self.dragHandled) {
        self.toggle();
      }
      self.dragHandled = false;
    });

    // Drag to expand/collapse
    this.headerEl.addEventListener('mousedown', function (e) { self._onDragStart(e); });
    this.headerEl.addEventListener('touchstart', function (e) { self._onDragStart(e); }, { passive: true });

    // Hover on the whole snackbar cancels auto-dismiss
    this.el.addEventListener('mouseenter', function () { self._cancelAutoDismiss(); });
    this.el.addEventListener('touchstart', function () { self._cancelAutoDismiss(); }, { passive: true });
  };

  Snackbar.prototype._onDragStart = function (e) {
    var self = this;
    var point = e.touches ? e.touches[0] : e;
    this.dragStartY = point.clientY;
    this.dragMoved = false;
    this.dragHandled = false;

    function onMove(ev) {
      var p = ev.touches ? ev.touches[0] : ev;
      var deltaY = p.clientY - self.dragStartY;

      if (Math.abs(deltaY) > 10) {
        self.dragMoved = true;
      }

      if (Math.abs(deltaY) > 30 && !self.dragHandled) {
        self.dragHandled = true;
        if (deltaY < 0) {
          self.expand();
        } else {
          self.collapse();
        }
      }
    }

    function onUp() {
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onUp);
      document.removeEventListener('touchmove', onMove);
      document.removeEventListener('touchend', onUp);

      // If not moved much, the click handler will fire
      if (!self.dragMoved) {
        self.dragHandled = false;
      }
    }

    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
    document.addEventListener('touchmove', onMove, { passive: true });
    document.addEventListener('touchend', onUp);
  };

  Snackbar.prototype.show = function () {
    var self = this;
    // Force reflow so the enter animation plays
    void this.el.offsetWidth;
    this.el.classList.add('md-snackbar-card--visible');
    this._startAutoDismiss();
  };

  Snackbar.prototype.expand = function () {
    if (this.isExpanded) return;
    this.isExpanded = true;
    this.el.classList.add('md-snackbar-card--expanded');
    this._cancelAutoDismiss();
  };

  Snackbar.prototype.collapse = function () {
    if (!this.isExpanded) return;
    this.isExpanded = false;
    this.el.classList.remove('md-snackbar-card--expanded');
    this._startAutoDismiss();
  };

  Snackbar.prototype.toggle = function () {
    if (this.isExpanded) {
      this.collapse();
    } else {
      this.expand();
    }
  };

  Snackbar.prototype._startAutoDismiss = function () {
    var self = this;
    this._cancelAutoDismiss();
    this.autoDismissTimer = setTimeout(function () {
      self.dismiss();
    }, 5000);
  };

  Snackbar.prototype._cancelAutoDismiss = function () {
    if (this.autoDismissTimer) {
      clearTimeout(this.autoDismissTimer);
      this.autoDismissTimer = null;
    }
  };

  Snackbar.prototype.dismiss = function () {
    if (this._dismissed) return;
    this._dismissed = true;
    this._cancelAutoDismiss();

    var self = this;
    this.el.classList.add('md-snackbar-card--hiding');
    this.el.addEventListener('transitionend', function () {
      if (self.el.parentNode) {
        self.el.remove();
      }
      if (current === self) {
        current = null;
      }
    }, { once: true });

    // Fallback: remove after timeout if transitionend doesn't fire
    setTimeout(function () {
      if (self.el.parentNode) {
        self.el.remove();
      }
      if (current === self) {
        current = null;
      }
    }, 500);
  };

  function escapeHtml(v) {
    return String(v ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;');
  }

  // Public API
  window.showSnackbar = function (options) {
    // Replace existing snackbar if present
    if (current) {
      current.dismiss();
    }
    current = new Snackbar(options);
    return current;
  };

  window.dismissSnackbar = function () {
    if (current) {
      current.dismiss();
    }
  };
})();
