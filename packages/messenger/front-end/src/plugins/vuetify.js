import Vue from 'vue'
import Vuetify from 'vuetify/lib'

Vue.use(Vuetify)

export default new Vuetify({ theme: {
  icons: {
    iconfont: 'mdiSvg',
  },
  themes: {
    light: {
      primary: '#2ca49c',
      secondary: '#b0bec5',
      accent: '#8c9eff',
      error: '#b71c1c',
    },
  },
},

})
