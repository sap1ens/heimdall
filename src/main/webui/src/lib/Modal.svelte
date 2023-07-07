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
        class="w-4/12 min-h-[25%] outline-none"
>
    <div on:click|stopPropagation>
        <slot />
        <div class="absolute top-2 right-2">
            <span title="Settings" on:click={() => dialog.close()} class="inline-block">
                <Fa fw icon={faX} class="text-gray-500 hover:cursor-pointer" />
            </span>
        </div>
    </div>
</dialog>
