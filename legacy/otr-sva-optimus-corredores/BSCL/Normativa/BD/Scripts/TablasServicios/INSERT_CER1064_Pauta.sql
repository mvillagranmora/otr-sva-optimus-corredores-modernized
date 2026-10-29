SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "OrdenFinDeMesInhabil", "EjecutarFinDeMesInhabil")
VALUES(1, 'GENERAR1064PAUTA_R', 'Generacion Circular 1064 - BCS en Rueda', 'SIS', 'N', 'S', 'ACT', 'S', 2, 'cl.bcs.plataforma.jobs.PautaGenerar1064R', 'N', 0, 'N');

INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "OrdenFinDeMesInhabil", "EjecutarFinDeMesInhabil")
VALUES(1, 'GENERAR1064PAUTA_FR', 'Generacion Circular 1064 - BCS Fuera de Rueda', 'SIS', 'N', 'S', 'ACT', 'S', 2, 'cl.bcs.plataforma.jobs.PautaGenerar1064FR', 'N', 0, 'N');