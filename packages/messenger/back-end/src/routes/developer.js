import { Router } from 'express'
import * as SocketManager from '../socket/SocketManager'

const router = Router()

router.post('/transmitMessage', (req, res) => {
  let targetIdentity = req.query.target.toUpperCase()

  let message = req.body.message

  SocketManager.emitToIdentity(targetIdentity, 'TRANSMIT_sendMessage', message)
  return res.sendStatus(200)
})

// eslint-disable-next-line no-undef
export default router