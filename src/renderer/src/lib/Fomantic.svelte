<script lang="ts">
  import { onMount } from 'svelte'

  const ready = new Promise((resolve: (value: void) => void) => {
    onMount(async () => {
      await import('jquery').then((jq) => {
        window['jQuery'] = jq.default
        window['$'] = jq.default
      })
      await import('fomantic-ui/dist/semantic.min.css')
      await import('fomantic-ui/dist/semantic.min.js')
      resolve()
    })
  })
</script>

{#await ready}
  <p>loading...</p>
{:then}
  <slot />
{/await}
