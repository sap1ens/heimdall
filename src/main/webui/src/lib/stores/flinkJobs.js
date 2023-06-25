import { writable } from 'svelte/store'
import axios from "axios";

const REFRESH_INTERVAL_MS = 10000;

let allFlinkJobs = [];

function createDataStore() {
    let intervalId;
    const {set, subscribe } = writable({data: [], loaded: false}, () => {
        return () => {
            clearInterval(intervalId)
        }
    });

    function loadJobs() {
        axios.get('jobs')
            .then(function (response) {
                allFlinkJobs = response.data;
                set({
                    data: allFlinkJobs,
                    error: null,
                    loaded: true,
                });
            })
            .catch(function (error) {
                // don't show an error if we already have some loaded jobs
                if (allFlinkJobs.length === 0) {
                    set({
                        data: allFlinkJobs,
                        error: error,
                        loaded: true,
                    });
                }
            })
    }

    loadJobs();
    intervalId = setInterval(loadJobs, REFRESH_INTERVAL_MS);

    return {
        subscribe
    }
}

export const flinkJobs = createDataStore()
