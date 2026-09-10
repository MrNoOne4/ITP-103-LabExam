function showPage(pageId) {

    // Hide all pages

    document.querySelectorAll(".page").forEach(page => {

        page.classList.remove("active");

    });


    // Show selected page

    const selectedPage =
        document.getElementById(pageId);

    if (selectedPage) {

        selectedPage.classList.add("active");

    }


    // Update sidebar

    document.querySelectorAll(".nav-btn").forEach(button => {

        button.classList.remove("active");

    });


    const buttons =
        document.querySelectorAll(".nav-btn");


    const pageMap = {

        dashboard: 0,
        task1: 1,
        task2: 2,
        task3: 3,
        task4: 4,
        task5: 5,
        architecture: 6

    };


    if (pageMap[pageId] !== undefined) {

        buttons[pageMap[pageId]]
            .classList.add("active");

    }


    // Update header

    const titles = {

        dashboard:
            [
                "Hospital EIP Dashboard",
                "Enterprise Integration Patterns"
            ],

        task1:
            [
                "Message Channel",
                "JMS patient registration"
            ],

        task2:
            [
                "Content-Based Router",
                "Department-based message routing"
            ],

        task3:
            [
                "Patient Aggregator",
                "Patient + Doctor + Laboratory"
            ],

        task4:
            [
                "Message Translator",
                "XML → JSON"
            ],

        task5:
            [
                "Error Handling & Retry",
                "Dead Letter Channel demonstration"
            ],

        architecture:
            [
                "Hospital EIP Architecture",
                "Apache Camel integration flow"
            ]

    };


    if (titles[pageId]) {

        document.getElementById("pageTitle")
            .textContent =
            titles[pageId][0];

        document.getElementById("pageDescription")
            .textContent =
            titles[pageId][1];

    }

}


// ============================================================
// API REQUEST HELPER
// ============================================================

async function apiRequest(
    endpoint,
    method,
    data
) {

    try {

        const response =
            await fetch(
                endpoint,
                {
                    method: method,

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(data)
                }
            );


        const result =
            await response.json();


        if (!response.ok) {

            throw new Error(
                result.message ||
                "Server returned an error."
            );

        }


        return result;

    } catch (error) {

        showError(
            "Connection Error",
            error.message
        );

        throw error;

    }

}


// ============================================================
// MODAL
// ============================================================

function showSuccess(
    title,
    message,
    data
) {

    document.getElementById("modalIcon")
        .textContent = "✓";

    document.getElementById("modalIcon")
        .className =
        "modal-icon success";


    document.getElementById("modalTitle")
        .textContent = title;


    document.getElementById("modalMessage")
        .textContent = message;


    const dataElement =
        document.getElementById("modalData");


    if (data !== undefined) {

        if (typeof data === "object") {

            dataElement.textContent =
                JSON.stringify(
                    data,
                    null,
                    4
                );

        } else {

            dataElement.textContent =
                String(data);

        }

    } else {

        dataElement.textContent = "";

    }


    document.getElementById("resultModal")
        .classList.add("show");

}


function showError(
    title,
    message
) {

    document.getElementById("modalIcon")
        .textContent = "!";

    document.getElementById("modalIcon")
        .className =
        "modal-icon success";


    document.getElementById("modalTitle")
        .textContent = title;


    document.getElementById("modalMessage")
        .textContent = message;


    document.getElementById("modalData")
        .textContent = "";


    document.getElementById("resultModal")
        .classList.add("show");

}


function closeModal() {

    document.getElementById("resultModal")
        .classList.remove("show");

}


// Close modal if clicking outside

document.getElementById("resultModal")
    .addEventListener(
        "click",
        function(event) {

            if (event.target === this) {

                closeModal();

            }

        }
    );


// ============================================================
// TASK 1
// ============================================================

