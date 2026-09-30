import {Box, Button, Card, CardContent, Stack, Typography} from "@mui/material";
import {useNavigate} from "react-router-dom";
import {API} from "../api/API.ts";


export default function HomePage() {

    const navigation = useNavigate()

    const logout = () => {
        API.post('/logout')
            .then((response) => {
                window.location.href = response.data
            })
    }
    return (
        <Box>
            <Card>
                <CardContent>
                    <Stack direction={'column'} spacing={2}>
                        <Typography sx={{fontWeight:'bold'}} variant={'h5'}>Home page</Typography>
                        <Stack direction={'row'} spacing={2}>
                            <Button onClick={() => navigation('/secured')} variant={'outlined'}>
                                <Typography>Secured page</Typography>
                            </Button>
                            <Button onClick={() => logout()}>Log out</Button>
                        </Stack>
                    </Stack>
                </CardContent>
            </Card>
        </Box>
    )
}
