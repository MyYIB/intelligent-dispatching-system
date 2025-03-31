import { defineStore } from 'pinia';
export const useUserStore = defineStore( 'user',{
    state: () => ({
        token: sessionStorage.getItem('token') || '',
        user: JSON.parse(sessionStorage.getItem('user')) || {},
        currentPathName: localStorage.getItem('currentPathName') || ''
    }),
    actions: {
        setToken(token) {
            this.token = token;
            sessionStorage.setItem('token', token);
        },
        setUser(user) {
            this.user = user;
            sessionStorage.setItem('user', JSON.stringify(user));
        },
        removeInfo() {
            this.token = '';
            this.user = {};
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('user');
        },
        logout() {
            this.token = '';
            this.user = null;
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('user');
        },
        setPathName(name) {
            this.currentPathName = name;
            localStorage.setItem('currentPathName', name);
        }
    }
});
