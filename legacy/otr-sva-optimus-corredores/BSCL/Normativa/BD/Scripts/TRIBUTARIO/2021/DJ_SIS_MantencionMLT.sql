
--Se crea tabla de registros MLT

DROP TABLE BSCL."BSCL.Normativa.BD::SIS.RegistroMandatos";
CREATE TABLE BSCL."BSCL.Normativa.BD::SIS.RegistroMandatos" (
	--"IdRegistroMandatos"                BIGINT          NOT NULL,
	--"IdProcesoMandatos"                 BIGINT          NOT NULL,
	"IdSolicitudAporteRescate" 			BIGINT 			NOT NULL,
	"FolioMandato"						BIGINT 			DEFAULT 0,
	"Valor"								DECIMAL(26,8)	DEFAULT 0,
	--"CostoUF"							DECIMAL(26,8)	DEFAULT 0,
	--"RentabilidadReal"					DECIMAL(26,8)	DEFAULT 0,
	"IdEstado" 							NVARCHAR(40) 	DEFAULT '',
	"FechaIngreso"       				SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdUsuario"							BIGINT			NOT NULL DEFAULT 0,
	"IdEmpresa"							BIGINT			NOT NULL DEFAULT 0
	--PRIMARY KEY ("IdRegistroMandatos")
);

DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::SIS.IdRegistroMandatos";
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::SIS.IdRegistroMandatos"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;


DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::SIS.IdProcesoMandatos";
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::SIS.IdProcesoMandatos"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290687, 'BSCL.Normativa.BD.Vistas::SIS.ConsultarSolicitudesMLT', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290687);



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290691, 'PLUGIN_NOR.jar@cl.bcs.normativa.IngresarMLT', 24, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290691);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_MANTENEDOR_COSTO_18QUATER_y_RENTABILIDAD_APV',
'CL',
'Mantenedor de Costos (18Q) y Rentabilidad (APV)',
'MANTMLT'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_MANTENEDOR_COSTO_18QUATER_y_RENTABILIDAD_APV');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_INGRESO_DE_MLT',
'CL',
'Ingreso de MLT',
'MANTMLT'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_INGRESO_DE_MLT');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_FOLIO_MANDATO',
'CL',
'Folio Mandato',
'FolMan'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_FOLIO_MANDATO');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_ARCHIVO_DE_COSTO_18QUATER_y_RENTABILIDAD_APV',
'CL',
'Archivo de costo 18 Quater y Rentabilidad APV',
'ARMLT'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_ARCHIVO_DE_COSTO_18QUATER_y_RENTABILIDAD_APV');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_PROCESAR_COSTOS',
'CL',
'Procesar Costos',
'PROCOS'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_PROCESAR_COSTOS');



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290703, 'PLUGIN_NOR.jar@cl.bcs.normativa.CargarMLT', 0, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290703);


    
      
INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta")
SELECT
'ACTUALIZA_COSTO_TRIBUTARIO' 					AS "IdTipoPrograma"
, 'Actualizacion de costos Tributarios'				AS "Descripcion"
, 1 											AS "MaximoIntentos"
, 'Normativa'						 			AS "Modulo"
, 'cl.bcs.normativa.jobs.BcsJobProcesarCostos'  AS "NombreClasePrograma"
, 'JAVA' 										AS "TipoClasePrograma"
, 'N' 											AS "EsPauta"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ACTUALIZA_COSTO_TRIBUTARIO');



--SELECT * FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT'										
										
