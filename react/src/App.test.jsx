import { afterEach, expect, test, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import App from './App.jsx'

afterEach(() => { cleanup(); vi.unstubAllGlobals() })

test('zeigt die Antwort von Service-02', async () => {
  const fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => ({
    originalData: { name: 'Hello World from Service 01' },
    message: 'Antwort von service-01 erfolgreich verarbeitet',
    timestamp: '2026-09-23T10:00:00',
  }) })
  vi.stubGlobal('fetch', fetchMock)
  render(<App />)
  expect(await screen.findByText('Hello World from Service 01')).toBeInTheDocument()
  expect(screen.getByText('Antwort von service-01 erfolgreich verarbeitet')).toBeInTheDocument()
  expect(fetchMock).toHaveBeenCalledWith('http://localhost:8082/call', { signal: expect.any(AbortSignal) })
})

test('zeigt einen Fehler, wenn Service-02 nicht erreichbar ist', async () => {
  vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('offline')))
  render(<App />)
  expect(await screen.findByRole('alert')).toHaveTextContent('Service-02 ist nicht erreichbar')
})
