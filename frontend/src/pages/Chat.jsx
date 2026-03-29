import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Box, Typography, TextField, Button, Paper,
  CircularProgress, AppBar, Toolbar, IconButton,
  Alert, Chip, Avatar
} from '@mui/material'
import { Send, ArrowBack, SmartToy, Person } from '@mui/icons-material'
import api from '../api/axios'

export default function Chat() {
  const { docId } = useParams()
  const navigate = useNavigate()
  const messagesEndRef = useRef(null)

  const [conversationId, setConversationId] = useState(null)
  const [messages, setMessages] = useState([])
  const [question, setQuestion] = useState('')
  const [loading, setLoading] = useState(false)
  const [initializing, setInitializing] = useState(true)
  const [error, setError] = useState('')
  const [document, setDocument] = useState(null)

  // Create conversation on mount
  useEffect(() => {
    initChat()
  }, [docId])

  // Auto scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const initChat = async () => {
    try {
      // Get document info
      const docRes = await api.get(`/documents/${docId}`)
      setDocument(docRes.data)

      // Create conversation
      const convRes = await api.post(`/conversations?documentId=${docId}`)
      setConversationId(convRes.data.id)

      // Add welcome message
      setMessages([{
        role: 'assistant',
        content: `Hi! I've read "${docRes.data.filename}". Ask me anything about it!`
      }])
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to start chat')
    } finally {
      setInitializing(false)
    }
  }

  const handleSend = async () => {
    if (!question.trim() || loading) return

    const userQuestion = question.trim()
    setQuestion('')

    // Add user message immediately
    setMessages(prev => [...prev, { role: 'user', content: userQuestion }])
    setLoading(true)
    setError('')

    try {
      const res = await api.post(`/conversations/${conversationId}/query`, {
        question: userQuestion
      })

      // Add AI answer
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: res.data.answer,
        chunks: res.data.sourceChunks
      }])
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to get answer')
      // Remove the user message on error
      setMessages(prev => prev.slice(0, -1))
    } finally {
      setLoading(false)
    }
  }

  if (initializing) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
        <Typography ml={2}>Starting chat...</Typography>
      </Box>
    )
  }

  return (
    <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column', bgcolor: '#f5f5f5' }}>

      {/* Navbar */}
      <AppBar position="static" elevation={1}>
        <Toolbar>
          <IconButton color="inherit" onClick={() => navigate('/dashboard')} sx={{ mr: 1 }}>
            <ArrowBack />
          </IconButton>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="subtitle1" fontWeight="bold">
              {document?.filename || 'Chat'}
            </Typography>
            <Typography variant="caption">
              AI Document Assistant
            </Typography>
          </Box>
          <Chip label="AI Ready" color="success" size="small" />
        </Toolbar>
      </AppBar>

      {/* Messages */}
      <Box sx={{ flexGrow: 1, overflowY: 'auto', p: 3, display: 'flex', flexDirection: 'column', gap: 2 }}>

        {error && <Alert severity="error">{error}</Alert>}

        {messages.map((msg, index) => (
          <Box
            key={index}
            sx={{
              display: 'flex',
              justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
              gap: 1,
              alignItems: 'flex-start'
            }}
          >
            {/* AI Avatar */}
            {msg.role === 'assistant' && (
              <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }}>
                <SmartToy sx={{ fontSize: 18 }} />
              </Avatar>
            )}

            {/* Message bubble */}
            <Paper
              elevation={1}
              sx={{
                p: 2,
                maxWidth: '70%',
                borderRadius: msg.role === 'user' ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                bgcolor: msg.role === 'user' ? 'primary.main' : 'white',
                color: msg.role === 'user' ? 'white' : 'text.primary'
              }}
            >
              <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
                {msg.content}
              </Typography>
            </Paper>

            {/* User Avatar */}
            {msg.role === 'user' && (
              <Avatar sx={{ bgcolor: 'grey.500', width: 32, height: 32 }}>
                <Person sx={{ fontSize: 18 }} />
              </Avatar>
            )}
          </Box>
        ))}

        {/* Loading indicator */}
        {loading && (
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }}>
              <SmartToy sx={{ fontSize: 18 }} />
            </Avatar>
            <Paper elevation={1} sx={{ p: 2, borderRadius: '16px 16px 16px 4px' }}>
              <CircularProgress size={20} />
            </Paper>
          </Box>
        )}

        <div ref={messagesEndRef} />
      </Box>

      {/* Input */}
      <Paper elevation={3} sx={{ p: 2, borderRadius: 0 }}>
        <Box sx={{ display: 'flex', gap: 1, maxWidth: 800, mx: 'auto' }}>
          <TextField
            fullWidth
            placeholder="Ask anything about the document..."
            value={question}
            onChange={e => setQuestion(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && !e.shiftKey && handleSend()}
            disabled={loading}
            multiline
            maxRows={4}
            size="small"
            sx={{ bgcolor: 'white', borderRadius: 2 }}
          />
          <Button
            variant="contained"
            onClick={handleSend}
            disabled={loading || !question.trim()}
            sx={{ minWidth: 48, borderRadius: 2 }}
          >
            <Send />
          </Button>
        </Box>
      </Paper>
    </Box>
  )
}