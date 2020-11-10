<template>
    <v-app>
        <the-app-bar></the-app-bar>
        <v-content>
            <the-messenger></the-messenger>
        </v-content>
    </v-app>
</template>

<script>

    import TheMessenger from "@/components/message/TheMessenger";
    import TheAppBar from "@/components/TheAppBar";

    export default {
        name: 'App',

        components: {
            TheAppBar,
            TheMessenger
        },

        data: () => ({
            //
        }),
    };
</script>
