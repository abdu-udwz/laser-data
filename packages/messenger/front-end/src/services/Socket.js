import io from 'socket.io-client';

let socket = io(process.env.VUE_APP_API_URL);

export default socket;
