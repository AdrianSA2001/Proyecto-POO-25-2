document.getElementById("logout").addEventListener("click", () => {
  const confirmar = confirm("¿Seguro que deseas cerrar sesión?");

  if (confirmar) {
    // Limpias cualquier dato guardado (si usas localStorage)
    localStorage.removeItem("empleado");

    // Rediriges al login
    window.location.href = "../inicio/index.html";
  }
});