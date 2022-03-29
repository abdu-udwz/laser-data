/* eslint-disable no-undef */
class Message {
  constructor (owner, content, status, sentOn) {
    this.owner = owner
    this.content = content
    this.status = status
    this.sentOn = sentOn
  }

}

module.exports = Message