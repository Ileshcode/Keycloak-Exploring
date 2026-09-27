import {Box, Button, Card, CardContent, Stack, Typography} from "@mui/material";
import {useAuth} from "react-oidc-context";
import {useNavigate} from "react-router-dom";


export default function LoginPage() {

     const auth = useAuth()
    const navigation = useNavigate()

    return (
        <Box sx={{display:'flex', justifyContent:'center', alignItems:'center', height:'70vh'}}>
           <Card sx={{width:'50vh', display:'flex', justifyContent:'center'}}>
               <CardContent>
                   <Stack direction={'column'} spacing={2}>
                       <Typography onClick={() => navigation('/registration')} sx={{cursor:'pointer', "&:hover":{textDecoration:'underline'}}} color={'textSecondary'}>
                           Do not have an account? Create a new one</Typography>
                       <Button onClick={() => auth.signinRedirect()} variant={'contained'}>
                           <Typography>
                               Login
                           </Typography>
                       </Button>
                   </Stack>
               </CardContent>
           </Card>
        </Box>
    )
}
