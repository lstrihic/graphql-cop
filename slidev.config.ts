import { defineConfig } from '@slidev/cli'

export default defineConfig({
  title: 'GraphQL with Spring Boot',
  titleTemplate: '%s - GraphQL Demo',

  // Color scheme
  colorSchema: 'dark',

  // Font settings
  fonts: {
    sans: 'Inter',
    serif: 'Merriweather',
    mono: 'JetBrains Mono',
  },

  // Enable features
  highlighter: 'shiki',
  lineNumbers: true,
  monaco: true,

  // Export settings
  exportFilename: 'graphql-spring-boot-presentation',

  // Download settings
  download: true,

  // Info
  info: `
    ## GraphQL with Spring Boot

    A comprehensive demo covering:
    - N+1 Problem and @BatchMapping solution
    - Type-safe mutations with input classes
    - POJO pagination objects
    - Computed fields
    - Best practices
  `,
})
