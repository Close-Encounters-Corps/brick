<script lang="ts">
  import Versions from './components/Versions.svelte'
  import electronLogo from './assets/electron.svg'
  import { io } from 'socket.io-client'
  import Fomantic from './lib/Fomantic.svelte'
  import { writable } from 'svelte/store'
  import type { Writable } from 'svelte/store'

  // const ipcHandle = (): void => window.electron.ipcRenderer.send('ping')


  const socket = io('http://localhost:4500')

  const logs: Writable<Array<any>> = writable([])
  socket.on('connect', () => {
    console.log('connected!')
  })
  socket.on('event', function (raw: string) {
    const data = JSON.parse(raw)
    console.log(data)
    logs.update((x) => { x.push(data); return x })
  })

  const fetchLogs = () => {
    logs.set([])
    socket.emit('stream')
  }
</script>

<Fomantic>
  <img alt="logo" class="logo" src={electronLogo} />
  <div class="ui container">
    <!-- svelte-ignore a11y-click-events-have-key-events a11y-no-static-element-interactions a11y-missing-attribute-->
    <button class="ui button" on:click={fetchLogs}>Request logs</button>
    {#each $logs as data}
      <p>{data.event}</p>
    {/each}
  </div>
  <Versions />
</Fomantic>
