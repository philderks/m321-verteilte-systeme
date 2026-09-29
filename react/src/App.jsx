import { useEffect, useState } from 'react'

export default function App() {
  const [result, setResult] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    fetch('http://localhost:8082/call', { signal: controller.signal })
      .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        return response.json()
      })
      .then(setResult)
      .catch(err => { if (err.name !== 'AbortError') setError(true) })
    return () => controller.abort()
  }, [])

  return <main>
    <h1>M321 – Verteilte Systeme</h1>
    <p>React → Service-02 → Eureka → Service-01</p>
    {error && <p role="alert">Service-02 ist nicht erreichbar. Starte zuerst Eureka und beide Services.</p>}
    {!error && !result && <p>Antwort wird geladen …</p>}
    {result && <section>
      <h2>Antwort von Service-02</h2>
      <p>{result.originalData?.name}</p>
      <p>{result.message}</p>
      <small>{result.timestamp}</small>
    </section>}
  </main>
}
