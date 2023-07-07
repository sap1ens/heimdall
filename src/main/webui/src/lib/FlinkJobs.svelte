<script>
    import axios from 'axios';
    import { format } from 'date-fns'
    import Fa from 'svelte-fa'
    import { faImagePortrait, faChartColumn, faTable, faIdCard, faClock, faInfo, faGear } from '@fortawesome/free-solid-svg-icons'

    import { appConfig } from './stores/appConfig.js';
    import { settings } from './stores/settings.js';
    import { flinkJobs } from './stores/flinkJobs.js';
    import ExternalEndpoint from './ExternalEndpoint.svelte';
    import JobType from './JobType.svelte';
    import Modal from './Modal.svelte';

    let jobNameFilter;
    let statusFilter;

    let activeSorting;

    let showSettingsModal = false;

    $: visibleFlinkJobs = $flinkJobs.data.filter(job => {
        let nameMatch = true;
        let statusMatch = true;
        if (jobNameFilter) {
            nameMatch = displayName(job).includes(jobNameFilter);
        }
        if (statusFilter) {
            statusMatch = job.status === statusFilter;
        }
        return nameMatch && statusMatch;
    }).sort((a, b) => {
        if (!activeSorting || activeSorting === 'jobNameAsc') {
            return sortGeneric(displayName(a), displayName(b));
        } else if (activeSorting === 'jobNameDesc') {
            return sortGeneric(displayName(b), displayName(a));
        } else if (activeSorting === 'startTimeAsc') {
            return sortNumbers(a.startTime, b.startTime);
        } else if (activeSorting === 'startTimeDesc') {
            return sortNumbers(b.startTime, a.startTime);
        } else if (activeSorting === 'resourcesAsc') {
            return sortNumbers(totalResources(a), totalResources(b));
        } else if (activeSorting === 'resourcesDesc') {
            return sortNumbers(totalResources(b), totalResources(a));
        }
    });

    $: jobStatusList = [...new Set($flinkJobs.data.map(job => job.status))];

    $: displayNamePattern = $appConfig?.patterns?.['display-name'];

    $: $settings.refreshInterval && flinkJobs.setInterval($settings.refreshInterval);

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
        }
        // clean up metadata that was not found
        if (name.includes('$metadata.')) {
            name = name.replace(/.?\$metadata\.[^ ]*/g, '');
        }
        return name;
    }

    function sortGeneric(a, b) {
        if (a < b) return -1;
        if (a > b) return 1;
        return 0;
    }

    function sortNumbers(a, b) {
        if (a == null) return 1;
        if (b == null) return -1;
        return b - a;
    }

    function totalResources(flinkJob) {
        return flinkJob.resources.jm.replicas + flinkJob.resources.tm.replicas;
    }
</script>

<Modal bind:showModal={showSettingsModal}>
    <div>
        Refresh interval:
        <select name="refreshInterval" bind:value={$settings.refreshInterval} class="ml-2">
            <option value="-1">No refresh</option>
            <option value="10">10 sec</option>
            <option value="30">30 sec</option>
            <option value="60">60 sec</option>
            <option value="300">5 min</option>
        </select>
    </div>
    <div class="mt-2.5">
        Display details:
        <div class="mt-1">
            <div>
                <label>
                    <input name="showJobParallelism" type="checkbox" bind:checked={$settings.showJobParallelism} /> Parallelism
                </label>
            </div>
            <div>
                <label>
                    <input name="showJobFlinkVersion" type="checkbox" bind:checked={$settings.showJobFlinkVersion} /> Flink version
                </label>
            </div>
            <div>
                <label>
                    <input name="showJobImage" type="checkbox" bind:checked={$settings.showJobImage} /> Image
                </label>
            </div>
        </div>
    </div>
