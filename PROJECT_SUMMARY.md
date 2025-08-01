
# Project Summary: dashcraft

**Description:**
Dashcraft is a ClojureScript component library for building interactive dashboards. It is built on top of the `replicant` UI library and leverages `figwheel-main` for a powerful development experience with hot-reloading. The project uses `portfolio` for component development, visualization, and testing, allowing for isolated development and demonstration of UI components.

**Key Technologies:**
*   **ClojureScript:** The primary language for the project.
*   **Replicant:** A ClojureScript UI library for creating web applications.
*   **Figwheel-main:** Provides a hot-reloading development environment.
*   **Portfolio:** Used for developing and visualizing components in isolation.
*   **Malli:** For data validation and defining schemas for components like forms and the EDN editor.
*   **ECharts:** The underlying library for the charting components.
*   **Babashka:** Used for scripting and defining project tasks.
*   **Node.js/Bun:** For managing JavaScript dependencies.

**Project Structure:**
*   `src/`: Contains the core ClojureScript source code for the components.
*   `portfolio/`: Includes scenes and examples for the Portfolio UI.
*   `resources/`: For static assets like CSS and images.
*   `deps.edn`: Manages Clojure and ClojureScript dependencies.
*   `package.json`: Manages JavaScript dependencies.
*   `bb.edn`: Defines Babashka tasks for common development workflows.

**Core Components:**
The library provides a set of reusable components for building dashboards:
*   **Chart:** A versatile charting component that wraps ECharts and supports features like drill-down.
*   **DataTable:** A powerful data table component with support for sorting, grouping, and custom cell rendering.
*   **EDN Editor:** A schema-driven editor for EDN data, with validation provided by Malli.
*   **Form:** A component for generating forms from a Malli schema.
*   **LoadingContainer:** A simple container that displays a loading spinner.
*   **ErrorAwareContainer:** A container that can display error messages over its content.

**Development Workflow:**
The project is set up for a smooth development experience:
*   Run `bb dev` to start the development server with a REPL and hot-reloading.
*   Run `bb test` to execute the project's tests.
*   New components and features can be developed and tested in isolation using Portfolio by adding new scenes to the `portfolio/` directory.

