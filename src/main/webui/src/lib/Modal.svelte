<script>
    import Fa from 'svelte-fa'
    import { faX } from '@fortawesome/free-solid-svg-icons'

    export let showModal;

    let dialog;

    $: if (dialog && showModal) dialog.showModal();
</script>

<dialog
        bind:this={dialog}
        on:close={() => (showModal = false)}
        on:click|self={() => dialog.close()}
        class="w-[500px] min-h-[200px] p-8 outline-none rounded-2xl shadow-2xl backdrop:bg-black/50 border-0"
>
    <div on:click|stopPropagation class="relative">
        <div class="mb-6">
            <h2 class="text-2xl font-bold text-gray-800 flex items-center gap-2">
                ⚙️ Settings
            </h2>
            <div class="h-1 w-20 bg-gradient-to-r from-primary-500 to-accent-500 rounded-full mt-2"></div>
        </div>
        <div class="space-y-4">
            <slot />
        </div>
        <button on:click={() => dialog.close()}
                class="absolute top-0 right-0 p-2 rounded-lg hover:bg-gray-100 transition-colors">
            <Fa fw icon={faX} class="text-gray-400 hover:text-gray-600" />
        </button>
    </div>
</dialog>
