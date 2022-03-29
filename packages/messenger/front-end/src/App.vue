<template>
  <v-app>
    <the-app-bar></the-app-bar>
    <v-main>
      <the-messenger></the-messenger>
    </v-main>
  </v-app>
</template>

<script>
import TheMessenger from "@/components/message/TheMessenger.vue";
import TheAppBar from "@/components/TheAppBar.vue";

export default {
  name: "App",

  components: {
    TheAppBar,
    TheMessenger,
  },

  data: () => ({
    //
  }),
};
</script>
