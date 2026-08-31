# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some Oxlint rules.

## API configuration

Frontend API endpoint settings are read from Vite environment variables so backend host/port changes do not require code changes.

Create a `.env` file in the project root with one of the following options:

Option 1: Configure host/port separately

```env
VITE_API_PROTOCOL=http
VITE_API_HOST=localhost
VITE_API_PORT=8585
```

Option 2: Configure complete base URL directly

```env
VITE_API_BASE_URL=http://localhost:8585
```

`VITE_API_BASE_URL` takes precedence when set.

Important: for Vite apps this value is baked into static assets at build time. In Docker, if you change these values, rebuild the image and redeploy the container.

## Docker runtime configuration (no rebuild needed)

This project also supports runtime API configuration in Docker using `config.js` generated at container startup.

After building once, you can change API settings by passing container environment variables:

```bash
docker run -p 3000:80 \
  -e VITE_API_PROTOCOL=http \
  -e VITE_API_HOST=host.docker.internal \
  -e VITE_API_PORT=8585 \
  code-exercise-java-frontend
```

Or use a full base URL override:

```bash
docker run -p 3000:80 \
  -e VITE_API_BASE_URL=http://host.docker.internal:8585 \
  code-exercise-java-frontend
```

With this runtime mode, container restarts pick up new values without rebuilding the image.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the Oxlint configuration

If you are developing a production application, we recommend enabling type-aware lint rules by installing `oxlint-tsgolint` and editing `.oxlintrc.json`:

```json
{
  "$schema": "./node_modules/oxlint/configuration_schema.json",
  "plugins": ["react", "typescript", "oxc"],
  "options": {
    "typeAware": true
  },
  "rules": {
    "react/rules-of-hooks": "error",
    "react/only-export-components": ["warn", { "allowConstantExport": true }]
  }
}
```

See the [Oxlint rules documentation](https://oxc.rs/docs/guide/usage/linter/rules) for the full list of rules and categories.
