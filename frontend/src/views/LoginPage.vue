<template>
  <div class="login-page" @click="onPageClick">
    <div class="starfield">
      <div v-for="s in stars" :key="s.id" class="star" :style="s.style" />
    </div>
    <div class="nebula nebula--1" />
    <div class="nebula nebula--2" />

    <div class="login-card">
      <div class="login-card__accent" />
      <div class="login-card__glow" />
      <div class="login-card__content">
        <div class="brand">
          <svg class="brand__icon" viewBox="0 0 48 48" fill="none">
            <rect x="4" y="4" width="40" height="40" rx="10" stroke="currentColor" stroke-width="2" />
            <path d="M14 20L24 10L34 20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M18 24V34H30V24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <circle cx="24" cy="28" r="3" fill="currentColor" />
          </svg>
          <h1 class="brand__title">教学评价系统</h1>
          <p class="brand__sub">Teaching Evaluation System</p>
        </div>

        <!-- Quick login -->
        <div v-if="savedAccounts.length" class="quick-login">
          <span class="quick-login__label">快捷登录</span>
          <div class="quick-login__dropdown" ref="dropdownRef">
            <button class="quick-login__trigger" type="button" @click.stop="toggleDropdown">
              <span class="quick-login__trigger-text">{{ selectedAccount || '选择已保存的账号' }}</span>
              <svg class="quick-login__arrow" :class="{ 'quick-login__arrow--open': dropdownOpen }" viewBox="0 0 24 24" width="20" height="20"><path d="M7 10l5 5 5-5z" fill="currentColor"/></svg>
            </button>
            <div v-if="dropdownOpen" class="quick-login__menu">
              <div
                v-for="acc in savedAccounts"
                :key="acc"
                class="quick-login__item"
                @click.stop="selectAccount(acc)"
              >
                <span class="quick-login__avatar">{{ acc.charAt(0).toUpperCase() }}</span>
                <span class="quick-login__name">{{ acc }}</span>
                <button class="quick-login__remove" type="button" @click.stop="removeAccount(acc)">&times;</button>
              </div>
            </div>
          </div>
        </div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="input-group">
            <label class="input-group__label">用户名</label>
            <div class="input-group__wrap">
              <svg class="input-group__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="8" r="4" /><path d="M6 20v-1a6 6 0 0 1 12 0v1" />
              </svg>
              <input v-model="username" class="input-group__field" placeholder="请输入用户名" autocomplete="username" />
            </div>
          </div>
          <div class="input-group">
            <label class="input-group__label">密码</label>
            <div class="input-group__wrap">
              <svg class="input-group__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <input v-model="password" type="password" class="input-group__field" placeholder="请输入密码" autocomplete="current-password" />
            </div>
          </div>
          <label class="remember-row">
            <input v-model="rememberMe" type="checkbox" class="remember-check" />
            <span>记住账号</span>
          </label>
          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else class="spinner" />
          </button>
        </form>

        <p v-if="errorMsg" class="login-error">{{ errorMsg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { setCookie } from '@/utils/cookie';
import { ElMessage } from 'element-plus';

const STORAGE_KEY = 'saved-accounts';
const router = useRouter();
const username = ref('');
const password = ref('');
const loading = ref(false);
const errorMsg = ref('');
const rememberMe = ref(true);
const savedAccounts = ref<string[]>([]);
const selectedAccount = ref('');
const dropdownOpen = ref(false);
const dropdownRef = ref<HTMLElement | null>(null);

function starStyle(i: number) {
  const x = ((i * 137.5 + i * i * 7.3) % 100);
  const y = ((i * 263.1 + i * i * 3.7) % 100);
  const size = (i % 3) + 1;
  const dur = 2 + (i % 4) * 1.5;
  const delay = (i % 10) * 0.3;
  return {
    left: `${x}%`,
    top: `${y}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDuration: `${dur}s`,
    animationDelay: `${delay}s`,
  };
}

const stars = Array.from({ length: 80 }, (_, i) => ({ id: i, style: starStyle(i) }));

function loadAccounts() {
  try { savedAccounts.value = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]'); }
  catch { savedAccounts.value = []; }
}

function saveAccount(name: string) {
  const accounts = savedAccounts.value.filter(a => a !== name);
  accounts.unshift(name);
  if (accounts.length > 5) accounts.pop();
  savedAccounts.value = accounts;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(accounts));
}

function removeAccount(name: string) {
  savedAccounts.value = savedAccounts.value.filter(a => a !== name);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(savedAccounts.value));
  if (selectedAccount.value === name) selectedAccount.value = '';
}

function selectAccount(name: string) {
  username.value = name;
  password.value = '123456';
  selectedAccount.value = name;
  dropdownOpen.value = false;
}

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value;
}

function onPageClick() {
  dropdownOpen.value = false;
}

async function handleLogin() {
  errorMsg.value = '';
  if (!username.value.trim() || !password.value.trim()) {
    errorMsg.value = '请输入用户名和密码';
    return;
  }
  loading.value = true;
  await new Promise(r => setTimeout(r, 800));
  if (rememberMe.value) saveAccount(username.value.trim());
  setCookie('access_token', 'simulated-token');
  setCookie('user_info', JSON.stringify({ name: username.value }));
  loading.value = false;
  ElMessage.success('登录成功');
  router.replace('/class-selection');
}

onMounted(loadAccounts);
</script>

<style lang="scss" scoped>
.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0e1a 0%, #0d1528 30%, #111d3a 60%, #0a1628 100%);
  overflow: hidden;
}

.starfield { position: absolute; inset: 0; pointer-events: none; }
.star {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  animation: twinkle 3s ease-in-out infinite alternate;
}
@keyframes twinkle {
  0% { opacity: .2; transform: scale(1); }
  100% { opacity: .9; transform: scale(1.5); }
}

.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  pointer-events: none;
  &--1 {
    width: 500px; height: 500px;
    background: radial-gradient(circle, rgba(26,86,219,.12) 0%, transparent 70%);
    top: -120px; right: -100px;
    animation: drift1 12s ease-in-out infinite alternate;
  }
  &--2 {
    width: 400px; height: 400px;
    background: radial-gradient(circle, rgba(13,148,136,.08) 0%, transparent 70%);
    bottom: -80px; left: -60px;
    animation: drift2 15s ease-in-out infinite alternate;
  }
}
@keyframes drift1 { 0% { transform: translate(0, 0); } 100% { transform: translate(-40px, 30px); } }
@keyframes drift2 { 0% { transform: translate(0, 0); } 100% { transform: translate(30px, -20px); } }

.login-card {
  position: relative;
  width: 420px;
  background: rgba(255,255,255,.04);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,.08);
  border-radius: $shape-xl;
  overflow: hidden;
  z-index: 1;

  &__accent {
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(26,86,219,.6), rgba(13,148,136,.4), transparent);
  }
  &__glow {
    position: absolute;
    top: -60px; left: 50%; transform: translateX(-50%);
    width: 200px; height: 80px;
    background: radial-gradient(ellipse, rgba(26,86,219,.15) 0%, transparent 70%);
    pointer-events: none;
  }
  &__content { position: relative; padding: $space-8 $space-8 $space-10; }
}

.brand {
  text-align: center;
  margin-bottom: $space-6;
  &__icon { width: 56px; height: 56px; color: rgba(26,86,219,.8); margin-bottom: $space-4; }
  &__title { @include font(24px, 32px, 600); color: #e8edf5; letter-spacing: .04em; }
  &__sub { @include font(12px, 18px); color: rgba(255,255,255,.35); letter-spacing: .12em; text-transform: uppercase; margin-top: $space-1; }
}

// Quick login dropdown
.quick-login {
  margin-bottom: $space-5;
  &__label { @include font(12px, 18px, 500); color: rgba(255,255,255,.4); display: block; margin-bottom: $space-2; }
  &__dropdown { position: relative; }
  &__trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    height: 40px;
    padding: 0 12px;
    border: 1px solid rgba(255,255,255,.1);
    border-radius: $shape-sm;
    background: rgba(255,255,255,.04);
    color: rgba(255,255,255,.5);
    cursor: pointer;
    @include font(13px, 20px);
    transition: border-color .15s;
    &:hover { border-color: rgba(255,255,255,.2); }
  }
  &__trigger-text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  &__arrow {
    width: 18px; height: 18px;
    color: rgba(255,255,255,.3);
    flex-shrink: 0;
    transition: transform .2s;
    &--open { transform: rotate(180deg); }
  }
  &__menu {
    position: absolute;
    top: calc(100% + 4px);
    left: 0; right: 0;
    background: rgba(15,20,40,.95);
    border: 1px solid rgba(255,255,255,.1);
    border-radius: $shape-sm;
    overflow: hidden;
    z-index: 10;
  }
  &__item {
    display: flex;
    align-items: center;
    gap: $space-2;
    height: 40px;
    padding: 0 12px;
    cursor: pointer;
    transition: background .15s;
    &:hover { background: rgba(26,86,219,.12); }
  }
  &__avatar {
    width: 26px; height: 26px;
    border-radius: 50%;
    background: rgba(26,86,219,.25);
    color: rgba(255,255,255,.7);
    display: grid;
    place-items: center;
    @include font(12px, 16px, 600);
    flex-shrink: 0;
  }
  &__name {
    flex: 1;
    @include font(13px, 20px);
    color: rgba(255,255,255,.7);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  &__remove {
    width: 22px; height: 22px;
    border: none;
    border-radius: 50%;
    background: transparent;
    color: rgba(255,255,255,.3);
    cursor: pointer;
    @include font(16px, 22px);
    display: grid;
    place-items: center;
    flex-shrink: 0;
    &:hover { background: rgba(220,38,38,.2); color: #EF4444; }
  }
}

// Form
.login-form {
  display: flex;
  flex-direction: column;
  gap: $space-5;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: $space-2;
  &__label { @include font(13px, 20px, 500); color: rgba(255,255,255,.5); }
  &__wrap {
    display: flex;
    align-items: center;
    gap: $space-3;
    height: 44px;
    padding: 0 14px;
    background: rgba(255,255,255,.04);
    border: 1px solid rgba(255,255,255,.1);
    border-radius: $shape-sm;
    transition: border-color .2s ease, box-shadow .2s ease;
    &:focus-within {
      border-color: rgba(26,86,219,.5);
      box-shadow: 0 0 0 3px rgba(26,86,219,.1);
    }
  }
  &__icon { width: 18px; height: 18px; color: rgba(255,255,255,.3); flex-shrink: 0; }
  &__field {
    flex: 1;
    background: transparent;
    border: none;
    outline: none;
    color: #e8edf5;
    @include font(14px, 20px);
    &::placeholder { color: rgba(255,255,255,.2); }
  }
}

.remember-row {
  display: flex;
  align-items: center;
  gap: $space-2;
  cursor: pointer;
  @include font(13px, 20px);
  color: rgba(255,255,255,.45);
}
.remember-check {
  accent-color: $primary;
  width: 14px; height: 14px;
}

.login-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  border: none;
  border-radius: $shape-sm;
  background: linear-gradient(135deg, $primary, #2563EB);
  color: #fff;
  @include font(15px, 20px, 500);
  letter-spacing: .3em;
  cursor: pointer;
  transition: all .2s ease;
  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #2563EB, #3B82F6);
    box-shadow: 0 0 24px rgba(26,86,219,.3);
  }
  &:disabled { opacity: .5; cursor: not-allowed; }
}

.login-error {
  text-align: center;
  margin-top: $space-4;
  @include font(13px, 20px);
  color: $error;
}
</style>
