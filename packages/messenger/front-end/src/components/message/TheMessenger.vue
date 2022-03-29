<template>
  <v-container>
    <v-card height="85vh" class="d-flex">
      <v-card-text class="flex-grow-1 d-flex flex-column">
        <!--    messages container        -->
        <message-container class="flex-grow-1" :messages="messages">
        </message-container>

        <v-divider class="my-1"></v-divider>

        <div class="d-flex flex-column mb-2 mx-2">
          <v-alert
            dense
            type="error"
            :icon="false"
            v-model="showAlert"
            transition="scale-transition"
          >
            Please wait for the message to arrive first
          </v-alert>
          <!-- progress bar-->

          <template v-if="isTransmitting">
            <div class="d-flex justify-end">
              <span class="mx-2"
                >{{ progress }}% ({{
                  `${transmitProgress.sentBytes} of ${transmitProgress.totalBytes}`
                }})</span
              >
              <span
                >{{ transmitProgress.remainingSeconds.toFixed(2) }} sec
                left</span
              >
            </div>
            <v-progress-linear
              class="mb-2"
              height="8"
              rounded
              v-model="progress"
            >
            </v-progress-linear>
          </template>

          <!--   input box         -->

          <v-text-field
            :disabled="inputDisabled"
            flat
            solo
            filled
            dense
            single-line
            hide-details
            placeholder="Type here.."
            append-icon="mdi-send"
            v-model="input"
            @keydown="onKeyDown"
            @click:append="send"
          >
          </v-text-field>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import MessageContainer from "./MessageContainer.vue";
import { mapGetters, mapState } from "vuex";

export default {
  name: "TheMessenger",

  components: {
    MessageContainer,
  },

  data() {
    return {
      input: null,
      showAlert: false,
    };
  },

  computed: {
    messages() {
      return this.$store.state.messages;
    },

    inputDisabled() {
      return !this.isStandBy || !this.$store.state.transceiverOnline;
    },

    progress() {
      let percentage =
        (this.transmitProgress.sentBytes / this.transmitProgress.totalBytes) *
        100;
      return Math.ceil(percentage);
    },
    ...mapState(["transmitProgress"]),
    ...mapGetters(["isStandBy", "isTransmitting", "isReceiving"]),
  },

  methods: {
    send() {
      let message = this.input;
      if (!message || !message.trim()) return;
      message = message.trim();

      this.$store.dispatch("sendMessage", message);
      this.input = "";
    },

    onKeyDown(event) {
      if (event.key === "Enter") {
        this.send();
      }
    },
  },
};
</script>

<style scoped>
</style>