import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import './styles/global.scss';

const app = createApp(App);
app.use(createPinia());
app.use(ElementPlus, { size: 'default' });
app.use(router);
app.mount('#app');