---CARGA : 1.- CABECERA DEL ARCHIVO MLT										
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Archivo"  ( "IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")								
SELECT 
(SELECT MAX("IdArchivo") + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo")  AS "IdArchivo"
, 1 AS "IdEmpresa"
, 'ARCHIVO_CARGA_MLT' AS"CodigoArchivo"
, 'S' AS "TieneSeparador"
, ';' AS "SeparadorColumnas"
, 'N' AS "TieneCabecera"
FROM dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT');

--SELECT *  FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" IN  ('MLT_IdSolicitudAporteRescate','MLT_FolioMandato','MLT_Valor')

---CARGA : 2.- FORMATO DE CAMPOS 

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
SELECT 
(SELECT MAX("IdFormato") + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Formato")  AS "IdFormato"
, 'MLT_IdSolicitudAporteRescate' AS "CodigoCampo"
, 'NUMBER' AS "TipoDato"
, 0 	AS "Posicion"
, 0 	AS "LargoCampo"
, 'S' 	AS "EsObligatorio"
, '' 	AS "Validacion"
, '' 	AS "MensajeValidacion"
, 'N'	AS "IncluyeDecimales"
, 0 	AS "CantidadDecimales"
, NULL 	AS "ForzarMayuscula"
FROM dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_IdSolicitudAporteRescate');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
SELECT 
(SELECT MAX("IdFormato") + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Formato")  AS "IdFormato"
, 'MLT_FolioMandato' AS "CodigoCampo"
, 'NUMBER' AS "TipoDato"
, 0 	AS "Posicion"
, 0 	AS "LargoCampo"
, 'S' 	AS "EsObligatorio"
, '' 	AS "Validacion"
, '' 	AS "MensajeValidacion"
, 'N'	AS "IncluyeDecimales"
, 0 	AS "CantidadDecimales"
, NULL 	AS "ForzarMayuscula"
FROM dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_FolioMandato');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
SELECT 
(SELECT MAX("IdFormato") + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Formato")  AS "IdFormato"
, 'MLT_Valor' AS "CodigoCampo"
, 'NUMBER' AS "TipoDato"
, 0 	AS "Posicion"
, 26 	AS "LargoCampo"
, 'S' 	AS "EsObligatorio"
, '^([0-9]*[0-9]\.*[0-9]*)$' 	AS "Validacion"
, '' 	AS "MensajeValidacion"
, 'S'	AS "IncluyeDecimales"
, 12 	AS "CantidadDecimales"
, NULL 	AS "ForzarMayuscula"
FROM dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_Valor');





/*
SELECT * FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" 
                  WHERE "IdArchivo" IN (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT')
                  AND  "IdFormato"  IN (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" IN ('MLT_IdSolicitudAporteRescate','MLT_FolioMandato','MLT_Valor'))
*/                 
                 
---	---CARGA : 3.- RELACION DE FORMATOS Y CABECERA 			
						
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"  ( "IdArchivoFormato","IdArchivo","IdFormato")
SELECT 
  (SELECT MAX("IdArchivoFormato") + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato")  								AS "IdArchivoFormato"
, (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT') 			AS "IdArchivo"
, (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_IdSolicitudAporteRescate') 	AS "IdFormato"
FROM dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" 
                  WHERE "IdArchivo" IN (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT')
                  AND  "IdFormato"  IN (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_IdSolicitudAporteRescate')
                 );

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"  ( "IdArchivoFormato","IdArchivo","IdFormato")
SELECT 
  (SELECT MAX("IdArchivoFormato") + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato")  								AS "IdArchivoFormato"
, (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT') 			AS "IdArchivo"
, (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_FolioMandato') 	AS "IdFormato"
FROM dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" 
                  WHERE "IdArchivo" IN (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT')
                  AND  "IdFormato"  IN (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_FolioMandato')
                 );

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"  ( "IdArchivoFormato","IdArchivo","IdFormato")
SELECT 
  (SELECT MAX("IdArchivoFormato") + 1 FROM BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato")  								AS "IdArchivoFormato"
, (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT') 			AS "IdArchivo"
, (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_Valor') 	AS "IdFormato"
FROM dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" 
                  WHERE "IdArchivo" IN (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT')
                  AND  "IdFormato"  IN (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo"='MLT_Valor')
                 );

/*
 SELECT * FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT';
 SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" IN  ('MLT_IdSolicitudAporteRescate','MLT_FolioMandato','MLT_Valor');
 
SELECT * FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" 
                  WHERE "IdArchivo" IN (SELECT "IdArchivo" FROM  BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo"='ARCHIVO_CARGA_MLT')
                  AND  "IdFormato"  IN (SELECT "IdFormato" FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" IN ('MLT_IdSolicitudAporteRescate','MLT_FolioMandato','MLT_Valor'));
                  

                  
                
 */               
      




--------------------fin script 














