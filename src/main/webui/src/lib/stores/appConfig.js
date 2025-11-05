import { readable } from 'svelte/store';
import axios from 'axios';

export const appConfig = readable(null, function start(set) {
    axios.get('config')
        .then(function (response) {
            set(response.data);
        })
        .catch(function (error) {
            console.error('Failed to load application config:', error.message || error);
            // Set a fallback config or error state
            set({
                error: 'Failed to load configuration. Some features may not work correctly.',
                loaded: false
            });
        })

    return function stop() {};
});
