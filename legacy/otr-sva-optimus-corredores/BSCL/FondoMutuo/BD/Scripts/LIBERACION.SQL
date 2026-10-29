-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 03-06-2016
--
-- Por definir jeje
update bscl."BSCL.Plataforma.BD::INS.TipoOperacion" set "RecibePagaPortafolio" = 'P' where "IdTipoOperacion" = 'APORFM';

update bscl."BSCL.Plataforma.BD::INS.TipoOperacion" set "RecibePagaPortafolio" = 'R' where "IdTipoOperacion" = 'RESCFM';

UPDATE "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
SET "IdModulo" = 'LIM'
WHERE "IdCREModuloEmpresa" = 11;

UPDATE "BSCL.Plataforma.BD::PLA.Modulo"
SET "IdModulo" = 'FMM'
WHERE "IdModulo" = 'MFM';

UPDATE "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
SET "IdModulo" = 'FMM'
WHERE "IdCREModuloEmpresa" = 11;

--Formateo de campos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (136,'CLP','FMM','','',2,'T',2,'T',2,'T',2,'T',4,'T',4,'T',4,'T','ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (137,'USD','FMM','','',2,'T',2,'T',2,'T',2,'T',4,'T',4,'T',4,'T','ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (138,'EUR','FMM','','',2,'T',2,'T',2,'T',2,'T',4,'T',4,'T',4,'T','ACT');

insert into "BSCL"."BSCL.Plataforma.BD::PLA.UsoMonedaFormato"
("IdUsoMonedaFormato","IdEmpresa","Codigo","Nombre","IdEstado")
values
('FMM',1,'FONDOMUTUO','FORMATO PARA MODULO FONDOS MUTUOS','ACT');

UPDATE "BSCL"."BSCL.Mercado.BD::MER.Emisor" 
SET "RutEmisor" = '99549940-1'
WHERE "TipoEmisor" = 'AGF' AND "IdEmisor" = 'CREDICORPAGF';


SET SCHEMA BSCL;
INSERT INTO "BSCL.Plataforma.BD::MER.Emisor" 
VALUES ('CREDICORPAGF','','','SVS','N','N','N','N','S',20241,'ACT','2017-01-11 10:27:49.0','');




----CFLORES - inicio

SET SCHEMA BSCL;GO

ALTER TABLE BSCL."BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
  ADD (	"CambioFondo"       NVARCHAR(1)     NOT NULL DEFAULT 'N');GO
  
  
ALTER TABLE BSCL."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
  ADD (	"Switch"      		NVARCHAR(1)   NOT NULL DEFAULT 'N');GO
  
  
 ALTER TABLE BSCL."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 
 add ("IdTipoCanalIngreso" NVARCHAR(3) default '' not null,
 "FolioContrato" BIGINT);GO
  
  
----CFLORES - fin
SELECT * FROM bscl."BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"

SELECT * FROM bscl."BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "NombreTipoParametro"='MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE'

UPDATE bscl."BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro"='CALENDARIO'
WHERE "NombreTipoParametro"='MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE' 



SELECT *
    FROM "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador"


    
    
    
 SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Modulo"
WHERE "IdModulo" = 'APV'





