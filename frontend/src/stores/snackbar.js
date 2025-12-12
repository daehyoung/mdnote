import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useSnackbarStore = defineStore('snackbar', () => {
    const show = ref(false);
    const message = ref('');
    const color = ref('error');
    const timeout = ref(3000);

    const showMessage = (msg, type = 'error', duration = 3000) => {
        message.value = msg;
        color.value = type;
        timeout.value = duration;
        show.value = true;
    };

    const showError = (msg) => {
        showMessage(msg, 'error');
    };

    const showSuccess = (msg) => {
        showMessage(msg, 'success');
    };

    return {
        show,
        message,
        color,
        timeout,
        showMessage,
        showError,
        showSuccess
    };
});
