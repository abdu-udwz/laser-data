import express, { json, urlencoded } from 'express'
// middleware
import { createProxyMiddleware } from 'http-proxy-middleware'
import logger from 'morgan'
import developerRouter from './routes/developer'
// util
import { join } from 'path'

import './controllers/MessengerController'

const app = express()

app.use(json())
app.use(urlencoded({ extended: false }))
app.use(express.static(join(__dirname, 'public')))

app.use('/dev', logger('dev'), developerRouter)

if (process.env.NODE_ENV == null || process.env.NODE_ENV === 'development') {
  app.use('/', createProxyMiddleware(process.env.APP_SERVER_URL ?? 'http://localhost:8080', {
    secure: false,
    changeOrigin: true,
    ws: true,
  }))
}


export default app
