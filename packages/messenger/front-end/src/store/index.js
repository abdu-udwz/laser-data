import Vue from 'vue'
import Vuex from 'vuex'
import SocketService from '@/services/Socket';

Vue.use(Vuex);

export default new Vuex.Store({
    strict: true,

    modules: {},

    state: {
        commState: 'STANDBY',

        identity: localStorage.getItem('identity') || 'ALPHA',

        transceiverOnline: false,
        messages: [],

        messagePending: false,

        transmitProgress: {},

        receiverPending: false,
    },

    getters: {
        isStandBy(state){
            return state.commState === 'STANDBY'
        },

        isTransmitting(state){
            return state.commState === 'TRANSMITTING'
        },

        isReceiving(state){
            return state.commState === 'RECEIVING'
        }
    },

    mutations: {

        setCommState(state, newState){
            state.commState = newState;
        },

        setIdentity(state, newIdentity) {
            state.identity = newIdentity;
            localStorage.setItem('identity', newIdentity)
        },

        setTransceiverOnline(state, isOnline) {
            state.transceiverOnline = isOnline;
        },

        addMessage(state, message) {
            state.messages.push(message)
        },

        setMessagePending(state, pending) {
            state.messagePending = pending;
        },

        replacePendingMessage(state, newMessage) {
            let pendingIndex = state.messages.findIndex(message => message.status === 'PENDING');
            if (pendingIndex >= 0)
                Vue.set(state.messages, pendingIndex, newMessage)
        },

        setTransmitProgress(state, progress){
            state.transmitProgress  = progress;
        },

        setReceiverPending(state, pending){
            state.receiverPending = pending;
        }
    },

    actions: {

        setIdentity({commit, state}, newIdentity) {
            SocketService.emit('signIn', newIdentity, "REMOTE_CONTROL");
            SocketService.emit('TRANSCEIVER_checkState')
        },

        sendMessage(context, messageContent) {
            context.commit('addMessage', {
                owner: context.state.identity,
                content: messageContent,
                sentOn: new Date(),
                status: 'PENDING'
            });
            context.commit('setMessagePending', true);
            SocketService.emit('TRANSMIT_sendMessage', messageContent);
        },

        toggleReceiver({commit}){
            commit('setReceiverPending', true);
            SocketService.emit('RECEIVE_toggle')
        },

        SOCKET_STATE_updated({commit, state}, newState){
            commit('setCommState', newState);
            if (state.receiverPending)
                commit('setReceiverPending', false);
        },

        SOCKET_AUTHENTICATION_signInSucceeded({commit}, identity) {
            commit('setIdentity', identity)
        },

        SOCKET_TRANSCEIVER_stateUpdated({commit}, newState) {
            commit('setTransceiverOnline', newState);
            if (newState === 'STANDBY'){
                commit('setTransmitProgress', {totalBytes: 1, sentBytes: 0})
            }
        },

        SOCKET_TRANSMIT_progressUpdated({commit}, progress){
            commit('setTransmitProgress', progress);
        },

        SOCKET_TRANSMIT_messageSent({commit, state}, message) {

            if (state.messagePending){
                commit('replacePendingMessage', message);
                commit('setMessagePending', false);
            } else {
                commit('addMessage', message)
            }
        },

        SOCKET_RECEIVE_messageReceived({commit, state}, message) {
            commit('addMessage', message);
        }
    }
})
