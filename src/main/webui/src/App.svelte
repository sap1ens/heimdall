<script>
  import Fa from 'svelte-fa'
  import { faGithub } from '@fortawesome/free-brands-svg-icons'

  import heimdallLogo from './assets/heimdall-logo.png'
  import FlinkJobs from './lib/FlinkJobs.svelte'
  import { appConfig } from "./lib/stores/appConfig.js";
</script>

<main class="min-h-screen">
  <div class="gradient-header shadow-lg">
    <div class="w-11/12 mx-auto">
      <div class="flex items-center justify-start py-6 animate-fade-in">
        <img src={heimdallLogo} alt="Heimdall Logo" width="80" height="80" class="drop-shadow-lg" />
        <div class="ml-4">
          <h1 class="text-4xl font-bold text-white drop-shadow-md">Heimdall</h1>
          <p class="text-white/90 text-sm mt-1">Apache Flink Job Monitor</p>
        </div>
      </div>
    </div>
  </div>
  <div class="w-11/12 mx-auto pb-8 flex flex-col grow">
    <div class="animate-slide-in">
      <FlinkJobs/>
    </div>
    <div class="mt-auto pt-8 text-center">
      <div class="inline-flex items-center gap-2 bg-white px-6 py-3 rounded-full shadow-md">
        <Fa fw icon={faGithub} class="text-gray-600" />
        <a href="https://github.com/sap1ens/heimdall" target="_blank" class="text-gray-600 hover:text-primary-600 transition-colors">sap1ens/heimdall</a>
        {#if $appConfig?.appVersion}
        <span class="text-gray-400">·</span>
        <span class="text-gray-500 font-mono text-sm">v{$appConfig?.appVersion}</span>
        {/if}
      </div>
    </div>
  </div>
</main>
