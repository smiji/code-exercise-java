import React from 'react';
import { ApiError, shortenUrl } from '../api/apiCaller';
import type { ApiErrorResponse } from '../types/ApiErrorResponse';
import ErrorModal from './error/ErrorModal';

type URLShortnerProps = {
  onUrlCreated?: () => void;
};

const initialForm = {
  fullUrl: '',
  customAlias: '',
};

//for checking the validity of the URL 
// as part of input validation before sending the request to the backend
function isValidUrl(value: string) {
  try {
    new URL(value);
    return true;
  } catch {
    return false;
  }
}


function URLShortner({ onUrlCreated }: URLShortnerProps) : React.ReactElement {

  const [form, setForm] = React.useState(initialForm);
  const [loading, setLoading] = React.useState(false);
  const [apiError, setApiError] = React.useState<ApiErrorResponse | null>(null);
  const [error, setError] = React.useState<string>('');
  const [result, setResult] = React.useState<string>('');

  const handleChange = (field: 'fullUrl' | 'customAlias', value: string) => {
   setForm((prev) => ({ ...prev, [field]: value }));
  };

   const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
   event.preventDefault();

    // get the trimmed URL from the form state
    const trimmedUrl = form.fullUrl.trim();

    //input validation  - BOC
    //check if the trimmed URL is empty or invalid
    if (!trimmedUrl) {
      document.getElementById('fullUrl')?.focus();
      setError('Original URL is required.');
      return;
    }

    // check if the trimmed URL is valid
    if (!isValidUrl(trimmedUrl)) {
      setError('Please enter a valid URL.');
      return;
    }

    //input validation  - EOC

    setLoading(true);
    setError('');
    setApiError(null);
    setResult('');

    try {
      console.log('Submitting:', { fullUrl: trimmedUrl, customAlias: form.customAlias.trim() || undefined });
      const response = await shortenUrl({
        fullUrl: trimmedUrl,
        customAlias: form.customAlias.trim() || undefined,
      });

      if (!response?.shortUrl) {
        setError('Invalid response from server');
        return;
      }
      setResult(`${response.shortUrl}`);
      setForm(initialForm);
      onUrlCreated?.();
    } catch (err) {
      if (err instanceof ApiError) {
        setApiError(err.details);
      } else {
        setError(err instanceof Error ? err.message : 'Something went wrong');
      }
    } finally {
      setLoading(false);
    }    
  };

  const copyToClipboard = async () => {
    if (!result) return;
    try {
      await navigator.clipboard.writeText(result);
      alert('Short URL copied to clipboard');
    } catch {
      alert('Unable to copy automatically. Please copy the URL manually.');
    }
  };
  

  function handleReset() {
    setForm(initialForm);
    setError('');
    setResult('');
  }

  return (
    <section className="px-4 py-8 sm:px-6">
      <form
        onSubmit={handleSubmit}
        noValidate
        className="mx-auto 
        w-full 
        max-w-2xl 
        rounded-3xl 
        border border-cyan-100 
        bg-white/95 
        p-6 shadow-[0_18px_45px_-24px_rgba(8,47,73,0.5)] ring-1 ring-amber-100 backdrop-blur sm:p-8"
      >
        <div className="mb-6">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-700">Link Utility</p>
          <h2 className="mt-2 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">URL Shortener</h2>
          <p className="mt-1 text-sm text-slate-600">Paste a long URL and get a compact link you can share quickly.</p>
        </div>

        <div className="space-y-3">
          {error && !apiError && (
            <p
              role="alert"
              className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-medium text-rose-700"
            >
              {error}
            </p>
          )}
          {apiError && (
            <div className="rounded-xl border border-rose-200 bg-rose-50/90 p-3">
              <ErrorModal
                title="Request failed"
                message={apiError.message}
                status={apiError.status}
                error={apiError.error}
                timestamp={apiError.timestamp}
                onConfirm={() => setApiError(null)}
              />
            </div>
          )}
        </div>

        <div className="mt-6 space-y-5">
          <div className="space-y-2">
            <label htmlFor="fullUrl" className="block text-sm font-semibold text-slate-700">
              Enter URL to shorten:
            </label>
            <input
              id="fullUrl"
              type="url"
              value={form.fullUrl}
              onChange={(e) => handleChange('fullUrl', e.target.value)}
              placeholder="https://example.com"
              className="w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-cyan-500 focus:ring-4 focus:ring-cyan-100"
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="customAlias" className="block text-sm font-semibold text-slate-700">
              Custom alias (optional):
            </label>
            <input
              type="text"
              id="customAlias"
              name="custom"
              value={form.customAlias}
              onChange={(e) => handleChange('customAlias', e.target.value)}
              className="w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-cyan-500 focus:ring-4 focus:ring-cyan-100"
            />
          </div>

          {result && (
            <div className="rounded-2xl border border-emerald-200 bg-gradient-to-r from-emerald-50 to-cyan-50 p-4">
              <strong className="text-sm uppercase tracking-wide text-emerald-800">Short URL:</strong>
              <div className="mt-2">
                <a
                  href={result}
                  target="_blank"
                  rel="noreferrer"
                  className="break-all text-base font-medium text-cyan-800 underline decoration-cyan-400 underline-offset-4"
                >
                  {result}
                </a>
              </div>
              <button
                type="button"
                onClick={copyToClipboard}
                className="mt-4 rounded-lg border border-cyan-200 bg-white px-4 py-2 text-sm font-semibold text-cyan-700 transition hover:bg-cyan-50"
              >
                Copy to Clipboard
              </button>
            </div>
          )}

          <div className="flex flex-col gap-3 pt-2 sm:flex-row sm:items-center">
            <button
              type="reset"
              onClick={handleReset}
              className="rounded-lg 
              border border-slate-300 
              bg-white px-4 py-2.5 
              text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            >
              Clear
            </button>
            <button
              type="submit"
              disabled={loading}
              className="rounded-lg bg-gradient-to-r 
              from-blue-600 to-emerald-500 px-5 py-2.5 
              text-sm font-semibold text-white shadow-sm 
              transition hover:from-blue-700 hover:to-emerald-600 
              disabled:cursor-not-allowed disabled:from-blue-400 disabled:to-emerald-300 disabled:opacity-80"
            >
              {loading ? 'Shortening...' : 'Shorten URL'}
            </button>
          </div>

          <div className="min-h-6">
            {loading && <p className="text-sm font-medium text-cyan-700">Loading...</p>}
          </div>
        </div>
      </form>
    </section>
  );
}
export default URLShortner;

