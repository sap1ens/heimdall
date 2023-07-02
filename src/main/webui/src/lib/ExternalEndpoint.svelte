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
    <a href="{processEndpointPathPattern(endpointPathPattern, jobName)}" target="_blank" class="text-blue-600 mr-1">
        {title} <Fa fw icon={faArrowUpRightFromSquare} />
    </a>
{/if}
