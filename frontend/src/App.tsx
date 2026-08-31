import React from 'react'
import './App.css'
import URLShortner from './components/ULShortner'
import UrlList from './components/UrlList'

function App() {
  const [urlListRefreshKey, setUrlListRefreshKey] = React.useState(0);
  const [activeView, setActiveView] = React.useState<'shortener' | 'list'>('shortener');

  const handleUrlCreated = () => {
    setUrlListRefreshKey((prev) => prev + 1);
  };

  return (
    <main className="pb-10">
      {activeView === 'shortener' ? (
        <>
          <URLShortner onUrlCreated={handleUrlCreated} />
          <div className="mx-auto w-full max-w-2xl px-4 sm:px-6">
            <button
              type="button"
              onClick={() => setActiveView('list')}
              className="rounded-lg bg-gradient-to-r from-blue-600 to-emerald-500 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:from-blue-700 hover:to-emerald-600"
            >
              View All URLs
            </button>
          </div>
        </>
      ) : (
        <>
          <div className="mx-auto mt-8 w-full max-w-5xl px-4 sm:px-6">
            <button
              type="button"
              onClick={() => setActiveView('shortener')}
              className="rounded-lg border border-slate-300 
              bg-white 
              px-4 py-2 text-sm 
              font-semibold 
              text-slate-700 
              transition 
              hover:bg-slate-50"
            >
              Back to Shortener
            </button>
          </div>
          <UrlList refreshToken={urlListRefreshKey} />
        </>
      )}
    </main>
  )
}

export default App
