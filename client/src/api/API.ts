import * as axios from "axios";
import {userManager} from "../auth/UserManager.ts";


export const API = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    withXSRFToken:true,
})

API.interceptors.request.use(async (config) => {
    const user = await userManager.getUser();
    if(user?.access_token) {
        config.headers.Authorization = `Bearer ${user.access_token}`
    }

    return config;
})


