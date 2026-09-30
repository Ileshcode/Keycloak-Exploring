import {Box, Button, Card, CardContent, Stack, Typography} from "@mui/material";
import {API} from "../api/API.ts";
import {useState} from "react";
import { useRouteLoaderData} from "react-router-dom";
import type {UserProfileData} from "../data/UserProfileData.ts";


export default function SecuredPage() {

    const userData = useRouteLoaderData('securedLayout') as  UserProfileData
    const [securedStatus, setSecuredStatus] = useState('')
    const test = () => {
        API.get('/status')
            .then((response) => {
                setSecuredStatus(response.data)
            })
    }

    return (
        <Box>
            <Card>
                <CardContent>
                    <Stack direction={'column'} spacing={2}>
                        <Typography variant={'h5'} sx={{fontWeight:'bold'}}>Welcome to secured page. Tap the button for verify request to Resource Server</Typography>
                        <Typography>Email: {userData.email}</Typography>
                        <Typography>Firstname: {userData.firstname}</Typography>
                        <Typography>Lastname: {userData.lastname}</Typography>
                        <Typography>Username: {userData.username}</Typography>
                        {userData.roles.map((role) => (
                            <Typography>{role}</Typography>
                        ))}
                        <Button onClick={test} variant={'outlined'}>
                            {securedStatus
                                ? <Typography color={'success'}>
                                    Server status OK
                                </Typography>
                                : <Typography>
                                    Request server status
                                </Typography>
                            }
                        </Button>
                    </Stack>
                </CardContent>
            </Card>
        </Box>
    )
}
