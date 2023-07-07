import { writable } from 'svelte/store'
import axios from 'axios';

let allFlinkJobs = [];

function createDataStore() {
    let intervalId;
    const {set, subscribe } = writable({data: [], loaded: false}, () => {
        return () => {
            clearInterval(intervalId);
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

    return {
        subscribe,
        setInterval: function(intervalSec) {
            intervalSec = parseInt(intervalSec);
            clearInterval(intervalId);
            if (intervalSec > 0) {
                intervalId = setInterval(loadJobs, intervalSec * 1000);
            }
        }
    }
}

export const flinkJobs = createDataStore()
