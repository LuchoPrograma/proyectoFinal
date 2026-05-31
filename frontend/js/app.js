/**
 * @file app.js
 * @description Pantalla principal de selección de sucursal y empleado (login).
 * Permite al usuario elegir una sucursal de cine y luego seleccionar su empleado
 * para ingresar al sistema. La selección se persiste en localStorage antes de
 * redirigir al dashboard.
 */

document.addEventListener('DOMContentLoaded', () => {
    const branchesGrid = document.getElementById('branchesGrid');
    const employeesList = document.getElementById('employeesList');
    const searchInput = document.getElementById('searchInput');
    const btnIngresar = document.getElementById('btnIngresar');

    // Estado local de la pantalla de selección
    let allCines = [];
    let currentEmpleados = [];
    let selectedEmpleado = null;
    let selectedCine = null;

    // Obtiene la lista de todas las sucursales desde el backend al cargar la página
    fetch('http://localhost:9000/api/v1/cines')
        .then(response => response.json())
        .then(data => {
            allCines = data;
            renderBranches();
        })
        .catch(err => {
            console.error('Error al obtener las sucursales:', err);
            branchesGrid.innerHTML = '<div class="empty-state">Error al cargar las sucursales. Verifique que el backend esté corriendo.</div>';
        });

    /**
     * Renderiza las sucursales como tarjetas con radio buttons en la grilla.
     * Al seleccionar una sucursal se cargan automáticamente sus empleados.
     */
    function renderBranches() {
        branchesGrid.innerHTML = '';
        allCines.forEach(cine => {
            const label = document.createElement('label');
            label.className = 'branch-card';
            
            label.innerHTML = `
                <input type="radio" name="sucursal" value="${cine.id}">
                <div class="card-content">
                    ${cine.nombre}
                </div>
            `;
            
            label.querySelector('input').addEventListener('change', (e) => {
                if(e.target.checked) {
                    loadEmpleados(cine);
                }
            });
            
            branchesGrid.appendChild(label);
        });
    }

    /**
     * Actualiza el estado interno con los empleados de la sucursal seleccionada
     * y resetea la selección de empleado y el campo de búsqueda.
     * @param {Object} cine - Objeto de la sucursal seleccionada, incluyendo su lista de empleados.
     */
    function loadEmpleados(cine) {
        selectedCine = cine;
        currentEmpleados = cine.empleados || [];
        selectedEmpleado = null;
        btnIngresar.disabled = true;
        searchInput.value = '';
        renderEmpleados(currentEmpleados);
    }

    /**
     * Renderiza la lista de empleados recibida, resaltando visualmente el que
     * está actualmente seleccionado. Al hacer clic en un empleado se actualiza
     * la selección y se habilita el botón de ingreso.
     * @param {Array<Object>} empleados - Lista de empleados a mostrar.
     */
    function renderEmpleados(empleados) {
        employeesList.innerHTML = '';
        
        if (empleados.length === 0) {
            employeesList.innerHTML = '<div class="empty-state">No hay empleados encontrados.</div>';
            return;
        }

        empleados.forEach(emp => {
            const div = document.createElement('div');
            div.className = 'employee-item';
            if(selectedEmpleado && selectedEmpleado.id === emp.id) {
                div.classList.add('selected');
            }
            
            // Genera las iniciales del empleado tomando la primera letra de cada palabra del nombre
            const initials = emp.nombre.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();

            div.innerHTML = `
                <div class="employee-avatar">${initials}</div>
                <div class="employee-info">
                    <span class="employee-name">${emp.nombre}</span>
                    <span class="employee-dni">DNI: ${emp.dni}</span>
                </div>
            `;

            div.addEventListener('click', () => {
                selectedEmpleado = emp;
                btnIngresar.disabled = false;
                // Vuelve a renderizar la lista para reflejar la nueva selección visualmente
                renderEmpleados(empleados);
            });

            employeesList.appendChild(div);
        });
    }

    /**
     * Filtro de búsqueda en tiempo real sobre la lista de empleados de la sucursal activa.
     * Sanitiza la entrada eliminando caracteres no alfanuméricos para evitar inyecciones,
     * y filtra por coincidencia parcial en el nombre o el DNI del empleado.
     */
    searchInput.addEventListener('input', (e) => {
        // Elimina inmediatamente cualquier carácter que no sea letra, número o espacio
        let val = e.target.value;
        const validVal = val.replace(/[^a-zA-Z0-9\s]/g, '');
        
        if (val !== validVal) {
            e.target.value = validVal;
        }

        // Filtra los empleados de la sucursal actual según el término ingresado
        const searchTerm = validVal.toLowerCase();
        const filtered = currentEmpleados.filter(emp => 
            emp.nombre.toLowerCase().includes(searchTerm) || 
            emp.dni.toString().includes(searchTerm)
        );
        
        renderEmpleados(filtered);
    });

    /**
     * Persiste la sucursal y el empleado seleccionados en localStorage
     * y redirige al dashboard para iniciar la sesión de trabajo.
     */
    btnIngresar.addEventListener('click', () => {
        if(selectedEmpleado && selectedCine) {
            localStorage.setItem('selectedEmpleado', JSON.stringify(selectedEmpleado));
            localStorage.setItem('selectedCine', JSON.stringify(selectedCine));
            window.location.href = 'dashboard.html';
        }
    });
});
