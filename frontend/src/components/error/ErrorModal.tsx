
import React from "react";

type ErrorModalProps = {
  title: string;
  message: string;
  status: number;
  error: string;
  timestamp: string;
  onConfirm: () => void;
};
  
function ErrorModal({ title, message, status, error, timestamp, onConfirm}: ErrorModalProps): React.ReactElement {
  console.log('ErrorModal props:', { title, message, status, error, timestamp });
  return (
     <div role="alert" className="text-rose-800">
      <p className="font-semibold">{title} - {error}</p>
      <p className="mt-1 text-sm">{message}</p>
      <p className="mt-1 text-xs text-rose-700/80">Status: {status} | {new Date(timestamp).toLocaleString()}</p>
      <p className="mt-3">
        <button
          type="button"
          onClick={onConfirm}
          className="rounded-md border border-rose-300 bg-white px-3 py-1.5 text-sm font-semibold text-rose-700 hover:bg-rose-50"
        >
          Close
        </button>
      </p>
    </div>
  );
}

export default ErrorModal;  