import * as SocketManager from '../socket/SocketManager'
const Message = require('../models/Message');

SocketManager.registerEventListener('STATE', 'set', (socketInfo, state) => {
  SocketManager.emitToIdentity(socketInfo.identity, 'STATE_updated', state);
});

SocketManager.registerEventListener('TRANSCEIVER', 'checkState', (socketInfo, message) => {
  let isOnline = SocketManager.isTransceiverOnline(socketInfo.identity);
  SocketManager.emitToIdentity(socketInfo.identity, 'TRANSCEIVER_stateUpdated', isOnline)
});

// ============== TRANSMIT events =====================//
SocketManager.registerEventListener('TRANSMIT', 'messageSent', (socketInfo, messageContent) => {
  let message = new Message(socketInfo.identity, messageContent, 'SENT', new Date());
  SocketManager.emitToIdentity(socketInfo.identity, 'TRANSMIT_messageSent', message)
});

SocketManager.registerEventListener('TRANSMIT', 'sendMessage', (socketInfo, message) => {
  SocketManager.emitToTransceiver(socketInfo.identity, 'TRANSMIT_sendMessage', message)
});

SocketManager.registerEventListener('TRANSMIT', 'updateProgress', (socketInfo, progress) => {
  SocketManager.emitToIdentity(socketInfo.identity, 'TRANSMIT_progressUpdated', progress)
});


// ============== RECEIVE events =====================//
SocketManager.registerEventListener('RECEIVE', 'toggle', (socketInfo,) => {
  SocketManager.emitToTransceiver(socketInfo.identity, 'RECEIVE_toggle')
});

SocketManager.registerEventListener('RECEIVE', 'toggleError', (socketInfo,) => {
  SocketManager.emitToTransceiver(socketInfo.identity, 'RECEIVE_toggle')
});

SocketManager.registerEventListener('RECEIVE', 'messageReceived', (socketInfo, messageContent) => {
  let senderIdentity = 'ALPHA';
  if (socketInfo.identity === 'ALPHA')
    senderIdentity = 'BETA';
  let message = new Message(senderIdentity, messageContent, 'SENT', new Date());

  SocketManager.emitToIdentity(socketInfo.identity, 'RECEIVE_messageReceived', message)
});