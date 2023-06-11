<script>
    import axios from "axios";
    import { format } from 'date-fns'
    import Fa from 'svelte-fa'
    import { faImagePortrait, faArrowTrendUp, faArrowUpRightFromSquare } from '@fortawesome/free-solid-svg-icons'

    const API_ENDPOINT = "http://localhost:8080/jobs";

    let loadingError;
    let jobNameFilter;
    let statusFilter;
    let allFlinkJobs = [];

    axios.get(API_ENDPOINT)
        .then(function (response) {
            allFlinkJobs = response.data;
        })
        .catch(function (error) {
            loadingError = error;
        })

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
</script>

<div class="py-6 text-base">
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

{#if loadingError}
    <p class="text-xl text-center text-red-500 font-bold">Couldn't load data: {loadingError}</p>
{:else}
    {#if visibleFlinkJobs.length > 0 || jobNameFilter || statusFilter}
        {#if displayMode === 'tabular'}
            <table class="table-auto w-full border">
                <thead class="text-lg">
                <tr>
                    <th class="border border-slate-300 p-2 w-4/12">Flink Job</th>
                    <th class="border border-slate-300 p-2 w-1/12">Status</th>
                    <th class="border border-slate-300 p-2 w-3/12">Resources</th>
                    <th class="border border-slate-300 p-2 w-2/12">Started At</th>
                    <th class="border border-slate-300 p-2 w-2/12">Endpoints</th>
                </tr>
                </thead>
                <tbody class="text-base">
                {#each visibleFlinkJobs as flinkJob (flinkJob.id)}
                    <tr>
                        <td class="border border-slate-300 p-2">
                            <p class="text-lg">{flinkJob.name}</p>
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
                        <td class="border border-slate-300 p-2">
                            <p>1 JM (0.5 CPU, 1024M RAM)</p>
                            <p>2 TM (1 CPU, 2048M RAM)</p>
                        </td>
                        <td class="border border-slate-300 p-2">{formatStartTime(flinkJob.startTime)}</td>
                        <td class="border border-slate-300 p-2">
                            <p>
                                <a href="" class="text-blue-600">Flink UI <Fa fw icon={faArrowUpRightFromSquare} /></a>
                                <a href="" class="text-blue-600">Flink API <Fa fw icon={faArrowUpRightFromSquare} /></a>
                            </p>
                            <p>
                                <a href="" class="text-blue-600">Metrics <Fa fw icon={faArrowUpRightFromSquare} /></a>
                                <a href="" class="text-blue-600">Logs <Fa fw icon={faArrowUpRightFromSquare} /></a>
                            </p>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        {/if}
    {:else}
        <p class="text-xl text-center font-bold">Loading...</p>
    {/if}
{/if}
