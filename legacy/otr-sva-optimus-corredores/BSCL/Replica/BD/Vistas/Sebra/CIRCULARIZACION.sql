SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.CIRCULARIZACION"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.CIRCULARIZACION" AS

 
	SELECT
        	CAST(
				CASE WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS varchar) AS "PER_ID", -- INT
				
            COALESCE (NOM."IdDocumento" , 0 )  AS "FOLIO", -- INT
            LEFT(coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( DOC."DocumentoIdentidad" AS VARCHAR(11))), ''),11)  AS "RUT_CLI", -- VARCHAR(11)
            COALESCE (CAST(NOM."CodigoPortafolio" AS VARCHAR(100)),'-1')  AS "CUENTA", -- VARCHAR(100)
            
            COALESCE (CAST(SUBSTRING (DOCNOR."AnoMes",0,4) || '-' || SUBSTRING(DOCNOR."AnoMes",5,2) AS VARCHAR(7)),'')  AS "PERIODO", -- VARCHAR(7)
            
            COALESCE (CAST(CASE WHEN PER."Tipo" = 'N' THEN (PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" || ' ' || PER."Nombres")
            					ELSE PER."RazonSocial" END AS VARCHAR(255)),'')  AS "NOMBRE_RAZON_SOCIAL", -- VARCHAR(255)
            COALESCE (CAST(CASE WHEN PER."Tipo" = 'N' THEN 'NAT'
            					ELSE 'JUR' END AS VARCHAR(50)),'')  AS "TIPO_PERSONA", -- VARCHAR(50)
            COALESCE (CAST(ELE."Direccion" AS VARCHAR(50)),'')  AS "MAIL", -- VARCHAR(50)
            COALESCE (DF."IdDireccionFisica", 0 )  AS "DIR_ID", -- INT
            COALESCE (CAST(DF."Calle" || ' ' || DF."Numero" AS VARCHAR(200)),'')  AS "DIRECCION", -- VARCHAR(200)
            COALESCE (CAST(DIV."NombreDivisionPais" AS VARCHAR(50)),'')  AS "COMUNA", -- VARCHAR(50)
            COALESCE (CAST(CIU."NombreCiudad" AS VARCHAR(50)),'')  AS "CIUDAD", -- VARCHAR(50)
            COALESCE (CAST(CLAS."NombreClasificacionDivisionPais" AS VARCHAR(50)),'')  AS "REGION", -- VARCHAR(50)
            COALESCE (CAST(TEL."Numero" AS VARCHAR(20)),'')  AS "TELEFONO", -- VARCHAR(20)
            COALESCE (CAST(NOM."Notificar" AS VARCHAR(2)),'')  AS "ENVIAR", -- CHAR(2)
            COALESCE (CAST(CASE WHEN NOM."FormaEnvio" = 'INTERNET' THEN 'IN'
            					WHEN NOM."FormaEnvio" = 'MANUAL' THEN 'MA'
            					ELSE 'AM' END AS VARCHAR(2)),'')  AS "MEDIO", -- CHAR(2)
            COALESCE (CAST(NOM."IdEstadoRespuesta" AS VARCHAR(2)),'')  AS "RESPUESTA", -- CHAR(2)
            COALESCE (CAST(NOM."Motivo" AS VARCHAR(255)),'')  AS "RAZON", -- VARCHAR(255)
            COALESCE (CAST(CASE WHEN NOM."IdEstadoNotificacion" = 'ENV' THEN 'SI' ELSE 'NO' END AS VARCHAR(2)),'')  AS "ENVIADO", -- CHAR(2)
            COALESCE (CAST('NN' AS VARCHAR(2)),'')  AS "RECIBIDO", -- CHAR(2)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "GENERACION_PDF" -- CHAR(1)
        
        FROM "BSCL.Normativa.BD::CIR.Nomina" NOM
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON NOM."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON PER."IdDireccionFisicaPrincipal" = DF."IdDireccionFisica"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL ON PER."IdTelefonoPrincipal" = TEL."IdTelefono"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" ELE ON PER."IdDireccionElectronicaPrincipal" = ELE."IdDireccionElectronica"
		LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIV ON DIV."IdDivisionPais" = DF."IdDivisionPais"
		LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU ON DIV."IdCiudad" = CIU."IdCiudad"
		LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" CLAS ON DIV."IdClasificacion1" = CLAS."IdClasificacionDivisionPais" AND CLAS."TipoClasificacionDivisionPais" = 'REGION'
		LEFT JOIN "BSCL.Normativa.BD::DOC.Documento" AS DOCNOR	ON NOM."IdDocumento" = DOCNOR."IdDocumento"       
			
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.CIRCULARIZACION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
