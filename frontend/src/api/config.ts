type RuntimeConfig = {
	API_PROTOCOL?: string;
	API_HOST?: string;
	API_PORT?: string;
	API_BASE_URL?: string;
};

function getRuntimeConfig(): RuntimeConfig {
	const value = (globalThis as { __APP_CONFIG__?: RuntimeConfig }).__APP_CONFIG__;
	return value || {};
}

const runtime = getRuntimeConfig();

const protocol = runtime.API_PROTOCOL || import.meta.env.VITE_API_PROTOCOL || 'http';
const host = runtime.API_HOST || import.meta.env.VITE_API_HOST || 'localhost';
const port = runtime.API_PORT || import.meta.env.VITE_API_PORT || '8585';

// Runtime API_BASE_URL overrides all; then build-time VITE_API_BASE_URL; then compose from protocol/host/port.
export const API_BASE_URL =
	runtime.API_BASE_URL ||
	import.meta.env.VITE_API_BASE_URL ||
	`${protocol}://${host}:${port}`;
