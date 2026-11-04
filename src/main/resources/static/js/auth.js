const loginTab = document.getElementById("login-tab");
const signupTab = document.getElementById("signup-tab");
const loginForm = document.getElementById("login-form");
const signupForm = document.getElementById("signup-form");
const authError = document.getElementById("auth-error");
const authSuccess = document.getElementById("auth-success");

function clearMessages() {
	hideMessage(authError);
	hideMessage(authSuccess);
}

function showTab(tab) {
	const isLogin = tab === "login";
	loginForm.hidden = !isLogin;
	signupForm.hidden = isLogin;
	loginTab.classList.toggle("active", isLogin);
	signupTab.classList.toggle("active", !isLogin);
	loginTab.setAttribute("aria-selected", String(isLogin));
	signupTab.setAttribute("aria-selected", String(!isLogin));
	clearMessages();
}

// Disables the submit button while waiting, so a double click can't send two requests
async function submitWith(form, action) {
	const button = form.querySelector("button[type=submit]");
	button.disabled = true;
	clearMessages();
	try {
		await action();
	} catch (error) {
		showMessage(authError, error.message);
	} finally {
		button.disabled = false;
	}
}

loginTab.addEventListener("click", () => showTab("login"));
signupTab.addEventListener("click", () => showTab("signup"));

loginForm.addEventListener("submit", (event) => {
	event.preventDefault();
	submitWith(loginForm, async () => {
		const result = await apiFetch("/api/auth/login", {
			method: "POST",
			auth: false,
			body: {
				email: document.getElementById("login-email").value.trim(),
				password: document.getElementById("login-password").value,
			},
		});
		setToken(result.token);
		window.location.replace("/dashboard.html");
	});
});

signupForm.addEventListener("submit", (event) => {
	event.preventDefault();
	submitWith(signupForm, async () => {
		const email = document.getElementById("signup-email").value.trim();
		await apiFetch("/api/auth/register", {
			method: "POST",
			auth: false,
			body: {
				name: document.getElementById("signup-name").value.trim(),
				email,
				password: document.getElementById("signup-password").value,
			},
		});
		signupForm.reset();
		showTab("login");
		document.getElementById("login-email").value = email;
		showMessage(authSuccess, "Account created, please log in");
	});
});
