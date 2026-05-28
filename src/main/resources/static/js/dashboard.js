document.addEventListener('DOMContentLoaded', () => {
    const BASE_URL = 'http://localhost:9000/api/v1';
    const PRECIO_ENTRADA = 8000;

    // ── Auth check ──────────────────────────────────────────────────────────
    const employeeDataStr = localStorage.getItem('selectedEmpleado');
    const branchDataStr   = localStorage.getItem('selectedCine');
    if (!employeeDataStr || !branchDataStr) {
        window.location.href = 'index.html';
        return;
    }

    const employee = JSON.parse(employeeDataStr);
    const branch   = JSON.parse(branchDataStr);

    // ── Header ──────────────────────────────────────────────────────────────
    document.getElementById('employeeGreeting').textContent = employee.nombre;
    document.getElementById('employeeDni').textContent      = `DNI: ${employee.dni}`;
    document.getElementById('branchName').textContent       = branch.nombre;
    const initials = employee.nombre.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    document.getElementById('userAvatar').textContent = initials;

    // ── Back button ─────────────────────────────────────────────────────────
    document.getElementById('btnVolver').addEventListener('click', () => {
        localStorage.removeItem('selectedEmpleado');
        localStorage.removeItem('selectedCine');
        window.location.href = 'index.html';
    });

    // ── Global state ─────────────────────────────────────────────────────────
    let cineData    = null;
    let allClientes = [];

    // Modal step state (reset on each open)
    let ms = {};   // ms = modalState

    // ── DOM refs ─────────────────────────────────────────────────────────────
    const salesTableBody = document.getElementById('salesTableBody');
    const ventaModal     = document.getElementById('ventaModal');
    const btnModalClose  = document.getElementById('btnModalClose');
    const btnPrevStep    = document.getElementById('btnPrevStep');
    const btnNextStep    = document.getElementById('btnNextStep');
    const modalFooter    = document.getElementById('modalFooter');
    const stepIndicator  = document.getElementById('stepIndicator');

    // ── Load initial cine data ───────────────────────────────────────────────
    function loadCineData() {
        return fetch(`${BASE_URL}/cines/${branch.id}`)
            .then(r => {
                if (!r.ok) throw new Error('No se pudo obtener los datos del cine.');
                return r.json();
            })
            .then(data => {
                cineData = data;
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

    loadCineData();

    // ── Render sales table ────────────────────────────────────────────────────
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
            const qty        = venta.entradas ? venta.entradas.length : 0;
            const tipoPago   = venta.pago ? venta.pago.tipo : 'EFECTIVO';
            const total      = venta.pago
                ? `$${venta.pago.monto.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                : '$0,00';

            // Try to get movie/function info from entradas.funciones
            let movieTitle   = '—';
            let functionTime = '—';
            if (venta.entradas && venta.entradas.length > 0) {
                const firstEntrada = venta.entradas[0];
                if (firstEntrada.funciones && firstEntrada.funciones.length > 0) {
                    const func = firstEntrada.funciones[0];
                    functionTime = func.horario || '—';
                    movieTitle   = func.pelicula ? func.pelicula.titulo : '—';
                }
            }
            // Fallback: annotated from modal state on freshly added rows
            if (movieTitle === '—' && venta._movieTitle)   movieTitle   = venta._movieTitle;
            if (functionTime === '—' && venta._funcTime)   functionTime = venta._funcTime;

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
    //  MODAL LOGIC
    // ════════════════════════════════════════════════════════════════════════

    // ── Open / close ──────────────────────────────────────────────────────────
    document.getElementById('btnNuevaVenta').addEventListener('click', openModal);
    btnModalClose.addEventListener('click', closeModal);
    ventaModal.addEventListener('click', e => { if (e.target === ventaModal) closeModal(); });

    function openModal() {
        resetModal();
        ventaModal.classList.remove('hidden');
        fetchClientes();
        populatePeliculas();
    }

    function closeModal() {
        ventaModal.classList.add('hidden');
    }

    // ── Reset all modal state & DOM ───────────────────────────────────────────
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

        // Step 1
        setClientMode('existing');
        document.getElementById('inputNombre').value  = '';
        document.getElementById('inputEmail').value   = '';
        document.getElementById('emailError').textContent = '';

        // Step 2
        document.getElementById('selectPelicula').value = '';
        const funcSel = document.getElementById('selectFuncion');
        funcSel.innerHTML  = '<option value="">Primero seleccione una película</option>';
        funcSel.disabled   = true;
        document.getElementById('funcionError').textContent = '';

        // Step 3
        document.getElementById('inputCantidad').value = '1';
        document.getElementById('cantidadError').textContent = '';
        document.getElementById('seatsGrid').innerHTML = '';

        // Step 4
        document.getElementById('pay-tarjeta').classList.add('active');
        document.getElementById('pay-efectivo').classList.remove('active');
        document.getElementById('inputFecha').value = ms.fechaVenta;
        document.getElementById('saleSummary').innerHTML = '';

        // Step 5
        document.getElementById('modal-step-5').innerHTML = '';

        goToStep(1);
    }

    // ── Step navigation ───────────────────────────────────────────────────────
    function goToStep(step) {
        [1, 2, 3, 4, 5].forEach(s =>
            document.getElementById(`modal-step-${s}`).classList.add('hidden')
        );
        document.getElementById(`modal-step-${step}`).classList.remove('hidden');

        // Update dots (only 1-4 shown in indicator)
        for (let i = 1; i <= 4; i++) {
            const dot = document.getElementById(`dot-${i}`);
            const lbl = document.getElementById(`lbl-${i}`);
            dot.classList.remove('active', 'done');
            lbl.classList.remove('active');
            if (i < step)      { dot.classList.add('done');   dot.textContent = '✓'; }
            else if (i === step){ dot.classList.add('active'); dot.textContent = i; lbl.classList.add('active'); }
            else                { dot.textContent = i; }
        }

        // Footer / indicator visibility
        if (step === 5) {
            stepIndicator.classList.add('hidden');
            modalFooter.classList.add('hidden');
        } else {
            stepIndicator.classList.remove('hidden');
            modalFooter.classList.remove('hidden');
        }

        // Prev button
        (step === 1 || step === 5)
            ? btnPrevStep.classList.add('hidden')
            : btnPrevStep.classList.remove('hidden');

        // Next button label
        if (step === 5) {
            btnNextStep.classList.add('hidden');
        } else {
            btnNextStep.classList.remove('hidden');
            btnNextStep.textContent = step === 4 ? 'Generar Venta' : 'Siguiente';
            btnNextStep.disabled    = false;
        }

        ms.currentStep = step;
    }

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

    btnPrevStep.addEventListener('click', () => {
        if (ms.currentStep > 1) goToStep(ms.currentStep - 1);
    });

    // ════════════════════════════════════════════════════════════════════════
    //  STEP 1 — CLIENT
    // ════════════════════════════════════════════════════════════════════════

    function fetchClientes() {
        fetch(`${BASE_URL}/clientes`)
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
    //  STEP 2 — MOVIE & FUNCTION
    // ════════════════════════════════════════════════════════════════════════

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

        // Gather all funciones for this pelicula from every sala in the cine
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

        funcSel.innerHTML = '<option value="">Seleccionar función...</option>';
        funciones.forEach((func, idx) => {
            const opt = document.createElement('option');
            opt.value       = idx;
            opt.textContent = `${func.horario} — Sala ${func._sala.numero} (cap. ${func._sala.capacidad})`;
            funcSel.appendChild(opt);
        });
        funcSel.disabled = false;
    });

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
    //  STEP 3 — SEATS
    // ════════════════════════════════════════════════════════════════════════

    function buildSeatsGrid() {
        const sala    = ms.selectedSala;
        const funcion = ms.selectedFuncion;
        if (!sala || !funcion) return;

        const capacidad     = sala.capacidad;
        const occupiedSeats = (funcion.entradas || []).map(e => e.asiento);
        ms.occupiedSeats    = occupiedSeats;
        ms.selectedSeats    = [];

        // Layout: up to 10 columns
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

    function toggleSeat(btn, label) {
        const cantidad = parseInt(document.getElementById('inputCantidad').value) || 1;
        if (btn.classList.contains('seat-selected')) {
            btn.classList.remove('seat-selected');
            ms.selectedSeats = ms.selectedSeats.filter(s => s !== label);
        } else {
            if (ms.selectedSeats.length < cantidad) {
                btn.classList.add('seat-selected');
                ms.selectedSeats.push(label);
            }
        }
        document.getElementById('cantidadError').textContent = '';
    }

    // When quantity changes, clear seat selection
    document.getElementById('inputCantidad').addEventListener('change', () => {
        ms.selectedSeats = [];
        document.querySelectorAll('.seat-btn.seat-selected').forEach(b => b.classList.remove('seat-selected'));
        document.getElementById('cantidadError').textContent = '';
    });

    // ════════════════════════════════════════════════════════════════════════
    //  STEP 4 — PAYMENT & SUMMARY
    // ════════════════════════════════════════════════════════════════════════

    document.getElementById('pay-tarjeta').addEventListener('click', () => {
        ms.paymentType = 'TARJETA';
        document.getElementById('pay-tarjeta').classList.add('active');
        document.getElementById('pay-efectivo').classList.remove('active');
    });

    document.getElementById('pay-efectivo').addEventListener('click', () => {
        ms.paymentType = 'EFECTIVO';
        document.getElementById('pay-efectivo').classList.add('active');
        document.getElementById('pay-tarjeta').classList.remove('active');
    });

    document.getElementById('inputFecha').addEventListener('change', e => {
        ms.fechaVenta = e.target.value;
    });

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
    //  VALIDATION
    // ════════════════════════════════════════════════════════════════════════

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

                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!email || !emailRegex.test(email)) {
                    emailError.textContent = 'Ingrese un email válido.';
                    return false;
                }

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
    //  CONFIRM & SAVE (async)
    // ════════════════════════════════════════════════════════════════════════

    async function confirmSale() {
        const funcion  = ms.selectedFuncion;
        const cantidad = ms.selectedSeats.length;
        const pelTitle = funcion.pelicula ? funcion.pelicula.titulo : '—';

        const confirmed = confirm(
            `¿Confirmar la venta de ${cantidad} entrada(s) para "${pelTitle}" - ${funcion.horario}?`
        );
        if (!confirmed) return;

        btnNextStep.disabled    = true;
        btnNextStep.textContent = 'Guardando...';

        try {
            // ── 1. Create client if new ──────────────────────────────────────
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

            // ── 2. PUT funcion — add new entradas (so the join table is updated) ──
            const originalEntradaIds = new Set((funcion.entradas || []).map(e => e.id));
            const newEntradasPayload  = ms.selectedSeats.map(seat => ({
                precio: PRECIO_ENTRADA,
                asiento: seat
            }));

            // Build a clean funcion object for the API (strip custom _sala property)
            const { _sala, ...funcionClean } = funcion;
            funcionClean.entradas = [...(funcion.entradas || []), ...newEntradasPayload];

            const funcResp = await fetch(`${BASE_URL}/funciones/${funcion.id}`, {
                method:  'PUT',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify(funcionClean)
            });
            if (!funcResp.ok) throw new Error('Error al actualizar la función con las nuevas entradas.');
            const updatedFuncion = await funcResp.json();

            // Find the newly persisted entradas by comparing IDs
            const newEntradas = (updatedFuncion.entradas || []).filter(e => !originalEntradaIds.has(e.id));
            if (newEntradas.length === 0) {
                throw new Error('No se recibieron las entradas creadas en la función. Revisá el backend.');
            }

            // ── 3. Build venta & PUT cine ────────────────────────────────────
            const fechaISO = new Date(`${ms.fechaVenta}T12:00:00`).toISOString();
            const total    = cantidad * PRECIO_ENTRADA;

            const newVenta = {
                fecha:   fechaISO,
                pago:    { monto: total, tipo: ms.paymentType },
                cliente: { id: clienteObj.id },
                entradas: newEntradas.map(e => ({ id: e.id }))
            };

            // Fetch fresh cine to avoid stale/dirty state
            const freshCineResp = await fetch(`${BASE_URL}/cines/${branch.id}`);
            if (!freshCineResp.ok) throw new Error('Error al obtener datos actualizados del cine.');
            const freshCine = await freshCineResp.json();
            freshCine.ventas = [...(freshCine.ventas || []), newVenta];

            const cineResp = await fetch(`${BASE_URL}/cines/${branch.id}`, {
                method:  'PUT',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify(freshCine)
            });
            if (!cineResp.ok) throw new Error('Error al registrar la venta en el cine.');
            const savedCine = await cineResp.json();

            // Update local cineData
            cineData = savedCine;

            // Re-render sales table with metadata for instant display
            const ventasOrdenadas = (savedCine.ventas || []).slice().sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
            // Annotate the newest venta so the table renders movie/time even before reload
            if (ventasOrdenadas.length > 0) {
                const newest = ventasOrdenadas[0];
                newest._movieTitle = funcion.pelicula ? funcion.pelicula.titulo : '—';
                newest._funcTime   = funcion.horario;
            }
            renderSalesTable(ventasOrdenadas);

            // ── 4. Show success screen ───────────────────────────────────────
            showSuccessScreen(clienteObj, updatedFuncion, newEntradas, total, savedCine.ventas.length);

        } catch (err) {
            console.error(err);
            alert(`No se pudo registrar la venta:\n${err.message}`);
            btnNextStep.disabled    = false;
            btnNextStep.textContent = 'Generar Venta';
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STEP 5 — SUCCESS SCREEN
    // ════════════════════════════════════════════════════════════════════════

    function showSuccessScreen(cliente, funcion, entradas, total, totalVentasCine) {
        goToStep(5);

        // Ticket numbers: base them on total ventas * some offset + seat index
        const ticketBase = (totalVentasCine - 1) * 10;

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
