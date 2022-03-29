import { defineConfig, loadEnv } from 'vite'
import { createVuePlugin } from 'vite-plugin-vue2'
import components from 'unplugin-vue-components/vite'
import { VuetifyResolver } from 'unplugin-vue-components/resolvers'

import path from 'path'
import fs from 'fs'

const ABS_ROOT_DIR = path.resolve(__dirname, 'src/')

// sass/scss custom variables
let sassAdditionalData
if (fs.existsSync(path.resolve(ABS_ROOT_DIR, 'styles/variables.sass'))) {
  sassAdditionalData = '\n@import \'@/styles/variables.sass\'\n'
}

let scssAdditionalData
if (fs.existsSync(path.resolve(ABS_ROOT_DIR, 'styles/variables.scss'))) {
  scssAdditionalData = '\n@import \'@/styles/variables.scss\'\n'
}

// console.log(sassAdditionalData, scssAdditionalData)
// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  let { SERVER_HOST, SERVER_BASE_PATH, SERVER_API_PATH } = loadEnv(mode, process.cwd(), '')


  // == dev serve proxy-ing config
  let relativeToServerPath = ''
  let serverBaseUrl = ''
  let serverApiURL = ''
  if (command === 'serve') {
    if (SERVER_HOST == null) {
      SERVER_HOST = 'http://localhost:3000'
    }

    if (SERVER_BASE_PATH == null) {
      SERVER_BASE_PATH = '/'
    }

    if (SERVER_API_PATH == null) {
      SERVER_API_PATH = '/_api'
    }

    // prepare server urls
    relativeToServerPath = path.join(SERVER_BASE_PATH, SERVER_API_PATH)
    serverBaseUrl = new URL(SERVER_BASE_PATH, SERVER_HOST).href
    serverApiURL = new URL(relativeToServerPath, SERVER_HOST).href

    console.log('Use server at', serverBaseUrl, 'as API server')
    console.log('Full API url is:', serverApiURL)
  }

  return {
    plugins: [
      createVuePlugin(/* options */),
      components({
        resolvers: [
          VuetifyResolver(),
        ],
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    css: {
      preprocessorOptions: {
        sass: {
          additionalData: sassAdditionalData,
        },
        scss: {
          additionalData: scssAdditionalData,
        },
      },
    },
    server: {
      port: 8080,
      proxy: {
        '/_api': {
          target: serverBaseUrl,
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})
