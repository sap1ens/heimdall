<script>
  import Fa from 'svelte-fa'
  import { faGithub } from '@fortawesome/free-brands-svg-icons'

  import heimdallLogo from './assets/heimdall-logo.png'
  import FlinkJobs from './lib/FlinkJobs.svelte'
  import { appConfig } from "./lib/stores/appConfig.js";
</script>

<main>
  <div class="w-11/12 mx-auto mt-2 pb-4 flex flex-col grow min-h-screen">
    <div class="flex items-center justify-start">
      <img src={heimdallLogo} alt="Heimdall Logo" width="100" height="100" />
      <h1 class="text-3xl font-bold py-6 text-[#e6516f] ml-3">Heimdall</h1>
    </div>
    <FlinkJobs/>
    <div class="mt-auto pt-4 text-center text-gray-500">
      <Fa fw icon={faGithub} />
      <a href="https://github.com/sap1ens/heimdall" target="_blank" class="underline hover:no-underline">sap1ens/heimdall</a>
      {#if $appConfig?.appVersion}
      · v{$appConfig?.appVersion}
      {/if}
    </div>
  </div>
</main>
