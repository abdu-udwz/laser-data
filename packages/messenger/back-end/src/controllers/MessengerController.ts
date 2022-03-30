import * as SocketManager from '../socket/SocketManager'
import Message from '../models/Message.js'

SocketManager.registerEventListener('STATE', 'set', (socketInfo, state) => {
  SocketManager.emitToIdentity(socketInfo.identity, 'STATE_updated', state)
})

SocketManager.registerEventListener('TRANSCEIVER', 'checkState', (socketInfo) => {
  const isOnline = SocketManager.isTransceiverOnline(socketInfo.identity)
  SocketManager.emitToIdentity(socketInfo.identity, 'TRANSCEIVER_stateUpdated', isOnline)
})

// ============== TRANSMIT events =====================//
SocketManager.registerEventListener('TRANSMIT', 'messageSent', (socketInfo, messageContent) => {
  const message = new Message(socketInfo.identity, messageContent, 'SENT', new Date())
  SocketManager.emitToIdentity(socketInfo.identity, 'TRANSMIT_messageSent', message)
})

// remote to transceiver
SocketManager.registerEventListener('TRANSMIT', 'sendMessage', (socketInfo, message) => {
  SocketManager.emitToTransceiver(socketInfo.identity, 'TRANSMIT_sendMessage', message)
})

// transceiver to remote
SocketManager.registerEventListener('TRANSMIT', 'updateProgress', (socketInfo, progress) => {
  SocketManager.emitToIdentity(socketInfo.identity, 'TRANSMIT_progressUpdated', progress)
})


// ============== RECEIVE events =====================//
SocketManager.registerEventListener('RECEIVE', 'toggle', (socketInfo) => {
  SocketManager.emitToTransceiver(socketInfo.identity, 'RECEIVE_toggle')
})

SocketManager.registerEventListener('RECEIVE', 'toggleError', (socketInfo) => {
  SocketManager.emitToTransceiver(socketInfo.identity, 'RECEIVE_toggle')
})

SocketManager.registerEventListener('RECEIVE', 'messageReceived', (socketInfo, messageContent) => {
  let senderIdentity = 'ALPHA'
  if (socketInfo.identity === 'ALPHA')
    senderIdentity = 'BETA'
  const message = new Message(senderIdentity, messageContent, 'SENT', new Date())

  SocketManager.emitToIdentity(socketInfo.identity, 'RECEIVE_messageReceived', message)
})


SocketManager.registerEventListener('RECEIVE', 'bitBufferUpdated', (socketInfo, newBuffer) => {
  SocketManager.emitToIdentity(socketInfo.identity, 'RECEIVE_bitBufferUpdated', newBuffer)
})


// const key = setInterval(() => {
//   SocketManager.emitToIdentity('ALPHA', 'RECEIVE_bitBufferUpdated', Math.round(Math.random() * 127).toString(2))
// }, 48)

// clearInterval(key)