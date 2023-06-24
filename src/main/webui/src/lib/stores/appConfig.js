import { readable } from 'svelte/store';
import axios from 'axios';

const API_ROOT = "http://localhost:8080";
const CONFIG_ENDPOINT = `${API_ROOT}/config`;

export const appConfig = readable(null, function start(set) {
    axios.get(CONFIG_ENDPOINT)
        .then(function (response) {
            set(response.data);
        })
        .catch(function (error) {
            // TODO
            console.log(error);
        })

    return function stop() {};
});
