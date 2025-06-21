# Dashcraft

A ClojureScript component library using [replicant](https://replicant.fun) with a development environment powered by Figwheel-main and Portfolio for component development and visualization.

## Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) (for deps.edn dependencies)
- [Babashka](https://babashka.org)
- [Node.js](https://nodejs.org/) OR [Bun](https://bun.sh/) (for JavaScript dependencies)

## Development

### Starting the REPL

Start the development REPL with Figwheel-main:

```bash
bb dev
```

This will start a REPL with hot-reloading enabled and nREPL support for editor integration.

### Test

`bb test`

### Component Development with Portfolio

Component examples and demos are managed through Portfolio. Add your component examples in the `portfolio/` directory to see them in the development environment.

## Project Structure

```
.
├── src/          # Core ClojureScript source files
├── portfolio/    # Component examples and demos
└── resources/    # Static assets and public files
```
