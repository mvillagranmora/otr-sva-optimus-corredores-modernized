-- Test data aligned with real HANA QA column names (2026-10-08)
-- PLA.Configuracion: key is 'PLA.FechaSistema' (confirmed in HANA QA: returns '2026-09-14')
INSERT INTO CONFIGURACION_PLA ("IdEmpresa", "CodigoParametro", "Valor")
    VALUES (1, 'PLA.FechaSistema', '2024-01-15');

-- CustodiaPortafolio: test positions for portfolio 101
INSERT INTO CUSTODIA_PORTAFOLIO ("IdPortafolio", "IdInstrumento", "Nemotecnico", "CantidadCustodia")
    VALUES (101, 201, 'CFIPGTPC-E', 1000.000000);
