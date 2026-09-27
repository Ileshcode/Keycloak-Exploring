import {UserManager, WebStorageStateStore} from "oidc-client-ts";

export  const userManager = new UserManager({
    authority: import.meta.env.VITE_KEYCLOAK_AUTHORITY,
    client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
    automaticSilentRenew: true,
    redirect_uri: import.meta.env.VITE_REDIRECT_URI,
    userStore: new WebStorageStateStore({store: window.sessionStorage})
})
