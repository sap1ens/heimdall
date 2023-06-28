<script>
    import axios from "axios";
    import { format } from 'date-fns'
    import Fa from 'svelte-fa'
    import { faImagePortrait, faChartColumn, faTable, faIdCard, faClock } from '@fortawesome/free-solid-svg-icons'

    import { appConfig } from "./stores/appConfig.js";
    import { flinkJobs } from "./stores/flinkJobs.js";
    import ExternalEndpoint from "./ExternalEndpoint.svelte";
    import JobType from "./JobType.svelte";

    let jobNameFilter;
    let statusFilter;

    let displayMode = 'tabular';

    $: visibleFlinkJobs = $flinkJobs.data.filter(job => {
        if (jobNameFilter) {
            return displayName(job).includes(jobNameFilter);
        }
        if (statusFilter) {
            return job.status === statusFilter;
        }
        return true;
    });

    $: jobStatusList = [...new Set($flinkJobs.data.map(job => job.status))];

    $: displayNamePattern = $appConfig?.patterns?.['display-name'];

    function statusColor(status) {
        switch(status) {
            case 'RUNNING':
                return 'green';
            case 'FAILED':
                return 'red';
            case 'FINISHED':
            case 'UNKNOWN':
                return 'gray';
            default:
                return 'yellow';
        }
    }

    function formatStartTime(startTime) {
        if (startTime == null) return '';
        return format(new Date(startTime), 'yyyy-MM-dd HH:mm:ss OOO')
    }

    function displayName(flinkJob) {
        let name = displayNamePattern.replace('$jobName', flinkJob.name);
        if (Object.keys(flinkJob.metadata).length > 0) {
            for (const [key, value] of Object.entries(flinkJob.metadata)) {
                name = name.replace(`$metadata.${key}`, value);
            }
        } else {
            name = name.replace(/.?\$metadata\.[^ ]*/g, '');
        }
        return name;
    }
</script>

