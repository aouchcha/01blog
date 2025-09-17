export function generateHeader(token: String | null): Object {
    return { headers: { Authorization: `Bearer ${token}` } }
}

export function generateURL(endpoint: String) : string {
    return `http://localhost:8080/api/${endpoint}`
}