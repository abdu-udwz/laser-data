<template>
  <VAppBar 
    app
    color="primary"
  >
    <VToolbarTitle class="white--text">
      LaserData
    </VToolbarTitle>

    <VSpacer />

    <!--   online status circle     -->
    <VBadge
      :color="transceiverOnline ? 'green' : 'grey'"
      left
      dot
      offset-x="22"
      offset-y="22"
    >
      <VBtn
        class="mt-2 mx-2"
        color="white"
        :loading="receiverPending"
        :disabled="receiverToggleDisabled"
        icon
        @click="toggleReceiver"
      >
        <VIcon>{{ receiverToggleIcon }}</VIcon>
      </VBtn>
    </VBadge>
    <VToolbarItems>
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
  </VAppBar>
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