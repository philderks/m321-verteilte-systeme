import { useEffect, useState } from 'react'

const gateway = 'http://localhost:8080'

export default function App() {
  const [result, setResult] = useState(null)
  const [books, setBooks] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    const get = path => fetch(`${gateway}${path}`, { signal: controller.signal }).then(response => {
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      return response.json()
    })
    Promise.all([get('/api/call'), get('/api/books')])
      .then(([call, list]) => { setResult(call); setBooks(list) })
      .catch(err => { if (err.name !== 'AbortError') setError(true) })
    return () => controller.abort()
  }, [])

  return <main>
    <h1>M321 – Verteilte Systeme</h1>
    <p>React → Gateway → Service-02 → Eureka → Service-01</p>
    {error && <p role="alert">Gateway ist nicht erreichbar. Starte zuerst Eureka, beide Services und das Gateway.</p>}
    {!error && !result && <p>Antwort wird geladen …</p>}
    {result && <section>
      <h2>Antwort von Service-02</h2>
      <p>{result.originalData?.name}</p>
      <p>{result.message}</p>
      <small>{result.timestamp}</small>
    </section>}
    {books && <section>
      <h2>Bücher über Feign</h2>
      <ul>{books.map((book, index) => <li key={index}>{book}</li>)}</ul>
    </section>}
  </main>
}
