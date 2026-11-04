const TOKEN_KEY = "token";
const LOGIN_PAGE = "/index.html";

function getToken() {
	return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
	localStorage.setItem(TOKEN_KEY, token);
}

function clearToken() {
	localStorage.removeItem(TOKEN_KEY);
}

// The API has two error shapes: {"detail": "..."} and {"fieldName": "message"}
function errorMessageFrom(data, status) {
	if (data && typeof data.detail === "string") {
		return data.detail;
	}
	if (data && typeof data === "object") {
		const fields = Object.entries(data).map(([field, message]) => `${field}: ${message}`);
		if (fields.length > 0) {
			return fields.join(", ");
		}
	}
	return `Request failed (${status})`;
}

async function readBody(response) {
	const text = await response.text();
	if (!text) {
		return null;
	}
	const contentType = response.headers.get("Content-Type") || "";
	return contentType.includes("json") ? JSON.parse(text) : text;
}

/**
 * Every API call goes through here.
 *
 * Login and sign-up pass auth: false. They send no token, and a 401 from login means
 * "wrong password", which must show a message rather than redirect.
 */
async function apiFetch(path, { method = "GET", body, auth = true } = {}) {
	const headers = {};
	if (body !== undefined) {
		headers["Content-Type"] = "application/json";
	}
	if (auth) {
		headers["Authorization"] = `Bearer ${getToken()}`;
	}

	let response;
	try {
		response = await fetch(path, {
			method,
			headers,
			body: body === undefined ? undefined : JSON.stringify(body),
		});
	} catch {
		throw new Error("Could not reach the server. Please try again.");
	}

	if (auth && response.status === 401) {
		clearToken();
		window.location.replace(LOGIN_PAGE);
		throw new Error("Your session has expired. Please log in again.");
	}

	const data = await readBody(response);
	if (!response.ok) {
		throw new Error(errorMessageFrom(data, response.status));
	}
	return data;
}

function showMessage(element, text) {
	element.textContent = text;
	element.hidden = false;
}

function hideMessage(element) {
	element.textContent = "";
	element.hidden = true;
}
