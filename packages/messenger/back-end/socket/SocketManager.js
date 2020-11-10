let io = null;

// a list contains a list of connected sockets associated with some info like
// to whom it belongs, an authenticated user or just a guest.
const identifiedSockets = [];

// a list of event listeners to attach to sockets
//  {
//      prefix: String,
//      event: String,
//  }

const eventListeners = [];

function init(socketManager) {
    io = socketManager;
    socketManager.on('connection', function (socket) {
        console.log('[Socket]:', 'New socket connected with id', socket.id);

        socketConnected(socket);

        socket.on('signIn', function (identity, type) {
            socketSignIn(socket, identity, type)
        });

        socket.on('signOut', function () {
            // TODO: implement socket sign out
        });

        socket.on('disconnect', function (reason) {
            socketDisconnected(socket);
        })
    });
}

function socketConnected(socket) {

}

function socketSignIn(socket, identity, type) {
    let matchedInfo = identifiedSockets.find(item => item.socket.id === socket.id);

    if (matchedInfo) {
        socket.leave(matchedInfo.identity);
        matchedInfo.identity = identity;
    } else {
        identifiedSockets.push({socket: socket, identity: identity, type: type});
        for (let listener of eventListeners) {
            registerEventListenerOnSocket(socket, listener);
        }
    }
    socket.join(identity);
    socket.emit('AUTHENTICATION_signInSucceeded', identity);

    if (type === 'TRANSCEIVER') {
        emitToIdentity(identity, 'TRANSCEIVER_stateUpdated', true)
    }

    console.log(`[Socket]: Socket with id ${socket.id} signed in with identity ${identity} as ${type}`);
}

function socketDisconnected(socket) {
    let matchedIndex = identifiedSockets.findIndex(item => item.socket.id === socket.id);
    let matchedInfo = identifiedSockets[matchedIndex];

    if (matchedInfo) {
        // check if the last socket of a user
        // if so. mark user as in active
        let identity = matchedInfo.identity;

        identifiedSockets.splice(matchedIndex, 1);

        if (matchedInfo.type === 'TRANSCEIVER'){
            emitToIdentity(identity, "TRANSCEIVER_stateUpdated", false)
        }
        console.log(`[Socket]: Socket ${socket.id} disconnected.`);

    }

}

function registerEventListenerOnSocket(socket, listener) {
    let socketInfo = identifiedSockets.find(item => item.socket.id === socket.id);

    if (!socketInfo)
        throw Error('SOCKET_REGISTER_EVENT_ERROR');

    let eventName = `${listener.prefix}_${listener.event}`;
    let eventCallback = function (...args) {
        listener.callback(socketInfo, ...args)
    };

    socket.on(eventName, eventCallback)
}

function removeEventListenersFromSocket(socket, listener) {
    socket.removeAllListeners(`${listener.prefix}_${listener.event}`);
}

// ==================================== //

function getIdentitySockets(identity) {
    return identifiedSockets.filter(item => item.identity === identity);
}

// =============================== //

function emitToIdentity(identity, event, ...args) {
    io.to(identity).emit(event, ...args)
}

module.exports = {

    init,

    emitToIdentity,

    emitToTransceiver(identity, event, ...args) {
        let matched = identifiedSockets.find(item => item.type === 'TRANSCEIVER' && item.identity === identity);
        if (matched) {
            io.to(matched.socket.id).emit(event, ...args);
        }
    },

    emitToRoom(room, event, ...args) {
        io.to(room).emit(event, ...args);
    },

    registerEventListener(prefix, event, callback) {

        eventListeners.push({
            prefix,
            event,
            callback
        });
    },

    isTransceiverOnline(identity) {
        return identifiedSockets.findIndex(item => item.identity === identity && item.type === 'TRANSCEIVER') > -1;
    }
};