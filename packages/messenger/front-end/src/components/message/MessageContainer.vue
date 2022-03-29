<template>
  <div
    class="d-flex flex-column px-3 overflow-y overflow-x-hidden"
    ref="container"
  >
    <template v-for="(item, index) of displayItems">
      <div v-if="item.type === 'DAY_DIVIDER'" :key="index" class="my-2">
        <message-day-divider
          :date="item.date"
          class="mx-12"
        ></message-day-divider>
      </div>

      <div
        v-if="item.type === 'MESSAGE'"
        :key="index"
        class="d-flex my-1"
        :class="getMessageCardClass(item)"
      >
        <message-card :message="item"></message-card>
      </div>
    </template>
  </div>
</template>

<script>
import MessageCard from "./MessageCard.vue";
import MessageDayDivider from "./MessageDayDivider.vue";

export default {
  name: "MessageWindowMessageContainer",

  components: {
    MessageCard,
    MessageDayDivider,
  },

  props: {
    messages: Array,
    default: () => [],
  },

  computed: {
    displayItems() {
      let items = [];

      // by using timestamp of 1970 we make sure the first message
      // will have a day divider
      let lastDate = new Date(1);
      this.messages.forEach((message) => {
        let messageDate = new Date(message.sentOn);
        if (messageDate.toDateString() !== lastDate.toDateString()) {
          items.push({ type: "DAY_DIVIDER", date: messageDate });
          lastDate = messageDate;
        }

        items.push({
          type: "MESSAGE",
          ...message,
        });
      });

      return items;
    },
  },

  methods: {
    getMessageCardClass(message) {
      let isSent = message.owner === this.$store.state.identity;
      return {
        "pe-4 justify-start": !isSent,
        "ps-4 justify-end": isSent,
      };
    },
  },

  watch: {
    displayItems() {
      this.$refs.container.scrollTop = this.$refs.container.scrollHeight;
    },
  },
};
</script>

<style scoped>
</style>