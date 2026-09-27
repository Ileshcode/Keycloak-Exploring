import {createBrowserRouter, Navigate, Outlet, RouterProvider} from "react-router-dom";
import RegistrationPage from "./pages/RegistrationPage.tsx";
import LoginPage from "./pages/LoginPage.tsx";
import HomePage from "./pages/HomePage.tsx";
import SecuredPage from "./pages/SecuredPage.tsx";
import {AuthProvider, useAuth} from "react-oidc-context";
import {userManager} from "./auth/UserManager.ts";
import {Box, Typography} from "@mui/material";

export default function App() {


    const router = createBrowserRouter([
        {
            element: <MainLayout/>, children: [
                {element: <HomePage/>, path: '/'},
                {element: <RegistrationPage/>, path: '/registration'},
                {element: <LoginPage/>, path: '/login'},
                {
                    element: <SecuredLayout/>, children: [
                        {element: <SecuredPage/>, path: '/secured'}
                    ]
                }
            ]
        }
    ])

    return (
        <AuthProvider userManager={userManager} onSigninCallback={() => {
            window.history.replaceState({}, document.title, window.location.pathname)}}>
            <RouterProvider router={router}></RouterProvider>
        </AuthProvider>


    )
}

function SecuredLayout() {
    const auth = useAuth()

    if(auth.isLoading) {
        return <Box><Typography>Is loading</Typography></Box>
    }
    if(auth.isAuthenticated) {
        return <Outlet></Outlet>
    }

    return <Navigate to={'/login'}/>
}

function MainLayout() {
    return (
        <>
            <Outlet></Outlet>
        </>
    )
}


