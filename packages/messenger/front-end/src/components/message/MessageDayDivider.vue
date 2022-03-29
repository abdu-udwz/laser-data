<template>
  <VCard
    color="primary dark-2"
    dark
  >
    <VCardText class="py-2 text-center">
      {{ date | moment('LL') }}
    </VCardText>
  </VCard>
</template>

<script>
export default {
  name: 'MessageDaySection',

  props: {
    date: Date,
  },
}
</script>

<style scoped>

</style>