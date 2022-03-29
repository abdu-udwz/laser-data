const router = require('express').Router();
const SocketManager = require('../socket/SocketManager');


router.post('/transmitMessage', (req, res, next) => {
    let targetIdentity = req.query.target.toUpperCase();

    let message = req.body.message;

    SocketManager.emitToIdentity(targetIdentity, "TRANSMIT_sendMessage", message);
    return res.sendStatus(200);
});

module.exports = router;