</Modal>
<div class="flex items-center justify-between py-6 text-base">
    <div>
        Filter by name:
        <input name="jobNameFilter" type="text" placeholder="Flink Job name" bind:value={jobNameFilter}>
        &nbsp;
        Filter by status
        <select name="statusFilter" bind:value={statusFilter}>
            <option value="">Show all</option>
            {#each jobStatusList as status}
                <option value="{status}">{status}</option>
            {/each}
        </select>
    </div>
    <div>
        {#if $settings.displayMode === 'card'}
        <span title="Table view" on:click={() => $settings.displayMode = 'tabular'} class="inline-block">
            <Fa fw icon={faTable} size="2x" class="text-gray-500 hover:cursor-pointer" />
        </span>
        {/if}
        {#if $settings.displayMode === 'tabular'}
        <span title="Card view" on:click={() => $settings.displayMode = 'card'} class="inline-block">
            <Fa fw icon={faIdCard} size="2x" class="text-gray-500 hover:cursor-pointer" />
        </span>
        {/if}
        <span title="Settings" on:click={() => showSettingsModal = true} class="inline-block">
            <Fa fw icon={faGear} size="2x" class="text-gray-500 hover:cursor-pointer ml-1" />
        </span>
    </div>
</div>

{#if $flinkJobs.error}
    <p class="text-xl text-center text-red-500 font-bold">Couldn't load data: {$flinkJobs.error}</p>
{:else}
    {#if visibleFlinkJobs.length > 0 || jobNameFilter || statusFilter}
        {#if $settings.displayMode === 'tabular'}
            <table class="table-auto w-full border">
                <thead class="text-lg">
                <tr class="bg-slate-50">
                    <th class="border border-slate-300 p-2 w-4/12">
                        <div class="flex items-center justify-center">
                            Flink Job
                            <div class="flex flex-col ml-2">
                                <div
                                        class="h-0 w-0 border-x-8 border-x-transparent border-b-[10px] hover:cursor-pointer mb-1"
                                        on:click={() => activeSorting = 'jobNameAsc'}
                                        class:border-b-black={activeSorting !== 'jobNameAsc'}
                                        class:border-b-[#e6516f]={activeSorting === 'jobNameAsc'}
                                ></div>
                                <div
                                        class="h-0 w-0 border-x-8 border-x-transparent border-t-[10px] hover:cursor-pointer"
                                        on:click={() => activeSorting = 'jobNameDesc'}
                                        class:border-t-black={activeSorting !== 'jobNameDesc'}
                                        class:border-t-[#e6516f]={activeSorting === 'jobNameDesc'}
                                ></div>
                            </div>
                        </div>
                    </th>
                    <th class="border border-slate-300 p-2 w-1/12">Status</th>
                    <th class="border border-slate-300 p-2 w-3/12">
                        <div class="flex items-center justify-center">
                            Resources
                            <div class="flex flex-col ml-2">
                                <div
                                        class="h-0 w-0 border-x-8 border-x-transparent border-b-[10px] border-b-black hover:cursor-pointer mb-1"
                                        on:click={() => activeSorting = 'resourcesAsc'}
                                        class:border-b-black={activeSorting !== 'resourcesAsc'}
                                        class:border-b-[#e6516f]={activeSorting === 'resourcesAsc'}
                                ></div>
                                <div
                                        class="h-0 w-0 border-x-8 border-x-transparent border-t-[10px] border-t-black hover:cursor-pointer"
                                        on:click={() => activeSorting = 'resourcesDesc'}
                                        class:border-t-black={activeSorting !== 'resourcesDesc'}
                                        class:border-t-[#e6516f]={activeSorting === 'resourcesDesc'}
                                ></div>
                            </div>
                        </div>
                    </th>
                    <th class="border border-slate-300 p-2 w-2/12">
                        <div class="flex items-center justify-center">
                            Started At
                            <div class="flex flex-col ml-2">
                                <div
                                     class="h-0 w-0 border-x-8 border-x-transparent border-b-[10px] border-b-black hover:cursor-pointer mb-1"
                                     on:click={() => activeSorting = 'startTimeAsc'}
                                     class:border-b-black={activeSorting !== 'startTimeAsc'}
                                     class:border-b-[#e6516f]={activeSorting === 'startTimeAsc'}
                                ></div>
                                <div
                                     class="h-0 w-0 border-x-8 border-x-transparent border-t-[10px] border-t-black hover:cursor-pointer"
                                     on:click={() => activeSorting = 'startTimeDesc'}
                                     class:border-t-black={activeSorting !== 'startTimeDesc'}
                                     class:border-t-[#e6516f]={activeSorting === 'startTimeDesc'}
                                ></div>
                            </div>
                        </div>
                    </th>
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
                                    {#if $settings.showJobParallelism}
                                    <p class="text-sm text-gray-500">
                                        <Fa fw icon={faChartColumn} /> Parallelism: {flinkJob.parallelism || 'N/A' }
                                    </p>
                                    {/if}
                                    {#if $settings.showJobFlinkVersion && flinkJob.flinkVersion}
                                    <p class="text-sm text-gray-500">
                                        <Fa fw icon={faInfo} /> Flink version: {flinkJob.flinkVersion}
                                    </p>
                                    {/if}
                                    {#if $settings.showJobImage}
                                    <p class="text-sm text-gray-500">
                                        <Fa fw icon={faImagePortrait} /> Image: {flinkJob.shortImage || 'N/A'}
                                    </p>
                                    {/if}
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
                        {#if $settings.showJobParallelism}
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faChartColumn} /> Parallelism: {flinkJob.parallelism || 'N/A' }
                        </p>
                        {/if}
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faClock} /> Started at: {formatStartTime(flinkJob.startTime)}
                        </p>
                        {#if $settings.showJobFlinkVersion && flinkJob.flinkVersion}
                            <p class="text-sm text-gray-500">
                                <Fa fw icon={faInfo} /> Flink version: {flinkJob.flinkVersion}
                            </p>
                        {/if}
                        {#if $settings.showJobImage}
                        <p class="text-sm text-gray-500">
                            <Fa fw icon={faImagePortrait} /> Image: {flinkJob.shortImage || 'N/A'}
                        </p>
                        {/if}
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
