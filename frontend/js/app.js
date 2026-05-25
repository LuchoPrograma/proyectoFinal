document.addEventListener('DOMContentLoaded', () => {
    const branchesGrid = document.getElementById('branchesGrid');
    const employeesList = document.getElementById('employeesList');
    const searchInput = document.getElementById('searchInput');
    const btnIngresar = document.getElementById('btnIngresar');

    let allCines = [];
    let currentEmpleados = [];
    let selectedEmpleado = null;

    // Fetch data from backend
    fetch('http://localhost:9000/api/v1/cines')
        .then(response => response.json())
        .then(data => {
            allCines = data;
            renderBranches();
        })
        .catch(err => {
            console.error('Error fetching cines:', err);
            branchesGrid.innerHTML = '<div class="empty-state">Error al cargar las sucursales. Verifique que el backend esté corriendo.</div>';
        });

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

    function loadEmpleados(cine) {
        // En una API real, podríamos hacer fetch(`http://localhost:9000/api/v1/cines/${cine.id}/empleados`)
        // Por ahora, asumimos que cine.empleados viene en el JSON inicial por la relación
        currentEmpleados = cine.empleados || [];
        selectedEmpleado = null;
        btnIngresar.disabled = true;
        searchInput.value = '';
        renderEmpleados(currentEmpleados);
    }

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
            
            // Generate initials
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
                renderEmpleados(empleados); // Re-render to show selection
            });

            employeesList.appendChild(div);
        });
    }

    // Input Validation and Filtering
    searchInput.addEventListener('input', (e) => {
        // Replace non-alphanumeric characters instantly
        let val = e.target.value;
        const validVal = val.replace(/[^a-zA-Z0-9\s]/g, '');
        
        if (val !== validVal) {
            e.target.value = validVal;
        }

        // Filter current empleados
        const searchTerm = validVal.toLowerCase();
        const filtered = currentEmpleados.filter(emp => 
            emp.nombre.toLowerCase().includes(searchTerm) || 
            emp.dni.toString().includes(searchTerm)
        );
        
        renderEmpleados(filtered);
    });

    btnIngresar.addEventListener('click', () => {
        if(selectedEmpleado) {
            alert(`Ingresando al sistema como: ${selectedEmpleado.nombre}`);
            // Here you would redirect or manage the session
        }
    });
});
