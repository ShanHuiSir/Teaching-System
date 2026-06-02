import { ref, watch, onBeforeUnmount, readonly, type Ref } from 'vue';

interface PollingOptions {
  interval?: number;
  immediate?: boolean;
  enabled?: Ref<boolean>;
}

export default function usePolling(
  callback: () => void | Promise<void>,
  { interval = 5000, immediate = true, enabled = ref(true) }: PollingOptions = {},
) {
  const MIN_INTERVAL = 1000;
  const currentInterval = ref(Math.max(interval, MIN_INTERVAL));
  const isActive = ref(false);
  let timer: ReturnType<typeof setInterval> | null = null;

  // ── Guard: never allow < 1000ms ──
  if (interval < MIN_INTERVAL) {
    console.warn(`[usePolling] interval ${interval}ms clamped to minimum ${MIN_INTERVAL}ms`);
    currentInterval.value = MIN_INTERVAL;
  }

  function start() {
    stop();
    if (immediate && isActive.value) {
      safeCall();
    }
    timer = setInterval(() => {
      if (!document.hidden && isActive.value) {
        safeCall();
      }
    }, currentInterval.value);
  }

  function safeCall() {
    Promise.resolve(callback()).catch((e) => {
      console.error('[usePolling] callback error:', e);
    });
  }

  function pause() {
    isActive.value = false;
    if (timer) { clearInterval(timer); timer = null; }
  }

  function resume() {
    isActive.value = true;
    start();
  }

  function stop() {
    if (timer) { clearInterval(timer); timer = null; }
  }

  // ── Browser-tab visibility: skip inactive, fire on return ──
  function onVisibility() {
    if (!document.hidden && isActive.value) {
      safeCall();
    }
  }
  document.addEventListener('visibilitychange', onVisibility);

  // ── React to enabled ref ──
  watch(() => enabled.value, (val) => {
    if (val) resume(); else pause();
  }, { immediate: true });

  // ── React to dynamic interval changes ──
  watch(currentInterval, () => {
    if (isActive.value) start();
  });

  // ── Cleanup ──
  onBeforeUnmount(() => {
    stop();
    document.removeEventListener('visibilitychange', onVisibility);
  });

  return {
    pause,
    resume,
    stop,
    isActive: readonly(isActive),
    interval: currentInterval, // writable: page can adjust this at runtime
  };
}
