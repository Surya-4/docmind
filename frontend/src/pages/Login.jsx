import { useState } from "react"
import {useNavigate} from 'react-router-dom'
import { Box, Button, TextField, Typography,
  Paper, Tabs, Tab, Alert, CircularProgress} from '@mui/material'
  import api from '../api/axios'

  export default function Login(){
    const navigate = useNavigate()
    const [tab, setTab] = useState(0)        
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const handleSubmit = async () =>{
    setError('')
    setSuccess('')
    setLoading(true)

    try {
        if(tab === 0){
            await api.post('/auth/login',{email,password})
            navigate('/dashboard')
        }
        else{
            await api.post('/auth/register',{email,password})
            setSuccess('Registered successfully! Please login.')
            setTab(0)
        }
    } catch (error) {
        setError(error.response?.data?.error || 'Something went wrong')
    } finally {
        setLoading(false)
    }
  }
  return(
    <Box sx={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      bgcolor: '#f5f5f5'
    }}>
      <Paper elevation={3} sx={{ p: 4, width: 400, borderRadius: 3 }}>

        {/* Title */}
        <Typography variant="h4" fontWeight="bold" textAlign="center" mb={1}>
          DocMind
        </Typography>
        <Typography variant="body2" textAlign="center" color="text.secondary" mb={3}>
          AI-Powered Document Intelligence
        </Typography>

        {/* Tabs */}
        <Tabs value={tab} onChange={(e, v) => setTab(v)} centered sx={{ mb: 3 }}>
          <Tab label="Login" />
          <Tab label="Register" />
        </Tabs>

        {/* Alerts */}
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

        {/* Fields */}
        <TextField
          fullWidth label="Email" type="email"
          value={email} onChange={e => setEmail(e.target.value)}
          sx={{ mb: 2 }}
        />
        <TextField
          fullWidth label="Password" type="password"
          value={password} onChange={e => setPassword(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSubmit()}
          sx={{ mb: 3 }}
        />

        {/* Submit */}
        <Button
          fullWidth variant="contained" size="large"
          onClick={handleSubmit} disabled={loading}
          sx={{ borderRadius: 2, py: 1.5 }}
        >
          {loading ? <CircularProgress size={24} color="inherit" /> : tab === 0 ? 'Login' : 'Register'}
        </Button>

      </Paper>
    </Box>
  )
  }