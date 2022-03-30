<template>
  <VApp>
    <TheAppBar />
    <VMain>
      <TheMessenger />
      <TheBoard 
        v-if="isReceiving"
        :message="bitBuffer"
      />
    </VMain>
  </VApp>
</template>

<script>
import TheMessenger from '@/components/message/TheMessenger.vue'
import TheBoard from '@/components/Board.vue'
import TheAppBar from '@/components/TheAppBar.vue'
import { mapState, mapGetters } from 'vuex'
export default {
  name: 'App',

  components: {
    TheAppBar,
    TheBoard,
    TheMessenger,
  },

  data: () => ({
    //
  }),

  computed: {
    ...mapState(['bitBuffer']),
    ...mapGetters(['isReceiving']),
  },
}
</script>
