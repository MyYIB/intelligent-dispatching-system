import { request } from './request';

// 更新员工位置和状态
export function updateEmployeeLocation(data) {
    return request('/employee/updateLocation', 'POST', data);
}

// 获取员工信息
export function getEmployeeInfo(employeeId) {
  return request(`/employee/getEmployeeInfo?employeeId=${employeeId}`, 'GET');
}