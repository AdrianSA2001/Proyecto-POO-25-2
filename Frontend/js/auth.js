/**
 * Servicio de Autenticación
 * Maneja el login, logout y verificación de sesión
 */
const AuthService = {
  STORAGE_KEY: 'agrosan_user',
  TOKEN_KEY: 'agrosan_token',
  
  /**
   * Iniciar sesión
   */
  async login(dni, password) {
    try {
      const response = await fetch('http://localhost:8080/agrosan/empleado/autenticar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          dni: dni,
          contrasena: password
        })
      });
      
      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Credenciales inválidas');
      }
      
      const user = await response.json();
      
      // Guardar usuario en localStorage
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
      localStorage.setItem(this.TOKEN_KEY, 'authenticated'); // Simulación de token
      
      return user;
    } catch (error) {
      throw error;
    }
  },
  
  /**
   * Cerrar sesión
   */
  logout() {
    localStorage.removeItem(this.STORAGE_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
    window.location.href = 'index.html';
  },
  
  /**
   * Verificar si el usuario está autenticado
   */
  isAuthenticated() {
    return !!localStorage.getItem(this.STORAGE_KEY);
  },
  
  /**
   * Obtener usuario actual
   */
  getCurrentUser() {
    const userStr = localStorage.getItem(this.STORAGE_KEY);
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch (e) {
      return null;
    }
  },
  
  /**
   * Proteger ruta - redirige al login si no está autenticado
   */
  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = 'index.html';
      return false;
    }
    return true;
  }
};


