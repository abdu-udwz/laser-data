<template>
  <VApp>
    <TheAppBar />
    <VMain>
      <TheMessenger />
    </VMain>
  </VApp>
</template>

<script>
import TheMessenger from '@/components/message/TheMessenger.vue'
import TheAppBar from '@/components/TheAppBar.vue'

export default {
  name: 'App',

  components: {
    TheAppBar,
    TheMessenger,
  },

  data: () => ({
    //
  }),
}
</script>
