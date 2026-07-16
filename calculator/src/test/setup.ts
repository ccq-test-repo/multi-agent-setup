import "@testing-library/jest-dom/vitest";

// Enable act environment for @testing-library/react
(globalThis as Record<string, unknown>).IS_REACT_ACT_ENVIRONMENT = true;
