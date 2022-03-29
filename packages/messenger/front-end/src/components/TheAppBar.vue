<template>
  <VToolbar 
    color="primary"
  >
    <VToolbarTitle class="white--text">
      LaserData
    </VToolbarTitle>

    <VSpacer />

    <!--   online status circle     -->
    <VToolbarItems>
      <VIcon
        small
        :color="transceiverOnline ? 'green' : 'grey'"
      >
        {{ mdiCircle }}
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
        :prepend-inner-icon="mdiAccount"
        menu-props="offset-y"
        hide-details
        class="mt-1"
      />
    </VToolbarItems>
  </VToolbar>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import { mdiAccessPointNetworkOff,
  mdiAccessPointNetwork,
  mdiCircle,
  mdiAccount, 
} from '@mdi/js'

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
        return mdiAccessPointNetworkOff
      } else {
        return mdiAccessPointNetwork
      }
    },

    receiverToggleDisabled () {
      if (!this.transceiverOnline) return true

      return this.receiverPending || !(this.isStandBy || this.isReceiving)
    },

    ...mapState(['receiverPending', 'transceiverOnline']),
    ...mapGetters(['isStandBy', 'isReceiving', 'isTransmitting']),
  },

  created () {
    this.mdiCircle = mdiCircle
    this.mdiAccount = mdiAccount
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