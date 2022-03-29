<template>
  <VToolbar color="primary">
    <VToolbarTitle>
      <VImg
        alt="Vuetify Name"
        class="shrink mt-1 hidden-sm-and-down"
        contain
        min-width="100"
        src="https://cdn.vuetifyjs.com/images/logos/vuetify-name-dark.png"
        width="100"
      />
    </VToolbarTitle>

    <VSpacer />

    <!--   online status circle     -->
    <VToolbarItems>
      <VIcon
        small
        :color="transceiverOnline ? 'green' : 'grey'"
      >
        mdi-circle
      </VIcon>

      <VBtn
        :loading="receiverPending"
        :disabled="receiverToggleDisabled"
        dark
        icon
        @click="toggleReceiver"
      >
        <VIcon>{{ receiverToggleIcon }}</VIcon>
      </VBtn>

      <VSelect
        v-model="identity"
        :items="identities"
        filled
        flat
        solo
        label="Identity"
        prepend-inner-icon="mdi-account"
        menu-props="offset-y"
        hide-details
        class="mt-1"
      />
    </VToolbarItems>
  </VToolbar>
</template>

<script>
import { mapState, mapGetters } from 'vuex'

export default {
  name: 'TheAppBar',

  data () {
    return {
      identities: [
        {
          text: 'Alpha',
          value: 'ALPHA',
        },
        {
          text: 'Beta',
          value: 'BETA',
        },
      ],
    }
  },

  computed: {
    identity: {
      get: function () {
        return this.$store.state.identity
      },

      set: function (newIdentity) {
        this.$store.dispatch('setIdentity', newIdentity)
      },
    },

    receiverToggleIcon () {
      if (!this.transceiverOnline || !this.isReceiving) {
        return 'mdi-access-point-network-off'
      } else {
        return 'mdi-access-point-network'
      }
    },

    receiverToggleDisabled () {
      if (!this.transceiverOnline) return true

      return this.receiverPending || !(this.isStandBy || this.isReceiving)
    },

    ...mapState(['receiverPending', 'transceiverOnline']),
    ...mapGetters(['isStandBy', 'isReceiving', 'isTransmitting']),
  },

  methods: {
    toggleReceiver () {
      this.$store.dispatch('toggleReceiver')
    },
  },

  sockets: {
    connect () {
      this.$store.dispatch('setIdentity', this.identity)
    },
  },
}
</script>

<style scoped>
</style>