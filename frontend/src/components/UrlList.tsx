import React, { useEffect, useMemo, useState } from 'react';
import { ApiError, deleteUrl, getUrlsPage, type PageResponse } from '../api/apiCaller';
import type { UrlRecord } from '../types/UrlRecord';

type UrlListProps = {
  refreshToken?: number;
};

function getAlias(item: UrlRecord): string {
  if (item.alias) return item.alias;

  try {
    const url = new URL(item.shortUrl);
    return url.pathname.split('/').filter(Boolean).pop() || item.shortUrl;
  } catch {
    return item.shortUrl;
  }
}

function UrlList({ refreshToken = 0 }: UrlListProps): React.ReactElement {
  const [filter, setFilter] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);
  const [reloadToken, setReloadToken] = useState<number>(0);
  const [pageData, setPageData] = useState<PageResponse<UrlRecord>>({
    content: [],
    totalPages: 0,
    totalElements: 0,
    number: 0,
    size: 10,
    first: true,
    last: true,
    numberOfElements: 0,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState<UrlRecord | null>(null);

  useEffect(() => {
    const fetchUrls = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await getUrlsPage({
          page,
          size: pageSize,
          alias: filter.trim() || undefined,
        });
        setPageData(data);
      } catch (err) {
        if (err instanceof ApiError) {
          setError(err.details.message);
        } else {
          setError(err instanceof Error ? err.message : 'Something went wrong');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUrls();
  }, [page, pageSize, filter, refreshToken, reloadToken]);

  const filteredUrls = useMemo(() => {
    return pageData.content.filter((item) => {
      const alias = getAlias(item).toLowerCase();
      return alias.includes(filter.trim().toLowerCase());
    });
  }, [pageData.content, filter]);

  const handleDelete = async () => {
    if (!confirmDelete) return;

    const alias = getAlias(confirmDelete);

    try {
      await deleteUrl(alias);

      setConfirmDelete(null);

      if (pageData.content.length === 1 && page > 0) {
        setPage(page - 1);
      } else {
        setReloadToken((prev) => prev + 1);
      }
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.details.message);
      } else {
        setError(err instanceof Error ? err.message : 'Unable to delete');
      }
    }
  };

  return (
    <section className="px-4 pb-12 sm:px-6">
      <div className="mx-auto w-full max-w-5xl rounded-3xl border border-cyan-100 bg-white/95 p-6 shadow-[0_18px_45px_-24px_rgba(8,47,73,0.5)] ring-1 ring-amber-100 backdrop-blur sm:p-8">
        <div className="mb-5 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-700">History</p>
            <h3 className="mt-2 text-2xl font-bold tracking-tight text-slate-900">Shortened URLs</h3>
          </div>
          <p className="text-sm text-slate-600">{pageData.totalElements} total records</p>
        </div>

        <div className="mb-4 flex flex-col gap-3 sm:flex-row">
          <input
            type="text"
            value={filter}
            onChange={(e) => {
              setFilter(e.target.value);
              setPage(0);
            }}
            placeholder="Filter by alias"
            className="w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-cyan-500 focus:ring-4 focus:ring-cyan-100"
          />

          <select
            value={pageSize}
            onChange={(e) => {
              setPageSize(Number(e.target.value));
              setPage(0);
            }}
            className="rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-slate-900 outline-none transition focus:border-cyan-500 focus:ring-4 focus:ring-cyan-100"
          >
            <option value={5}>5 per page</option>
            <option value={10}>10 per page</option>
            <option value={20}>20 per page</option>
          </select>
        </div>

        {error && (
          <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-medium text-rose-700">{error}</p>
        )}

        {loading ? (
          <p className="py-6 text-sm font-medium text-cyan-700">Loading...</p>
        ) : (
          <>
            <div className="overflow-x-auto rounded-2xl border border-slate-200">
              <table className="min-w-full border-collapse bg-white">
                <thead>
                  <tr className="bg-slate-50/80">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Alias</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Original URL</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Short URL</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredUrls.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-4 py-6 text-sm text-slate-600">No records found</td>
                    </tr>
                  ) : (
                    filteredUrls.map((item) => {
                      const alias = getAlias(item);

                      return (
                        <tr key={item.shortUrl} className="border-t border-slate-200 align-top">
                          <td className="px-4 py-3 text-sm font-semibold text-slate-800">{alias}</td>
                          <td className="px-4 py-3 text-sm">
                            <a
                              href={item.actualUrl}
                              target="_blank"
                              rel="noreferrer"
                              className="break-all text-slate-700 underline decoration-slate-300 underline-offset-4 hover:text-cyan-700"
                            >
                              {item.actualUrl}
                            </a>
                          </td>
                          <td className="px-4 py-3 text-sm">
                            <a
                              href={item.shortUrl}
                              target="_blank"
                              rel="noreferrer"
                              className="break-all font-medium text-cyan-800 underline decoration-cyan-300 underline-offset-4 hover:text-cyan-900"
                            >
                              {item.shortUrl}
                            </a>
                          </td>
                          <td className="px-4 py-3">
                            <button
                              type="button"
                              onClick={() => setConfirmDelete(item)}
                              className="rounded-lg border border-rose-200 bg-white px-3 py-1.5 text-sm font-semibold text-rose-700 transition hover:bg-rose-50"
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            <div className="mt-4 flex flex-col gap-3 text-sm sm:flex-row sm:items-center sm:justify-between">
              <span className="font-medium text-slate-700">
                Page {pageData.number + 1} of {pageData.totalPages || 1}
              </span>

              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={pageData.first}
                  onClick={() => setPage((p) => Math.max(p - 1, 0))}
                  className="rounded-lg border border-slate-300 bg-white px-3 py-2 font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  Previous
                </button>

                <button
                  type="button"
                  disabled={pageData.last}
                  onClick={() => setPage((p) => p + 1)}
                  className="rounded-lg bg-cyan-700 px-4 py-2 font-semibold text-white transition hover:bg-cyan-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  Next
                </button>
              </div>
            </div>
          </>
        )}

        {confirmDelete && (
          <div
            role="dialog"
            aria-modal="true"
            className="mt-5 max-w-md rounded-2xl border border-amber-200 bg-amber-50 p-4 shadow-sm"
          >
            <p className="text-sm text-amber-900">
              Delete alias <strong>{getAlias(confirmDelete)}</strong>?
            </p>
            <div className="mt-3 flex gap-2">
              <button
                type="button"
                onClick={handleDelete}
                className="rounded-lg bg-rose-700 px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-rose-800"
              >
                Confirm Delete
              </button>
              <button
                type="button"
                onClick={() => setConfirmDelete(null)}
                className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              >
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

export default UrlList;