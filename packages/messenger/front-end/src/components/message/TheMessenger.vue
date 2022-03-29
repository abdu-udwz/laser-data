<template>
  <VContainer>
    <VCard
      height="85vh"
      class="d-flex"
    >
      <VCardText class="flex-grow-1 d-flex flex-column">
        <!--    messages container        -->
        <MessageContainer
          class="flex-grow-1"
          :messages="messages"
        />

        <VDivider class="my-1" />

        <div class="d-flex flex-column mb-2 mx-2">
          <VAlert
            v-model="showAlert"
            dense
            type="error"
            :icon="false"
            transition="scale-transition"
          >
            Please wait for the message to arrive first
          </VAlert>
          <!-- progress bar-->

          <template v-if="isTransmitting">
            <div class="d-flex justify-end">
              <span class="mx-2">{{ progress }}% ({{
                `${transmitProgress.sentBytes} of ${transmitProgress.totalBytes}`
              }})</span>
              <span>{{ transmitProgress.remainingSeconds.toFixed(2) }} sec
                left</span>
            </div>
            <VProgressLinear
              v-model="progress"
              class="mb-2"
              height="8"
              rounded
            />
          </template>

          <!--   input box         -->

          <VTextField
            v-model="input"
            :disabled="inputDisabled"
            flat
            solo
            filled
            dense
            single-line
            hide-details
            placeholder="Type here.."
            append-icon="mdi-send"
            @keydown="onKeyDown"
            @click:append="send"
          />
        </div>
      </VCardText>
    </VCard>
  </VContainer>
</template>

<script>
import MessageContainer from './MessageContainer.vue'
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'TheMessenger',

  components: {
    MessageContainer,
  },

  data () {
    return {
      input: null,
      showAlert: false,
    }
  },

  computed: {
    messages () {
      return this.$store.state.messages
    },

    inputDisabled () {
      return !this.isStandBy || !this.$store.state.transceiverOnline
    },

    progress () {
      let percentage =
        (this.transmitProgress.sentBytes / this.transmitProgress.totalBytes) *
        100
      return Math.ceil(percentage)
    },
    ...mapState(['transmitProgress']),
    ...mapGetters(['isStandBy', 'isTransmitting', 'isReceiving']),
  },

  methods: {
    send () {
      let message = this.input
      if (!message || !message.trim()) return
      message = message.trim()

      this.$store.dispatch('sendMessage', message)
      this.input = ''
    },

    onKeyDown (event) {
      if (event.key === 'Enter') {
        this.send()
      }
    },
  },
}
</script>

<style scoped>
</style>