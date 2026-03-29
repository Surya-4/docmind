import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Button, Typography, Paper, CircularProgress,
  List, ListItem, ListItemText, ListItemButton,
  Chip, AppBar, Toolbar, Alert, LinearProgress
} from '@mui/material'
import { CloudUpload, Chat, Logout } from '@mui/icons-material'
import api from '../api/axios'

export default function Dashboard() {
  const navigate = useNavigate()
  const [documents, setDocuments] = useState([])
  const [uploading, setUploading] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [polling, setPolling] = useState(null)

  // Load documents on mount
  useEffect(() => {
    fetchDocuments()
  }, [])

  // Poll every 3 seconds if any doc is processing
  useEffect(() => {
    const hasProcessing = documents.some(d => d.status === 'PROCESSING')
    if (hasProcessing && !polling) {
      const interval = setInterval(fetchDocuments, 3000)
      setPolling(interval)
    } else if (!hasProcessing && polling) {
      clearInterval(polling)
      setPolling(null)
    }
    return () => { if (polling) clearInterval(polling) }
  }, [documents])

  const fetchDocuments = async () => {
    try {
      const res = await api.get('/documents')
      setDocuments(res.data)
    } catch (err) {
      setError('Failed to load documents')
    } finally {
      setLoading(false)
    }
  }

  const handleUpload = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    setUploading(true)
    setError('')

    const formData = new FormData()
    formData.append('file', file)

    try {
      await api.post('/documents/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      fetchDocuments()
    } catch (err) {
      setError(err.response?.data?.error || 'Upload failed')
    } finally {
      setUploading(false)
    }
  }

  const handleLogout = async () => {
    await api.post('/auth/logout')
    navigate('/login')
  }

  const getStatusColor = (status) => {
    if (status === 'READY') return 'success'
    if (status === 'PROCESSING') return 'warning'
    if (status === 'FAILED') return 'error'
    return 'default'
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>

      {/* Navbar */}
      <AppBar position="static" elevation={1}>
        <Toolbar>
          <Typography variant="h6" fontWeight="bold" sx={{ flexGrow: 1 }}>
            DocMind
          </Typography>
          <Button color="inherit" startIcon={<Logout />} onClick={handleLogout}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>

      <Box sx={{ maxWidth: 800, mx: 'auto', p: 4 }}>

        {/* Upload Section */}
        <Paper elevation={2} sx={{ p: 4, mb: 4, borderRadius: 3, textAlign: 'center' }}>
          <CloudUpload sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
          <Typography variant="h6" mb={1}>Upload a PDF</Typography>
          <Typography variant="body2" color="text.secondary" mb={3}>
            Upload any PDF and start chatting with it using AI
          </Typography>

          <Button
            variant="contained"
            component="label"
            disabled={uploading}
            startIcon={uploading ? <CircularProgress size={18} color="inherit" /> : <CloudUpload />}
            sx={{ borderRadius: 2 }}
          >
            {uploading ? 'Uploading...' : 'Choose PDF'}
            <input type="file" accept=".pdf" hidden onChange={handleUpload} />
          </Button>
        </Paper>

        {/* Error */}
        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

        {/* Documents List */}
        <Typography variant="h6" fontWeight="bold" mb={2}>
          Your Documents
        </Typography>

        {loading ? (
          <LinearProgress />
        ) : documents.length === 0 ? (
          <Paper sx={{ p: 4, textAlign: 'center', borderRadius: 3 }}>
            <Typography color="text.secondary">
              No documents yet. Upload a PDF to get started!
            </Typography>
          </Paper>
        ) : (
          <Paper elevation={2} sx={{ borderRadius: 3 }}>
            <List disablePadding>
              {documents.map((doc, index) => (
                <ListItem
                  key={doc.id}
                  divider={index < documents.length - 1}
                  disablePadding
                >
                  <ListItemButton
                    disabled={doc.status !== 'READY'}
                    onClick={() => navigate(`/chat/${doc.id}`)}
                    sx={{ px: 3, py: 2 }}
                  >
                    <ListItemText
                      primary={doc.filename}
                      secondary={new Date(doc.createdAt).toLocaleDateString()}
                    />
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {doc.status === 'PROCESSING' && (
                        <CircularProgress size={16} />
                      )}
                      <Chip
                        label={doc.status}
                        color={getStatusColor(doc.status)}
                        size="small"
                      />
                      {doc.status === 'READY' && (
                        <Chat fontSize="small" color="primary" />
                      )}
                    </Box>
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          </Paper>
        )}
      </Box>
    </Box>
  )
}