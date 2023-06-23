<script>
  import heimdallLogo from './assets/heimdall-logo.png'
  import FlinkJobs from './lib/FlinkJobs.svelte'
</script>

<main>
  <div class="w-11/12 mx-auto mt-2 pb-3">
    <div class="flex items-center justify-start">
      <img src={heimdallLogo} alt="Heimdall Logo" width="100" height="100" />
      <h1 class="text-3xl font-bold py-6 text-[#e6516f] ml-3">Heimdall</h1>
    </div>
    <FlinkJobs/>
  </div>
</main>
