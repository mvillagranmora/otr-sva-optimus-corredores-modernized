SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCabeceraCargaOperacionesTermino";
GO 
CREATE VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCabeceraCargaOperacionesTermino" AS

SELECT 
DETALLE."IdCargaMasivaDetalle"
,CARGA_MASIVA."IdCargaMasiva"
,DETALLE."SecuenciaOperacion"
,DETALLE."FolioAgrupadorCartera"
,DETALLE."DocumentoIdentidad"
,DETALLE."CodigoPortafolio"
,DETALLE."TipoOperacion"
,DETALLE."TipoReajuste"
,DETALLE."ValorInicial"
,DETALLE."IdEstado"
,CARGA_MASIVA."IdUsuario"
,CARGA_MASIVA."FechaCarga"
,USUARIO."CodigoUsuario"
,CARGA_MASIVA."CodigoTipoCargaMasiva"
,TIPO_CARGA."IdTipoCargaMasiva"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaDetalle" AS DETALLE
INNER JOIN "BSCL.Mesa.BD::CAR.CargaMasiva" AS CARGA_MASIVA ON CARGA_MASIVA."IdCargaMasiva" = DETALLE."IdCargaMasiva"
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USUARIO ON USUARIO."IdUsuario" = CARGA_MASIVA."IdUsuario"
INNER JOIN "BSCL.Mesa.BD::CAR.TipoCargaMasivaRF" TIPO_CARGA ON TIPO_CARGA."CodigoTipoCargaMasiva" = CARGA_MASIVA."CodigoTipoCargaMasiva"
WHERE DETALLE."TipoRegistro" = 'C' AND DETALLE."IdEstado" IN ('ING', 'PRO');
GO 
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::CAR.ConsultarCabeceraCargaOperacionesTermino" TO  BCS_DESARROLLADOR WITH GRANT OPTION;