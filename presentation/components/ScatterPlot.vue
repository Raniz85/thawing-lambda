<template>
  <Scatter
      id="my-chart-id"
      v-if="loaded"
      :data="chartData"
      :options="{
        scales: {
          y: {
            min: 0,
            max: 7000,
            title: {
              display: true,
              text: 'Milliseconds',
            }
          },
          x: {
            title: {
              display: true,
              text: 'Run #',
            }
          }
        }
      }"
  />
</template>

<script>
import { Scatter } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, PointElement, CategoryScale, LinearScale } from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, PointElement, CategoryScale, LinearScale)

export default {
  name: 'ScatterPlot',
  components: { Scatter },
  props: {
    dataFile: {
      type: String,
      required: true,
    }
  },
  data: () => ({
    loaded: false,
    chartData: null
  }),
  async mounted () {
    this.loaded = false

    try {
      const response = await fetch(`/data/${this.dataFile}`)
      this.chartData = await response.json()

      this.loaded = true
    } catch (e) {
      console.error(e)
    }
  }
}
</script>
