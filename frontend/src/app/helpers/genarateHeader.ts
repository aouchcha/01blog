export function generateHeader(token: String | null): Object {
  return { headers: { Authorization: `Bearer ${token}` } }
}

export function generateURL(endpoint: String): string {
  return `http://localhost:8080/api/${endpoint}`
}

export function CheckToken(): string | null {

  const Token = localStorage.getItem("JWT");
  if (!Token) {
    return null;
  }
  return Token;
}