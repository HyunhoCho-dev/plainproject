import "dotenv/config";

export const config = {
  port: Number(process.env.PORT || 3000),
  openRouterApiKey: process.env.OPENROUTER_API_KEY || "",
  openRouterModel: process.env.OPENROUTER_MODEL || "deepseek/deepseek-v4-flash",
  appUrl: process.env.APP_URL || "http://localhost:3000",
  appName: process.env.APP_NAME || "PLAIN AI Prototype",
};
