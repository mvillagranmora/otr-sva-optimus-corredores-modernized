-- Configuration entries
INSERT INTO CONFIGURACION_T VALUES (1, 'PLA.FechaSistema', '2024-01-15');
INSERT INTO CONFIGURACION_T VALUES (1, 'PLA.NumeroDiasFechaVencimiento', '30');

-- Documents in various states:
-- Row 1: ACT, BloquearAlVencer=S, expires 2024-01-15 (today) → should be blocked
INSERT INTO DOCUMENTOS_CLIENTE_T VALUES (100, 1, 'ACT', 'S', '2024-01-15', '2023-12-16');
-- Row 2: ACT, BloquearAlVencer=N, expires 2024-01-15 (today) → annex updated, NOT blocked
INSERT INTO DOCUMENTOS_CLIENTE_T VALUES (101, 2, 'ACT', 'N', '2024-01-15', '2023-12-16');
-- Row 3: PEN, BloquearAlVencer=S, expires 2024-01-15 → skipped (PEN state)
INSERT INTO DOCUMENTOS_CLIENTE_T VALUES (102, 3, 'PEN', 'S', '2024-01-15', '2023-12-16');
-- Row 4: ACT, BloquearAlVencer=S, expires 2024-01-20 (future) → not processed today
INSERT INTO DOCUMENTOS_CLIENTE_T VALUES (103, 4, 'ACT', 'S', '2024-01-20', '2023-12-21');

-- Clients
INSERT INTO CLIENTES_T VALUES (100, NULL, NULL);
INSERT INTO CLIENTES_T VALUES (101, NULL, NULL);
