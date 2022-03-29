<template>
  <VCard
    flat
    :color="isSent ? 'primary' :'grey lighten-3' "
    :dark="isSent"
    class="d-flex flex-wrap"
  >
    <VCardText
      class="d-inline pa-2 body-1"
    >
      <!--       main content                -->
      <span>{{ message.content }}</span>

      <div class="d-flex justify-end align-self-end align-items-end">
        <!--     time       -->
        <span class="mx-1 body-2 text-no-wrap">{{ message.sentOn | moment('LT') }}</span>

        <!--   status indicator   -->
        <div v-if="isSent">
          <VProgressCircular
            v-if="message.status === 'PENDING' "
            indeterminate
            size="18"
            width="3"
          />

          <VIcon
            v-else
            small
            color="white"
          >
            mdi-check
          </VIcon>
        </div>
      </div>
    </VCardText>
  </VCard>
</template>

<script>
export default {
  name: 'MessageBase',

  props: {
    message: {
      type: Object,
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      default: (() => {}),
    },
  },

  computed: {
    isSent (){
      return this.message.owner === this.$store.state.identity
    },
  },
}
</script>

<style scoped>

</style>