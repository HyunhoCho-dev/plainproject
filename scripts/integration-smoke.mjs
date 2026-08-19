const BACKEND_URL = process.env.BACKEND_URL || "http://127.0.0.1:8080";

async function request(path, options = {}) {
  const response = await fetch(`${BACKEND_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  const body = await response.json();
  if (!response.ok || body.ok !== true) {
    throw new Error(body?.error?.message || `Request failed with HTTP ${response.status}`);
  }
  return body.data;
}

const user = await request("/api/users/demo", {
  method: "POST",
  body: "{}",
});

const plan = await request("/api/plans/generate", {
  method: "POST",
  body: JSON.stringify({
    userId: user.id,
    goal: "PLAIN GitHub Actions integration check",
    currentLevel: "beginner",
    dailyHours: 1,
    startDate: new Date().toISOString().slice(0, 10),
    constraints: "Create a practical seven-day plan.",
  }),
});

const blockCount = plan.days.reduce((total, day) => total + day.blocks.length, 0);
if (!plan.planId || plan.days.length === 0 || blockCount === 0) {
  throw new Error("The real AI response was not saved as a usable study plan.");
}

console.log(`Real AI integration succeeded: plan ${plan.planId}, ${plan.days.length} days, ${blockCount} blocks.`);
