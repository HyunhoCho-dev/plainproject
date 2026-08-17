import { createApp } from "./app.js";
import { config } from "./config.js";

// 이 파일은 앱을 실제 포트에 연결하는 시작점입니다.
const app = createApp();
app.listen(config.port, () => console.log(`PLAIN AI server: http://localhost:${config.port}`));
