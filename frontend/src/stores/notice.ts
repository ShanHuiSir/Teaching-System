import { ref } from 'vue';
import { defineStore } from 'pinia';

export type NoticeType = 'info' | 'success' | 'warning' | 'error' | 'pending' | 'ai' | 'done';

export const useNoticeStore = defineStore('notice', () => {
  const visible = ref(false);
  const message = ref('');
  const type = ref<NoticeType>('info');
  const actionText = ref('');
  let actionCallback: (() => void) | null = null;
  let timer: ReturnType<typeof setTimeout> | null = null;

  function show(msg: string, opts?: { type?: NoticeType; actionText?: string; action?: () => void; duration?: number }) {
    if (timer) clearTimeout(timer);
    message.value = msg;
    type.value = opts?.type ?? 'info';
    actionText.value = opts?.actionText ?? '';
    actionCallback = opts?.action ?? null;
    visible.value = true;
    if (opts?.duration) {
      timer = setTimeout(close, opts.duration);
    }
  }

  function close() {
    visible.value = false;
    if (timer) { clearTimeout(timer); timer = null; }
  }

  function fireAction() {
    if (actionCallback) actionCallback();
    close();
  }

  function success(msg: string) { show(msg, { type: 'success' }); }
  function warning(msg: string) { show(msg, { type: 'warning' }); }
  function error(msg: string) { show(msg, { type: 'error' }); }
  function info(msg: string) { show(msg, { type: 'info' }); }

  return { visible, message, type, actionText, show, close, fireAction, success, warning, error, info };
});
