# ClinicaApp: Gestión de Citas Médicas

## Descripción General

**ClinicaApp** es una aplicación móvil Android desarrollada como proyecto final para la asignatura **Desarrollo de Aplicaciones Móviles II**. Esta aplicación simula un sistema de gestión para una clínica, permitiendo la interacción entre pacientes y doctores a través de una interfaz intuitiva y funcional.

El sistema cuenta con dos roles principales (Paciente y Doctor), cada uno con un conjunto de funcionalidades específicas diseñadas para facilitar la administración de citas, perfiles y registros médicos.

---

## Información Académica

- **Universidad:** Universidad de El Salvador (UES)
- **Carrera:** Ingeniería en Desarrollo de Software
- **Asignatura:** Desarrollo de Aplicaciones Móviles II

---

## Desarrolladores

- **Franklin Giovanny Avila González**
- **Leonel Antonio Hernández Pérez**

---

## Características Principales

- **Autenticación de Usuarios:**
  - Sistema de registro y login diferenciado para Pacientes y Doctores.
  - Integración con Firebase Authentication para un inicio de sesión seguro.

- **Gestión de Perfiles:**
  - Los usuarios pueden ver y editar la información de su perfil, incluyendo nombre, teléfono y foto.
  - Los doctores pueden, además, especificar su especialidad.

- **Dashboard Personalizado:**
  - **Vista de Paciente:** Muestra la próxima cita programada y permite acceder a su historial de citas y recetas.
  - **Vista de Doctor:** Presenta un listado completo de todas las citas registradas en el sistema para una gestión centralizada.

- **Módulo de Citas:**
  - Creación, edición y eliminación de citas médicas.
  - Selección de paciente, doctor, fecha y hora.
  - Asignación automática del doctor logueado al crear una nueva cita.
  - Exportación de los detalles de una cita a formato PDF.

- **Persistencia y Sincronización de Datos:**
  - Uso de **Room** como base de datos local para una experiencia fluida y offline.
  - Sincronización de datos con **Firebase Firestore** para mantener la información actualizada en la nube.

---

## Tecnologías Utilizadas

- **Lenguaje:** Java
- **Arquitectura:** Android SDK Nativo
- **Base de Datos Local:** Room Persistence Library
- **Backend y Sincronización:** Firebase (Authentication, Firestore)
- **Componentes de UI:**
  - Material Components for Android
  - ViewBinding
  - RecyclerView para la gestión de listas.
- **Navegación:** Android Navigation Component
- **Carga de Imágenes:** Glide
