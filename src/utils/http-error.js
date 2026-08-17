// HTTP 상태 코드와 사용자에게 보여줄 메시지를 함께 보관하는 공통 오류입니다.
export class HttpError extends Error {
  constructor(status, message, details = undefined) {
    super(message);
    this.name = "HttpError";
    this.status = status;
    this.details = details;
  }
}
