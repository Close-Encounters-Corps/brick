<script lang="ts">
  import Versions from './components/Versions.svelte'
  import electronLogo from './assets/electron.svg'
  import { io } from 'socket.io-client'
  import Fomantic from './lib/Fomantic.svelte'
  import { writable } from 'svelte/store'
  import type { Writable } from 'svelte/store'

  // const ipcHandle = (): void => window.electron.ipcRenderer.send('ping')

  const logs: Writable<Array<any>> = writable([])

  const fetchLogs = () => {
    const socket = io('http://localhost:4500')
    socket.on('connect', () => {
        console.log('connected!')
    })
    socket.on('event', function (raw: string) {
        const data = JSON.parse(raw)
        logs.update((x) => { x.push(data); return x })
    })
    logs.set([])
    socket.emit('stream')
  }
</script>

<Fomantic>
  <img alt="logo" class="logo" src={electronLogo} />
  <div class="ui container">
    <!-- svelte-ignore a11y-click-events-have-key-events a11y-no-static-element-interactions a11y-missing-attribute-->
    <button class="ui button" on:click={fetchLogs}>Request logs</button>
    <div class="ui container logs">
      <p>
        {#each $logs as data}
          {data.event}
          <br>
        {/each}
      </p>
    </div>
  </div>
  <Versions />
</Fomantic>

<style>
.ui.container.logs {
  max-height: 40%;
}
</style>