// Security rule for this file: user data only ever reaches the page through textContent,
// never innerHTML, so text like <script> is displayed rather than run.

const STATUS_LABELS = {
	APPLIED: "Applied",
	ONLINE_TEST: "Online Test",
	INTERVIEW: "Interview",
	OFFER: "Offer",
	REJECTED: "Rejected",
};

const pageError = document.getElementById("page-error");
const tableBody = document.getElementById("applications-body");
const emptyMessage = document.getElementById("empty-message");
const statusFilter = document.getElementById("status-filter");
const dialog = document.getElementById("application-dialog");
const form = document.getElementById("application-form");
const formError = document.getElementById("form-error");
const dialogTitle = document.getElementById("dialog-title");

const field = (name) => document.getElementById(`field-${name}`);

// The id being edited, or null when adding a new application
let editingId = null;

function isSafeLink(url) {
	return /^https?:\/\//i.test(url);
}

function textCell(text, className) {
	const cell = document.createElement("td");
	cell.textContent = text ?? "";
	if (className) {
		cell.className = className;
	}
	return cell;
}

function linkCell(url) {
	const cell = document.createElement("td");
	cell.className = "link-cell";
	if (!url) {
		return cell;
	}
	if (isSafeLink(url)) {
		const link = document.createElement("a");
		link.href = url;
		link.textContent = "Open";
		link.target = "_blank";
		link.rel = "noopener noreferrer";
		cell.appendChild(link);
	} else {
		// Blocks links like javascript:... by showing them as plain text
		cell.textContent = url;
	}
	return cell;
}

function actionsCell(application) {
	const cell = document.createElement("td");
	const wrapper = document.createElement("div");
	wrapper.className = "actions-cell";

	const edit = document.createElement("button");
	edit.type = "button";
	edit.className = "button";
	edit.textContent = "Edit";
	edit.addEventListener("click", () => openDialog(application));

	const remove = document.createElement("button");
	remove.type = "button";
	remove.className = "button danger";
	remove.textContent = "Delete";
	remove.addEventListener("click", () => deleteApplication(application));

	wrapper.append(edit, remove);
	cell.appendChild(wrapper);
	return cell;
}

function renderApplications(applications) {
	const rows = applications.map((application) => {
		const row = document.createElement("tr");
		const lpa = application.packageLpa;
		row.append(
			textCell(application.companyName),
			textCell(application.role),
			textCell(lpa === null ? "" : Number(lpa).toFixed(2)),
			textCell(application.appliedDate),
			textCell(STATUS_LABELS[application.status] ?? application.status),
			linkCell(application.jobLink),
			textCell(application.notes, "notes-cell"),
			actionsCell(application),
		);
		return row;
	});
	tableBody.replaceChildren(...rows);
	emptyMessage.hidden = applications.length > 0;
}

async function loadUser() {
	const user = await apiFetch("/api/auth/me");
	document.getElementById("user-name").textContent = user.name;
}

async function loadStats() {
	const stats = await apiFetch("/api/applications/stats");
	document.getElementById("stat-total").textContent = stats.total;
	for (const status of Object.keys(STATUS_LABELS)) {
		document.getElementById(`stat-${status}`).textContent = stats.byStatus[status];
	}
}

async function loadApplications() {
	const status = statusFilter.value;
	const query = status ? `?status=${encodeURIComponent(status)}` : "";
	renderApplications(await apiFetch(`/api/applications${query}`));
}

async function refresh() {
	await Promise.all([loadStats(), loadApplications()]);
}

function openDialog(application = null) {
	editingId = application ? application.id : null;
	dialogTitle.textContent = application ? "Edit application" : "Add application";
	hideMessage(formError);
	form.reset();
	if (application) {
		field("companyName").value = application.companyName;
		field("role").value = application.role;
		field("packageLpa").value = application.packageLpa ?? "";
		field("appliedDate").value = application.appliedDate ?? "";
		field("jobLink").value = application.jobLink ?? "";
		field("status").value = application.status;
		field("notes").value = application.notes ?? "";
	}
	dialog.showModal();
}

function readForm() {
	const packageLpa = field("packageLpa").value;
	return {
		companyName: field("companyName").value.trim(),
		role: field("role").value.trim(),
		packageLpa: packageLpa === "" ? null : Number(packageLpa),
		appliedDate: field("appliedDate").value || null,
		jobLink: field("jobLink").value.trim() || null,
		status: field("status").value,
		notes: field("notes").value || null,
	};
}

async function deleteApplication(application) {
	if (!confirm(`Delete the application to ${application.companyName}?`)) {
		return;
	}
	hideMessage(pageError);
	try {
		await apiFetch(`/api/applications/${application.id}`, { method: "DELETE" });
		await refresh();
	} catch (error) {
		showMessage(pageError, error.message);
	}
}

form.addEventListener("submit", async (event) => {
	event.preventDefault();
	const saveButton = form.querySelector("button[type=submit]");
	saveButton.disabled = true;
	hideMessage(formError);
	try {
		const path = editingId === null ? "/api/applications" : `/api/applications/${editingId}`;
		await apiFetch(path, { method: editingId === null ? "POST" : "PUT", body: readForm() });
		dialog.close();
		await refresh();
	} catch (error) {
		if (dialog.open) {
			showMessage(formError, error.message);
		} else {
			showMessage(pageError, error.message);
		}
	} finally {
		saveButton.disabled = false;
	}
});

document.getElementById("add-button").addEventListener("click", () => openDialog());
document.getElementById("cancel-button").addEventListener("click", () => dialog.close());

document.getElementById("logout-button").addEventListener("click", () => {
	clearToken();
	window.location.replace(LOGIN_PAGE);
});

statusFilter.addEventListener("change", async () => {
	hideMessage(pageError);
	try {
		await loadApplications();
	} catch (error) {
		showMessage(pageError, error.message);
	}
});

async function start() {
	if (!getToken()) {
		window.location.replace(LOGIN_PAGE);
		return;
	}
	try {
		await Promise.all([loadUser(), refresh()]);
	} catch (error) {
		showMessage(pageError, error.message);
	}
}

start();
