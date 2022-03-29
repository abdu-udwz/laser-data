const express = require('express')
// middleware
const { createProxyMiddleware } = require('http-proxy-middleware')
const logger = require('morgan')
const developerRouter = require('./routes/developer')
// util
const path = require('path')

const MessengerController = require('./controllers/MessengerController')

const app = express()

// view engine setup

app.use(express.json())
app.use(express.urlencoded({ extended: false }))
app.use(express.static(path.join(__dirname, 'public')))

app.use('/dev', logger('dev'), developerRouter);

if (process.env.NODE_ENV == null || process.env.NODE_ENV === 'development') {
  app.use('/', createProxyMiddleware(process.env.APP_SERVER_URL ?? 'http://localhost:8080', {
    secure: false,
    changeOrigin: true,
    ws: true,
  }))
}


module.exports = app;
