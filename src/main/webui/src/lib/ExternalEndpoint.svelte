<script>
import {faArrowUpRightFromSquare} from '@fortawesome/free-solid-svg-icons';
import Fa from 'svelte-fa';

import { appConfig } from './stores/appConfig.js';

export let jobName;
export let type;
export let title;

$: endpointPathPattern = $appConfig?.endpointPathPatterns?.[type];

function processEndpointPathPattern(pattern, jobName) {
    return pattern.replace('$jobName', jobName);
}
</script>

{#if endpointPathPattern}
    <a href="{processEndpointPathPattern(endpointPathPattern, jobName)}" target="_blank"
       class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-gradient-to-r from-accent-500 to-blue-500 hover:from-accent-600 hover:to-blue-600 text-white text-sm font-medium rounded-lg shadow-sm hover:shadow-md transition-all duration-200 transform hover:-translate-y-0.5">
        {title}
        <Fa fw icon={faArrowUpRightFromSquare} size="sm" />
    </a>
{/if}
