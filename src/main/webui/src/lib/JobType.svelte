<script>
    export let type;
</script>

{#if type === 'APPLICATION'}
    <span class="px-2 py-1 rounded text-xs font-semibold bg-blue-100 text-blue-800 border border-blue-300" title="Application Job">
        APP
    </span>
{:else if type === 'SESSION'}
    <span class="px-2 py-1 rounded text-xs font-semibold bg-purple-100 text-purple-800 border border-purple-300" title="Session Job">
        SESSION
    </span>
{/if}
