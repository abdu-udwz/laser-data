<template>
    <v-card
            flat
            :color="isSent ? 'primary' :'grey lighten-3' "
            :dark="isSent"
            class="d-flex flex-wrap"
    >
        <v-card-text
                class="d-inline pa-2 body-1"
        >
            <!--       main content                -->
            <span>{{message.content}}</span>

            <div class="d-flex justify-end align-self-end align-items-end">
                <!--     time       -->
                <span class="mx-1 body-2 text-no-wrap">{{message.sentOn | moment('LT')}}</span>

                <!--   status indicator   -->
                <div v-if="isSent">
                    <v-progress-circular
                            v-if="message.status === 'PENDING' "
                            indeterminate
                            size="18"
                            width="3"
                    ></v-progress-circular>

                    <v-icon
                            v-else
                            small
                            color="white"
                    >
                        mdi-check
                    </v-icon>
                </div>
            </div>
        </v-card-text>
    </v-card>
</template>

<script>
    export default {
        name: "MessageBase",

        props: {
            message: Object,
        },

        computed: {
            isSent(){
                return this.message.owner === this.$store.state.identity;
            }
        }
    }
</script>

<style scoped>

</style>