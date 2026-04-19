import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: "autoUpdate",
      includeAssets: ["favicon.svg"],
      manifest: {
        name: "Pflege Learning & Exam PWA",
        short_name: "Pflege",
        theme_color: "#0f766e",
        background_color: "#f4f7f7",
        display: "standalone",
        start_url: "/",
        lang: "de",
        icons: []
      }
    })
  ],
  server: {
    port: 5173
  }
});
