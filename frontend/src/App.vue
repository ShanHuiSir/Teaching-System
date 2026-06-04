<template>
  <AppNotice
    v-if="notice.visible"
    :message="notice.message"
    :type="notice.type"
    :action-text="notice.actionText"
    @close="notice.close()"
    @action="notice.fireAction()"
  />
  <router-view />
</template>

<script setup lang="ts">
import { useNoticeStore } from '@/stores/notice';
import AppNotice from '@/components/AppNotice.vue';

const notice = useNoticeStore();
</script>
