import {Box, Button, Card, CardContent, Stack, Typography} from "@mui/material";
import {useAuth} from "react-oidc-context";
import {useNavigate} from "react-router-dom";


export default function HomePage() {
    const auth = useAuth()
    const navigation = useNavigate()
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
                        </Stack>
                        {auth.isAuthenticated &&
                            <Button onClick={() => auth.signoutRedirect()} sx={{maxWidth:'40vh'}} variant={'contained'}>
                                <Typography>Logout</Typography>
                            </Button>
                        }
                    </Stack>
                </CardContent>
            </Card>
        </Box>
    )
}
