import { readable } from 'svelte/store';
import axios from 'axios';

export const appConfig = readable(null, function start(set) {
    axios.get('config')
        .then(function (response) {
            set(response.data);
        })
        .catch(function (error) {
            // TODO
            console.log(error);
        })

    return function stop() {};
});
