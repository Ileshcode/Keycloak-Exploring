import {createBrowserRouter, Outlet, redirect, RouterProvider} from "react-router-dom";
import RegistrationPage from "./pages/RegistrationPage.tsx";
import LoginPage from "./pages/LoginPage.tsx";
import HomePage from "./pages/HomePage.tsx";
import SecuredPage from "./pages/SecuredPage.tsx";
import {API} from "./api/API.ts";
import {isAxiosError} from "axios";
import type {UserProfileData} from "./data/UserProfileData.ts";

export default function App() {


    const router = createBrowserRouter([
        {
            element: <MainLayout/>, children: [
                {element: <RegistrationPage/>, path: '/registration'},
                {element: <LoginPage/>, path: '/login'},
                {
                    element: <SecuredLayout/>, loader: securedLoader,id:'securedLayout', children: [
                        {element: <SecuredPage/>, path: '/secured'},
                        {element: <HomePage/>, path: '/'},
                    ]
                }
            ]
        }
    ])

    return (

            <RouterProvider router={router}></RouterProvider>



    )
}

async function securedLoader () {
    let userData: UserProfileData;
   try {
       userData = await API.get<UserProfileData>('/me').then((response) => {return response.data})
   } catch (error) {
       if(isAxiosError(error)) {
           const status = error.response?.status

           if(status === 401) {
               console.error('Login error 401')
               throw redirect('/login')
           } else {
               console.error('Login error with code: ' + status)
               throw redirect('/login')
           }
       } else  {
           console.error('Login error, but not axios error')
           throw redirect('/login')
       }
   }

   return userData
}

function SecuredLayout() {
    return (
        <><Outlet></Outlet></>
    )
}

function MainLayout() {
    return (
        <>
            <Outlet></Outlet>
        </>
    )
}


