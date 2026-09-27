import {Box, Button, Card, CardContent, Stack, Typography} from "@mui/material";
import {API} from "../api/API.ts";
import {useState} from "react";


export default function SecuredPage() {

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
