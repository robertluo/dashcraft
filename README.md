# Ramachart

A ClojureScript component library using [replicant](https://replicant.fun) with a development environment powered by Figwheel-main and Portfolio for component development and visualization.

## Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) (for deps.edn dependencies)
- [Node.js](https://nodejs.org/) OR [Bun](https://bun.sh/) (for JavaScript dependencies)

## Installation

1. Clone the repository
2. Install JavaScript dependencies:
   ```bash
   # Using bun (recommended)
   bun install

   # OR using npm
   npm install
   ```

The Clojure dependencies will be automatically downloaded when you first start the development environment.

## Development

### Starting the REPL

Start the development REPL with Figwheel-main:

```bash
clojure -M:dev
```

This will start a REPL with hot-reloading enabled and nREPL support for editor integration.

### Component Development with Portfolio

Component examples and demos are managed through Portfolio. Add your component examples in the `portfolio/` directory to see them in the development environment.

## Project Structure

```
.
├── src/          # Core ClojureScript source files
├── test/         # Unit tests
├── portfolio/    # Component examples and demos
└── resources/    # Static assets and public files
```
