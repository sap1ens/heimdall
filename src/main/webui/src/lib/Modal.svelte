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
        on:keydown={(e) => e.key === 'Escape' && dialog.close()}
        class="w-[500px] min-h-[200px] p-[25px] outline-none rounded shadow-lg border border-gray-300"
>
    <div role="presentation" on:click|stopPropagation on:keydown|stopPropagation>
        <slot />
        <div class="absolute top-2 right-2">
            <button type="button" title="Close" on:click={() => dialog.close()} class="inline-block cursor-pointer bg-transparent border-0 p-0 m-0">
                <Fa fw icon={faX} class="text-gray-500 hover:text-gray-700" />
            </button>
        </div>
    </div>
</dialog>
