<script>
    export let type;
</script>

{#if type === 'APPLICATION'}
    <span class="px-3 py-1 rounded-full text-xs font-bold bg-gradient-to-r from-purple-500 to-indigo-500 text-white shadow-sm" title="Application Job">
        APP
    </span>
{:else if type === 'SESSION'}
    <span class="px-3 py-1 rounded-full text-xs font-bold bg-gradient-to-r from-blue-500 to-cyan-500 text-white shadow-sm" title="Session Job">
        SESSION
    </span>
{/if}
