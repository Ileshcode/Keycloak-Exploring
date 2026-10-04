import {
    Box,
    Button,
    Card,
    CardContent,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Stack, TextField,
    Typography
} from "@mui/material";
import {API} from "../api/API.ts";
import {useEffect, useState} from "react";
import {useNavigate, useRouteLoaderData} from "react-router-dom";
import type {UserProfileData} from "../data/UserProfileData.ts";
import {useForm} from "react-hook-form";
import type {UpdateUserData} from "../data/UpdateUserData.ts";


export default function SecuredPage() {

    const userData = useRouteLoaderData('securedLayout') as  UserProfileData
    const [securedStatus, setSecuredStatus] = useState('')
    const [updateOpen, setUpdateOpen] = useState(false)
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
                        <Button
                            variant={'outlined'}
                            onClick={() => window.location.href=import.meta.env.VITE_BACKEND_URL + '/oauth2/authorization/keycloak?kc_action=UPDATE_EMAIL'}>Change email</Button>
                        <Button variant={'outlined'}
                                onClick={() => window.location.href = import.meta.env.VITE_BACKEND_URL + '/oauth2/authorization/keycloak?kc_action=UPDATE_PASSWORD'}
                        >Update password
                        </Button>
                        <Button variant={'outlined'} onClick={() => setUpdateOpen(true)}>
                            Update profile
                        </Button>

                    </Stack>
                </CardContent>
            </Card>
            <UpdateUserModal isOpen={updateOpen} onClose={() => setUpdateOpen(false)}></UpdateUserModal>
        </Box>
    )
}

interface UpdateUserModalProps {
    isOpen: boolean,
    onClose: () => void
}
function UpdateUserModal (modalProps: UpdateUserModalProps) {

    const navigation = useNavigate()

    const {
        register,
        handleSubmit,
        reset
    } = useForm<UpdateUserData>()

    useEffect(() => {
        reset()
    }, [modalProps.isOpen]);

    const handleOnSubmit = (data: UpdateUserData) => {
        API.put('/update', data)
            .then(() => {
                navigation('/')
            })
    }
    return (
        <Dialog open={modalProps.isOpen} onClose={modalProps.onClose}>
            <DialogTitle>
                Update user profile
            </DialogTitle>
            <DialogContent>
                <form onSubmit={handleSubmit(handleOnSubmit)} id={'update-us-form'}>
                    <Stack direction={'column'} spacing={2}>
                        <TextField {...register('username')} label={'Username'}></TextField>
                        <TextField {...register('firstname')} label={'Firstname'}></TextField>
                        <TextField {...register('lastname')} label={'Lastname'}></TextField>
                    </Stack>
                </form>
            </DialogContent>
            <DialogActions>
                <Button onClick={modalProps.onClose}>Close</Button>
                <Button type={'submit'} form={'update-us-form'}>Submit</Button>
            </DialogActions>
        </Dialog>
    )
}
