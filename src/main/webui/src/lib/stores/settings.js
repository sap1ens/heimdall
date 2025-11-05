import { writable } from 'svelte/store'

const defaults = {
    'refreshInterval': '30', // Refresh interval in seconds (stored as string for localStorage compatibility)
    'displayMode': 'tabular',
    'showJobParallelism': true,
    'showJobFlinkVersion': true,
    'showJobImage': true
};
const stored = JSON.parse(localStorage.getItem('heimdall_settings'));

export const settings = writable(stored || defaults);

settings.subscribe((value) => localStorage.setItem('heimdall_settings', JSON.stringify(value)));
