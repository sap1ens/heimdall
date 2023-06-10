import { readable } from 'svelte/store';
import axios from 'axios';

const API_ENDPOINT = "http://localhost:8080/jobs"

export const flinkJobs = readable(null, function start(set) {
    axios.get(API_ENDPOINT)
        .then(function (response) {
            set(response.data);
        })
        .catch(function (error) {
            // TODO
            console.log(error);
        })

    return function stop() {};
});