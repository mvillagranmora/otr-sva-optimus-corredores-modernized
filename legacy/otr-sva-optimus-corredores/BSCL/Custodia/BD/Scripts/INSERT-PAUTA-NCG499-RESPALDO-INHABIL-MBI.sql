SET SCHEMA "BSCL";
 
INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "EjecutarFinDeMesInhabil", "OrdenFinDeMesInhabil", "Notifica")
SELECT 1, 'CIRCULAR_CARTERA_CLIENTE_INHABIL', 'RESPALDO NCG 499 DIA INHABIL', 'SIS', 'N', 'S', 'ACT', 'S', 655, 'cl.bcs.plataforma.jobs.PautaCustodiaCarteraCircular', 'N', 'N', 0, 'N'
FROM DUMMY  WHERE
NOT EXISTS(SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PAU.Proceso" WHERE "CodigoProceso"='CIRCULARCARTERACLIENTE');
 
COMMIT;
 