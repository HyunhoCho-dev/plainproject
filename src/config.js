import "dotenv/config";

export const config = {
  port: Number(process.env.PORT || 3000),
  openRouterApiKey: process.env.OPENROUTER_API_KEY || "",
  openRouterModel: process.env.OPENROUTER_MODEL || "openai/gpt-4.1-mini",
  appUrl: process.env.APP_URL || "http://localhost:3000",
  appName: process.env.APP_NAME || "PLAIN AI Prototype",
};

