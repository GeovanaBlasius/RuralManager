import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['icon.svg'],
      manifest: {
        name: 'RuralManager',
        short_name: 'RuralManager',
        description: 'Gestão de safras, gastos, colheitas e vendas para o produtor rural.',
        lang: 'pt-BR',
        start_url: '/',
        scope: '/',
        display: 'standalone',
        orientation: 'portrait',
        background_color: '#0a0d0a',
        theme_color: '#22c55e',
        icons: [
          { src: 'icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'icon-512.png', sizes: '512x512', type: 'image/png' },
          { src: 'icon-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // O app em si funciona offline (abre e mostra a tela), mas os DADOS nunca ficam em cache:
        // dado financeiro desatualizado ou de outra conta e pior do que um aviso de "sem conexão".
        navigateFallbackDenylist: [/^\/api\//],
        runtimeCaching: [],
      },
    }),
  ],
  server: {
    port: 5173,
    // Em desenvolvimento a API roda em outra porta; o proxy evita CORS e mantem o front sem URL fixa.
    proxy: { '/api': 'http://localhost:8080' },
  },
  test: { environment: 'node' },
});
