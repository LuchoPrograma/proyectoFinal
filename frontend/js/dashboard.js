/**
 * @file dashboard.js
 * @description Módulo de control del panel de administración (dashboard) de la sucursal de cine.
 * Gestiona el flujo de trabajo para registrar nuevas ventas de entradas en múltiples pasos (wizard),
 * la visualización en tiempo real del historial de ventas de la sucursal activa, la interacción con la sala
 * y selección gráfica de asientos libres/ocupados, la creación de clientes al vuelo y la generación y
 * visualización de comprobantes (tickets) al finalizar exitosamente la venta.
 */

document.addEventListener('DOMContentLoaded', () => {
    /**
     * URL base de la API REST de Spring Boot.
     * @type {string}
     */
    const BASE_URL = 'http://localhost:9000/api/v1';

    /**
     * Precio unitario fijo establecido para cada entrada de cine.
     * @type {number}
     */
    const PRECIO_ENTRADA = 8000;

    // ── Control de autenticación y sesión de empleado/sucursal ───────────────
    const employeeDataStr = localStorage.getItem('selectedEmpleado');
    const branchDataStr   = localStorage.getItem('selectedCine');
    
    // Si no existen datos de sesión guardados, redirigir inmediatamente al login (index.html)
    if (!employeeDataStr || !branchDataStr) {
        window.location.href = 'index.html';
        return;
    }

    /**
     * Objeto con los datos del empleado logueado.
     * @type {Object}
     */
    const employee = JSON.parse(employeeDataStr);

    /**
     * Objeto con los datos de la sucursal (cine) seleccionada.
     * @type {Object}
     */
    const branch   = JSON.parse(branchDataStr);

    // ── Configuración del encabezado de la página (Header) ───────────────────
    document.getElementById('employeeGreeting').textContent = employee.nombre;
    document.getElementById('employeeDni').textContent      = `DNI: ${employee.dni}`;
    document.getElementById('branchName').textContent       = branch.nombre;
    
    // Generar iniciales del empleado para el círculo de avatar visual
    const initials = employee.nombre.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    document.getElementById('userAvatar').textContent = initials;

    // ── Botón de cierre de sesión / Volver ──────────────────────────────────
    document.getElementById('btnVolver').addEventListener('click', () => {
        // Limpiar el estado de sesión actual en localStorage al salir
        localStorage.removeItem('selectedEmpleado');
        localStorage.removeItem('selectedCine');
        window.location.href = 'index.html';
    });

    // ── Estado global de la aplicación ───────────────────────────────────────
    /**
     * Almacena los datos completos de la sucursal de cine actual cargados desde el backend.
     * @type {Object|null}
     */
    let cineData    = null;

    /**
     * Lista de todos los clientes con ventas asociadas a esta sucursal.
     * @type {Array<Object>}
     */
    let allClientes = [];

    /**
     * Estado del paso a paso (wizard) para el registro de una venta.
     * Se reinicia cada vez que se abre el modal.
     * @type {Object}
     */
    let ms = {};   // ms = modalState

    // ── Referencias a elementos del DOM ─────────────────────────────────────
    const salesTableBody = document.getElementById('salesTableBody');
    const ventaModal     = document.getElementById('ventaModal');
    const btnModalClose  = document.getElementById('btnModalClose');
    const btnPrevStep    = document.getElementById('btnPrevStep');
    const btnNextStep    = document.getElementById('btnNextStep');
    const modalFooter    = document.getElementById('modalFooter');
    const stepIndicator  = document.getElementById('stepIndicator');

    // ── Carga inicial de datos del cine ──────────────────────────────────────
    /**
     * Carga los datos de la sucursal (cine) activa desde el backend.
     * Recupera información detallada incluyendo salas, películas y el historial de ventas.
     * Ordena las ventas cronológicamente (de la más reciente a la más antigua) y las renderiza en la tabla.
     * @returns {Promise<void>} Promesa que se resuelve tras cargar y renderizar los datos.
     */
    function loadCineData() {
        return fetch(`${BASE_URL}/cines/${branch.id}`)
            .then(r => {
                if (!r.ok) throw new Error('No se pudo obtener los datos del cine.');
                return r.json();
            })
            .then(data => {
                cineData = data;
                // Ordenar las ventas por fecha en orden descendente (más recientes primero)
                const ventas = (cineData.ventas || []).slice().sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
                renderSalesTable(ventas);
            })
            .catch(err => {
                console.error(err);
                salesTableBody.innerHTML = `
                    <tr><td colspan="7" class="table-error">
                        <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                        Error al cargar las ventas. Verificá que el backend esté activo.
                     </td></tr>`;
            });
    }

    // Invocar la carga inicial de datos del cine al montar la vista
    loadCineData();

    // ── Renderizado de la tabla de ventas ─────────────────────────────────────
    /**
     * Renderiza el historial de ventas en la tabla de la interfaz de usuario.
     * Calcula la cantidad de entradas basándose en el precio unitario y el monto pagado,
     * formatea fechas y valores monetarios de acuerdo a la configuración local (es-AR),
     * y maneja la visualización de datos faltantes mediante fallbacks y placeholders.
     * @param {Array<Object>} ventas - Listado de ventas a renderizar.
     */
    function renderSalesTable(ventas) {
        salesTableBody.innerHTML = '';
        if (!ventas || ventas.length === 0) {
            salesTableBody.innerHTML = `
                <tr><td colspan="7">
                    <div class="empty-state">No se registran ventas en esta sucursal.</div>
                </td></tr>`;
            return;
        }
        ventas.forEach(venta => {
            const row       = document.createElement('tr');
            const dateObj   = new Date(venta.fecha);
            const dateFmt   = dateObj.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' })
                            + ' ' + dateObj.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
            const clientName = venta.cliente ? venta.cliente.nombre : 'Consumidor Final';
            const tipoPago   = venta.pago ? venta.pago.tipo : 'EFECTIVO';
            const total      = venta.pago
                ? `$${venta.pago.monto.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                : '$0,00';

            // Intentar obtener información de película y función desde la entidad del backend
            let movieTitle   = '—';
            let functionTime = '—';
            
            if (venta.funcion) {
                functionTime = venta.funcion.horario || '—';
                movieTitle   = venta.funcion.pelicula ? venta.funcion.pelicula.titulo : '—';
            }

            // Fallback: usar anotaciones manuales del estado del modal para filas recién agregadas
            if (movieTitle === '—' && venta._movieTitle)   movieTitle   = venta._movieTitle;
            if (functionTime === '—' && venta._funcTime)   functionTime = venta._funcTime;
            
            // Calcular cantidad de entradas basándose en el monto / precio unitario, ya que
            // la venta no persiste una relación directa de cantidad, sino entradas asociadas a la función
            let calcQty = 0;
            if (venta.pago && venta.pago.monto) {
                calcQty = Math.round(venta.pago.monto / PRECIO_ENTRADA);
            }
            const qty = calcQty > 0 ? calcQty : 0;

            row.innerHTML = `
                <td class="td-date">${dateFmt}</td>
                <td class="td-client">${clientName}</td>
                <td class="td-movie">${movieTitle}</td>
                <td class="td-time"><span class="badge-time">${functionTime}</span></td>
                <td class="td-qty">${qty}</td>
                <td class="td-payment"><span class="badge-payment ${tipoPago.toLowerCase()}">${tipoPago}</span></td>
                <td class="td-total font-semibold">${total}</td>`;
            salesTableBody.appendChild(row);
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LÓGICA DEL MODAL (COMPRA DE ENTRADAS)
    // ════════════════════════════════════════════════════════════════════════

    // ── Apertura y cierre ───────────────────────────────────────────────────
    document.getElementById('btnNuevaVenta').addEventListener('click', openModal);
    btnModalClose.addEventListener('click', closeModal);
    // Permite cerrar el modal haciendo clic fuera del contenedor (en el backdrop)
    ventaModal.addEventListener('click', e => { if (e.target === ventaModal) closeModal(); });

    /**
     * Abre el modal de registro de nueva venta.
     * Inicializa el estado del modal (wizard), muestra el modal en pantalla,
     * y dispara la precarga de los clientes y películas disponibles.
     */
    function openModal() {
        resetModal();
        ventaModal.classList.remove('hidden');
        fetchClientes();
        populatePeliculas();
    }

    /**
     * Cierra el modal de registro de nueva venta, ocultando el diálogo.
     */
    function closeModal() {
        ventaModal.classList.add('hidden');
    }

    // ── Reinicio del estado interno y vista del modal ────────────────────────
    /**
     * Reinicia por completo el estado interno del asistente (wizard) de venta y limpia
     * todos los campos, errores y secciones del formulario en el DOM para preparar
     * una nueva operación limpia desde el Paso 1.
     */
    function resetModal() {
        ms = {
            currentStep:    1,
            clientMode:     'existing',      // 'existing' | 'new'
            selectedCliente: null,           // { id, nombre, email }
            newClientData:  { nombre: '', email: '' },
            selectedPeliculaId: null,
            availableFunciones: [],          // [{ ...funcion, _sala }]
            selectedFuncion: null,
            selectedSala:   null,
            occupiedSeats:  [],
            cantidad:       1,
            selectedSeats:  [],
            paymentType:    'TARJETA',
            fechaVenta:     new Date().toISOString().split('T')[0]
        };

        // Paso 1
        setClientMode('existing');
        document.getElementById('inputNombre').value  = '';
        document.getElementById('inputEmail').value   = '';
        document.getElementById('emailError').textContent = '';

        // Paso 2
        document.getElementById('selectPelicula').value = '';
        const funcSel = document.getElementById('selectFuncion');
        funcSel.innerHTML  = '<option value="">Primero seleccione una película</option>';
        funcSel.disabled   = true;
        document.getElementById('funcionError').textContent = '';

        // Paso 3
        document.getElementById('inputCantidad').value = '1';
        document.getElementById('cantidadError').textContent = '';
        document.getElementById('seatsGrid').innerHTML = '';

        // Paso 4
        document.getElementById('pay-tarjeta').classList.add('active');
        document.getElementById('pay-efectivo').classList.remove('active');
        document.getElementById('inputFecha').value = ms.fechaVenta;
        document.getElementById('saleSummary').innerHTML = '';

        // Paso 5
        document.getElementById('modal-step-5').innerHTML = '';

        goToStep(1);
    }

    // ── Navegación paso a paso (Wizard) ──────────────────────────────────────
    /**
     * Navega a un paso específico del wizard del modal de venta.
     * Oculta todas las secciones del modal y muestra únicamente la sección correspondiente
     * al paso indicado. Actualiza la barra indicadora visual de pasos,
     * y configura la visibilidad de los botones de navegación ("Siguiente", "Atrás", etc.)
     * de acuerdo al contexto del paso actual.
     * @param {number} step - Número del paso al que se desea navegar (1 a 5).
     */
    function goToStep(step) {
        [1, 2, 3, 4, 5].forEach(s =>
            document.getElementById(`modal-step-${s}`).classList.add('hidden')
        );
        document.getElementById(`modal-step-${step}`).classList.remove('hidden');

        // Actualiza el indicador de progreso visual (círculos 1 a 4)
        for (let i = 1; i <= 4; i++) {
            const dot = document.getElementById(`dot-${i}`);
            const lbl = document.getElementById(`lbl-${i}`);
            dot.classList.remove('active', 'done');
            lbl.classList.remove('active');
            if (i < step)      { dot.classList.add('done');   dot.textContent = '✓'; }
            else if (i === step){ dot.classList.add('active'); dot.textContent = i; lbl.classList.add('active'); }
            else                { dot.textContent = i; }
        }

        // Visibilidad del footer y el indicador de progreso.
        // Se ocultan en el Paso 5 (pantalla de éxito final).
        if (step === 5) {
            stepIndicator.classList.add('hidden');
            modalFooter.classList.add('hidden');
        } else {
            stepIndicator.classList.remove('hidden');
            modalFooter.classList.remove('hidden');
        }

        // Configuración del botón "Volver atrás"
        (step === 1 || step === 5)
            ? btnPrevStep.classList.add('hidden')
            : btnPrevStep.classList.remove('hidden');

        // Configuración del botón "Siguiente"
        if (step === 5) {
            btnNextStep.classList.add('hidden');
        } else {
            btnNextStep.classList.remove('hidden');
            btnNextStep.textContent = step === 4 ? 'Generar Venta' : 'Siguiente';
            btnNextStep.disabled    = false;
        }

        ms.currentStep = step;
    }

    // Botón "Siguiente" o "Generar Venta" del modal
    btnNextStep.addEventListener('click', async () => {
        const step = ms.currentStep;
        if (step < 4) {
            if (!validateStep(step)) return;
            if (step === 2) buildSeatsGrid();
            if (step === 3) buildSummary();
            goToStep(step + 1);
        } else if (step === 4) {
            await confirmSale();
        }
    });

    // Botón "Volver atrás" del modal
    btnPrevStep.addEventListener('click', () => {
        if (ms.currentStep > 1) goToStep(ms.currentStep - 1);
    });

    // ════════════════════════════════════════════════════════════════════════
    //  PASO 1 — SELECCIÓN / ALTA DE CLIENTE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Obtiene el listado de clientes que tienen compras registradas en este cine
     * y popula el selector de clientes existentes en el Paso 1 del modal.
     */
    function fetchClientes() {
        fetch(`${BASE_URL}/cines/${branch.id}/clientes`)
            .then(r => r.json())
            .then(data => {
                allClientes = data;
                const sel = document.getElementById('selectCliente');
                sel.innerHTML = '<option value="">Seleccionar cliente...</option>';
                allClientes.forEach(c => {
                    const opt = document.createElement('option');
                    opt.value       = c.id;
                    opt.textContent = `${c.nombre} — ${c.email}`;
                    sel.appendChild(opt);
                });
            })
            .catch(() => {
                document.getElementById('selectCliente').innerHTML =
                    '<option value="">Error al cargar clientes</option>';
            });
    }

    /**
     * Configura el modo de selección de cliente (cliente existente vs. nuevo cliente).
     * Muestra u oculta los controles de formulario correspondientes y actualiza el estado visual de los botones de alternancia.
     * @param {'existing'|'new'} mode - El modo de cliente a establecer.
     */
    function setClientMode(mode) {
        ms.clientMode = mode;
        const isExisting = mode === 'existing';
        document.getElementById('toggle-existing').classList.toggle('active',  isExisting);
        document.getElementById('toggle-new').classList.toggle('active',      !isExisting);
        document.getElementById('existing-client-section').classList.toggle('hidden', !isExisting);
        document.getElementById('new-client-section').classList.toggle('hidden',  isExisting);
        document.getElementById('emailError').textContent = '';
    }

    document.getElementById('toggle-existing').addEventListener('click', () => setClientMode('existing'));
    document.getElementById('toggle-new').addEventListener('click',      () => setClientMode('new'));

    // ════════════════════════════════════════════════════════════════════════
    //  PASO 2 — SELECCIÓN DE PELÍCULA Y FUNCIÓN HORARIA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Popula el selector de películas del Paso 2 con los títulos y géneros de las películas
     * actualmente asociadas a la sucursal de cine activa.
     */
    function populatePeliculas() {
        if (!cineData || !cineData.peliculas) return;
        const sel = document.getElementById('selectPelicula');
        sel.innerHTML = '<option value="">Seleccionar película...</option>';
        cineData.peliculas.forEach(p => {
            const opt = document.createElement('option');
            opt.value       = p.id;
            opt.textContent = `${p.titulo} — ${p.genero}`;
            sel.appendChild(opt);
        });
    }

    // Escucha de cambio en película: carga dinámicamente sus funciones horarias y salas disponibles
    document.getElementById('selectPelicula').addEventListener('change', e => {
        const peliculaId = parseInt(e.target.value) || null;
        ms.selectedPeliculaId  = peliculaId;
        ms.selectedFuncion     = null;
        ms.selectedSala        = null;
        ms.availableFunciones  = [];
        document.getElementById('funcionError').textContent = '';

        const funcSel = document.getElementById('selectFuncion');

        if (!peliculaId) {
            funcSel.innerHTML = '<option value="">Primero seleccione una película</option>';
            funcSel.disabled  = true;
            return;
        }

        // Recopila todas las funciones asociadas a la película seleccionada en todas las salas de esta sucursal
        const funciones = [];
        (cineData.salas || []).forEach(sala => {
            (sala.funciones || []).forEach(func => {
                if (func.pelicula && func.pelicula.id === peliculaId) {
                    funciones.push({ ...func, _sala: sala });
                }
            });
        });

        ms.availableFunciones = funciones;

        if (funciones.length === 0) {
            funcSel.innerHTML = '<option value="">Sin funciones disponibles</option>';
            funcSel.disabled  = true;
            return;
        }

        // Popula el selector de funciones con los horarios y salas disponibles
        funcSel.innerHTML = '<option value="">Seleccionar función...</option>';
        funciones.forEach((func, idx) => {
            const opt = document.createElement('option');
            opt.value       = idx;
            opt.textContent = `${func.horario} — Sala ${func._sala.numero} (cap. ${func._sala.capacidad})`;
            funcSel.appendChild(opt);
        });
        funcSel.disabled = false;
    });

    // Escucha de cambio en la función seleccionada
    document.getElementById('selectFuncion').addEventListener('change', e => {
        const idx = e.target.value;
        if (idx === '') {
            ms.selectedFuncion = null;
            ms.selectedSala    = null;
            return;
        }
        const func = ms.availableFunciones[parseInt(idx)];
        ms.selectedFuncion = func;
        ms.selectedSala    = func._sala;
        document.getElementById('funcionError').textContent = '';
    });

    // ════════════════════════════════════════════════════════════════════════
    //  PASO 3 — SELECCIÓN DE ENTRADAS Y ASIENTOS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Construye y dibuja dinámicamente la cuadrícula interactiva de asientos para la sala seleccionada.
     * Consulta los asientos ya ocupados en la función actual y los deshabilita en la grilla visual,
     * configura los eventos de selección/deselección de asientos libres, y actualiza el texto informativo
     * sobre la disponibilidad total de la sala.
     */
    function buildSeatsGrid() {
        const sala    = ms.selectedSala;
        const funcion = ms.selectedFuncion;
        if (!sala || !funcion) return;

        const capacidad     = sala.capacidad;
        const occupiedSeats = (funcion.entradas || []).map(e => e.asiento);
        ms.occupiedSeats    = occupiedSeats;
        ms.selectedSeats    = [];

        // Estructura visual: cuadrícula flexible de hasta 10 columnas por fila
        const cols = Math.min(10, capacidad);
        const rows = Math.ceil(capacidad / cols);
        const LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

        const grid = document.getElementById('seatsGrid');
        grid.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;
        grid.innerHTML = '';

        let seatCount = 0;
        for (let r = 0; r < rows; r++) {
            for (let c = 1; c <= cols; c++) {
                if (++seatCount > capacidad) break;
                const label = `${LETTERS[r]}${c}`;
                const btn   = document.createElement('button');
                btn.type        = 'button';
                btn.className   = 'seat-btn';
                btn.textContent = label;
                btn.dataset.seat = label;

                // Deshabilitar y marcar visualmente los asientos que ya han sido ocupados/vendidos en esta función
                if (occupiedSeats.includes(label)) {
                    btn.classList.add('seat-occupied');
                    btn.disabled = true;
                } else {
                    btn.addEventListener('click', () => toggleSeat(btn, label));
                }
                grid.appendChild(btn);
            }
        }

        const available = capacidad - occupiedSeats.length;
        const cantInput = document.getElementById('inputCantidad');
        cantInput.max   = available;
        document.getElementById('seatsInfo').textContent =
            `Sala ${sala.numero} — ${available} de ${capacidad} asiento(s) disponibles`;
    }

    /**
     * Alterna (selecciona o deselecciona) un asiento específico en la cuadrícula visual de asientos.
     * Verifica que no se supere la cantidad de entradas indicada por el usuario en el Paso 3.
     * @param {HTMLButtonElement} btn - El elemento de botón del asiento interactivo.
     * @param {string} label - El identificador del asiento (por ejemplo, "A1").
     */
    function toggleSeat(btn, label) {
        const cantidad = parseInt(document.getElementById('inputCantidad').value) || 1;
        if (btn.classList.contains('seat-selected')) {
            btn.classList.remove('seat-selected');
            ms.selectedSeats = ms.selectedSeats.filter(s => s !== label);
        } else {
            // Asegura que no se puedan seleccionar más asientos que la cantidad estipulada
            if (ms.selectedSeats.length < cantidad) {
                btn.classList.add('seat-selected');
                ms.selectedSeats.push(label);
            }
        }
        document.getElementById('cantidadError').textContent = '';
    }

    // Al cambiar la cantidad de entradas deseadas, se resetea la selección de asientos actual
    document.getElementById('inputCantidad').addEventListener('change', () => {
        ms.selectedSeats = [];
        document.querySelectorAll('.seat-btn.seat-selected').forEach(b => b.classList.remove('seat-selected'));
        document.getElementById('cantidadError').textContent = '';
    });

    // ════════════════════════════════════════════════════════════════════════
    //  PASO 4 — MÉTODO DE PAGO Y RESUMEN DE COMPRA
    // ════════════════════════════════════════════════════════════════════════

    // Selección de método de pago: TARJETA
    document.getElementById('pay-tarjeta').addEventListener('click', () => {
        ms.paymentType = 'TARJETA';
        document.getElementById('pay-tarjeta').classList.add('active');
        document.getElementById('pay-efectivo').classList.remove('active');
    });

    // Selección de método de pago: EFECTIVO
    document.getElementById('pay-efectivo').addEventListener('click', () => {
        ms.paymentType = 'EFECTIVO';
        document.getElementById('pay-efectivo').classList.add('active');
        document.getElementById('pay-tarjeta').classList.remove('active');
    });

    // Control del cambio en la fecha de la venta
    document.getElementById('inputFecha').addEventListener('change', e => {
        ms.fechaVenta = e.target.value;
    });

    /**
     * Genera e inyecta dinámicamente el resumen detallado de la venta en el Paso 4 del modal.
     * Recopila datos de cliente, película, sala, horario, asientos seleccionados,
     * cantidad de entradas, precio unitario y calcula el monto total a pagar.
     */
    function buildSummary() {
        const funcion  = ms.selectedFuncion;
        const sala     = ms.selectedSala;
        const cantidad = ms.selectedSeats.length;
        const total    = cantidad * PRECIO_ENTRADA;

        let clientName = '—';
        if (ms.clientMode === 'existing') {
            const sel = document.getElementById('selectCliente');
            if (sel.value) {
                const found = allClientes.find(c => c.id === parseInt(sel.value));
                clientName = found ? `${found.nombre} — ${found.email}` : sel.options[sel.selectedIndex].text;
            }
        } else {
            const nombre = document.getElementById('inputNombre').value.trim();
            const email  = document.getElementById('inputEmail').value.trim();
            clientName   = nombre ? `${nombre} — ${email}` : '—';
        }

        document.getElementById('inputFecha').value = ms.fechaVenta;

        document.getElementById('saleSummary').innerHTML = `
            <div class="summary-row">
                <span class="s-key">Cliente</span>
                <span class="s-val">${clientName}</span>
            </div>
            <div class="summary-row">
                <span class="s-key">Película</span>
                <span class="s-val">${funcion.pelicula ? funcion.pelicula.titulo : '—'}</span>
            </div>
            <div class="summary-row">
                <span class="s-key">Género</span>
                <span class="s-val">${funcion.pelicula ? funcion.pelicula.genero : '—'}</span>
            </div>
            <div class="summary-row">
                <span class="s-key">Sala</span>
                <span class="s-val">Sala ${sala.numero}</span>
            </div>
            <div class="summary-row">
                <span class="s-key">Horario</span>
                <span class="s-val">${funcion.horario}</span>
            </div>
            <div class="summary-row">
                <span class="s-key">Asientos</span>
                <span class="s-val">${ms.selectedSeats.join(', ')}</span>
            </div>
            <div class="summary-row">
                <span class="s-key">Cantidad</span>
                <span class="s-val">${cantidad} entrada(s)</span>
            </div>
            <div class="summary-row">
                <span class="s-key">Precio unitario</span>
                <span class="s-val">$${PRECIO_ENTRADA.toLocaleString('es-AR')}</span>
            </div>
            <div class="summary-row total-row">
                <span class="s-key">Total</span>
                <span class="s-val">$${total.toLocaleString('es-AR')}</span>
            </div>`;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  VALIDACIÓN DE PASOS DEL WIZARD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Valida la corrección de los datos ingresados en el paso actual del wizard
     * antes de permitir la navegación al siguiente paso.
     * - Paso 1: Valida selección de cliente existente o ingreso de datos válidos para nuevo cliente (nombre y email).
     * - Paso 2: Valida que se haya seleccionado una función horaria.
     * - Paso 3: Valida que la cantidad de entradas sea >= 1 y coincida exactamente con la cantidad de asientos seleccionados en la grilla.
     * @param {number} step - El número de paso del wizard a validar.
     * @returns {boolean} True si los datos del paso son válidos y puede continuar; False en caso contrario.
     */
    function validateStep(step) {
        if (step === 1) {
            if (ms.clientMode === 'existing') {
                const sel = document.getElementById('selectCliente');
                if (!sel.value) { alert('Seleccione un cliente de la lista.'); return false; }
                ms.selectedCliente = allClientes.find(c => c.id === parseInt(sel.value)) || null;
                return true;
            } else {
                const nombre     = document.getElementById('inputNombre').value.trim();
                const email      = document.getElementById('inputEmail').value.trim();
                const emailError = document.getElementById('emailError');

                if (!nombre) { alert('Ingrese el nombre del cliente.'); return false; }

                // Expresión regular estándar para validación sintáctica de direcciones de correo electrónico
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!email || !emailRegex.test(email)) {
                    emailError.textContent = 'Ingrese un email válido.';
                    return false;
                }

                // Verificar localmente que el email no corresponda a un cliente ya cargado
                const exists = allClientes.some(c => c.email.toLowerCase() === email.toLowerCase());
                if (exists) {
                    emailError.textContent = 'Este email ya está registrado. Selecciónelo como cliente existente.';
                    return false;
                }

                emailError.textContent  = '';
                ms.newClientData        = { nombre, email };
                ms.selectedCliente      = null;
                return true;
            }
        }

        if (step === 2) {
            if (!ms.selectedFuncion) {
                document.getElementById('funcionError').textContent = 'Seleccione una función para continuar.';
                return false;
            }
            document.getElementById('funcionError').textContent = '';
            return true;
        }

        if (step === 3) {
            const cantidad   = parseInt(document.getElementById('inputCantidad').value);
            const cantError  = document.getElementById('cantidadError');

            if (!cantidad || cantidad < 1) {
                cantError.textContent = 'La cantidad debe ser al menos 1.';
                return false;
            }
            // Obligar a que el usuario pinche exactamente tantos asientos como entradas desea comprar
            if (ms.selectedSeats.length !== cantidad) {
                cantError.textContent =
                    `Seleccioná exactamente ${cantidad} asiento(s). Tenés ${ms.selectedSeats.length} marcado(s).`;
                return false;
            }

            cantError.textContent = '';
            ms.cantidad = cantidad;
            return true;
        }

        return true;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONFIRMACIÓN Y ENVÍO DE LA TRANSACCIÓN (ATÓMICO)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Confirma y procesa de forma asíncrona la venta.
     * Realiza un flujo transaccional en el frontend/backend:
     * 1. Registra al cliente en el servidor si se trata de un nuevo cliente.
     * 2. Envía la solicitud de registro de venta al endpoint atómico del cine en el backend,
     *    pasando DNI, función, asientos elegidos, tipo de pago y fecha.
     * 3. Refresca los datos del cine y el historial de ventas en la pantalla principal.
     * 4. Lanza y dibuja la pantalla de éxito con los comprobantes de entrada (tickets) correspondientes.
     * @async
     * @returns {Promise<void>}
     */
    async function confirmSale() {
        const funcion  = ms.selectedFuncion;
        const cantidad = ms.selectedSeats.length;
        const pelTitle = funcion.pelicula ? funcion.pelicula.titulo : '—';

        const confirmed = confirm(
            `¿Confirmar la venta de ${cantidad} entrada(s) para "${pelTitle}" - ${funcion.horario}?`
        );
        if (!confirmed) return;

        // Cambiar estado visual del botón de confirmación
        btnNextStep.disabled    = true;
        btnNextStep.textContent = 'Guardando...';

        try {
            // ── 1. Crear cliente en el backend en caso de ser nuevo ─────────
            let clienteObj;
            if (ms.clientMode === 'existing' && ms.selectedCliente) {
                clienteObj = ms.selectedCliente;
            } else {
                const r = await fetch(`${BASE_URL}/clientes`, {
                    method:  'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body:    JSON.stringify(ms.newClientData)
                });
                if (!r.ok) throw new Error('Error al crear el cliente.');
                clienteObj = await r.json();
                allClientes.push(clienteObj);
            }

            // ── 2. POST al endpoint dedicado — una sola llamada atómica ──────
            //    El backend carga entidades manejadas por JPA desde la BD y las
            //    vincula en una única transacción, evitando el error de Hibernate
            //    "Multiple representations of the same entity".
            //    Ya NO se hace PUT /funciones antes, lo que eliminaba el bloqueo
            //    prematuro de asientos si la venta no se completaba.
            const ventaResp = await fetch(`${BASE_URL}/cines/${branch.id}/ventas`, {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify({
                    clienteId:      clienteObj.id,
                    funcionId:      funcion.id,
                    asientos:       ms.selectedSeats,
                    precioUnitario: PRECIO_ENTRADA,
                    tipoPago:       ms.paymentType,
                    fecha:          ms.fechaVenta
                })
            });
            if (!ventaResp.ok) {
                const errBody = await ventaResp.json().catch(() => ({}));
                throw new Error(errBody.error || 'Error al registrar la venta en el cine.');
            }

            // ── 3. Refrescar datos completos del cine en la pantalla principal 
            const freshCineResp = await fetch(`${BASE_URL}/cines/${branch.id}`);
            if (!freshCineResp.ok) throw new Error('Error al refrescar los datos del cine.');
            cineData = await freshCineResp.json();

            // Ordenar cronológicamente e inyectar datos calculados locales en la fila fresca
            const ventasOrdenadas = (cineData.ventas || []).slice()
                .sort((a, b) => new Date(b.fecha) - new Date(a.fecha));

            if (ventasOrdenadas.length > 0) {
                ventasOrdenadas[0]._movieTitle = funcion.pelicula ? funcion.pelicula.titulo : '—';
                ventasOrdenadas[0]._funcTime   = funcion.horario;
            }
            renderSalesTable(ventasOrdenadas);

            // ── 4. Construir lista de entradas para la pantalla de éxito ─────
            const displayEntradas = ms.selectedSeats.map(asiento => ({
                asiento,
                precio: PRECIO_ENTRADA
            }));
            const total = cantidad * PRECIO_ENTRADA;

            // ── 5. Mostrar la pantalla de éxito (Paso 5) ─────────────────────
            showSuccessScreen(clienteObj, funcion, displayEntradas, total, cineData.ventas.length);

        } catch (err) {
            console.error(err);
            alert(`No se pudo registrar la venta:\n${err.message}`);
            btnNextStep.disabled    = false;
            btnNextStep.textContent = 'Generar Venta';
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PASO 5 — COMPROBANTES DE ENTRADAS (PANTALLA DE ÉXITO)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Muestra la pantalla de éxito (Paso 5) renderizando los comprobantes de entradas (tickets) individuales.
     * Calcula números de ticket correlativos basados en el historial y expone detalles completos
     * de la compra (película, género, sala, horario, cliente y método de pago).
     * @param {Object} cliente - Los datos del cliente comprador.
     * @param {Object} funcion - La función de cine comprada.
     * @param {Array<Object>} entradas - La lista de entradas generadas (cada una con su asiento y precio).
     * @param {number} total - El costo total de la transacción.
     * @param {number} totalVentasCine - Cantidad total de ventas del cine para calcular el número de ticket.
     */
    function showSuccessScreen(cliente, funcion, entradas, total, totalVentasCine) {
        goToStep(5);

        // Numeración base de tickets para la sucursal
        const ticketBase = (totalVentasCine - 1) * 10;

        // Genera el código HTML para cada ticket individual
        const ticketsHtml = entradas.map((entrada, idx) => {
            const ticketNum = `TK-${String(ticketBase + idx + 1).padStart(4, '0')}`;
            return `
                <div class="ticket-item">
                    <div class="ticket-header">
                        <span class="ticket-id">${ticketNum}</span>
                        <span class="ticket-seat">Asiento ${entrada.asiento}</span>
                    </div>
                    <div class="ticket-details">
                        <div class="ticket-detail">Película: <span>${funcion.pelicula ? funcion.pelicula.titulo : '—'}</span></div>
                        <div class="ticket-detail">Género: <span>${funcion.pelicula ? funcion.pelicula.genero : '—'}</span></div>
                        <div class="ticket-detail">Sala: <span>${ms.selectedSala ? `Sala ${ms.selectedSala.numero}` : '—'}</span></div>
                        <div class="ticket-detail">Horario: <span>${funcion.horario}</span></div>
                        <div class="ticket-detail">Cliente: <span>${cliente.nombre}</span></div>
                        <div class="ticket-detail">Email: <span>${cliente.email}</span></div>
                        <div class="ticket-detail">Método de pago: <span>${ms.paymentType}</span></div>
                        <div class="ticket-detail">Precio: <span>$${PRECIO_ENTRADA.toLocaleString('es-AR')}</span></div>
                    </div>
                </div>`;
        }).join('');

        // Inserta la vista estructurada con el botón de cierre finalizador
        document.getElementById('modal-step-5').innerHTML = `
            <div class="success-screen">
                <div class="success-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="2.5"
                         stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                </div>
                <div class="success-title">¡Venta registrada!</div>
                <div class="success-subtitle">
                    ${entradas.length} entrada(s) &bull;
                    Total $${total.toLocaleString('es-AR')} &bull;
                    ${ms.paymentType}
                </div>

                <div class="tickets-list">${ticketsHtml}</div>

                <button id="btnCloseSuccess" class="btn-primary" style="width:100%;margin-top:0.5rem;">
                    Cerrar
                </button>
            </div>`;

        document.getElementById('btnCloseSuccess').addEventListener('click', closeModal);
    }
});
