export function generateHeader(token: String | null): Object {
  // console.log({token});
  
  return { headers: { Authorization: `Bearer ${token}` } }
}

export function generateURL(endpoint: String): string {
  return `http://localhost:8080/api/${endpoint}`
}

export function CheckToken(): string | null {
  if (typeof window === 'undefined') return null; // running on server

  const Token = localStorage.getItem("JWT");
  if (!Token) {
    return null;
  }
  return Token;
}