document.getElementById("task1Form")
    .addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const patientId =
                document.getElementById(
                    "task1PatientId"
                ).value;


            const patientName =
                document.getElementById(
                    "task1PatientName"
                ).value;


            const department =
                document.getElementById(
                    "task1Department"
                ).value;


            const result =
                await apiRequest(
                    "/api/task1",
                    "POST",
                    {
                        patientId:
                            patientId,

                        patientName:
                            patientName,

                        department:
                            department
                    }
                );


            showSuccess(
                result.title,
                result.message,
                result.data
            );

        }
    );


// ============================================================
// TASK 2
// ============================================================

document.getElementById("task2Form")
    .addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const patientId =
                document.getElementById(
                    "task2PatientId"
                ).value;


            const department =
                document.getElementById(
                    "task2Department"
                ).value;


            const result =
                await apiRequest(
                    "/api/task2",
                    "POST",
                    {
                        patientId:
                            patientId,

                        department:
                            department
                    }
                );


            showSuccess(
                result.title,
                result.message,
                result.data
            );

        }
    );


// ============================================================
// TASK 3
// ============================================================

document.getElementById("task3Form")
    .addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const patientId =
                document.getElementById(
                    "task3PatientId"
                ).value;


            const patientName =
                document.getElementById(
                    "task3PatientName"
                ).value;


            const doctor =
                document.getElementById(
                    "task3Doctor"
                ).value;


            const assessment =
                document.getElementById(
                    "task3Assessment"
                ).value;


            const test =
                document.getElementById(
                    "task3Test"
                ).value;


            const labResult =
                document.getElementById(
                    "task3Result"
                ).value;


            const result =
                await apiRequest(
                    "/api/task3",
                    "POST",
                    {
                        patientId:
                            patientId,

                        patientName:
                            patientName,

                        doctor:
                            doctor,

                        assessment:
                            assessment,

                        test:
                            test,

                        labResult:
                            labResult
                    }
                );


            showSuccess(
                result.title,
                result.message,
                result.data
            );

        }
    );


// ============================================================
// TASK 4
// ============================================================

document.getElementById("task4Form")
    .addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const patientId =
                document.getElementById(
                    "task4PatientId"
                ).value;


            const patientName =
                document.getElementById(
                    "task4PatientName"
                ).value;


            const test =
                document.getElementById(
                    "task4Test"
                ).value;


            const resultValue =
                document.getElementById(
                    "task4Result"
                ).value;


            // Update XML preview

            const xml =
`<PatientResult>
    <PatientId>${escapeHtml(patientId)}</PatientId>
    <PatientName>${escapeHtml(patientName)}</PatientName>
    <Test>${escapeHtml(test)}</Test>
    <Result>${escapeHtml(resultValue)}</Result>
</PatientResult>`;


            document.getElementById(
                "xmlPreview"
            ).textContent = xml;


            const result =
                await apiRequest(
                    "/api/task4",
                    "POST",
                    {
                        patientId:
                            patientId,

                        patientName:
                            patientName,

                        test:
                            test,

                        result:
                            resultValue
                    }
                );


            // Display translated JSON

            document.getElementById(
                "jsonOutput"
            ).textContent =
                JSON.stringify(
                    result.data,
                    null,
                    4
                );


            showSuccess(
                result.title,
                result.message,
                result.data
            );

        }
    );


// ============================================================
// TASK 5
// ============================================================

document.getElementById("task5Form")
    .addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const patientId =
                document.getElementById(
                    "task5PatientId"
                ).value;


            const type =
                document.getElementById(
                    "task5Type"
                ).value;


            const result =
                await apiRequest(
                    "/api/task5",
                    "POST",
                    {
                        patientId:
                            patientId,

                        type:
                            type
                    }
                );


            showSuccess(
                result.title,
                result.message,
                result.data
            );

        }
    );


// ============================================================
// HTML ESCAPING
// ============================================================

function escapeHtml(value) {

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


// ============================================================
// CHECK SERVER
// ============================================================

async function checkServer() {

    try {

        const response =
            await fetch("/api/health");


        if (response.ok) {

            console.log(
                "Hospital EIP server is online."
            );

        }

    } catch (error) {

        console.error(
            "Hospital EIP server is unavailable."
        );

    }

}


// Run health check

checkServer();