-- Marcelo Rojas
-- CELBUHOPRO-1086
-- Script para insertar en Pauta Servicio CircularCarteraCliente

-- Marcelo Rojas
-- CELBUHOPRO-1086
-- Script para insertar en Pauta Servicio CircularCarteraCliente

SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "EjecutarFinDeMesInhabil", "OrdenFinDeMesInhabil", "Notifica")
SELECT 1, 'CIRCULARCARTERACLIENTE', 'RESPALDO DIARIO', 'SIS', 'N', 'S', 'ACT', 'S', 4, 'cl.bcs.plataforma.jobs.PautaCustodiaCarteraCircular', 'N', 'N', 0, 'N'
FROM DUMMY  WHERE
NOT EXISTS(SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PAU.Proceso" WHERE "CodigoProceso"='CIRCULARCARTERACLIENTE');

COMMIT;