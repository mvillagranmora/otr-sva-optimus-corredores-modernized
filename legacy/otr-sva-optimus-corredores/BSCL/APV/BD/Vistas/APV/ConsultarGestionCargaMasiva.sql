SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarGestionCargaMasiva"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarGestionCargaMasiva"  AS 

SELECT CARGA."IdEmpresa"
     , "IdCargaMasiva"
     , "NombreCargaArchivo"
     , "CantidadRegistros"
     , USUARIO."IdUsuario"
     , USUARIO."CodigoUsuario"
     , TIPOCARGA."IdTipoCarga"
     , TIPOCARGA."NombreTipoCarga"
     , "Descripcion"
     , "FechaCarga"
     , CARGA."IdEstado"
FROM "BSCL.APV.BD::APV.CargaMasiva" AS CARGA
INNER JOIN "BSCL.APV.BD.Vistas::APV.ConsultarTipoCargaArchivo" AS TIPOCARGA
ON TIPOCARGA."IdTipoCarga" = CARGA."IdTipoCarga"
AND TIPOCARGA."IdEmpresa" = CARGA."IdEmpresa"
INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USUARIO
ON USUARIO."IdEmpresa" = CARGA."IdEmpresa"
AND USUARIO."IdUsuario" = CARGA."IdUsuario";

GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarGestionCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 