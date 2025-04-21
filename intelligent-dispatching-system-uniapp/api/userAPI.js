
import { request } from './request';

// 用户相关接口
export function login(data) {
  return request('/login/login', 'POST', data);
}

// 注册接口
export function register(data) {
  return request('/login/register', 'POST', data);
}

// 获取用户信息
export function getUserInfo() {
  return request('/user/info', 'GET');
}

// 更新用户信息
export function updateUserInfo(data) {
  return request('/user/update', 'POST', data);
}

// 修改密码
export function changePassword(data) {
  return request('/user/password', 'POST', data);
}

//获取保修类型
export function getRepairT() {
  return request('/user/getRepairType','GET');
}