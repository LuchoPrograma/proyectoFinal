document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    const employeeDataStr = localStorage.getItem('selectedEmpleado');
    const branchDataStr = localStorage.getItem('selectedCine');

    if (!employeeDataStr || !branchDataStr) {
        // Not logged in, redirect to login page
        window.location.href = 'index.html';
        return;
    }

    const employee = JSON.parse(employeeDataStr);
    const branch = JSON.parse(branchDataStr);

    // Update welcome header details
    document.getElementById('employeeGreeting').textContent = employee.nombre;
    document.getElementById('employeeDni').textContent = `DNI: ${employee.dni}`;
    document.getElementById('branchName').textContent = branch.nombre;

    // Generate initials for avatar
    const initials = employee.nombre
        .split(' ')
        .map(n => n[0])
        .join('')
        .substring(0, 2)
        .toUpperCase();
    document.getElementById('userAvatar').textContent = initials;

    // Back Button Logic
    document.getElementById('btnVolver').addEventListener('click', () => {
        // Clear session and return to login
        localStorage.removeItem('selectedEmpleado');
        localStorage.removeItem('selectedCine');
        window.location.href = 'index.html';
    });

    // Add Sale Button (Visual only for now)
    document.getElementById('btnNuevaVenta').addEventListener('click', () => {
        alert('Funcionalidad de nueva venta: ¡Próximamente!');
    });

    const salesTableBody = document.getElementById('salesTableBody');

    // Fetch up-to-date branch sales details from backend API using relative path
    fetch(`/api/v1/cines/${branch.id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cargar la información del cine.');
            }
            return response.json();
        })
        .then(cineData => {
            const ventas = cineData.ventas || [];
            
            // Sort sales by date descending (latest sales first)
            ventas.sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
            
            renderSalesTable(ventas);
        })
        .catch(err => {
            console.error('Error fetching sales:', err);
            salesTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="table-error">
                        <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                        Error al cargar las ventas de la sucursal. Por favor, intente más tarde.
                    </td>
                </tr>
            `;
        });

    function renderSalesTable(ventas) {
        salesTableBody.innerHTML = '';

        if (ventas.length === 0) {
            salesTableBody.innerHTML = `
                <tr>
                    <td colspan="7">
                        <div class="empty-state">No se registran ventas realizadas en esta sucursal.</div>
                    </td>
                </tr>
            `;
            return;
        }

        ventas.forEach(venta => {
            const row = document.createElement('tr');

            // Format date: DD/MM/AAAA HH:MM
            const dateObj = new Date(venta.fecha);
            const formattedDate = dateObj.toLocaleDateString('es-AR', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
            }) + ' ' + dateObj.toLocaleTimeString('es-AR', {
                hour: '2-digit',
                minute: '2-digit'
            });

            // Client name
            const clientName = venta.cliente ? venta.cliente.nombre : 'Consumidor Final';

            // Ticket qty
            const ticketQty = venta.entradas ? venta.entradas.length : 0;

            // Payment method
            const paymentType = venta.pago ? venta.pago.tipo : 'EFECTIVO';

            // Total amount
            const totalAmount = venta.pago ? `$${venta.pago.monto.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '$0,00';

            // Get Movie Title and Function Time from the first ticket (entrada)
            let movieTitle = 'Sin Película';
            let functionTime = 'N/A';

            if (venta.entradas && venta.entradas.length > 0) {
                const firstTicket = venta.entradas[0];
                if (firstTicket.funciones && firstTicket.funciones.length > 0) {
                    const func = firstTicket.funciones[0];
                    functionTime = func.horario || 'N/A';
                    if (func.pelicula) {
                        movieTitle = func.pelicula.titulo || 'Sin Película';
                    }
                }
            }

            row.innerHTML = `
                <td class="td-date">${formattedDate}</td>
                <td class="td-client">${clientName}</td>
                <td class="td-movie">${movieTitle}</td>
                <td class="td-time"><span class="badge-time">${functionTime}</span></td>
                <td class="td-qty">${ticketQty}</td>
                <td class="td-payment"><span class="badge-payment ${paymentType.toLowerCase()}">${paymentType}</span></td>
                <td class="td-total font-semibold">${totalAmount}</td>
            `;

            salesTableBody.appendChild(row);
        });
    }
});
