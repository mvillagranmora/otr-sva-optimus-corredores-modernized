SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.ENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE"; 

-- select * from "BSCL.Replica.BD.Vistas::ReplicaENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE"

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.ENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE" AS

        Select    
        	COALESCE (REG."IdRegistroEncuesta" , 0 )  AS "ENC_ID_HISTORICA", -- INT
            CAST(
				CASE WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
				ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END  AS varchar)  AS "PER_ID", -- INT
            COALESCE (CAST(REG."IdCategoriaInversionista" AS VARCHAR(90)),'')  AS "CATEGORIA", -- VARCHAR(90)
            COALESCE (CASE WHEN REG."FechaEncuesta" < '1900-01-01' THEN '1900-01-01' ELSE REG."FechaEncuesta" end   , '1900-01-01' )  AS "FECHA_ASIGNACION", -- DATETIME
            COALESCE (REG."Puntaje" , 0 )  AS "PUNTAJE_OBTENIDO", -- INT
            COALESCE (REG."NombreEncuesta",'')  AS "ENCUESTA", -- TEXT
            COALESCE (CAST('T' AS VARCHAR(1)),'')  AS "ESTADO", -- CHAR(1)
            COALESCE (CAST(REG."IdCanal" AS VARCHAR(3)),'')  AS "CANAL", -- CHAR(3)
            CAST(CASE UPPER(TRIM(REG."IdCategoriaInversionista"))
            	WHEN 'ARRIESGADO' THEN 'A'
            	WHEN 'BALANCEADO' THEN 'B'
            	WHEN 'SIN CATEGORIA' THEN 'N'
            	WHEN 'CALIFICADO' THEN 'D'
            	WHEN 'CONSERVADOR' THEN 'C'
            	WHEN 'MODERADO' THEN 'M'
            	ELSE ''
            END AS VARCHAR(1)) AS "DIN_ID", -- VARCHAR(1)
            COALESCE (REG."IdUsuarioResponde" , 0 )  AS "ID_USER", -- INT
            COALESCE (CAST(REG."IdPerfilProductos" AS VARCHAR(80)),'')  AS "PLANTILLA_PRODUCTO" -- VARCHAR(80)

        FROM "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta" REG
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON REG."IdCliente" = CLI."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.ENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
