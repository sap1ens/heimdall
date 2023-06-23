<script>
    import axios from "axios";
    import { format } from 'date-fns'
    import Fa from 'svelte-fa'
    import { faImagePortrait, faArrowTrendUp, faArrowUpRightFromSquare, faTable, faIdCard, faClock } from '@fortawesome/free-solid-svg-icons'

    import { appConfig } from "./stores.js";

    const API_ROOT = "http://localhost:8080";
    const JOBS_ENDPOINT = `${API_ROOT}/jobs`;

    let loadingError;
    let jobNameFilter;
    let statusFilter;
    let allFlinkJobs = [];
    let initialLoad = false;

    loadFlinkJobs();
    setInterval(loadFlinkJobs, 10000);

    let displayMode = 'tabular';

    $: visibleFlinkJobs = allFlinkJobs.filter(job => {
        if (jobNameFilter) {
            return job.name.includes(jobNameFilter);
        }
        if (statusFilter) {
            return job.status === statusFilter;
        }
        return true;
    });

    $: jobStatusList = [...new Set(allFlinkJobs.map(job => job.status))];

    function loadFlinkJobs() {
        axios.get(JOBS_ENDPOINT)
            .then(function (response) {
                allFlinkJobs = response.data;
                loadingError = null;
                initialLoad = true;
            })
            .catch(function (error) {
                // don't show an error if we already have some loaded jobs
                if (allFlinkJobs.length === 0) {
                    loadingError = error;
                }
            })
    }

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

    function processEndpointPathPattern(pattern, jobName) {
        return pattern.replace('$jobName', jobName);
    }

    function formatStartTime(startTime) {
        if (startTime == null) return '';
        return format(new Date(startTime), 'yyyy-MM-dd HH:mm:ss OOO')
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

{#if loadingError}
    <p class="text-xl text-center text-red-500 font-bold">Couldn't load data: {loadingError}</p>
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
                                    <p>{flinkJob.name}</p>
                                    <p class="text-sm text-gray-500">
                                        <Fa fw icon={faArrowTrendUp} /> Parallelism: {flinkJob.parallelism || 'N/A' }
                                    </p>
                                    <p class="text-sm text-gray-500">
                                        <Fa fw icon={faImagePortrait} /> Image: {flinkJob.shortImage || 'N/A'}
                                    </p>
                                </div>
                                <div>
                                    <p class="ml-1 px-1 border border-gray-500 rounded bg-white" title="Type: {flinkJob.type}">
                                        {#if flinkJob.type === 'APPLICATION'}
                                            A
                                        {:else if flinkJob.type === 'SESSION'}
                                            S
                                        {/if}
                                    </p>
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
                            <p>{flinkJob.resources.jm.replicas} JobManager{#if flinkJob.resources.jm.replicas > 1}s{/if}
                                ({flinkJob.resources.jm.cpu} cpu, {flinkJob.resources.jm.mem} mem)</p>
                            {#if flinkJob.resources.tm.replicas > 0}
                            <p>{flinkJob.resources.tm.replicas} TaskManager{#if flinkJob.resources.tm.replicas > 1}s{/if}
                                ({flinkJob.resources.tm.cpu} cpu, {flinkJob.resources.tm.mem} mem)</p>
                            {/if}
                        </td>
                        <td class="border border-slate-300 p-2">{formatStartTime(flinkJob.startTime)}</td>
                        <td class="border border-slate-300 p-2">
                            <p>
                                <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.['flink-ui'], flinkJob.name)}" target="_blank" class="text-blue-600 mr-1">Flink UI <Fa fw icon={faArrowUpRightFromSquare} /></a>
                                <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.['flink-api'], flinkJob.name)}" target="_blank" class="text-blue-600">Flink API <Fa fw icon={faArrowUpRightFromSquare} /></a>
                            </p>
                            <p>
                                <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.metrics, flinkJob.name)}" target="_blank" class="text-blue-600 mr-1">Metrics <Fa fw icon={faArrowUpRightFromSquare} /></a>
                                <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.logs, flinkJob.name)}" target="_blank" class="text-blue-600">Logs <Fa fw icon={faArrowUpRightFromSquare} /></a>
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
                        <p>{flinkJob.name}</p>
                        <p class="ml-1 px-1 border border-gray-500 rounded bg-white" title="Type: {flinkJob.type}">
                            {#if flinkJob.type === 'APPLICATION'}
                                A
                            {:else if flinkJob.type === 'SESSION'}
                                S
                            {/if}
                        </p>
                    </div>
                    <div class="flex items-center pb-4">
                        <div class="mr-1.5 w-4 h-4 rounded-full bg-{statusColor(flinkJob.status)}-500"></div>
                        {flinkJob.status}
                    </div>
                    <div class="pb-4">
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faArrowTrendUp} /> Parallelism: {flinkJob.parallelism || 'N/A' }
                        </p>
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faClock} /> Started at: {formatStartTime(flinkJob.startTime)}
                        </p>
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faImagePortrait} /> Image: {flinkJob.shortImage || 'N/A'}
                        </p>
                    </div>
                    <p>
                        <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.['flink-ui'], flinkJob.name)}" target="_blank" class="text-blue-600 mr-1">Flink UI <Fa fw icon={faArrowUpRightFromSquare} /></a>
                        <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.['flink-api'], flinkJob.name)}" target="_blank" class="text-blue-600 mr-1">Flink API <Fa fw icon={faArrowUpRightFromSquare} /></a>
                        <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.metrics, flinkJob.name)}" target="_blank" class="text-blue-600 mr-1">Metrics <Fa fw icon={faArrowUpRightFromSquare} /></a>
                        <a href="{processEndpointPathPattern($appConfig?.endpointPathPatterns?.logs, flinkJob.name)}" target="_blank" class="text-blue-600">Logs <Fa fw icon={faArrowUpRightFromSquare} /></a>
                    </p>
                </div>
            {/each}
            </div>
        {/if}
    {:else if initialLoad}
        <p class="text-xl text-center font-bold">No Flink Jobs found</p>
    {:else}
        <p class="text-xl text-center font-bold">Loading...</p>
    {/if}
{/if}
