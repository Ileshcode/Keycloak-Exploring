import {Box, Button, Card, CardActions, CardContent, Stack, TextField, Typography} from "@mui/material";
import {useForm} from "react-hook-form";
import {useEffect, useState} from "react";
import {API} from "../api/API.ts";
import {useNavigate} from "react-router-dom";

export default function RegistrationPage() {

    const navigation = useNavigate()
    const [isSuccess, setIsSuccess] = useState(false)
    const {
        reset,
        handleSubmit,
        register
    } = useForm({
        defaultValues: {
            username: '',
            firstname: '',
            lastname: '',
            email: ''
        }
    })
    useEffect(() => {
        reset()
    }, []);

    const handleOnSubmit = (data: { username: string, firstname: string, lastname: string, email: string }) => {
        API.post('/registration', data)
            .then((response) => {
                if(response.status === 200) {
                    setIsSuccess(true)
                    reset()
                    navigation('/login')
                }
            })

    }
    return (
        <Box sx={{display:'flex', justifyContent:'center', alignItems:'center', height:'80vh'}}>
            <Card sx={{width:'40vh'}}>
                <CardContent>
                    <form id={'registr-form'} onSubmit={handleSubmit(handleOnSubmit)}>
                        <Stack direction={'column'} spacing={2}>
                            <Typography variant={'h4'} sx={{fontWeight:'bold', textAlign:'center'}}>Registration</Typography>
                            <TextField {...register('username')} label={'Username'}></TextField>
                            <TextField {...register('firstname')} label={'Firstname'}></TextField>
                            <TextField {...register('lastname')} label={'Lastname'}></TextField>
                            <TextField {...register('email')} label={'Email'}></TextField>
                            {isSuccess &&
                                <Typography>Success</Typography>
                            }
                            <Typography onClick={() => navigation('/login')}
                                        sx={{textAlign:'center', cursor:'pointer', "&:hover": {textDecoration:'underline'}}}
                                        color={'textSecondary'}>Already have account ? Login</Typography>
                        </Stack>
                    </form>
                </CardContent>
                <CardActions sx={{display:'flex', justifyContent:'flex-end'}}>
                    <Button variant={'outlined'} type={'submit'} form={'registr-form'}>
                        <Typography sx={{fontWeight:'bold'}}>Submit</Typography>
                    </Button>
                </CardActions>
            </Card>
        </Box>
    )
}
