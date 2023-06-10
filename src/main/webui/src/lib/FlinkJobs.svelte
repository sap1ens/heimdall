<script>
    import { flinkJobs } from './stores.js';

    import { format } from 'date-fns'
    import Fa from 'svelte-fa'
    import { faImagePortrait, faArrowTrendUp } from '@fortawesome/free-solid-svg-icons'


    let displayMode = 'tabular';

    function statusColor(status) {
        switch(status) {
            case 'RUNNING': return 'green';
            case 'FAILED': return 'red';
            case 'FINISHED': return 'gray';
            default: return 'yellow';
        }
    }
</script>

{#if $flinkJobs}
    {#if displayMode === 'tabular'}
        <table class="table-auto w-full border text-lg">
            <thead>
            <tr>
                <th class="border border-slate-300 p-2 w-2/5">Flink Job</th>
                <th class="border border-slate-300 p-2">Status</th>
                <th class="border border-slate-300 p-2">Started At</th>
                <th class="border border-slate-300 p-2 w-1/5">Actions</th>
            </tr>
            </thead>
            <tbody>
            {#each $flinkJobs as flinkJob (flinkJob.id)}
                <tr>
                    <td class="border border-slate-300 p-2">
                        {flinkJob.name}
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faArrowTrendUp} /> Parallelism: {flinkJob.parallelism || 'N/A' }
                        </p>
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faImagePortrait} /> Image: {flinkJob.shortImage || 'N/A'}
                        </p>
                    </td>
                    <td class="border border-slate-300 p-2">
                        <div class="flex items-center">
                            <div class="mr-1.5 w-4 h-4 rounded-full bg-{statusColor(flinkJob.status)}-500"></div>
                            {flinkJob.status}
                        </div>
                    </td>
                    <td class="border border-slate-300 p-2">{format(new Date(flinkJob.startTime), 'yyyy-MM-dd HH:mm:ss OOO')}</td>
                    <td class="border border-slate-300 p-2">TODO</td>
                </tr>
            {/each}
            </tbody>
        </table>
    {:else}

    {/if}

{:else}
    <p>Loading...</p>
{/if}
