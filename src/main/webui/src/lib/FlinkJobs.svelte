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
    let namespaceFilter;

    let activeSorting;

    let showSettingsModal = false;

    $: visibleFlinkJobs = $flinkJobs.data.filter(job => {
        let nameMatch = true;
        let statusMatch = true;
        let namespaceMatch = true;
        if (jobNameFilter) {
            nameMatch = displayName(job).includes(jobNameFilter);
        }
        if (statusFilter) {
            statusMatch = job.status === statusFilter;
        }
        if (namespaceFilter) {
            namespaceMatch = job.namespace === namespaceFilter;
        }
        return nameMatch && statusMatch && namespaceMatch;
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
        } else if (activeSorting === 'namespaceAsc') {
            return sortGeneric(a.namespace, b.namespace);
        } else if (activeSorting === 'namespaceDesc') {
            return sortGeneric(b.namespace, a.namespace);
        }
    });

    $: jobStatusList = [...new Set($flinkJobs.data.map(job => job.status))];
    $: jobNamespaceList = [...new Set($flinkJobs.data.map(job => job.namespace))].sort();

    $: displayNamePattern = $appConfig?.patterns?.['display-name'];

    $: $settings.refreshInterval && flinkJobs.setInterval($settings.refreshInterval);

    function statusColor(status) {
        switch(status) {
            case 'RUNNING':
                return 'emerald';
            case 'FAILED':
                return 'rose';
            case 'FINISHED':
            case 'UNKNOWN':
                return 'slate';
            default:
                return 'amber';
        }
    }

    function formatStartTime(startTime) {
        if (startTime == null) return '';
        return format(new Date(startTime), 'yyyy-MM-dd HH:mm:ss OOO')
    }

    function displayName(flinkJob) {
        // Fallback to just the job name if displayNamePattern is not loaded yet
        if (!displayNamePattern) {
            return flinkJob.name;
        }

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
    <div class="space-y-4">
        <div>
            <label for="refreshInterval" class="block text-sm font-semibold text-gray-700 mb-2">Refresh Interval</label>
            <select id="refreshInterval" name="refreshInterval" bind:value={$settings.refreshInterval}
                    class="w-full input-modern">
                <option value="-1">No refresh</option>
                <option value="10">10 seconds</option>
                <option value="30">30 seconds</option>
                <option value="60">1 minute</option>
                <option value="300">5 minutes</option>
            </select>
        </div>
        <div>
            <p class="block text-sm font-semibold text-gray-700 mb-3">Display Details</p>
            <div class="space-y-2">
                <label class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
                    <input name="showJobParallelism" type="checkbox" bind:checked={$settings.showJobParallelism}
                           class="w-4 h-4 text-primary-600 rounded focus:ring-primary-500" />
                    <span class="text-sm text-gray-700">Show Parallelism</span>
                </label>
                <label class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
                    <input name="showJobFlinkVersion" type="checkbox" bind:checked={$settings.showJobFlinkVersion}
                           class="w-4 h-4 text-primary-600 rounded focus:ring-primary-500" />
                    <span class="text-sm text-gray-700">Show Flink Version</span>
                </label>
                <label class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
                    <input name="showJobImage" type="checkbox" bind:checked={$settings.showJobImage}
                           class="w-4 h-4 text-primary-600 rounded focus:ring-primary-500" />
                    <span class="text-sm text-gray-700">Show Image</span>
                </label>
            </div>
        </div>
    </div>
</Modal>
<div class="flex items-center justify-between py-4 text-base flex-wrap gap-4">
    <div class="flex items-center gap-3 flex-wrap">
        <div>
            <label for="jobNameFilter" class="text-sm font-medium text-gray-700 mr-2">Filter by name:</label>
            <input id="jobNameFilter" name="jobNameFilter" type="text" placeholder="Flink Job name" bind:value={jobNameFilter}
                   class="input-modern">
        </div>
        <div>
            <label for="statusFilter" class="text-sm font-medium text-gray-700 mr-2">Status:</label>
            <select id="statusFilter" name="statusFilter" bind:value={statusFilter}
                    class="input-modern">
                <option value="">Show all</option>
                {#each jobStatusList as status}
                    <option value="{status}">{status}</option>
                {/each}
            </select>
        </div>
        <div>
            <label for="namespaceFilter" class="text-sm font-medium text-gray-700 mr-2">Namespace:</label>
            <select id="namespaceFilter" name="namespaceFilter" bind:value={namespaceFilter}
                    class="input-modern">
                <option value="">Show all</option>
                {#each jobNamespaceList as namespace}
                    <option value="{namespace}">{namespace}</option>
                {/each}
            </select>
        </div>
    </div>
    <div class="flex items-center gap-2">
        {#if $settings.displayMode === 'card'}
        <button type="button" title="Table view" on:click={() => $settings.displayMode = 'tabular'} class="inline-block cursor-pointer bg-transparent border-0 p-0 m-0">
            <Fa fw icon={faTable} size="2x" class="text-gray-500 hover:text-gray-700" />
        </button>
        {/if}
        {#if $settings.displayMode === 'tabular'}
        <button type="button" title="Card view" on:click={() => $settings.displayMode = 'card'} class="inline-block cursor-pointer bg-transparent border-0 p-0 m-0">
            <Fa fw icon={faIdCard} size="2x" class="text-gray-500 hover:text-gray-700" />
        </button>
        {/if}
        <button type="button" title="Settings" on:click={() => showSettingsModal = true} class="inline-block cursor-pointer ml-1 bg-transparent border-0 p-0 m-0">
            <Fa fw icon={faGear} size="2x" class="text-gray-500 hover:text-gray-700" />
        </button>
    </div>
</div>

{#if $flinkJobs.error}
    <div class="bg-red-50 border-l-4 border-red-500 rounded-lg p-6 mt-6 shadow-md">
        <p class="text-xl text-red-700 font-semibold">⚠️ Error Loading Data</p>
        <p class="text-red-600 mt-2">{$flinkJobs.error}</p>
    </div>
{:else}
    {#if visibleFlinkJobs.length > 0 || jobNameFilter || statusFilter}
        {#if $settings.displayMode === 'tabular'}
            <table class="table-auto w-full border">
                <thead class="text-lg">
                <tr class="bg-slate-50">
                    <th class="border border-slate-300 p-2 w-3/12">
                        <div class="flex items-center justify-center font-semibold text-gray-700">
                            Flink Job
                            <div class="flex flex-col ml-2">
                                <div
                                        role="button"
                                        tabindex="0"
                                        aria-label="Sort by job name ascending"
                                        class="h-0 w-0 border-x-8 border-x-transparent border-b-[10px] hover:cursor-pointer mb-1 transition-colors"
                                        on:click={() => activeSorting = 'jobNameAsc'}
                                        on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'jobNameAsc')}
                                        class:border-b-gray-400={activeSorting !== 'jobNameAsc'}
                                        class:border-b-primary-600={activeSorting === 'jobNameAsc'}
                                ></div>
                                <div
                                        role="button"
                                        tabindex="0"
                                        aria-label="Sort by job name descending"
                                        class="h-0 w-0 border-x-8 border-x-transparent border-t-[10px] hover:cursor-pointer transition-colors"
                                        on:click={() => activeSorting = 'jobNameDesc'}
                                        on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'jobNameDesc')}
                                        class:border-t-gray-400={activeSorting !== 'jobNameDesc'}
                                        class:border-t-primary-600={activeSorting === 'jobNameDesc'}
                                ></div>
                            </div>
                        </div>
                    </th>
                    <th class="border-b-2 border-gray-200 p-4 w-1/12">
                        <div class="flex items-center justify-center font-semibold text-gray-700">
                            Namespace
                            <div class="flex flex-col ml-2">
                                <div
                                        role="button"
                                        tabindex="0"
                                        aria-label="Sort by namespace ascending"
                                        class="h-0 w-0 border-x-8 border-x-transparent border-b-[10px] hover:cursor-pointer mb-1 transition-colors"
                                        on:click={() => activeSorting = 'namespaceAsc'}
                                        on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'namespaceAsc')}
                                        class:border-b-gray-400={activeSorting !== 'namespaceAsc'}
                                        class:border-b-primary-600={activeSorting === 'namespaceAsc'}
                                ></div>
                                <div
                                        role="button"
                                        tabindex="0"
                                        aria-label="Sort by namespace descending"
                                        class="h-0 w-0 border-x-8 border-x-transparent border-t-[10px] hover:cursor-pointer transition-colors"
                                        on:click={() => activeSorting = 'namespaceDesc'}
                                        on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'namespaceDesc')}
                                        class:border-t-gray-400={activeSorting !== 'namespaceDesc'}
                                        class:border-t-primary-600={activeSorting === 'namespaceDesc'}
                                ></div>
                            </div>
                        </div>
                    </th>
                    <th class="border-b-2 border-gray-200 p-4 w-1/12 font-semibold text-gray-700">Status</th>
                    <th class="border-b-2 border-gray-200 p-4 w-3/12">
                        <div class="flex items-center justify-center font-semibold text-gray-700">
                            Resources
                            <div class="flex flex-col ml-2">
                                <div
                                        role="button"
                                        tabindex="0"
                                        aria-label="Sort by resources ascending"
                                        class="h-0 w-0 border-x-8 border-x-transparent border-b-[10px] hover:cursor-pointer mb-1 transition-colors"
                                        on:click={() => activeSorting = 'resourcesAsc'}
                                        on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'resourcesAsc')}
                                        class:border-b-gray-400={activeSorting !== 'resourcesAsc'}
                                        class:border-b-primary-600={activeSorting === 'resourcesAsc'}
                                ></div>
                                <div
                                        role="button"
                                        tabindex="0"
                                        aria-label="Sort by resources descending"
                                        class="h-0 w-0 border-x-8 border-x-transparent border-t-[10px] hover:cursor-pointer transition-colors"
                                        on:click={() => activeSorting = 'resourcesDesc'}
                                        on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'resourcesDesc')}
                                        class:border-t-gray-400={activeSorting !== 'resourcesDesc'}
                                        class:border-t-primary-600={activeSorting === 'resourcesDesc'}
                                ></div>
                            </div>
                        </div>
                    </th>
                    <th class="border-b-2 border-gray-200 p-4 w-2/12">
                        <div class="flex items-center justify-center font-semibold text-gray-700">
                            Started At
                            <div class="flex flex-col ml-2">
                                <div
                                     role="button"
                                     tabindex="0"
                                     aria-label="Sort by start time ascending"
                                     class="h-0 w-0 border-x-8 border-x-transparent border-b-[10px] hover:cursor-pointer mb-1 transition-colors"
                                     on:click={() => activeSorting = 'startTimeAsc'}
                                     on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'startTimeAsc')}
                                     class:border-b-gray-400={activeSorting !== 'startTimeAsc'}
                                     class:border-b-primary-600={activeSorting === 'startTimeAsc'}
                                ></div>
                                <div
                                     role="button"
                                     tabindex="0"
                                     aria-label="Sort by start time descending"
                                     class="h-0 w-0 border-x-8 border-x-transparent border-t-[10px] hover:cursor-pointer transition-colors"
                                     on:click={() => activeSorting = 'startTimeDesc'}
                                     on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && (activeSorting = 'startTimeDesc')}
                                     class:border-t-gray-400={activeSorting !== 'startTimeDesc'}
                                     class:border-t-primary-600={activeSorting === 'startTimeDesc'}
                                ></div>
                            </div>
                        </div>
                    </th>
                    <th class="border-b-2 border-gray-200 p-4 w-2/12 font-semibold text-gray-700">Endpoints</th>
                </tr>
                </thead>
                <tbody class="text-base">
                {#each visibleFlinkJobs as flinkJob (flinkJob.id)}
                    <tr class="hover:bg-gray-50 transition-colors border-b border-gray-100">
                        <td class="p-4">
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
                        <td class="p-4">
                            <span class="bg-gray-100 text-gray-800 px-2 py-1 rounded text-sm font-mono">{flinkJob.namespace}</span>
                        </td>
                        <td class="p-4">
                            <div class="flex items-center">
                                <div class="mr-1.5 w-3 h-3 rounded-full bg-{statusColor(flinkJob.status)}-500"></div>
                                {flinkJob.status}
                            </div>
                        </td>
                        <td class="p-4">
                            <p>JobManager{#if flinkJob.resources.jm.replicas > 1}s{/if}:
                                <strong>{flinkJob.resources.jm.replicas}</strong> x {flinkJob.resources.jm.cpu} cpu, {flinkJob.resources.jm.mem} memory</p>
                            {#if flinkJob.resources.tm.replicas > 0}
                            <p>TaskManager{#if flinkJob.resources.tm.replicas > 1}s{/if}:
                                <strong>{flinkJob.resources.tm.replicas}</strong> x {flinkJob.resources.tm.cpu} cpu, {flinkJob.resources.tm.mem} memory</p>
                            {/if}
                        </td>
                        <td class="p-4">
                            <span class="text-gray-600 text-sm">{formatStartTime(flinkJob.startTime)}</span>
                        </td>
                        <td class="p-4">
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
                        <div>
                            <p>{displayName(flinkJob)}</p>
                            <p class="text-sm text-gray-500 mt-1">
                                <span class="bg-gray-100 text-gray-800 px-2 py-1 rounded text-xs font-mono">{flinkJob.namespace}</span>
                            </p>
                        </div>
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
        <div class="bg-white rounded-xl shadow-md p-12 mt-6 text-center">
            <div class="text-6xl mb-4">📊</div>
            <p class="text-2xl font-bold text-gray-700 mb-2">No Flink Jobs Found</p>
            <p class="text-gray-500">There are no jobs matching your criteria</p>
        </div>
    {:else}
        <div class="bg-white rounded-xl shadow-md p-12 mt-6 text-center">
            <div class="inline-block animate-spin text-6xl mb-4">⚙️</div>
            <p class="text-2xl font-bold text-gray-700">Loading Jobs...</p>
            <p class="text-gray-500 mt-2">Please wait while we fetch your data</p>
        </div>
    {/if}
{/if}
