SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.PERFIL_ACCESO_CUENTA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.PERFIL_ACCESO_CUENTA" AS

	SELECT ID_USER,PER_ID_USER,PER_ID,CUENTA,FIL_ID,MIN(ID_PROFILE) AS ID_PROFILE,MIN(PAC_FEC_INGRESO) AS PAC_FEC_INGRESO,MIN(PAC_FEC_MODIFICA) AS PAC_FEC_MODIFICA
	FROM (
		SELECT 
			COALESCE (USU."IdUsuario", 0) AS "ID_USER", -- INT,
			COALESCE (CAST(CASE 
					WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) 
				END AS INT), 0) AS "PER_ID_USER", -- INT,
			COALESCE (CAST(CASE 
					WHEN instr(CLI."DocumentoIdentidad" , '-') = 0 THEN -1 
					ELSE trim(LEFT(CLI."DocumentoIdentidad"  , instr(CLI."DocumentoIdentidad" , '-')-1)) 
				END AS INT), 0) AS "PER_ID", -- INT,
			COALESCE (CAST(CLI."CodigoPortafolio" AS VARCHAR(100)),'') AS "CUENTA", -- VARCHAR(100),
			COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
			COALESCE (CAST(CASE 
						WHEN AUT."IdTipoAutorizado" = 'ICLIENTE-OPERAR' THEN 10 --iCliente
						WHEN AUT."IdTipoAutorizado" = 'ICLIENTE-CONSULTAR' THEN 21 --iClienteCons
						ELSE 0 END AS INT) , 0 )  AS "ID_PROFILE", -- INT
			COALESCE (AUT."FechaIngreso", '1900-01-01' ) AS "PAC_FEC_INGRESO", -- DATETIME
			COALESCE (AUT."FechaModificacion", '1900-01-01' ) AS "PAC_FEC_MODIFICA" -- DATETIME
	
		FROM "BSCL.Portafolio.BD::POR.Autorizado" AUT
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" DOC ON DOC."IdPersona" = AUT."IdPersona"
		INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" CLI ON AUT."IdPortafolio" = CLI."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON USU."IdEntidad" = CLI."IdCliente"
		WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(CLI."CodigoPortafolio") != 0
	)
	GROUP BY ID_USER,PER_ID_USER,PER_ID,CUENTA,FIL_ID
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.PERFIL_ACCESO_CUENTA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
