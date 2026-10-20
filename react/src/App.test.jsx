import { afterEach, expect, test, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import App from './App.jsx'

afterEach(() => { cleanup(); vi.unstubAllGlobals() })

const response = value => ({ ok: true, json: async () => value })

test('zeigt die Antwort und Bücher über das Gateway', async () => {
  const fetchMock = vi.fn(url => Promise.resolve(response(url.endsWith('/books')
    ? ['The Metamorphosis (Franz Kafka)', '1984 (George Orwell)']
    : { originalData: { name: 'Hello World from Service 01' }, message: 'Antwort von service-01 erfolgreich verarbeitet', timestamp: '2026-09-23T10:00:00' })))
  vi.stubGlobal('fetch', fetchMock)
  render(<App />)
  expect(await screen.findByText('Hello World from Service 01')).toBeInTheDocument()
  expect(await screen.findByText('The Metamorphosis (Franz Kafka)')).toBeInTheDocument()
  expect(screen.getByText('1984 (George Orwell)')).toBeInTheDocument()
  expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/call', { signal: expect.any(AbortSignal) })
  expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/books', { signal: expect.any(AbortSignal) })
})

test('zeigt einen Fehler, wenn das Gateway nicht erreichbar ist', async () => {
  vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('offline')))
  render(<App />)
  expect(await screen.findByRole('alert')).toHaveTextContent('Gateway ist nicht erreichbar')
})
