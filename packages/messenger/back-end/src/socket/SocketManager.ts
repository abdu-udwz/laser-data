import type { Server, Socket } from 'socket.io'
let io: Server | null = null

// a list contains a list of connected sockets associated with some info like
// to whom it belongs, an authenticated user or just a guest.

export type SocketIdentity = 'ALPHA' | 'BETA'
export type SocketDeviceType = 'TRANSCEIVER' | 'REMOTE_CONTROL'
interface IdentifiedSocket {
  socket: Socket
  identity: SocketIdentity
  type: SocketDeviceType
}

const identifiedSockets: IdentifiedSocket[] = []

export interface SocketEventListener {
  prefix: string
  event: string
  callback: (socketInfo: IdentifiedSocket, ...args: any[]) => void
}
const eventListeners: SocketEventListener[] = []

export function init (socketServer: Server): void {
  io = socketServer
  socketServer.on('connection', function (socket) {
    console.log('[Socket]:', 'New socket connected with id', socket.id)

    socketConnected(socket)

    socket.on('signIn', function (identity, type) {
      socketSignIn(socket, identity, type)
    })

    socket.on('signOut', function () {
      // TODO: implement socket sign out
    })

    socket.on('disconnect', function () {
      socketDisconnected(socket)
    })
  })
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function socketConnected (socket: Socket): void {
  // to be done
}

function socketSignIn (socket: Socket, identity: SocketIdentity, type: SocketDeviceType): void {
  const matchedInfo = identifiedSockets.find(item => item.socket.id === socket.id)

  if (matchedInfo) {
    // socket has signed in previously
    // update the identity
    socket.leave(matchedInfo.identity)
    matchedInfo.identity = identity
  } else {
    identifiedSockets.push({ socket: socket, identity: identity, type: type })
    for (const listener of eventListeners) {
      registerEventListenerOnSocket(socket, listener)
    }
  }
  socket.join(identity)
  socket.emit('AUTHENTICATION_signInSucceeded', identity)

  if (type === 'TRANSCEIVER') {
    emitToIdentity(identity, 'TRANSCEIVER_stateUpdated', true)
  }

  console.log(`[Socket]: Socket with id ${socket.id} signed in with identity ${identity} as ${type}`)
}

function socketDisconnected (socket: Socket): void {
  const matchedIndex = identifiedSockets.findIndex(item => item.socket.id === socket.id)
  const matchedInfo = identifiedSockets[matchedIndex]

  if (matchedInfo) {
    // check if the last socket of a user
    // if so. mark user as in active
    const identity = matchedInfo.identity

    identifiedSockets.splice(matchedIndex, 1)

    if (matchedInfo.type === 'TRANSCEIVER') {
      emitToIdentity(identity, 'TRANSCEIVER_stateUpdated', false)
    }
    console.log(`[Socket]: Socket ${socket.id} disconnected.`)

  }

}

function registerEventListenerOnSocket (socket: Socket, listener: SocketEventListener): void {
  const socketInfo = identifiedSockets.find(item => item.socket.id === socket.id)

  if (socketInfo == null) {
    throw Error('SOCKET_REGISTER_EVENT_ERROR')
    return
  }

  const eventName = `${listener.prefix}_${listener.event}`
  const eventCallback = function (...args: any[]): void {
    listener.callback(socketInfo!, ...args)
  }

  socket.on(eventName, eventCallback)
}

export function removeEventListenersFromSocket (socket: Socket, listener: SocketEventListener): void {
  socket.removeAllListeners(`${listener.prefix}_${listener.event}`)
}

// ==================================== //

export function getIdentitySockets (identity: SocketIdentity): IdentifiedSocket[] {
  return identifiedSockets.filter(item => item.identity === identity)
}

// =============================== //

export function emitToIdentity (identity: SocketIdentity, event: string, ...args: any[]): void {
  io!.to(identity).emit(event, ...args)
}

export function emitToTransceiver (identity: SocketIdentity, event: string, ...args: any[]): void {
  const matched = identifiedSockets.find(item => item.type === 'TRANSCEIVER' && item.identity === identity)
  if (matched) {
    io?.to(matched.socket.id).emit(event, ...args)
  }
}

export function registerEventListener (
  prefix: string,
  event: string,
  callback: SocketEventListener['callback']): void {
  eventListeners.push({
    prefix,
    event,
    callback,
  })
}

export function isTransceiverOnline (identity: SocketIdentity): boolean {
  return identifiedSockets
    .findIndex(item => item.identity === identity && item.type === 'TRANSCEIVER') > -1
}
