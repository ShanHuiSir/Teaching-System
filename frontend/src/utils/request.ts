import axios, { type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor
instance.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error),
);

// Response interceptor — unwrap axios response, handle errors centrally
instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const msg: string | undefined = error.response?.data?.message;
    if (msg) {
      ElMessage.error(msg);
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('网络异常，请检查连接');
    } else if (error.response?.status === 404 || error.response?.status === 500) {
      ElMessage.error('服务暂时不可用，请稍后重试');
    } else {
      ElMessage.error('请求失败，请稍后重试');
    }
    return Promise.reject(error);
  },
);

// Typed wrapper: strips AxiosResponse so callers get T directly
export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return instance.get<any, T>(url, config);
}

export async function post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return instance.post<any, T>(url, data, config);
}

export async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return instance.delete<any, T>(url, config);
}

export default instance;
