
import { renderHook, waitFor, act } from '@testing-library/react';
import { useAsyncData } from './useAsyncData';

test('starts loading, then exposes the resolved data', async () => {
  const { result } = renderHook(() => useAsyncData(() => Promise.resolve('hi'), []));

  expect(result.current.loading).toBe(true);
  await waitFor(() => expect(result.current.loading).toBe(false));
  expect(result.current.data).toBe('hi');
  expect(result.current.error).toBeNull();
});

test('captures the error on rejection and stops loading', async () => {
  const { result } = renderHook(() =>
    useAsyncData(() => Promise.reject(new Error('boom')), []));

  await waitFor(() => expect(result.current.loading).toBe(false));
  expect(result.current.error).toBeInstanceOf(Error);
  expect(result.current.data).toBeNull();
});

test('retry re-runs the fetcher', async () => {
  let calls = 0;
  const { result } = renderHook(() =>
    useAsyncData(() => { calls += 1; return Promise.resolve(calls); }, []));

  await waitFor(() => expect(result.current.data).toBe(1));
  act(() => result.current.retry());
  await waitFor(() => expect(result.current.data).toBe(2));
});
