SET SCHEMA "BSCL"; 

alter table "BSCL.Custodia.BD::CUS.EventoCapital"
 add ("ISFUT" NVARCHAR(1) NOT NULL DEFAULT 'N',"TasaEfectiva" DECIMAL(26,8) NOT NULL DEFAULT 0);

--Se agregar columnas nuevas para isfut de efento capital cliente
alter table "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
 add (  "ISFUT" NVARCHAR(1) NOT NULL DEFAULT 'N',
        "RetencionISFUT" NVARCHAR(1) NOT NULL DEFAULT 'N',
        "MontoRetencionISFUT" DECIMAL(26,8) NOT NULL DEFAULT 0);

--Se agrega diccionario para labels de campos nuevos en evento capital cliente
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_TASA_EFECTIVA', 'CL', 'Tasa Efectiva', 'TEFECT'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario"='UI_TASA_EFECTIVA');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT 'UI_ISFUT','CL','ISFUT','ISFUT'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario"='UI_ISFUT');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT 'UI_RETENCION_ISFUT','CL','Retencion ISFUT','Ret ISFUT'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario"='UI_RETENCION_ISFUT');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT 'UI_MONTO_RETENCION_ISFUT','CL','Monto Retencion','Monto Ret'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario"='UI_MONTO_RETENCION_ISFUT');

--Insert concepto nuevo RETISF
INSERT INTO "BSCL.Custodia.BD::CUS.ConceptoCustodia"
("IdConceptoCustodia", "IdConcepto", "NombreEntidad", "IdClasificacionTipoInstrumento", "IdTipoOperacion", "ConCertificadoAutomatico", "IdTipoInstrumento", "IdInstrumento", "IdContraparte", "Descripcion", "GeneraMovimientoEnTransito", "EsGarantiaVendible", "EsTransferenciaCliente", "GeneraTransferenciaBoveda", "EsTransferenciaPortafolio", "GeneraTraspaso", "VisiblePorEjecutivo", "IdTipoCuenta", "ConfiguradoEnModulo", "RetieneDividendos", "IdEntidadDestinoGarantia", "FolioObligatorio", "VisibleFiori", "VisibleWS", "VisibleOptimus", "RetieneIntereses", "RetieneAmortizacion", "EsGarantiaPrendaria")
SELECT "BSCL.Custodia.BD.Secuencias::ConceptoCustodia".NEXTVAL as "IdConceptoCustodia", 'RETISF', 'CUS.INGRESORETIROTITULO', 'RV', '-', 'S', '', 0, 3, '', '*', 'N', 'N', 'N', 'N', '*', 'S', '', 'S', 'N', 0, 'N', 'N', 'N', 'S', 'N', 'N', 'N'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia" WHERE "IdConcepto"='RETISF' AND "NombreEntidad"='CUS.INGRESORETIROTITULO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"
("IdConcepto", "Nombre", "IdEstado", "TipoConcepto", "IdTipoCriterioNeteo", "NombreConceptoCartola", "ExclusivoCartola")
SELECT 'RETISF', 'RETENCION ISFUT', 'VIG', 'SISTEMA', NULL, '', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Concepto" WHERE "IdConcepto"='RETISF');

INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
("IdConcepto", "TipoConcepto", "DesplegarEnCombobox", "DisponibleIngresoManual", "VisiblePorEjecutivo", "NombreEntidad", "Origen", "ConfiguradoEnModulo", "VisibleFiori", "VisibleWS", "VisibleOptimus", "AfectaRentabilidad", "MarcaInterfazLavadoDeDinero")
SELECT 'RETISF', 'AM', 'S', 'S', 'S', 'CTA.CARGOABONOMOVIMIENTO', 'MAN', 'S', 'N', 'N', 'S', 'S', 'N'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" WHERE "IdConcepto"='RETISF' AND "NombreEntidad"='CTA.CARGOABONOMOVIMIENTO');
