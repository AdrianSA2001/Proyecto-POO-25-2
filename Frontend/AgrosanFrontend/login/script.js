document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault(); // Evita recargar la página

  const dni = document.getElementById("dni").value.trim();
  const contrasena = document.getElementById("contrasena").value.trim();
  const mensaje = document.getElementById("mensaje");

  try {
    const response = await fetch("http://localhost:8080/agrosan/empleado/autenticar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ dni, contrasena })
    });

    const data = await response.json();

    if (!response.ok) {
      mensaje.textContent = data.message || "Credenciales incorrectas";
      mensaje.style.color = "red";
      return;
    }

    mensaje.textContent = "Inicio de sesión exitoso.";
    mensaje.style.color = "green";

    // Redirigir al menú
    setTimeout(() => {
      window.location.href = "../empleado/index.html";
    }, 900);

  } catch (error) {
    mensaje.textContent = "Error de conexión con el servidor.";
    mensaje.style.color = "red";
    console.log(error);
  }
});