<div class="flex items-center justify-between py-6 text-base">
    <div>
        Filter by name:
        <input type="text" placeholder="Flink Job name" bind:value={jobNameFilter}>
        &nbsp;
        Filter by status
        <select bind:value={statusFilter}>
            <option value="">Show all</option>
            {#each jobStatusList as status}
                <option value="{status}">{status}</option>
            {/each}
        </select>
    </div>
    <div>
        {#if displayMode === 'card'}
        <a href="#" title="Table view" on:click={() => displayMode = 'tabular'}>
            <Fa fw icon={faTable} size="2x" class="text-gray-500 hover:cursor-pointer" />
        </a>
        {/if}
        {#if displayMode === 'tabular'}
        <a href="#" title="Card view" on:click={() => displayMode = 'card'}>
            <Fa fw icon={faIdCard} size="2x" class="text-gray-500 hover:cursor-pointer" />
        </a>
        {/if}
    </div>
</div>

{#if $flinkJobs.error}
    <p class="text-xl text-center text-red-500 font-bold">Couldn't load data: {$flinkJobs.error}</p>
{:else}
    {#if visibleFlinkJobs.length > 0 || jobNameFilter || statusFilter}
        {#if displayMode === 'tabular'}
            <table class="table-auto w-full border">
                <thead class="text-lg">
                <tr class="bg-slate-50">
                    <th class="border border-slate-300 p-2 w-4/12">Flink Job</th>
                    <th class="border border-slate-300 p-2 w-1/12">Status</th>
                    <th class="border border-slate-300 p-2 w-3/12">Resources</th>
                    <th class="border border-slate-300 p-2 w-2/12">Started At</th>
                    <th class="border border-slate-300 p-2 w-2/12">Endpoints</th>
                </tr>
                </thead>
                <tbody class="text-base">
                {#each visibleFlinkJobs as flinkJob (flinkJob.id)}
                    <tr class="odd:bg-white even:bg-slate-50">
                        <td class="border border-slate-300 p-2">
                            <div class="flex items-start justify-between text-lg">
                                <div>
                                    <p>{displayName(flinkJob)}</p>
                                    <p class="text-sm text-gray-500">
                                        <Fa fw icon={faChartColumn} /> Parallelism: {flinkJob.parallelism || 'N/A' }
                                    </p>
                                    <p class="text-sm text-gray-500">
                                        <Fa fw icon={faImagePortrait} /> Image: {flinkJob.shortImage || 'N/A'}
                                    </p>
                                </div>
                                <div>
                                    <JobType type={flinkJob.type} />
                                </div>
                            </div>
                        </td>
                        <td class="border border-slate-300 p-2">
                            <div class="flex items-center">
                                <div class="mr-1.5 w-4 h-4 rounded-full bg-{statusColor(flinkJob.status)}-500"></div>
                                {flinkJob.status}
                            </div>
                        </td>
                        <td class="border border-slate-300 p-2">
                            <p>JobManager{#if flinkJob.resources.jm.replicas > 1}s{/if}:
                                <strong>{flinkJob.resources.jm.replicas}</strong> x {flinkJob.resources.jm.cpu} cpu, {flinkJob.resources.jm.mem} memory</p>
                            {#if flinkJob.resources.tm.replicas > 0}
                            <p>TaskManager{#if flinkJob.resources.tm.replicas > 1}s{/if}:
                                <strong>{flinkJob.resources.tm.replicas}</strong> x {flinkJob.resources.tm.cpu} cpu, {flinkJob.resources.tm.mem} memory</p>
                            {/if}
                        </td>
                        <td class="border border-slate-300 p-2">{formatStartTime(flinkJob.startTime)}</td>
                        <td class="border border-slate-300 p-2">
                            <p>
                                <ExternalEndpoint type="flink-ui" title="Flink UI" jobName="{flinkJob.name}" />
                                <ExternalEndpoint type="flink-api" title="Flink API" jobName="{flinkJob.name}" />
                            </p>
                            <p>
                                <ExternalEndpoint type="metrics" title="Metrics" jobName="{flinkJob.name}" />
                                <ExternalEndpoint type="logs" title="Logs" jobName="{flinkJob.name}" />
                            </p>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        {:else}
            <div class="grid gap-6 grid-cols-3">
            {#each visibleFlinkJobs as flinkJob (flinkJob.id)}
                <div class="border border-slate-300 p-2">
                    <div class="flex items-start justify-between pb-4 text-lg">
                        <p>{displayName(flinkJob)}</p>
                        <JobType type={flinkJob.type} />
                    </div>
                    <div class="flex items-center pb-4">
                        <div class="mr-1.5 w-4 h-4 rounded-full bg-{statusColor(flinkJob.status)}-500"></div>
                        {flinkJob.status}
                    </div>
                    <div class="pb-4">
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faChartColumn} /> Parallelism: {flinkJob.parallelism || 'N/A' }
                        </p>
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faClock} /> Started at: {formatStartTime(flinkJob.startTime)}
                        </p>
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faImagePortrait} /> Image: {flinkJob.shortImage || 'N/A'}
                        </p>
                    </div>
                    <p>
                        <ExternalEndpoint type="flink-ui" title="Flink UI" jobName="{flinkJob.name}" />
                        <ExternalEndpoint type="flink-api" title="Flink API" jobName="{flinkJob.name}" />
                        <ExternalEndpoint type="metrics" title="Metrics" jobName="{flinkJob.name}" />
                        <ExternalEndpoint type="logs" title="Logs" jobName="{flinkJob.name}" />
                    </p>
                </div>
            {/each}
            </div>
        {/if}
    {:else if $flinkJobs.loaded}
        <p class="text-xl text-center font-bold">No Flink Jobs found</p>
    {:else}
        <p class="text-xl text-center font-bold">Loading...</p>
    {/if}
{/if}
