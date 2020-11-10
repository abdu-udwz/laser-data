<template>
    <v-card
            color="primary dark-2"
            dark
    >
        <v-card-text class="py-2 text-center">
            {{date | moment('LL')}}
        </v-card-text>
    </v-card>
</template>

<script>
    export default {
        name: "MessageDaySection",

        props: {
            date: Date,
        }
    }
</script>

<style scoped>

</style>