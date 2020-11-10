import Vue from 'vue'
import App from './App.vue'
import store from './store'
import vuetify from './plugins/vuetify';
import VueSocketIO from 'vue-socket.io'
import SocketService from '@/services/Socket'
import vMoment from 'vue-moment'

Vue.use(vMoment);
Vue.config.productionTip = false;

Vue.use(new VueSocketIO({
    // only enable logging in development mode
    debug: process.env.NODE_ENV === 'development',

    connection: SocketService,
    vuex: {
        store,
        actionPrefix: 'SOCKET_',
        mutationPrefix: 'SOCKET_'
    },

}));

new Vue({
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app');
